package com.bobby.pictures.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Keep;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bobby.pictures.app.App;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * 提供各种工具
 * <HR>
 * 创建者 Bobby
 * <p>
 * 时间 2017/8/15 17:33
 * <p>
 */
public class Utils
{

    /**
     * 获取当前程序的版本号
     *
     * @param context 运行中的上下文对象
     * @return 当前程序的版本号码
     */
    public static int getCurrentVersionCode(Context context)
    {
        PackageManager mPackageManager = context.getPackageManager();
        try
        {
            PackageInfo mPackageInfo = mPackageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return mPackageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取当前程序的版本名称
     *
     * @param context 运行中的上下文对象
     * @return 当前程序的版本名称
     */
    public static String getCurrentVersionName(Context context)
    {
        String versionname = "";
        PackageManager mPackageManager = context.getPackageManager();
        try
        {
            PackageInfo mPackageInfo = mPackageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            versionname = mPackageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return versionname;
    }

    /**
     * 检测当前环境是否有可用网络
     *
     * @param context 当前上下文
     * @return 返回当前环境是否有网络
     */
    public static boolean isOpenNetwork(Context context)
    {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connManager != null;
        return connManager.getActiveNetworkInfo() != null && connManager.getActiveNetworkInfo().isAvailable();
    }

    /**
     * 判断WIFI网络是否可用
     *
     * @param context 上下文
     * @return 如果wifi可用返回true
     */
    public static boolean isWifiConnected(Context context)
    {
        if (context != null)
        {
            // 获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取NetworkInfo对象
            assert manager != null;
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空 并且类型是否为WIFI
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return networkInfo.isAvailable();
        }
        return false;
    }

    /**
     * 获取当前程序中所有的缓存大小。包含程序内部缓存，外部SD缓存，程序内data/data/files的所有文件。
     * 需要注意的是，这里并不会获取shared_prefs和databases的相关文件大小
     *
     * @param context 当前程序上下文
     * @return 返回被格式化之后的缓存数据表示
     */
    public static String getFormatCacheSize(Context context)
    {
        long mInsideCacheSize = getLocalCacheFolderSize(context.getCacheDir());
        long mDataFilesSize = getLocalCacheFolderSize(context.getFilesDir());
        long mExternalCacheSize = getLocalCacheFolderSize(new File(getExternalCacheDirPath(context)));
        long mTotalCacheSize = (mInsideCacheSize + mDataFilesSize + mExternalCacheSize);
        return formatCalculateResult(mTotalCacheSize);
    }

    /**
     * 格式化并计算一个指定的字节大小数值
     *
     * @param size 要格式化的字节数字
     * @return 被格式化后的结果以标准的文字显示形式返回，默认返回0.0K
     */
    public static String formatCalculateResult(long size)
    {
        DecimalFormat format = new DecimalFormat("0.00");
        float kSize = 1024;
        float mSize = kSize * 1024;
        float gSize = mSize * 1024;
        String result = "0.0K";
        if (size >= gSize)
            result = format.format(size / gSize) + "G";
        else if (size >= mSize)
            result = format.format(size / mSize) + "M";
        else if (size >= kSize)
            result = format.format(size / kSize) + "K";
        return result;
    }

    /**
     * 清除当前程序中的所有缓存数据，其中也包括数据库文件
     *
     * @param context 当前程序上下文
     */
    public static void clearAppCaches(Context context)
    {
        deleteFolderFile(context.getCacheDir());
        deleteFolderFile(context.getFilesDir());
        deleteFolderFile(new File(getExternalCacheDirPath(context)));
    }

    private static boolean deleteFolderFile(File dir)
    {
        if (null == dir || !dir.exists())
            return false;
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (String aChildren : children)
            {
                boolean success = deleteFolderFile(new File(dir, aChildren));
                if (!success)
                    return false;
            }
        }
        return dir.delete();
    }

    /**
     * 获取当前程序在SD卡中的缓存路径
     *
     * @param context 当前程序上下文
     * @return 返回mnt/sdcard/Android/data/com.signalmust.mobile/cache
     */
    public static String getExternalCacheDirPath(Context context)
    {
        return context.getExternalCacheDir().getAbsolutePath();
    }

    /**
     * 计算指定的文件夹的总大小，单位以字节为准
     *
     * @param cacheFolder 要计算大小的文件夹绝对路径
     * @return 如果参数cacheFolder不是一个标准的文件夹，则返回0，否则返回该文件夹的标准总大小
     */
    public static long getLocalCacheFolderSize(File cacheFolder)
    {
        long folderSize = 0;
        if (!cacheFolder.isDirectory())
            return folderSize;
        File[] cacheFiles = cacheFolder.listFiles();
        for (File cacheFile : cacheFiles)
            if (cacheFile.isFile())
                folderSize += cacheFile.length();
            else if (cacheFile.isDirectory())
                folderSize += getLocalCacheFolderSize(cacheFile);
        return folderSize;
    }

    /**
     * 保存Bitmap对象到本地缓存
     *
     * @param context 当前程序上下文
     * @param bitmap  要保存的位图对象
     * @return 返回保存成功后的图片路径地址，若保存失败则返回<code>null</code>
     */
    public String saveBitmapToDeviceCache(Context context, Bitmap bitmap)
    {
        return saveBitmapToDeviceCache(context, bitmap, 100);
    }

    public String saveBitmapToDeviceCache(Context context, Bitmap bitmap, int quality)
    {
        try
        {
            String dir = getExternalCacheDirPath(context);
            String filePath = dir + File.separator + "screenshot" + System.currentTimeMillis() + ".png";
            File file = new File(filePath);
            FileOutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, os);
            os.flush();
            os.close();
            return filePath;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 启动压缩。
     *
     * @param path       需要压缩的图片路径
     * @param targetSize 需要压缩的目标大小
     * @param listener   对于压缩结果的回调
     */
    public void onStartCompress(String path, int targetSize, OnCompressCompeteListener listener)
    {
        new CompressThread(path, targetSize, listener).start();
    }

    private final static class CompressHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 1)
            {
                Object object = msg.obj;
                if (object instanceof OnCompressCompeteListener)
                {
                    OnCompressCompeteListener listener = (OnCompressCompeteListener) object;
                    Bundle data = msg.getData();
                    byte[] bArray = data.getByteArray(App.Key.KEY_EXTRA_DATA);
                    listener.onComplete(bArray);
                }
            }
        }
    }

    public interface OnCompressCompeteListener
    {
        void onComplete(byte[] bArray);
    }

    private final CompressHandler mCompressHandler = new CompressHandler();

    private class CompressThread extends Thread
    {
        private String path;
        private int targetSize;
        private OnCompressCompeteListener listener;

        CompressThread(String path, int targetSize, OnCompressCompeteListener listener)
        {
            this.path = path;
            this.targetSize = targetSize;
            this.listener = listener;
        }

        @Override
        public void run()
        {
            try
            {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int options = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
                int subtract;
                int length = baos.toByteArray().length;
                while (length > targetSize && options > 10)
                {  //循环判断如果压缩后图片是否大于ImageSize kb,大于继续压缩
                    subtract = setSubstractSize(length / 1024);
                    baos.reset();//重置baos即清空baos
                    // 只能是JPEG的图片可以重复压缩，不能使用PNG
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos))//这里压缩options%，把压缩后的数据存放到baos中
                    {
                        options -= subtract;//每次都减少10
                        length = baos.toByteArray().length;
                    }
                }
                byte[] rArray = baos.toByteArray();
                baos.close();
                bitmap.recycle();
                Message msg = mCompressHandler.obtainMessage();
                msg.what = 1;
                msg.obj = listener;
                Bundle data = msg.getData();
                data.putByteArray(App.Key.KEY_EXTRA_DATA, rArray);
                mCompressHandler.sendMessage(msg);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        /**
         * 根据图片的大小设置压缩的比例，提高速度
         */
        private int setSubstractSize(int imageMB)
        {
            if (imageMB > 1000)
                return 60;
            else if (imageMB > 750)
                return 40;
            else if (imageMB > 500)
                return 20;
            else
                return 10;
        }
    }
}
