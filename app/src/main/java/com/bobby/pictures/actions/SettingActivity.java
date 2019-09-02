package com.bobby.pictures.actions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bobby.pictures.R;
import com.bobby.pictures.actions.abs.ImmerseAppCompatActivity;
import com.bobby.pictures.service.DownloadSettingsWorker;
import com.bobby.pictures.util.SettingManager;
import com.bobby.pictures.util.ViewUtil;

import java.util.concurrent.TimeUnit;

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
        int minute = 15;
        if (!TextUtils.isEmpty(mIntervalTimeInput.getText()))
            minute = Integer.parseInt(mIntervalTimeInput.getText().toString());
        SettingManager.setAutoRefreshIntervalTime(minute);
        NetworkType networkType = NetworkType.CONNECTED;
        switch (mNetworkTypeGroup.getCheckedRadioButtonId())
        {
            case R.id.radio_network_any:
                networkType = NetworkType.CONNECTED;
                SettingManager.setNetworkType(mNetworkType_Any);
                break;
            case R.id.radio_network_wifi:
                networkType = NetworkType.UNMETERED;
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
        this.startWorkerTask(networkType, minute);
    }

    private CompoundButton.OnCheckedChangeListener mCheckChangeListener = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            if (buttonView.getId() == R.id.switch_setting_wallpager)
            {
                if (isChecked)
                    mViewUtil.showAnimView(mWallpagerPanel);
                else
                    mViewUtil.hideAnimView(mWallpagerPanel, false);
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

    private void startWorkerTask(NetworkType networkType, int minute)
    {
        if (!isAllowStorage)
            return;
        WorkManager.getInstance().cancelAllWork();
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(
                DownloadSettingsWorker.class,
                minute,
                TimeUnit.MINUTES
        );
        builder.setInputData(new Data.Builder()
                .build());
        builder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(networkType)
                .setTriggerContentMaxDelay(5, TimeUnit.MINUTES)
                .build());
        WorkRequest request = builder.build();
        Log.i("AutoRefreshService", "开启新的服务：" + request.getId());
        Operation operation = WorkManager.getInstance().enqueue(request);
        if (operation.getState().getValue() instanceof Operation.State.SUCCESS)
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
