package com.bobby.pictures.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alexvasilkov.gestures.views.GestureImageView;
import com.bobby.pictures.R;
import com.bobby.pictures.entity.PhotoEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

/**
 * 图片单张和相似列表
 * <p>
 * Created by Bobby on 2018/07/17.
 */
public class PictureShowAdapter extends PagerAdapter
{
    private final List<PhotoEntity> entities;
    private final LayoutInflater inflater;

    public PictureShowAdapter(Context context, List<PhotoEntity> entities)
    {
        this.entities = entities;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return entities.size();
    }

    public PhotoEntity getItem(int position)
    {
        return entities.get(position);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position)
    {
        View view = inflater.inflate(R.layout.activity_picture_show_item, container, false);
        container.addView(view);
        this.setupViews(view, position);
        return view;
    }

    private void setupViews(final View view, int position)
    {
        PhotoEntity entity = entities.get(position);
        ColorDrawable mLoadingColor;
        int[] rgb = entity.rgb;
        if (rgb.length == 3)
            mLoadingColor = new ColorDrawable(Color.rgb(rgb[0], rgb[1], rgb[2]));
        else
            mLoadingColor = new ColorDrawable(Color.BLACK);
        final GestureImageView mImageView = view.findViewById(R.id.image);
        if (listener != null)
            listener.onViewChanged(mImageView, mLoadingColor, position);
        String srcUrl = TextUtils.isEmpty(entity.largeSrc) ? entity.bigSrc : entity.largeSrc;
        Glide.with(inflater.getContext())
                .load(srcUrl)
                .apply(new RequestOptions()
                        .placeholder(mLoadingColor)
                        .error(mLoadingColor))
                .into(new SimpleTarget<Drawable>()
                {
                    FrameLayout mProgressLayout = view.findViewById(R.id.image_loading_bar);

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition)
                    {
                        mProgressLayout.setVisibility(View.GONE);
                        mImageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable)
                    {
                        mProgressLayout.setVisibility(View.GONE);
                    }
                });
    }

    private OnChildViewChangeListener listener;

    public void setOnChildViewChangeListener(OnChildViewChangeListener listener)
    {
        this.listener = listener;
    }

    public interface OnChildViewChangeListener
    {
        void onViewChanged(GestureImageView mImageView, ColorDrawable backgroundColor, int position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object)
    {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
    {
        return object == view;
    }
}
