package com.bobby.pictures.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.bobby.pictures.actions.BaseApplication;

import java.util.HashSet;
import java.util.Set;

/**
 * 程序中的全局设置
 * <p/>
 * 涉及到的设置项包含有：
 * <ul>
 * <li>设置是否需要打开自动更新壁纸服务</li>
 * <li>设置要获取的壁纸对应的目标搜索关键字</li>
 * <li>设置自动更新壁纸的时间间隔</li>
 * <li>设置更新壁纸需要在什么网络环境下</li>
 * <li>是否启用壁纸裁剪</li>
 * <li>壁纸裁剪的目标宽/高</li>
 * </ul>
 * Created by Bobby on 2018/07/25.
 */
public final class SettingManager
{
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor mEditor;

    static
    {
        preferences = BaseApplication.getInstance().getSharedPreferences("bobby.pic.sett", Context.MODE_PRIVATE);
        mEditor = preferences.edit();
    }

    public static void setAutoRefreshServiceID(int id)
    {
        mEditor.putInt("s.id", id);
    }

    public static int getAutoRefreshServiceID()
    {
        return preferences.getInt("s.id", 0);
    }

    /**
     * 设置是否需要自动进行壁纸更新服务
     *
     * @param isAutoService 若需要自动更新就为<code>true</code>
     */
    public static void setAutoRefreshService(boolean isAutoService)
    {
        mEditor.putBoolean("pic.sett.auto.service", isAutoService);
    }

    /**
     * 获取是否需要自动更新壁纸服务
     *
     * @return 若需要自动更新就返回<code>true</code>
     */
    public static boolean isAutoRefreshService()
    {
        return preferences.getBoolean("pic.sett.auto.service", true);
    }

    /**
     * 设置一个用于壁纸搜索的关键字
     *
     * @param keyword 关键字
     */
    public static void setSearchKeyword(CharSequence keyword)
    {
        mEditor.putString("pic.sett.keyword", keyword.toString());
    }

    /**
     * 获取用户自定义设置的壁纸关键字
     *
     * @return 返回已设置的壁纸搜索关键字，默认返回“girl”
     */
    public static String getSearchKeyword()
    {
        return preferences.getString("pic.sett.keyword", "girl");
    }

    /**
     * 设置自动更新壁纸服务的时间间隔
     *
     * @param minute 间隔时间（单位分钟）
     */
    public static void setAutoRefreshIntervalTime(int minute)
    {
        mEditor.putInt("pic.sett.auto.minute", minute);
    }

    /**
     * 获取已设置的壁纸自动更新间隔时间
     *
     * @return 返回已设置的壁纸自动更新间隔时间，默认返回60分钟
     */
    public static int getAutoRefreshIntervalTime()
    {
        return preferences.getInt("pic.sett.auto.minute", 60);
    }

    /**
     * 设置自动更新需要在什么网络环境下
     *
     * @param type 网络类型名称（Any：任何网络、Wifi：热点网络）
     */
    public static void setNetworkType(CharSequence type)
    {
        mEditor.putString("pic.sett.network.type", type.toString());
    }

    /**
     * 获取已设置的网络环境
     *
     * @return 返回用户已设置的网络环境，默认返回Any
     */
    public static String getNetworkType()
    {
        return preferences.getString("pic.sett.network.type", "Any");
    }

    /**
     * 永久存储一个已下载的图片ID集合
     *
     * @param pictureIds 在本地的图片ID集合
     */
    public static void setDownloadedPictures(Set<String> pictureIds)
    {
        mEditor.putStringSet("pic.sett.ids", pictureIds);
    }

    /**
     * 只存储一个图片信息到ID集合中
     *
     * @param pid 图片ID
     */
    public static void setSingleDownloadedPicture(String pid)
    {
        Set<String> pids = getDownloadedPictures();
        if (!pids.contains(pid))
            pids.add(pid);
        setDownloadedPictures(pids);
    }

    /**
     * 获取存储在本地的所有图片ID集合
     *
     * @return 返回持久化在本地的所有图片ID，默认返回空的集合
     */
    public static Set<String> getDownloadedPictures()
    {
        return preferences.getStringSet("pic.sett.ids", new HashSet<String>());
    }

    /**
     * 在本地存储一个已下载的图片路径
     *
     * @param pid     图片的ID
     * @param absPath 已下载图片在本地的绝对路径
     */
    public static void putDownloadedPicture(String pid, String absPath)
    {
        mEditor.putString(pid, absPath);
    }

    /**
     * 根据图片的ID获取存储在本地的图片绝对路径
     *
     * @param pid 图片ID
     * @return 返回和图片ID对应的绝对路径，没有则返回<code>null</code>
     */
    public static String getDownloadedPicture(String pid)
    {
        return preferences.getString(pid, null);
    }

    /**
     * 设置是否需要对目标壁纸进行裁剪
     *
     * @param isNeedCrop 若需要对目标壁纸进行裁剪为<code>true</code>
     */
    public static void setNeedWallpageCrop(boolean isNeedCrop)
    {
        mEditor.putBoolean("pic.sett.need.crop", isNeedCrop);
    }

    /**
     * 是否需要对目标壁纸进行裁剪
     *
     * @return 若需要对目标壁纸进行裁剪返回<code>true</code>，否则为<code>false</code>
     */
    public static boolean isNeedWallpageCrop()
    {
        return preferences.getBoolean("pic.sett.need.crop", false);
    }

    /**
     * 设置目标壁纸的裁剪宽度
     *
     * @param targetWidth 目标宽度
     * @see #setNeedWallpageCrop(boolean)
     */
    public static void setWallpageCropWidth(int targetWidth)
    {
        mEditor.putInt("pic.sett.crop.width", targetWidth);
    }

    /**
     * 获取要对壁纸裁剪的目标宽度
     *
     * @return 返回要对壁纸裁剪的目标宽度，若没有设置则默认返回1080
     * @see #isNeedWallpageCrop()
     */
    public static int getWallpageTargetWidth()
    {
        int width = preferences.getInt("pic.sett.crop.width", 1080);
        if (width <= 0)
            width = 1080;
        return width;
    }

    /**
     * 设置目标壁纸的裁剪高度
     *
     * @param targetHeight 目标高度
     * @see #setNeedWallpageCrop(boolean)
     */
    public static void setWallpageCropHeight(int targetHeight)
    {
        mEditor.putInt("pic.sett.crop.height", targetHeight);
    }

    /**
     * 获取要对壁纸裁剪的目标高度
     *
     * @return 返回要对壁纸裁剪的目标高度，若没有设置则默认返回1920
     * @see #isNeedWallpageCrop()
     */
    public static int getWallpageCropHeight()
    {
        int height = preferences.getInt("pic.sett.crop.height", 1920);
        if (height <= 0)
            height = 1920;
        return height;
    }

    /**
     * 保存所有的设置
     */
    public static void save()
    {
        mEditor.apply();
    }
}
