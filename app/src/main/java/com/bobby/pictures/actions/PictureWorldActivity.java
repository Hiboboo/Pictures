package com.bobby.pictures.actions;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.bobby.pictures.R;
import com.bobby.pictures.actions.abs.BaseAppCompatActivity;
import com.bobby.pictures.actions.home.PictureHomeFragment;
import com.bobby.pictures.actions.home.PicturePopularFragment;
import com.bobby.pictures.adapter.PictureAdapter;
import com.bobby.pictures.entity.PhotoEntity;
import com.bobby.pictures.entity.UserEntity;
import com.bobby.pictures.widget.PictureRecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxItemDecoration;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.parceler.Parcels;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PictureWorldActivity extends BaseAppCompatActivity
{
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState, R.layout.activity_picture_world_layout);
    }

    @Override
    protected void setupViews()
    {
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add(R.string.tab_home, PictureHomeFragment.class)
                        .add(R.string.tab_popular, PicturePopularFragment.class)
                        .create()
        );
        ViewPager mViewPager = this.findViewById(R.id.picture_views_container);
        mViewPager.setAdapter(adapter);

        SmartTabLayout mTab = this.findViewById(R.id.tabs);
        mTab.setViewPager(mViewPager);
    }
}
