package com.bobby.pictures.adapter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bobby.pictures.R;
import com.bobby.pictures.entity.PhotoEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.othershe.library.NiceImageView;

import java.util.List;

/**
 * 图片列表
 * <p>
 * Created by Bobby on 2018/07/12.
 */
public class PictureAdapter extends BaseQuickAdapter<PhotoEntity, BaseViewHolder>
{
    private final String TAG = PictureAdapter.class.getSimpleName();

    public PictureAdapter(@Nullable List<PhotoEntity> data)
    {
        super(R.layout.fragment_picture_world_item, data);
    }

    @Override
    protected void convert(final BaseViewHolder helper, PhotoEntity item)
    {
        final NiceImageView mImageView = helper.getView(R.id.image_thumbnail);
        ViewGroup.LayoutParams params = helper.itemView.getLayoutParams();
        if (params instanceof FlexboxLayoutManager.LayoutParams)
            ((FlexboxLayoutManager.LayoutParams) params).setFlexGrow(1.0f);
        ColorDrawable mLoadingColor;
        int[] rgb = item.rgb;
        if (rgb.length == 3)
            mLoadingColor = new ColorDrawable(Color.rgb(rgb[0], rgb[1], rgb[2]));
        else
            mLoadingColor = new ColorDrawable(Color.WHITE);
        Log.d(TAG, item.smallSrc);
        Glide.with(mContext)
                .load(item.smallSrc)
                .apply(new RequestOptions()
                        .placeholder(mLoadingColor)
                        .error(mLoadingColor)
                        .centerCrop())
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition)
                    {
                        mImageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder)
                    {

                    }
                });
    }
}