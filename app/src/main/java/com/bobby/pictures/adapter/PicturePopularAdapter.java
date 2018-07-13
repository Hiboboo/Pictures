package com.bobby.pictures.adapter;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.bobby.pictures.R;
import com.bobby.pictures.entity.PopularEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * 图片热门分类列表
 * <p>
 * Created by Bobby on 2018/07/13.
 */
public class PicturePopularAdapter extends BaseQuickAdapter<PopularEntity, BaseViewHolder>
{
    public PicturePopularAdapter(@Nullable List<PopularEntity> data)
    {
        super(R.layout.fragment_picture_popular_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PopularEntity item)
    {
        helper.setText(R.id.text_popular_name, item.name);
        AppCompatImageView mImageView = helper.getView(R.id.image_thumbnail);
        Resources r = mContext.getResources();
        DisplayMetrics outMetrics = r.getDisplayMetrics();
        ViewGroup.LayoutParams params = mImageView.getLayoutParams();
        final int space = r.getDimensionPixelSize(R.dimen.dp_6);
        int width = ((outMetrics.widthPixels - space * 3) / 3);
        params.width = width;
        params.height = (int) (width / 1.4);
        Glide.with(mContext)
                .load(item.thumbnail)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.white_bg)
                        .error(R.drawable.white_bg)
                        .centerCrop()
                        .override(width, (int) (width / 1.4)))
                .into(mImageView);
    }
}
