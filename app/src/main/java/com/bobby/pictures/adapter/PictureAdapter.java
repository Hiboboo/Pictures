package com.bobby.pictures.adapter;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.DisplayMetrics;
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
        ViewGroup.LayoutParams params = mImageView.getLayoutParams();
        int targetWidth = this.calculateImageByScreenWidth(imageWidth);
        params.width = targetWidth;
        params.height = imageHeight;
        ColorDrawable mLoadingColor;
        int[] rgb = item.rgb;
        if (rgb.length == 3)
            mLoadingColor = new ColorDrawable(Color.rgb(rgb[0], rgb[1], rgb[2]));
        else
            mLoadingColor = new ColorDrawable(Color.WHITE);
        Glide.with(mContext)
                .load(item.smallSrc)
                .apply(new RequestOptions()
                        .placeholder(mLoadingColor)
                        .error(mLoadingColor)
                        .centerCrop()
                        .override(targetWidth, imageHeight))
                .into(mImageView);
    }

    /**
     * 计算一张图片在屏幕中所占的位置宽度
     * <p>
     * 这首先取决于图片本身的宽度，根据图片宽度占屏幕宽度比例（difference），算出三种结果：
     * <ul>
     * <li>difference < 0.4 ? screenWidth * 0.3</li>
     * <li>difference < 0.75 ? screenWidth * 0.485</li>
     * <li>screenWidth * 0.98</li>
     * </ul>
     *
     * @param imageWidth 图片的原始宽度
     * @return 返回经过计算后得到的图片占位宽
     */
    private int calculateImageByScreenWidth(int imageWidth)
    {
        Resources r = mContext.getResources();
        DisplayMetrics outMetrics = r.getDisplayMetrics();
        float difference = (imageWidth / (float) outMetrics.widthPixels);
        if (difference < 0.4)
            return (int) (outMetrics.widthPixels * 0.29);
        if (difference < 0.75)
            return (int) (outMetrics.widthPixels * 0.485);
        return (int) (outMetrics.widthPixels * 0.98);
    }
}