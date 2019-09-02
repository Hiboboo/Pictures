package com.bobby.pictures.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * 支持滑动到底部自动加载更多
 * <p>
 * Created by Bobby on 2018/07/12.
 */
public class PictureRecyclerView extends RecyclerView
{
    public PictureRecyclerView(Context context)
    {
        super(context);
        this.init();
    }

    public PictureRecyclerView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        this.init();
    }

    public PictureRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.init();
    }

    private void init()
    {
        this.addOnScrollListener(mOnScrollListener);
    }

    private OnScrollListener mOnScrollListener = new OnScrollListener()
    {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);
        }
    };
}
