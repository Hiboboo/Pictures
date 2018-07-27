package com.bobby.pictures.actions;

import android.app.Application;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;


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
        this.setupOkhttp();
    }

    public static synchronized BaseApplication getInstance()
    {
        return mInstance;
    }

    private void setupOkhttp()
    {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("Pictures Http");
        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        //log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.OFF);
        builder.addInterceptor(loggingInterceptor);

        final long timeout = 3;
        //全局的读取超时时间
        builder.readTimeout(timeout, TimeUnit.MINUTES);
        //全局的写入超时时间
        builder.writeTimeout(timeout, TimeUnit.MINUTES);
        //全局的连接超时时间
        builder.connectTimeout(timeout, TimeUnit.MINUTES);

        OkGo.getInstance().init(this) //必须调用初始化
                .setOkHttpClient(builder.build()) //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.NO_CACHE) //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE) //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3); //全局公共参数
    }
}
