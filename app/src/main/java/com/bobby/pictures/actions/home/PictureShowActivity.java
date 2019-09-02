package com.bobby.pictures.actions.home;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexvasilkov.gestures.commons.DepthPageTransformer;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bobby.pictures.R;
import com.bobby.pictures.actions.abs.ImmerseAppCompatActivity;
import com.bobby.pictures.adapter.PictureShowAdapter;
import com.bobby.pictures.app.App;
import com.bobby.pictures.entity.PhotoEntity;
import com.bobby.pictures.entity.UserEntity;
import com.bobby.pictures.util.AsynchronousManager;
import com.bobby.pictures.util.DownloadManager;
import com.bobby.pictures.util.ExecuteApi;
import com.bobby.pictures.util.SettingManager;
import com.bobby.pictures.widget.FreshDownloadView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lzy.okgo.model.Progress;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 单个及相似图片展示
 * <p>
 * Created by Bobby on 2018/07/16.
 */
public class PictureShowActivity extends ImmerseAppCompatActivity
{
    private ViewPager mViewPager;
    private LinearLayout mBottomPanel;
    private RelativeLayout mUserGroup;
    private CircleImageView mUserPortrait;
    private TextView mNickname;
    private TextView mPhotos;
    private FreshDownloadView mDownloadView;
    private AppCompatImageView mDownloadState;

    private Set<String> pids;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState, R.layout.activity_picture_show_layout);
        mImmersionBar
                .fitsSystemWindows(false)
                .transparentStatusBar()
                .init();
        this.onLoadingDatas();
        pids = SettingManager.getDownloadedPictures();
    }

    @Override
    protected void setupViews()
    {
        mViewPager = this.findViewById(R.id.viewpager);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mBottomPanel = this.findViewById(R.id.bottom_panel);
        mUserGroup = this.findViewById(R.id.group_user_panel);
        mUserPortrait = this.findViewById(R.id.image_user_portrait);
        mNickname = this.findViewById(R.id.text_user_nickname);
        mPhotos = this.findViewById(R.id.text_user_photos);
        mPhotos.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mPhotos.setOnClickListener(mClickListener);
        mDownloadView = this.findViewById(R.id.action_item_download);
        mDownloadView.setOnClickListener(mClickListener);
        mDownloadState = this.findViewById(R.id.action_item_download_state);
    }

    @Override
    protected void onLoadingDatas()
    {
        final String TAG = PictureShowActivity.class.getName();
        new AsynchronousManager.Builder(TAG)
                .addParams(new ExecuteApi.Params.Builder()
                        .addUrl(getIntent().getStringExtra(App.Key.KEY_EXTRA_DATA))
                        .build())
                .setApi(ExecuteApi.Apis.SHOW_IMG)
                .setOnResultCallback(new AsynchronousManager.OnResultCallback<PhotoEntity>()
                {
                    @Override
                    public void onSuccessResult(String tag, PhotoEntity data)
                    {
                        if (tag.equals(TAG))
                            showImage(data);
                    }

                    @Override
                    public void onFailure(String tag)
                    {
                        if (tag.equals(TAG))
                            showImage(null);
                    }
                }).execute();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // 保存当前已下载过的图片ID
        SettingManager.setDownloadedPictures(pids);
        SettingManager.save();
    }

    private String mCurrentDownloadTag;

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // 放弃所有获取源图片的请求
        AsynchronousManager.getInstance().setTag(PictureShowActivity.class.getName(), AsynchronousManager.ControlCommand.GIVEUP);
        if (!TextUtils.isEmpty(mCurrentDownloadTag))
            DownloadManager.getInstance().cancelDownload(mCurrentDownloadTag);
    }

    private void showImage(PhotoEntity entity)
    {
        if (null == entity)
        {
            this.onUpdateStateChangeLayout(PageLoadState.FAILURE);
            return;
        }
        this.onUpdateStateChangeLayout(PageLoadState.SUCCESSFUL);
        List<PhotoEntity> entities = new ArrayList<>();
        entities.add(entity);
        entities.addAll(entity.mSimilarPhotos);
        PictureShowAdapter adapter = new PictureShowAdapter(this, entities);
        adapter.setOnChildViewChangeListener(mChildListener);
        mViewPager.setAdapter(adapter);
        this.setUserInfo(entity.user);
    }

    private void setUserInfo(UserEntity entity)
    {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_occupy_portrait);
        ViewGroup.LayoutParams params = mUserPortrait.getLayoutParams();
        params.width = drawable.getIntrinsicWidth();
        params.height = drawable.getIntrinsicHeight();
        Glide.with(this)
                .load(entity.avatar)
                .apply(new RequestOptions()
                        .override(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()))
                .into(mUserPortrait);
        mNickname.setText(entity.author);
        if (!TextUtils.isEmpty(entity.userPage))
        {
            mPhotos.setText(entity.userPageTitle);
            mPhotos.setTag(R.id.tag_item_data, entity);
        }
    }

    private boolean isBottomPanelShowing = true;
    private int mCurrentDownloadPosition = -1;

    private View.OnClickListener mClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.text_user_photos:
                    Object object = v.getTag(R.id.tag_item_data);
                    if (object instanceof UserEntity)
                    {
                        UserEntity entity = (UserEntity) object;
                        Intent intent = new Intent(PictureShowActivity.this, PeoplePictureActivity.class);
                        intent.putExtra(App.Key.KEY_EXTRA_DATA, entity.userPage);
                        startActivity(intent);
                    }
                    break;
                case R.id.image:
                    mBottomPanel.setAnimation(AnimationUtils.loadAnimation(PictureShowActivity.this,
                            isBottomPanelShowing ? R.anim.window_hidden_anim : R.anim.window_show_anim));
                    mBottomPanel.setVisibility(isBottomPanelShowing ? View.GONE : View.VISIBLE);
//                    mImmersionBar.hideBar(isBottomPanelShowing ? BarHide.FLAG_HIDE_NAVIGATION_BAR : BarHide.FLAG_SHOW_BAR).init();
                    isBottomPanelShowing = !isBottomPanelShowing;
                    break;
                case R.id.action_item_download:
                    if (mDownloadView.using())
                        return;
                    PagerAdapter adapter = mViewPager.getAdapter();
                    if (adapter instanceof PictureShowAdapter)
                    {
                        PictureShowAdapter a = (PictureShowAdapter) adapter;
                        int currentItem = mViewPager.getCurrentItem();
                        mCurrentDownloadPosition = currentItem;
                        final PhotoEntity entity = a.getItem(currentItem);
                        final String tag = entity.id;
                        mCurrentDownloadTag = tag;
                        DownloadManager.getInstance().startDownload(tag, entity.downloadUrl,
                                new DownloadManager.OnDownloadListener(tag)
                                {
                                    @Override
                                    public void onStart(Progress progress)
                                    {
                                        mDownloadView.startDownload();
                                        super.onStart(progress);
                                    }

                                    @Override
                                    public void onProgress(Progress progress)
                                    {
                                        mDownloadView.upDateProgress(progress.fraction);
                                        super.onProgress(progress);
                                    }

                                    @Override
                                    public void onFinish(File file, Progress progress)
                                    {
                                        mDownloadView.showDownloadOk();
                                        pids.add(entity.id);
                                        SettingManager.putDownloadedPicture(entity.id, file.getAbsolutePath());
                                        SettingManager.save();
                                        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                        Ringtone rt = RingtoneManager.getRingtone(getApplicationContext(), uri);
                                        rt.play();
                                        if (mCurrentDownloadPosition != mViewPager.getCurrentItem())
                                            mDownloadView.reset();
                                        super.onFinish(file, progress);
                                    }

                                    @Override
                                    public void onError(Progress progress)
                                    {
                                        mDownloadView.showDownloadError();
                                        new Handler().postDelayed(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                mDownloadView.reset();
                                            }
                                        }, 1500);
                                        progress.exception.printStackTrace();
                                        super.onError(progress);
                                    }
                                });
                    }
                    break;
            }
        }
    };

    private ViewPager.SimpleOnPageChangeListener mPageChangeListener = new ViewPager.SimpleOnPageChangeListener()
    {
        @Override
        public void onPageSelected(int position)
        {
            mUserGroup.setVisibility((position == 0) ? View.VISIBLE : View.GONE);
            if (mDownloadView.getStatus() == FreshDownloadView.STATUS.DOWNLOADING) return;
            PhotoEntity entity = ((PictureShowAdapter) mViewPager.getAdapter()).getItem(position);
            updateDownloadViewState(entity);
        }
    };

    private int mOldColor = -1;

    private PictureShowAdapter.OnChildViewChangeListener mChildListener = new PictureShowAdapter.OnChildViewChangeListener()
    {
        @Override
        public void onViewChanged(GestureImageView mImageView, ColorDrawable backgroundColor, int position)
        {
            if (mViewPager.getAdapter() instanceof PictureShowAdapter)
            {
                PictureShowAdapter adapter = (PictureShowAdapter) mViewPager.getAdapter();
                int currentItem = mViewPager.getCurrentItem();
                PhotoEntity entity = adapter.getItem(currentItem);
                mImageView.setOnClickListener(mClickListener);
                mImageView.getController().enableScrollInViewPager(mViewPager);
                int[] rgb = entity.rgb;
                final int curColor = Color.rgb(rgb[0], rgb[1], rgb[2]);
                final int oldColor = (mOldColor != -1 ? mOldColor : curColor);
                mOldColor = curColor;
                ValueAnimator colorAnim = ObjectAnimator.ofObject(new ArgbEvaluator(), oldColor, curColor);
                colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation)
                    {
                        int color = (int) animation.getAnimatedValue();//之后就可以得到动画的颜色了
                        mViewPager.setBackgroundColor(color);
                    }
                });
                colorAnim.setDuration(399);
                colorAnim.start();

                if (position == 0)
                    updateDownloadViewState(entity);
            }
        }
    };

    private void updateDownloadViewState(PhotoEntity entity)
    {
        mDownloadView.reset();
        if (pids.contains(entity.id))
        {
            String absPath = SettingManager.getDownloadedPicture(entity.id);
            if (new File(absPath).exists())
            {
                mDownloadView.setVisibility(View.GONE);
                mDownloadState.setVisibility(View.VISIBLE);
                return;
            }
        }
        mDownloadState.setVisibility(View.GONE);
        mDownloadView.setVisibility(View.VISIBLE);
    }

    @Override
    protected boolean isNeedPackLayout()
    {
        return true;
    }
}
