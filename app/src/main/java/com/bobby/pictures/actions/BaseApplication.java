package com.bobby.pictures.actions;

import android.app.Application;


/**
 * 作者 Bobby on 2017/8/17.
 */
public class BaseApplication extends Application
{
    private static BaseApplication mInstance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized BaseApplication getInstance()
    {
        return mInstance;
    }
}
