package com.bobby.pictures.adapter;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;

import com.bobby.pictures.R;
import com.bobby.pictures.entity.PhotoEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
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
        NiceImageView mImageView = helper.getView(R.id.image_thumbnail);
        int imageWidth = item.width;
        int imageHeight = item.height;
        Resources r = mContext.getResources();
        DisplayMetrics outMetrics = r.getDisplayMetrics();
        int targetWidth = ((outMetrics.widthPixels - r.getDimensionPixelSize(R.dimen.dp_3) * 4) / 3);
        int targetHeight = (int) (imageHeight / (float) (imageWidth / targetWidth));
        ViewGroup.LayoutParams params = mImageView.getLayoutParams();
        params.width = targetWidth;
        params.height = targetHeight;
        Log.d(TAG, targetWidth + "|" + targetHeight + "&" + imageWidth + "|" + imageHeight);
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
                        .centerCrop()
                        .override(targetWidth, targetHeight))
                .into(mImageView);
    }
}