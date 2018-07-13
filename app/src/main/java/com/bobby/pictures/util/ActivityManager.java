package com.bobby.pictures.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者 Bobby on 2017/8/16.
 */
public class ActivityManager
{
    private final static ArrayList<Activity> activitys = new ArrayList<>();

    public static void addActivity(Activity activity)
    {
        if (!activitys.contains(activity))
            activitys.add(activity);
    }

    public static void removeActivity(Activity activity)
    {
        if (activitys.contains(activity))
            activitys.remove(activity);
    }

    public static void finishActivitys()
    {
        for (Activity activity : activitys)
            if (activity != null && !activity.isFinishing())
                activity.finish();
    }

    public static void finishSingleActivity(Activity activity)
    {
        if (null == activity)
            return;
        if (activitys.contains(activity))
            activitys.remove(activity);
        activity.finish();
    }

    public static void finishSingleActivityByClass(Class<? extends Activity> cls)
    {
        for (Activity activity : activitys)
            if (activity.getClass().equals(cls))
            {
                finishSingleActivity(activity);
                break;
            }
    }

    public static boolean containsForActivityCls(Class<? extends Activity> cls)
    {
        boolean isExist = false;
        for (Activity activity : activitys)
            if (activity.getClass().equals(cls))
            {
                isExist = true;
                break;
            }
        return isExist;
    }

    public static void finishActivitysByClass(List<Class<? extends Activity>> targetClss)
    {
        for (Activity activity : activitys)
            if (targetClss.contains(activity.getClass()) && !activity.isFinishing())
                activity.finish();
    }
}
