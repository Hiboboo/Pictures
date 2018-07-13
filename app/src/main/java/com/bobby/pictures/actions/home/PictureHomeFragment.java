package com.bobby.pictures.actions.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bobby.pictures.R;
import com.bobby.pictures.actions.abs.BaseFragment;
import com.bobby.pictures.adapter.PictureAdapter;
import com.bobby.pictures.entity.PhotoEntity;
import com.bobby.pictures.entity.UserEntity;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.parceler.Parcels;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
        adapter = new PictureAdapter(new ArrayList<PhotoEntity>());
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

    private int page = 2;
    private final RefreshRunnable refreshRunnable = new RefreshRunnable();

    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener()
    {
        @Override
        public void onRefresh()
        {
            page = 2;
            ids.clear();
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

    private void setNewDatas(ArrayList<PhotoEntity> entities)
    {
        mRefreshLayout.setRefreshing(false);
        if (page > 2)
        {
            adapter.loadMoreComplete();
            adapter.addData(entities);
        } else
            adapter.setNewData(entities);
    }

    private static class RefreshLoaderHandler extends Handler
    {
        private SoftReference<PictureHomeFragment> reference;

        RefreshLoaderHandler(PictureHomeFragment activity)
        {
            reference = new SoftReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 200)
            {
                ArrayList<PhotoEntity> entities = Parcels.unwrap(msg.getData().getParcelable("photos"));
                reference.get().setNewDatas(entities);
            } else
                reference.get().setNewDatas(new ArrayList<PhotoEntity>());
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
                ArrayList<PhotoEntity> entities = getImageList(page);
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

    private Set<String> ids = new HashSet<>();

    public ArrayList<PhotoEntity> getImageList(int page) throws IOException
    {
        final String baseUrl = "https://www.pexels.com";
        ArrayList<PhotoEntity> images = new ArrayList<>();
        Document doc = Jsoup.connect(baseUrl + "?page=" + page).get();
        Elements elements = doc.select("article[class*=photo-item photo-item--overlay]");
        for (Element element : elements)
        {
            String id = element.selectFirst("button").attr("data-photo-id");
            if (ids.contains(id))
                continue;
            ids.add(id);
            Element imageEl = element.selectFirst("img[class*=photo-item__img]");
            PhotoEntity photo = new PhotoEntity();
            photo.id = id;
            photo.title = imageEl.attr("alt");
            String srcset = imageEl.attr("srcset");
            if (srcset.contains(","))
            {
                String[] split = srcset.split(",");
                photo.thumbnail1x = split[0];
                photo.thumbnail2x = split[1];
            }
            photo.bigSrc = imageEl.attr("data-big-src");
            photo.largeSrc = imageEl.attr("data-large-src");
            photo.smallSrc = imageEl.attr("src");
            photo.pinSrc = imageEl.attr("data-pin-media");
            photo.width = Integer.parseInt(imageEl.attr("width"));
            photo.height = Integer.parseInt(imageEl.attr("height"));
            String style = imageEl.attr("style");
            String[] rgbs = style.replaceAll("[a-zA-Z():]", "").split(",");
            int[] rgb = new int[rgbs.length];
            for (int i = 0; i < rgbs.length; i++)
                rgb[i] = Integer.parseInt(rgbs[i]);
            photo.rgb = rgb;
            photo.deatilPage = baseUrl + element.selectFirst("a[class*=js-photo-link]").attr("href");
            UserEntity user = new UserEntity();
            user.avatar = element.selectFirst("img[class*=photo-item__avatar]").attr("src");
            user.author = element.selectFirst("span[class*=photo-item__name]").text();
            photo.user = user;
            images.add(photo);
        }
        return images;
    }
}
