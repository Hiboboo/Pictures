package com.bobby.pictures.actions.home;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bobby.pictures.R;
import com.bobby.pictures.actions.abs.BaseFragment;
import com.bobby.pictures.adapter.PicturePopularAdapter;
import com.bobby.pictures.entity.PhotoEntity;
import com.bobby.pictures.entity.PopularEntity;
import com.chad.library.adapter.base.BaseQuickAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.parceler.Parcels;

import java.io.IOException;
import java.lang.ref.SoftReference;
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

    private int page = 1;
    private final RefreshRunnable refreshRunnable = new RefreshRunnable();

    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener()
    {
        @Override
        public void onRefresh()
        {
            page = 1;
            new Thread(refreshRunnable).start();
        }
    };

    private BaseQuickAdapter.RequestLoadMoreListener mLoadMoreListener = new BaseQuickAdapter.RequestLoadMoreListener()
    {
        @Override
        public void onLoadMoreRequested()
        {
            page++;
            new Thread(refreshRunnable).start();
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

    private static class RefreshLoaderHandler extends Handler
    {
        private SoftReference<PicturePopularFragment> reference;

        RefreshLoaderHandler(PicturePopularFragment activity)
        {
            reference = new SoftReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 200)
            {
                ArrayList<PopularEntity> entities = Parcels.unwrap(msg.getData().getParcelable("photos"));
                reference.get().setPicturePopularDatas(entities);
            } else
                reference.get().setPicturePopularDatas(null);
        }
    }

    private final RefreshLoaderHandler mHandler = new RefreshLoaderHandler(this);

    private class RefreshRunnable implements Runnable
    {
        @Override
        public void run()
        {
            Message message = mHandler.obtainMessage();
            try
            {
                ArrayList<PopularEntity> entities = getPopluarList(page);
                message.getData().putParcelable("photos", Parcels.wrap(entities));
                message.what = 200;
            } catch (IOException e)
            {
                e.printStackTrace();
                message.what = 500;
            }
            mHandler.sendMessage(message);
        }
    }

    private ArrayList<PopularEntity> getPopluarList(int page) throws IOException
    {
        ArrayList<PopularEntity> entities = new ArrayList<>();
        final String baseUrl = "https://www.pexels.com";
        Document doc = Jsoup.connect(baseUrl + "/popular-searches?page=" + page).get();
        Elements elements = doc.select("div[class*=l-lg-3 l-md-4 l-sm-6 search-medium]");
        for (Element element : elements)
        {
            Element linkElement = element.selectFirst("a[class*=search-medium__link]");
            if (null == linkElement)
                continue;
            PopularEntity entity = new PopularEntity();
            entity.pageUrl = baseUrl + linkElement.attr("href");
            Element imageElement = element.selectFirst("img[class*=search-medium__image]");
            entity.name = imageElement.attr("alt");
            entity.thumbnail = imageElement.attr("src");
            entities.add(entity);
        }
        return entities;
    }
}
