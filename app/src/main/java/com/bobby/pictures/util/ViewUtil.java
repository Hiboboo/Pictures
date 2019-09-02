package com.bobby.pictures.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import com.bobby.pictures.R;

import java.lang.reflect.Method;

import permissions.dispatcher.PermissionRequest;

/**
 * <p>
 * 作者：孙博
 * <p>
 * 时间：2017/7/1 20:09
 */
public final class ViewUtil
{
    private final Context context;

    private View mView;

    public ViewUtil(Context context)
    {
        this.context = context;
    }

    /**
     * 检测判断界面中某一个控件是否正处于显示状态
     *
     * @param view 要检测的界面控件
     * @return 如果是显示状态返回{@code true}，否则返回{@code false}
     */
    public boolean isShown(View view)
    {
        if (view.getVisibility() == View.VISIBLE)
            return true;
        return false;
    }

    /**
     * 显示一个在界面存在但已经隐藏的控件
     *
     * @param view 要显示的界面控件，该控件必须是从<CODE>{@link View}</CODE>派生而来
     */
    public void showView(View view)
    {
        if (!this.isShown(view))
            view.setVisibility(View.VISIBLE);
    }

    /**
     * 以动画方式显示一个在界面存在但已经隐藏的控件
     *
     * @param view 要显示的界面控件，该控件必须是从<CODE>{@link View}</CODE>派生而来
     */
    public void showAnimView(View view)
    {
        if (!this.isShown(view))
        {
            view.setAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
            view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 以动画方式显示一个在界面存在但已经隐藏的控件
     *
     * @param view 要显示的界面控件，该控件必须是从<CODE>{@link View}</CODE>派生而来
     * @param id   指定控件显示时的动画来源
     */
    public void showAnimView(View view, int id)
    {
        if (!this.isShown(view))
        {
            view.setAnimation(AnimationUtils.loadAnimation(context, id));
            view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏一个正显示中的控件
     *
     * @param view       要隐藏的界面控件，该控件必须是从<CODE>{@link View}</CODE>派生而来
     * @param isKeepSeat 是否保留该控件之前的所在位置
     */
    public void hideView(View view, boolean isKeepSeat)
    {
        if (this.isShown(view))
            if (isKeepSeat)
                view.setVisibility(View.INVISIBLE);
            else
                view.setVisibility(View.GONE);
    }

    /**
     * 以动画方式隐藏一个正显示中的控件
     *
     * @param view       要隐藏的界面控件，该控件必须是从<CODE>{@link View}</CODE>派生而来
     * @param isKeepSeat 是否保留该控件之前的所在位置
     */
    public void hideAnimView(View view, boolean isKeepSeat)
    {
        if (this.isShown(view))
        {
            view.setAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_out));
            if (isKeepSeat)
                view.setVisibility(View.INVISIBLE);
            else
                view.setVisibility(View.GONE);
        }
    }

    /**
     * 交替显示并隐藏两个控件
     *
     * @param showView   将要显示的控件
     * @param hideView   将要隐藏的控件
     * @param isKeepSeat 是否保留该控件之前的所在位置
     */
    public void alternateShowView(View showView, View hideView, boolean isKeepSeat)
    {
        if (this.isShown(hideView))
        {
            this.hideView(hideView, isKeepSeat);
            this.showAnimView(showView);
        }
    }

    /**
     * 以动画方式隐藏一个正显示中的控件
     *
     * @param view       要隐藏的界面控件，该控件必须是从<CODE>{@link View}</CODE>派生而来
     * @param id         指定控件从屏幕中消失时的动画来源
     * @param isKeepSeat 是否保留该控件之前的所在位置
     */
    public void hideAnimView(View view, int id, boolean isKeepSeat)
    {
        if (this.isShown(view))
        {
            view.setAnimation(AnimationUtils.loadAnimation(context, id));
            if (isKeepSeat)
                view.setVisibility(View.INVISIBLE);
            else
                view.setVisibility(View.GONE);
        }
    }

    /**
     * 以动画形式显示一个控件，并设置该控件在屏幕中应该显示多长时间，在控件消失后，将不会再占用其在屏幕中的原有位置
     *
     * @param view        将要被显示的某个实例化控件对象
     * @param displayTime 指定控件将要在屏幕中显示的时间。<I>单位为毫秒</I>
     */
    public void setTimeShowAnimView(View view, long displayTime)
    {
        this.mView = view;
        mHandler.removeCallbacks(runnable);
        this.showAnimView(view);
        mHandler.postDelayed(runnable, displayTime);
    }

    private final Handler mHandler = new Handler();
    private final Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            hideAnimView(mView, false);
        }
    };

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int[] getTargetSize(Context context, @DrawableRes int resId)
    {
        int[] size = new int[2];
        Drawable drawable = context.getResources().getDrawable(resId);
        size[0] = drawable.getIntrinsicWidth();
        size[1] = drawable.getIntrinsicHeight();
        return size;
    }

    public static boolean isEMUI3_1()
    {
        return "EmotionUI_3.1".equals(getEmuiVersion());
    }

    private static String getEmuiVersion()
    {
        try
        {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", String.class);
            return (String) getMethod.invoke(classType, "ro.build.version.emui");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 为当前页面设置背景透明度
     *
     * @param activity 当前活动页面
     * @param alpha    透明度0.0~1.0之间
     */
    public static void setBackgroundAlpha(Activity activity, float alpha)
    {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = alpha; //0.0-1.0
        activity.getWindow().setAttributes(lp);
    }

    public static void showRationaleDialog(Activity activity, @StringRes int messageResId, final PermissionRequest request)
    {
        new AlertDialog.Builder(activity)
                .setPositiveButton(R.string.label_permission_allow, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which)
                    {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.label_permission_deny, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which)
                    {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    /**
     * 获取系统状态栏的高度
     *
     * @param activity 活动对象
     * @return 返回获取到的系统状态栏高度(px)
     */
    public static int getStatusbarHeight(Activity activity)
    {
        View view = activity.getWindow().getDecorView();
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    /**
     * 对指定控件区域进行截屏
     *
     * @param sourceView 将要被截取的控件
     * @param yOffset    被截取区域的Y坐标偏移量
     * @return 返回截取成功后的目标位图
     */
    public static Bitmap getScreenshotBitmap(View sourceView, int yOffset)
    {
        sourceView.setDrawingCacheEnabled(true);
        Bitmap source = sourceView.getDrawingCache();
        Bitmap mTempBitmap = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight() - yOffset);
        sourceView.setDrawingCacheEnabled(false);
        return mTempBitmap;

    }

    /**
     * 修饰并拼接两个图片
     *
     * @param context        上下文
     * @param sourceBitmap   原始图片（将要被修饰的）
     * @param secondBitmap   将要被拼接的图片
     * @param isNeedWipeArea 是否需要重绘某个区域的颜色
     * @param width          要绘制的区域宽度（px）
     * @param height         要绘制的区域高度（px）
     * @param paddingLeft    要绘制的区域基于父控件的向左偏移量（px）
     * @param paddingTop     向右偏移量（px）
     * @param color          将要被重绘的颜色，如果参数isNeedWipeArea是false，则可以忽略
     * @return 返回拼接后的新图片
     */
    public static Bitmap embellishSplit(Context context, @NonNull Bitmap sourceBitmap, @NonNull Bitmap secondBitmap,
                                        boolean isNeedWipeArea, int width, int height, int paddingLeft, int paddingTop,
                                        @ColorInt int color)
    {
        Bitmap mScreenBitmap = sourceBitmap;
        if (isNeedWipeArea)
            mScreenBitmap = setTransparentAreaForBitmap(sourceBitmap, width, height, paddingLeft, paddingTop, color);
        Resources r = context.getResources();
        DisplayMetrics metrics = r.getDisplayMetrics();
        float scaleWidth = ((float) metrics.widthPixels) / secondBitmap.getWidth();
        int dstHeight = (int) (secondBitmap.getHeight() * scaleWidth);
        Bitmap mScaleBitmap = Bitmap.createScaledBitmap(secondBitmap, metrics.widthPixels, dstHeight, true);
        return ViewUtil.splitVertical(mScreenBitmap, mScaleBitmap);
    }

    /**
     * 纵向拼接图片
     *
     * @param first  开始的图片
     * @param second 结尾的图片
     * @return 返回拼接后的图片
     */
    public static Bitmap splitVertical(Bitmap first, Bitmap second)
    {
        int width = Math.max(first.getWidth(), second.getWidth());
        int height = first.getHeight() + second.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(first, 0, 0, null);
        canvas.drawBitmap(second, 0, first.getHeight(), null);
        return result;
    }

    /**
     * 重新绘制图片中指定区域的颜色
     *
     * @param b           原图片
     * @param width       要重绘区域的宽度
     * @param height      要重绘区域的高度
     * @param paddingleft 在原图中的左偏移量
     * @param paddingtop  在原图中的上偏移量
     * @param color       需要重新绘制的颜色
     * @return 返回重绘后的图片
     */
    public static Bitmap setTransparentAreaForBitmap(Bitmap b, int width, int height,
                                                     int paddingleft, int paddingtop,
                                                     int color)
    {
        if (b == null)
            return null;
        int[] pix = new int[width * height];
        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int index = j * width + i;
                pix[index] = color;
            }
        }
        b.setPixels(pix, 0, width, paddingleft, paddingtop, width, height);
        return b;
    }
}
