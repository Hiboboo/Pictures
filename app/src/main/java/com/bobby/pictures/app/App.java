package com.bobby.pictures.app;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.StringRes;

import com.muddzdev.styleabletoastlibrary.StyleableToast;

/**
 * 作者 Bobby on 2018/05/29.
 */
public final class App
{
    public static class Key
    {
        public static final String KEY_EXTRA_STATE = "com.bobby.pictures.KEY_EXTRA_STATE";
        public static final String KEY_EXTRA_TITLE = "com.bobby.pictures.KEY_EXTRA_TITLE";
        public static final String KEY_EXTRA_DATA = "com.bobby.pictures.KEY_EXTRA_DATA";
        public static final String KEY_EXTRA_PATH = "com.bobby.pictures.KEY_EXTRA_PATH";
        public static final String KEY_EXTRA_TYPE = "com.bobby.pictures.KEY_EXTRA_TYPE";
        public static final String KEY_EXTRA_LEVEL = "com.bobby.pictures.KEY_EXTRA_LEVEL";
        public static final String KEY_EXTRA_POSITION = "com.bobby.pictures.KEY_EXTRA_POSITION";
        public static final String KEY_EXTRA_CONTENT = "com.bobby.pictures.KEY_EXTRA_CONTENT";
        public static final String KEY_EXTRA_DATA_ID = "com.bobby.pictures.KEY_EXTRA_DATA_ID";
        public static final String KEY_EXTRA_DATA_FOLLOW_ID = "com.bobby.pictures.KEY_EXTRA_DATA_FOLLOW_ID";
    }

    public static void showSuccessToast(Context context, @StringRes int res)
    {
        showSuccessToast(context, context.getResources().getString(res));
    }

    public static void showSuccessToast(Context context, CharSequence message)
    {
        new StyleableToast.Builder(context)
                .text(message.toString())
                .textColor(Color.WHITE)
                .cornerRadius(6)
                .backgroundColor(Color.parseColor("#34cc99"))
                .show();
    }

    public static void showAlertToast(Context context, @StringRes int res)
    {
        showAlertToast(context, context.getResources().getString(res));
    }

    public static void showAlertToast(Context context, CharSequence message)
    {
        new StyleableToast.Builder(context)
                .text(message.toString())
                .textColor(Color.WHITE)
                .cornerRadius(6)
                .backgroundColor(Color.argb(153, 0, 0, 0))
                .show();
    }
}
