package com.bobby.pictures.actions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.bobby.pictures.R;
import com.bobby.pictures.actions.abs.ImmerseAppCompatActivity;
import com.bobby.pictures.service.JobSchedulerService;
import com.bobby.pictures.util.SettingManager;
import com.bobby.pictures.util.ViewUtil;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 设置相关
 * <p>
 * Created by Bobby on 2018/07/27.
 */
@RuntimePermissions
public class SettingActivity extends ImmerseAppCompatActivity
{
    private Switch mSwitch;
    private LinearLayout mWallpagerPanel;
    private EditText mKeywordInput;
    private EditText mIntervalTimeInput;
    private RadioGroup mNetworkTypeGroup;
    private RadioGroup mWallpagerSizes;

    private ViewUtil mViewUtil;

    private final String mNetworkType_Any = "Any";
    private final String mNetworkType_Wifi = "Wifi";

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState, R.layout.activity_setting_layout);
        SettingActivityPermissionsDispatcher.allowExternalStorageWithPermissionCheck(this);
        mViewUtil = new ViewUtil(this);
        this.initConfigs();
    }

    @Override
    public int getPageTitleRes()
    {
        return R.string.action_settings;
    }

    @Override
    protected void setupViews()
    {
        mSwitch = this.findViewById(R.id.switch_setting_wallpager);
        mSwitch.setOnCheckedChangeListener(mCheckChangeListener);
        mWallpagerPanel = this.findViewById(R.id.wallpager_setting_panel);
        mKeywordInput = this.findViewById(R.id.input_keyword);
        mIntervalTimeInput = this.findViewById(R.id.input_interval_time);
        mNetworkTypeGroup = this.findViewById(R.id.group_network_types);
        mWallpagerSizes = this.findViewById(R.id.group_wallpager_sizes);
    }

    private void initConfigs()
    {
        mSwitch.setChecked(SettingManager.isAutoRefreshService());
        mKeywordInput.setText(SettingManager.getSearchKeyword());
        mIntervalTimeInput.setText(String.valueOf(SettingManager.getAutoRefreshIntervalTime()));
        switch (SettingManager.getNetworkType())
        {
            case mNetworkType_Any:
                ((RadioButton) mNetworkTypeGroup.findViewById(R.id.radio_network_any)).setChecked(true);
                break;
            case mNetworkType_Wifi:
                ((RadioButton) mNetworkTypeGroup.findViewById(R.id.radio_network_wifi)).setChecked(true);
                break;
        }
        if (!SettingManager.isNeedWallpageCrop())
            ((RadioButton) mWallpagerSizes.findViewById(R.id.radio_size_original)).setChecked(true);
        else
            ((RadioButton) mWallpagerSizes.findViewById(R.id.radio_size_resolution)).setChecked(true);
    }

    @Override
    protected void onStop()
    {
        this.saveConfigs();
        super.onStop();
    }

    private void saveConfigs()
    {
        SettingManager.setAutoRefreshService(mSwitch.isChecked());
        SettingManager.setSearchKeyword(mKeywordInput.getText());
        int minute = 60;
        if (!TextUtils.isEmpty(mIntervalTimeInput.getText()))
            minute = Integer.parseInt(mIntervalTimeInput.getText().toString());
        SettingManager.setAutoRefreshIntervalTime(minute);
        int networkType = JobInfo.NETWORK_TYPE_ANY;
        switch (mNetworkTypeGroup.getCheckedRadioButtonId())
        {
            case R.id.radio_network_any:
                networkType = JobInfo.NETWORK_TYPE_ANY;
                SettingManager.setNetworkType(mNetworkType_Any);
                break;
            case R.id.radio_network_wifi:
                networkType = JobInfo.NETWORK_TYPE_UNMETERED;
                SettingManager.setNetworkType(mNetworkType_Wifi);
                break;
        }
        boolean isNeedCrop = false;
        switch (mWallpagerSizes.getCheckedRadioButtonId())
        {
            case R.id.radio_size_original:
                isNeedCrop = false;
                break;
            case R.id.radio_size_resolution:
                isNeedCrop = true;
                DisplayMetrics outMetrics = getResources().getDisplayMetrics();
                SettingManager.setWallpageCropWidth(outMetrics.widthPixels);
                SettingManager.setWallpageCropHeight(outMetrics.heightPixels);
                break;
        }
        SettingManager.setNeedWallpageCrop(isNeedCrop);
        SettingManager.save();
        this.startJobService(networkType, minute);
    }

    private CompoundButton.OnCheckedChangeListener mCheckChangeListener = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            switch (buttonView.getId())
            {
                case R.id.switch_setting_wallpager:
                    if (isChecked)
                        mViewUtil.showAnimView(mWallpagerPanel);
                    else
                        mViewUtil.hideAnimView(mWallpagerPanel, false);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SettingActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private boolean isAllowStorage = false;

    private void startJobService(int networkType, int minute)
    {
        if (!isAllowStorage)
            return;
        JobScheduler scheduler = (JobScheduler) this.getSystemService(JOB_SCHEDULER_SERVICE);
        int mOldId = SettingManager.getAutoRefreshServiceID();
        if (mOldId > 0)
        {
            Log.i("AutoRefreshService", "已取消服务：" + mOldId);
            scheduler.cancel(mOldId);
        }
        // 如果已经关闭了自动更新，就不要再开启服务了
        if (!mSwitch.isChecked())
            return;
        int mNid = (int) (System.currentTimeMillis() / 1000);
        SettingManager.setAutoRefreshServiceID(mNid);
        SettingManager.save();
        Log.i("AutoRefreshService", "开启新的服务：" + mNid);
        ComponentName component = new ComponentName(this, JobSchedulerService.class);
        JobInfo info = new JobInfo.Builder(mNid, component)
                .setRequiredNetworkType(networkType)
                .setPeriodic(minute * 60 * 1000)
                .setRequiresDeviceIdle(false)
                .setPersisted(true)
                .setBackoffCriteria(3000, JobInfo.BACKOFF_POLICY_LINEAR)
                .build();
        if (scheduler.schedule(info) == JobScheduler.RESULT_SUCCESS)
            Log.v("AutoRefreshService", "Auto refresh service start ..");
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void allowExternalStorage()
    {
        isAllowStorage = true;
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForStorage(final PermissionRequest request)
    {
        new AlertDialog.Builder(this)
                .setMessage(R.string.message_storage_permission)
                .setPositiveButton(R.string.label_allow, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        request.proceed();
                    }
                }).setNegativeButton(R.string.label_repulse, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                request.cancel();
            }
        }).show();
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForStorage()
    {

    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNeverAskForStorage()
    {

    }

    @Override
    protected boolean isDisplayHomeAsUpEnabled()
    {
        return true;
    }
}
