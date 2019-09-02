package com.bobby.pictures.actions.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.bobby.pictures.R;
import com.bobby.pictures.actions.abs.ImmerseAppCompatActivity;
import com.bobby.pictures.adapter.PictureAdapter;
import com.bobby.pictures.app.App;
import com.bobby.pictures.entity.PhotoEntity;
import com.bobby.pictures.util.AsynchronousManager;
import com.bobby.pictures.util.ExecuteApi;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;

/**
 * 具体分类图片列表
 * <p>
 * Created by Bobby on 2018/07/18.
 */
public class PopularPictureActivity extends ImmerseAppCompatActivity
{
    private SwipeRefreshLayout mRefreshLayout;
    private PictureAdapter adapter;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState, R.layout.activity_popular_picture_layout);
        mRefreshLayout.setRefreshing(true);
        mRefreshListener.onRefresh();
    }

    @Override
    public CharSequence getPageTitle()
    {
        return getIntent().getStringExtra(App.Key.KEY_EXTRA_TITLE);
    }

    @Override
    protected void setupViews()
    {
        mRefreshLayout = this.findViewById(R.id.refreshlayout);
        mRefreshLayout.setColorSchemeResources(R.color.red_50, R.color.purple_50, R.color.blue_50);
        mRefreshLayout.setOnRefreshListener(mRefreshListener);
        RecyclerView mRecyclerView = this.findViewById(R.id.recyclerview);
        FlexboxLayoutManager manager = new FlexboxLayoutManager(this);
        manager.setJustifyContent(JustifyContent.SPACE_BETWEEN);
        manager.setFlexWrap(FlexWrap.WRAP);
        manager.setFlexDirection(FlexDirection.ROW);
        mRecyclerView.setLayoutManager(manager);
        adapter = new PictureAdapter(new ArrayList<PhotoEntity>());
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(mLoadMoreListener, mRecyclerView);
        adapter.setOnItemClickListener(mItemClickListener);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        AsynchronousManager.getInstance().setTag(PopularPictureActivity.class.getName(), AsynchronousManager.ControlCommand.GIVEUP);
    }

    private BaseQuickAdapter.OnItemClickListener mItemClickListener = new BaseQuickAdapter.OnItemClickListener()
    {
        @Override
        public void onItemClick(BaseQuickAdapter a, View view, int position)
        {
            PhotoEntity entity = adapter.getItem(position);
            Intent intent = new Intent(PopularPictureActivity.this, PictureShowActivity.class);
            intent.putExtra(App.Key.KEY_EXTRA_DATA, entity.deatilPage);
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

    private void setPicturePopularDatas(ArrayList<PhotoEntity> entities)
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
        final String TAG = PopularPictureActivity.class.getName();
        new AsynchronousManager.Builder(TAG)
                .setApi(ExecuteApi.Apis.POPULAR_IMAGES)
                .addParams(new ExecuteApi.Params.Builder()
                        .addUrl(getIntent().getStringExtra(App.Key.KEY_EXTRA_DATA))
                        .addPage(page)
                        .build())
                .setOnResultCallback(new AsynchronousManager.OnResultCallback<ArrayList<PhotoEntity>>()
                {
                    @Override
                    public void onSuccessResult(String tag, ArrayList<PhotoEntity> data)
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

    @Override
    protected boolean isDisplayHomeAsUpEnabled()
    {
        return true;
    }
}
