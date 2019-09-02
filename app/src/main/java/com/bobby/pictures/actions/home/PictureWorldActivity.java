package com.bobby.pictures.actions.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.bobby.pictures.R;
import com.bobby.pictures.actions.SettingActivity;
import com.bobby.pictures.actions.abs.ImmerseAppCompatActivity;
import com.bobby.pictures.util.AsynchronousManager;
import com.bobby.pictures.util.DownloadManager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class PictureWorldActivity extends ImmerseAppCompatActivity
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        AsynchronousManager.getInstance().clear();
        DownloadManager.getInstance().cancelAllTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
