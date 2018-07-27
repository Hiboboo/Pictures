package com.bobby.pictures.util;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步操作管理
 * <p>
 * Created by Bobby on 2018/07/18.
 */
public final class AsynchronousManager
{
    private static volatile AsynchronousManager manager = null;

    private final ExecutorService mExecutorService;

    private AsynchronousManager()
    {
        mExecutorService = Executors.newCachedThreadPool();
    }

    public static AsynchronousManager getInstance()
    {
        if (null == manager)
            synchronized (AsynchronousManager.class)
            {
                if (null == manager)
                    manager = new AsynchronousManager();
            }
        return manager;
    }

    // 回调事件的控制指令集和
    private final HashMap<String, ControlCommand> mCallbackControl = new HashMap<>();

    /**
     * 行为控制指令
     */
    public enum ControlCommand
    {
        /**
         * 放弃。
         * （一般用于网络线程执行完成后，但对该线程有实际操作的调用者已经停止或页面已关闭，此时调用者需要指定当前指令来告知网络线程放弃后续动作的执行）
         */
        GIVEUP,
        /**
         * 忽略。
         */
        IGNORE,
        /**
         * 继续执行并无视其他指令。
         */
        CONTINUE
    }

    /**
     * 为当前行为设置一个标签，并为其安排一个行为控制指令，可以要求其在异步行为执行完毕后可以准确的按照指令进行后续动作
     *
     * @param tag            当前行为的唯一标签
     * @param controlCommand 在异步行为执行完毕后的动作指令
     */
    public void setTag(String tag, ControlCommand controlCommand)
    {
        mCallbackControl.put(tag, controlCommand);
    }

    /**
     * 为当前异步线程添加一个新的唯一标签，它会默认为其行为指令设置为<code>{@link ControlCommand#IGNORE}</code>
     *
     * @param tag 新的唯一标签
     */
    public void addTag(String tag)
    {
        this.setTag(tag, ControlCommand.IGNORE);
    }

    /**
     * 获取一个现有的行为指令，这取决于指令集合中是否存在某个标签
     *
     * @param tag 标签名称
     * @return 若指令集和中存在对应的指令则返回，否则返回<code>null</code>
     */
    public ControlCommand getCommandByTag(String tag)
    {
        return mCallbackControl.get(tag);
    }

    private final ExecuteApi mExecuteApi = new ExecuteApi();
    private final AsynchronousHandler mHandler = new AsynchronousHandler(this);

    /**
     * 清空所有API请求接口获取到的临时数据
     */
    public void clear()
    {
        mExecuteApi.clear();
    }

    /**
     * 清空某一个API接口获取到的临时数据
     *
     * @param api 指定具体的API
     */
    public void refreshApiValues(ExecuteApi.Apis api)
    {
        mExecuteApi.refreshApiValues(api);
    }

    private final class AsynchronousRunnable implements Runnable
    {
        private String tag;
        private ExecuteApi.Apis api;
        private ExecuteApi.Params params;

        AsynchronousRunnable(String tag, ExecuteApi.Apis api, ExecuteApi.Params params)
        {
            this.tag = tag;
            this.api = api;
            this.params = params;
        }

        @Override
        public void run()
        {
            try
            {
                mExecuteApi.get(api, params, new OnAsynchronousCallback()
                {
                    @Override
                    public void onSuccessful(Object data)
                    {
                        if (getCommandByTag(tag) == ControlCommand.GIVEUP)
                            return;
                        Message message = mHandler.obtainMessage();
                        message.what = 200;
                        message.getData().putString("tag", tag);
                        message.obj = data;
                        mHandler.sendMessage(message);
                    }
                });
            } catch (IOException e)
            {
                Message message = mHandler.obtainMessage();
                message.what = 500;
                message.getData().putString("tag", tag);
                mHandler.sendMessage(message);
            }
        }
    }

    /**
     * 异步请求后的数据结果回调
     */
    interface OnAsynchronousCallback
    {
        /**
         * 从具体的数据请求方法中得到实际的结果数据
         *
         * @param data 结果数据
         */
        void onSuccessful(Object data);
    }

    /**
     * 开始一个系的请求
     *
     * @param tag    标识请求的标签
     * @param api    具体的请求API，可参见<code>{@link com.bobby.pictures.util.ExecuteApi.Apis}</code>
     * @param params 对照API的参数集，参见<code>{@link com.bobby.pictures.util.ExecuteApi.Params}</code>
     */
    private void start(String tag, ExecuteApi.Apis api, ExecuteApi.Params params)
    {
        mExecutorService.execute(new AsynchronousRunnable(tag, api, params));
    }

    private HashMap<String, OnResultCallback> callbacks = new HashMap<>();

    /**
     * 为调用者设置一个获取异步请求数据的结果回调
     *
     * @param callback 结果回调
     */
    public void setOnResultCallback(String tag, OnResultCallback callback)
    {
        callbacks.put(tag, callback);
    }

    /**
     * 调用者通过该回调结果，可以拿到异步请求的实际结果数据
     *
     * @param <T> 调用者可设置具体的结果类型
     */
    public interface OnResultCallback<T>
    {
        /**
         * 当成功时该回调方法会向调用者展示实际数据
         *
         * @param tag  当前异步行为的标签
         * @param data 成功时的结果数据
         */
        void onSuccessResult(String tag, T data);

        /**
         * 当异步行为执行时失败时
         *
         * @param tag 当前异步行为的标签
         */
        void onFailure(String tag);
    }

    private void setResult(int code, String tag, Object data)
    {
        OnResultCallback callback = callbacks.get(tag);
        switch (code)
        {
            case 200:
                if (callback != null)
                    callback.onSuccessResult(tag, data);
                break;
            case 500:
                if (callback != null)
                    callback.onFailure(tag);
                break;
        }
    }

    private final static class AsynchronousHandler extends Handler
    {
        private SoftReference<AsynchronousManager> reference;

        AsynchronousHandler(AsynchronousManager manager)
        {
            reference = new SoftReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg)
        {
            AsynchronousManager manager = reference.get();
            if (null == manager)
                return;
            String tag = msg.getData().getString("tag");
            manager.setResult(msg.what, tag, msg.obj);
        }
    }

    public static class Builder
    {
        private String tag;

        public Builder(String tag)
        {
            this.tag = tag;
        }

        private ExecuteApi.Apis api;
        private ExecuteApi.Params params;
        private OnResultCallback callback;

        public Builder setOnResultCallback(OnResultCallback callback)
        {
            this.callback = callback;
            return this;
        }

        public Builder setApi(ExecuteApi.Apis api)
        {
            this.api = api;
            return this;
        }

        public Builder addParams(ExecuteApi.Params params)
        {
            this.params = params;
            return this;
        }

        public AsynchronousManager execute()
        {
            AsynchronousManager manager = AsynchronousManager.getInstance();
            manager.addTag(tag);
            manager.setOnResultCallback(tag, callback);
            manager.start(tag, api, params);
            return manager;
        }
    }
}
