package com.bobby.pictures.actions.home;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bobby.pictures.R;
import com.bobby.pictures.actions.abs.BaseFragment;
import com.bobby.pictures.adapter.PicturePopularAdapter;
import com.bobby.pictures.app.App;
import com.bobby.pictures.entity.PopularEntity;
import com.bobby.pictures.util.AsynchronousManager;
import com.bobby.pictures.util.ExecuteApi;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;

/**
 * 热门分类
 * <p>
 * Created by Bobby on 2018/07/13.
 */
public class PicturePopularFragment extends BaseFragment
{
    private SwipeRefreshLayout mRefreshLayout;
    private PicturePopularAdapter adapter;

    @Override
    protected int getLayoutResId()
    {
        return R.layout.fragment_picture_popular_layout;
    }

    @Override
    protected void setupViews(View contentView)
    {
        mRefreshLayout = contentView.findViewById(R.id.refreshlayout);
        mRefreshLayout.setColorSchemeResources(R.color.red_50, R.color.purple_50, R.color.blue_50);
        mRefreshLayout.setOnRefreshListener(mRefreshListener);
        RecyclerView mRecyclerView = contentView.findViewById(R.id.recyclerview);
        GridLayoutManager manager = new GridLayoutManager(mContext, 3);
        mRecyclerView.setLayoutManager(manager);
        final int space = getResources().getDimensionPixelSize(R.dimen.dp_6);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(space));
        adapter = new PicturePopularAdapter(new ArrayList<PopularEntity>());
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(mLoadMoreListener, mRecyclerView);
        adapter.setOnItemClickListener(mItemClickListener);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mRefreshLayout.setRefreshing(true);
        mRefreshListener.onRefresh();
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration
    {
        private int space;

        SpacesItemDecoration(int space)
        {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state)
        {
            int position = parent.getChildAdapterPosition(view);
            if (position < 3)
            {
                outRect.top = space;
                outRect.bottom = space;
            } else
                outRect.bottom = space;
            int remainder = (position % 3);
            switch (remainder)
            {
                case 1:
                    outRect.left = space;
                    outRect.right = space;
                    break;
                case 0:
                    outRect.left = space;
                    break;
                case 2:
                    outRect.right = space;
                    break;
            }
        }
    }

    private BaseQuickAdapter.OnItemClickListener mItemClickListener = new BaseQuickAdapter.OnItemClickListener()
    {
        @Override
        public void onItemClick(BaseQuickAdapter a, View view, int position)
        {
            PopularEntity entity = adapter.getItem(position);
            Intent intent = new Intent(mContext, PopularPictureActivity.class);
            intent.putExtra(App.Key.KEY_EXTRA_TITLE, entity.name);
            intent.putExtra(App.Key.KEY_EXTRA_DATA, entity.pageUrl);
            startActivity(intent);
        }
    };

    private int page = 1;

    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener()
    {
        @Override
        public void onRefresh()
        {
            page = 1;
            AsynchronousManager.getInstance().refreshApiValues(ExecuteApi.Apis.POPULAR_LIST);
            getDatas();
        }
    };

    private BaseQuickAdapter.RequestLoadMoreListener mLoadMoreListener = new BaseQuickAdapter.RequestLoadMoreListener()
    {
        @Override
        public void onLoadMoreRequested()
        {
            page++;
            getDatas();
        }
    };

    private void setPicturePopularDatas(ArrayList<PopularEntity> entities)
    {
        mRefreshLayout.setRefreshing(false);
        adapter.loadMoreComplete();
        if (null == entities)
            return;
        if (page > 1)
        {
            adapter.addData(entities);
            adapter.setEnableLoadMore(!entities.isEmpty());
        } else
            adapter.setNewData(entities);
    }

    private void getDatas()
    {
        final String TAG = PicturePopularFragment.class.getName();
        new AsynchronousManager.Builder(TAG)
                .setApi(ExecuteApi.Apis.POPULAR_LIST)
                .addParams(new ExecuteApi.Params.Builder()
                        .addPage(page)
                        .build())
                .setOnResultCallback(new AsynchronousManager.OnResultCallback<ArrayList<PopularEntity>>()
                {
                    @Override
                    public void onSuccessResult(String tag, ArrayList<PopularEntity> data)
                    {
                        if (tag.equals(TAG))
                            setPicturePopularDatas(data);
                    }

                    @Override
                    public void onFailure(String tag)
                    {
                        if (tag.equals(TAG))
                            setPicturePopularDatas(null);
                    }
                }).execute();
    }
}
