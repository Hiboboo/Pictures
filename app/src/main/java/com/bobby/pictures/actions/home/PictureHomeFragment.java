package com.bobby.pictures.actions.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.bobby.pictures.R;
import com.bobby.pictures.actions.abs.BaseFragment;
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
 * 首页全部图片
 * <p>
 * Created by Bobby on 2018/07/13.
 */
public class PictureHomeFragment extends BaseFragment
{
    private SwipeRefreshLayout mRefreshLayout;
    private PictureAdapter adapter;

    @Override
    protected int getLayoutResId()
    {
        return R.layout.fragment_picture_world_layout;
    }

    @Override
    protected void setupViews(View contentView)
    {
        mRefreshLayout = contentView.findViewById(R.id.refreshlayout);
        mRefreshLayout.setColorSchemeResources(R.color.red_50, R.color.purple_50, R.color.blue_50);
        mRefreshLayout.setOnRefreshListener(mRefreshListener);
        RecyclerView mRecyclerView = contentView.findViewById(R.id.recyclerview);
        FlexboxLayoutManager manager = new FlexboxLayoutManager(mContext);
        manager.setJustifyContent(JustifyContent.SPACE_BETWEEN);
        manager.setFlexWrap(FlexWrap.WRAP);
        manager.setFlexDirection(FlexDirection.ROW);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        adapter = new PictureAdapter(new ArrayList<PhotoEntity>());
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        AsynchronousManager.getInstance().setTag(PictureHomeFragment.class.getName(), AsynchronousManager.ControlCommand.GIVEUP);
    }

    private BaseQuickAdapter.OnItemClickListener mItemClickListener = new BaseQuickAdapter.OnItemClickListener()
    {
        @Override
        public void onItemClick(BaseQuickAdapter a, View view, int position)
        {
            PhotoEntity entity = adapter.getItem(position);
            Intent intent = new Intent(mContext, PictureShowActivity.class);
            intent.putExtra(App.Key.KEY_EXTRA_DATA, entity.deatilPage);
            startActivity(intent);
        }
    };

    private int page = 2;

    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener()
    {
        @Override
        public void onRefresh()
        {
            page = 2;
            AsynchronousManager.getInstance().refreshApiValues(ExecuteApi.Apis.HOME_LIST);
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

    private void setNewDatas(ArrayList<PhotoEntity> entities)
    {
        mRefreshLayout.setRefreshing(false);
        adapter.loadMoreComplete();
        if (null == entities)
            return;
        if (page > 2)
        {
            adapter.addData(entities);
//            adapter.setEnableLoadMore(!entities.isEmpty());
        } else
            adapter.setNewData(entities);
    }

    private void getDatas()
    {
        final String TAG = PictureHomeFragment.class.getName();
        new AsynchronousManager.Builder(TAG)
                .addParams(new ExecuteApi.Params.Builder()
                        .addPage(page)
                        .build())
                .setApi(ExecuteApi.Apis.HOME_LIST)
                .setOnResultCallback(new AsynchronousManager.OnResultCallback<ArrayList<PhotoEntity>>()
                {
                    @Override
                    public void onSuccessResult(String tag, ArrayList<PhotoEntity> data)
                    {
                        if (TextUtils.equals(tag, TAG))
                            setNewDatas(data);
                    }

                    @Override
                    public void onFailure(String tag)
                    {
                        if (TextUtils.equals(tag, TAG))
                            setNewDatas(null);
                    }
                }).execute();
    }
}
