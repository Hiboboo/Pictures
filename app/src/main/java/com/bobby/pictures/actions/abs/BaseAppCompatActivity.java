package com.bobby.pictures.actions.abs;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.bobby.pictures.R;
import com.bobby.pictures.util.Utils;


/**
 * 作为一个基础的页面抽象层，抽象出一些通用且适用的方法，使其派生类能够实现高效简洁的代码编写，
 * 以及轻微的帮助派生类在业务实现上更加专注。
 * <HR>
 * 创建者 Bobby
 * <p>
 * 时间 2017/8/15 16:23
 * <p>
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity
{
    private int layoutRes;

    /**
     * 继承者必须传入合法的布局资源文件ID，且要保证在资源文件中已定义<code>{@link Toolbar}</code>，还需要注意，{@code Toolbar}的ID必须定义为toolbar
     *
     * @param layoutRes          合法的布局资源ID
     * @param savedInstanceState 继承自超类<code>{@link #onCreate(Bundle)}</code>
     */
    public void onCreate(@Nullable Bundle savedInstanceState, @LayoutRes int layoutRes)
    {
        super.onCreate(savedInstanceState);
        this.layoutRes = layoutRes;
        this.setContentView(isNeedPackLayout() ? R.layout.activity_basic_layout : layoutRes);
        this.onRefreshLayout(!isNeedPackLayout());
    }

    private void onRefreshLayout(boolean isAllowSetupViews)
    {
        Toolbar mToolbar = this.findViewById(R.id.toolbar);
        if (mToolbar != null)
            mToolbar.setTitle(getPageTitle());
        this.setSupportActionBar(mToolbar);
        ActionBar mActionbar = getSupportActionBar();
        if (mActionbar != null)
            mActionbar.setDisplayHomeAsUpEnabled(isDisplayHomeAsUpEnabled());
        if (isAllowSetupViews)
            this.setupViews();
    }

    /**
     * 获取页面标题
     *
     * @return 返回页面的标题，默认返回应用程序的名称
     */
    public CharSequence getPageTitle()
    {
        return getResources().getString(getPageTitleRes());
    }

    /**
     * 获取页面标题
     *
     * @return 返回页面标题的资源ID
     */
    public int getPageTitleRes()
    {
        return R.string.app_name;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // 检测当前环境下是否有可用网络，没有就提示用户去设置
        if (!Utils.isOpenNetwork(this))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.message_notfound_network).setMessage(R.string.message_setting_network_now);
            builder.setPositiveButton(R.string.label_network_settings, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Intent mSettingNetwork;
                    int mCurrentSDKVersion = Build.VERSION.SDK_INT;
                    if (mCurrentSDKVersion > 10)
                        mSettingNetwork = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                    else
                    {
                        mSettingNetwork = new Intent();
                        ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                        mSettingNetwork.setComponent(comp);
                        mSettingNetwork.setAction(Intent.ACTION_VIEW);
                    }
                    startActivity(mSettingNetwork);
                }
            }).setNegativeButton(R.string.label_next_settings, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                    finish();
                }
            }).show();
        }
    }

    /**
     * 是否需要对当前页面的布局进行重新包装
     *
     * @return 默认为<code>false</code>，若需要则重写该方法并返回<code>true</code>
     */
    protected boolean isNeedPackLayout()
    {
        return false;
    }

    private boolean isLoadSuccess = false;

    /**
     * 更新加载状态并同时改变当前页面的布局
     *
     * @param state 页面的加载状态
     */
    public void onUpdateStateChangeLayout(PageLoadState state)
    {
        if (state == PageLoadState.SUCCESSFUL)
        {
            if (!isLoadSuccess)
            {
                @SuppressLint("WrongViewCast") FrameLayout mRootLayout = this.findViewById(R.id.rootview_layout);
                mRootLayout.removeAllViews();
                mRootLayout.addView(getLayoutInflater().inflate(layoutRes, mRootLayout, false));
                this.onRefreshLayout(true);
                isLoadSuccess = true;
            }
        } else
        {
            final FrameLayout mRootLayout = this.findViewById(R.id.rootview_layout);
            mRootLayout.removeAllViews();
            mRootLayout.addView(getLayoutInflater().inflate(state.layout, mRootLayout, false));
        }
    }

    /**
     * 加载页面的数据。
     * <p/>
     * 当<code>{@link #isNeedPackLayout()}</code>方法返回<code>true</code>时，重写该方法是非常有意义的。
     *
     * @see #isNeedPackLayout()
     */
    protected void onLoadingDatas()
    {
        // TODO 由子类重写
    }

    /**
     * 布局加载的状态
     */
    public enum PageLoadState
    {
        /**
         * 什么都不做
         */
        NONE(R.layout.activity_basic_layout),
        /**
         * 正在加载中
         */
        LOADING(R.layout.activity_basic_loading_layout),
        /**
         * 成功时
         */
        SUCCESSFUL(0x0200),
        /**
         * 失败时
         */
        FAILURE(R.layout.activity_basic_failure_layout),
        /**
         * 成功但没有数据
         */
        NODATA(R.layout.activity_basic_nodata_layout);

        private int layout;

        PageLoadState(int layout)
        {
            this.layout = layout;
        }
    }

    /**
     * 实例化当前页面中的所有View对象
     */
    protected abstract void setupViews();

    /**
     * 当前页面是否需要左上角的返回按钮
     *
     * @return 若需要则返回<code>true</code>，否则返回<code>false</code>
     */
    protected boolean isDisplayHomeAsUpEnabled()
    {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
