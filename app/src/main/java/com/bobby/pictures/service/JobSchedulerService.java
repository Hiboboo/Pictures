package com.bobby.pictures.service;

import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.bobby.pictures.util.SettingManager;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 用于自动后台下载图片，并自动更新设置壁纸
 * <p>
 * Created by Bobby on 2018/07/24.
 */
public class JobSchedulerService extends JobService
{
    private static final String TAG = JobSchedulerService.class.getName();

    // 用于记录总共成功设置过多少张图片
    private int number;
    private SharedPreferences preferences;

    @Override
    public boolean onStartJob(JobParameters params)
    {
        Log.i(TAG, "服务已开始启动 .../" + params);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        number = preferences.getInt("wallpger.number", 0);
        new Thread(new WallpagerRunnable(params)).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params)
    {
        Log.i(TAG, "服务已停止! /" + params);
        return false;
    }

    private void setWallpagerSource(final JobParameters params, boolean isNeedRetry, final String id, String url)
    {
        if (!isNeedRetry)
        {
            Log.i(TAG, "已经没有下一个壁纸可用，尝试关闭服务。");
            jobFinished(params, false);
            return;
        }
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(url))
        {
            Log.i(TAG, "尝试重新一次下载请求。");
            jobFinished(params, true);
            return;
        }
        // 设置壁纸
        if (null == preferences)
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        number += 1;
        preferences.edit().putInt("wallpger.number", number).apply();
        Log.i(TAG, String.format("当前已经是第%d次设置！", number));
        File mPictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!mPictureDir.exists())
            mPictureDir.mkdirs();
        Log.i(TAG, "得到存储壁纸的存放目录=" + mPictureDir);
        String name = (id + ".jpg");
        if (SettingManager.isNeedWallpageCrop())
        {
            int targetWidth = SettingManager.getWallpageTargetWidth();
            int targetHeight = SettingManager.getWallpageCropHeight();
            url = url.concat(String.format(Locale.getDefault(), "?dl&fit=crop&crop=entropy&w=%d&h=%d", targetWidth, targetHeight));
            Log.i(TAG, "要对壁纸进行裁剪。url=" + url);
        }
        OkGo.<File>get(url)
                .tag(id)
                .execute(new FileCallback(mPictureDir.getAbsolutePath(), name)
                {
                    @Override
                    public void onSuccess(Response<File> response)
                    {
                        SettingManager.setSingleDownloadedPicture(id);
                        File imgFile = response.body();
                        SettingManager.putDownloadedPicture(id, imgFile.getAbsolutePath());
                        SettingManager.save();
                        setWallpage(params, imgFile);
                    }

                    @Override
                    public void onError(Response<File> response)
                    {
                        Log.i(TAG, "下载出现了错误！并尝试重新一次下载请求", response.getException());
                        jobFinished(params, true);
                    }
                });
    }

    private void setWallpage(JobParameters params, File localImgFile)
    {
        try
        {
            Log.i(TAG, "壁纸已下载完成，准备设置壁纸/" + localImgFile);
            WallpaperManager manager = WallpaperManager.getInstance(this);
            Bitmap wallpageBitmap = BitmapFactory.decodeFile(localImgFile.getAbsolutePath());
            manager.setBitmap(wallpageBitmap);
            Log.i(TAG, "壁纸已设置完成！/" + wallpageBitmap.getWidth() + "×" + wallpageBitmap.getHeight());
        } catch (IOException e)
        {
            Log.i(TAG, "设置壁纸出现了问题。", e);
        } finally
        {
            Log.i(TAG, "整个任务已完成。");
            jobFinished(params, false);
        }
    }

    private final WallpagerHandler mHandler = new WallpagerHandler(this);

    private static class WallpagerHandler extends Handler
    {
        final SoftReference<JobSchedulerService> reference;

        WallpagerHandler(JobSchedulerService service)
        {
            reference = new SoftReference<>(service);
        }

        @Override
        public void handleMessage(Message msg)
        {
            JobSchedulerService service = reference.get();
            if (null == service)
                return;
            Log.i(TAG, "获取壁纸任务已执行完成，状态码：" + msg.what);
            Bundle data = msg.getData();
            JobParameters params = data.getParcelable("img.params");
            switch (msg.what)
            {
                case 200:
                    boolean isNextState = data.getBoolean("img.state");
                    String id = data.getString("img.id");
                    String path = data.getString("img.path");
                    String url = path;
                    if (path.contains("?"))
                        url = path.substring(0, path.lastIndexOf("?"));
                    Log.i(TAG, "准备开始设置壁纸，id=" + id + "|url=" + url);
                    service.setWallpagerSource(params, isNextState, id, url);
                    break;
                case 500:
                    service.setWallpagerSource(params, true, null, null);
                    break;
            }
        }
    }

    private class WallpagerRunnable implements Runnable
    {
        private JobParameters params;

        WallpagerRunnable(JobParameters params)
        {
            this.params = params;
        }

        @Override
        public void run()
        {
            Log.i(TAG, "开始获取壁纸源信息！");
            Message message = mHandler.obtainMessage();
            Bundle data = message.getData();
            data.putParcelable("img.params", params);
            try
            {
                // 这个15的值不是随便写的，而是来自于网站每页请求返回的数据量，不可随意更改
                final int pageSize = 15;
                // 根据总的请求次数和每页的数量计算出当前要去请求哪一页
                final int page = (number / pageSize) + 1;
                // 获取用户自定义的搜索关键字
                final String keyword = SettingManager.getSearchKeyword();
                List<String> keys = new ArrayList<>();
                // 如果用户设置了多个关键字，则将其分割成集合
                if (keyword.contains(";"))
                    keys = Arrays.asList(keyword.split(";"));
                Random random = new Random();
                // 根据集合的总容量生成一个随机的下标位置
                int i = random.nextInt(keys.isEmpty() ? 1 : keys.size());
                String value = keys.isEmpty() ? keyword : keys.get(i);
                // 生成最终的请求URL，关键字value会被进行URL编码（
                String url = String.format(Locale.getDefault(),
                        "https://www.pexels.com/search/%s?page=%d", URLEncoder.encode(value, "UTF-8"), page);
                Log.i(TAG, "请求壁纸源的URL：" + url);
                Document document = Jsoup.connect(url).get();
                Elements articleElements = document.select("article[class*=photo-item]");
                final int index = (number % pageSize);
                boolean isNext = (articleElements.size() > index);
                data.putBoolean("img.state", isNext);
                Log.i(TAG, "是否还有下一张壁纸可取？" + isNext);
                if (isNext)
                {
                    Element itemElement = articleElements.get(index);
                    String imagePath = itemElement.selectFirst("a[download]").attr("href");
                    String id = itemElement.selectFirst("button").attr("data-photo-id");
                    data.putString("img.id", id);
                    data.putString("img.path", imagePath);
                    message.what = 200;
                }
            } catch (IOException e)
            {
                e.printStackTrace();
                message.what = 500;
            }
            mHandler.sendMessage(message);
        }
    }
}
