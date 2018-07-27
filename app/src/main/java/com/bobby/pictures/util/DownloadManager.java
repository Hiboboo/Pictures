package com.bobby.pictures.util;

import android.os.Environment;
import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Progress;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.lzy.okserver.download.DownloadTask;

import java.io.File;

/**
 * 统一的下载管理，支持多线程同步下载
 * <p>
 * Created by Bobby on 2018/07/26.
 */
public final class DownloadManager
{
    private static final String TAG = DownloadManager.class.getSimpleName();

    private static volatile DownloadManager manager = null;

    private OkDownload mDownload;

    private DownloadManager()
    {
        Log.i(TAG, "创建了一个下载对象。");
        mDownload = OkDownload.getInstance();
        this.setupDownloadConfig();
    }

    private void setupDownloadConfig()
    {
        File mPictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!mPictureDir.exists())
            mPictureDir.mkdirs();
        mDownload.setFolder(mPictureDir.getAbsolutePath());
        mDownload.getThreadPool().setCorePoolSize(5);
        Log.i(TAG, "已初始化全局的下载配置，目录为：" + mPictureDir);
    }

    public static DownloadManager getInstance()
    {
        if (null == manager)
            synchronized (DownloadManager.class)
            {
                if (null == manager)
                    manager = new DownloadManager();
            }
        return manager;
    }

    public DownloadTask startDownload(String tag, String url, DownloadListener listener)
    {
        if (mDownload.hasTask(tag))
        {
            DownloadTask task = mDownload.getTask(tag);
            task.register(listener);
            task.restart();
            return task;
        }
        String name = tag.concat(".jpg");
        DownloadTask task = OkDownload.request(tag, OkGo.<File>get(url))
                .fileName(name)
                .save()
                .register(listener);
        Log.i(TAG, "已开始了一次新的下载任务=" + tag + "｜" + task);
        task.start();
        return task;
    }

    public void cancelDownload(String tag)
    {
        if (!mDownload.hasTask(tag))
            return;
        DownloadTask task = mDownload.getTask(tag);
        task.unRegister(tag);
        // 只需要取消下载监听器，只要程序不关闭，就让这个任务执行完毕。
//        task.pause();
//        task.remove(true);
        Log.i(TAG, "已取消了下载监听器=" + tag + "｜" + task);
    }

    public void cancelAllTask()
    {
        mDownload.pauseAll();
        mDownload.removeAll(true);
        Log.i(TAG, "已取消了所有的下载任务！");
    }

    public static class OnDownloadListener extends DownloadListener
    {
        public OnDownloadListener(Object tag)
        {
            super(tag);
            Log.i(TAG, "创建了一个下载监听器：" + tag);
        }

        @Override
        public void onStart(Progress progress)
        {
            Log.i(TAG, "已开始下载");
        }

        @Override
        public void onProgress(Progress progress)
        {
            Log.i(TAG, "正在下载中");
        }

        @Override
        public void onError(Progress progress)
        {
            Log.i(TAG, "下载出现了错误");
        }

        @Override
        public void onFinish(File file, Progress progress)
        {
            Log.i(TAG, "下载完成了");
        }

        @Override
        public void onRemove(Progress progress)
        {
            Log.i(TAG, "任务被移除");
        }
    }
}
