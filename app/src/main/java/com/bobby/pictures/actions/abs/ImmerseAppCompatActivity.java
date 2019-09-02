package com.bobby.pictures.actions.abs;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.gyf.immersionbar.ImmersionBar;

/**
 * 具备沉浸式状态栏
 * <p>
 * 作者：孙博
 * <p>
 * 时间：2017/7/1 20:14
 */
public abstract class ImmerseAppCompatActivity extends BaseAppCompatActivity
{
    protected ImmersionBar mImmersionBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @LayoutRes int layoutRes)
    {
        super.onCreate(savedInstanceState, layoutRes);
        this.setupImmersionBar();
    }

    private void setupImmersionBar()
    {
        mImmersionBar = ImmersionBar.with(this)
                .statusBarColor(android.R.color.white)
                .fitsSystemWindows(true)
                .statusBarDarkFont(true, 1.0f);
        mImmersionBar.init();
    }
}
