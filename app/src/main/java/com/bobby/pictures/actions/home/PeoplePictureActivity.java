package com.bobby.pictures.actions.home;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.bobby.pictures.R;
import com.bobby.pictures.actions.abs.ImmerseAppCompatActivity;
import com.bobby.pictures.app.App;
import com.bobby.pictures.entity.UserEntity;
import com.bobby.pictures.util.ParseText;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 某个人的专属相册
 * <p>
 * Created by Bobby on 2018/07/17.
 */
public class PeoplePictureActivity extends ImmerseAppCompatActivity
{
    private CircleImageView mPortrait;
    private TextView mNickname;
    private TextView mTotalViews;
    private TextView mHistoryRank;
    private TextView m30DayRank;

    private Bundle data = new Bundle();

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState, R.layout.activity_people_picture_layout);
        this.onLoadingDatas();
    }

    @Override
    protected void setupViews()
    {
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add(R.string.tab_photo, PeoplePhotoFragment.class, data)
                        .add(R.string.tab_collect, PeopleCollectFragment.class, data)
                        .add(R.string.tab_stats, PeopleStatsFragment.class, data)
                        .create()
        );
        ViewPager mViewPager = this.findViewById(R.id.picture_views_container);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(3);

        SmartTabLayout mTab = this.findViewById(R.id.tabs);
        mTab.setViewPager(mViewPager);

        mPortrait = this.findViewById(R.id.image_user_portrait);
        mNickname = this.findViewById(R.id.text_user_nickname);
        mTotalViews = this.findViewById(R.id.text_total_views);
        mHistoryRank = this.findViewById(R.id.text_history_rank);
        m30DayRank = this.findViewById(R.id.text_30day_rank);
    }

    @Override
    protected void onLoadingDatas()
    {
        new Thread(new PeopleRunnable()).start();
    }

    private void setProplePictureDatas(UserEntity entity)
    {
        if (null == entity)
        {
            this.onUpdateStateChangeLayout(PageLoadState.FAILURE);
            return;
        }
        this.onUpdateStateChangeLayout(PageLoadState.SUCCESSFUL);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_occupy_large_portrait);
        Glide.with(this)
                .load(entity.avatar)
                .apply(new RequestOptions()
                        .circleCrop()
                        .override(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()))
                .into(mPortrait);
        mNickname.setText(entity.author);
        Resources r = this.getResources();
        String totalText = r.getString(R.string.format_label_total_views, entity.totalViews);
        int end = entity.totalViews.length();
        int sp12Size = r.getDimensionPixelSize(R.dimen.sp_12);
        mTotalViews.setText(ParseText.arrangeContentStyle(totalText, 0, end, Color.BLACK, sp12Size, Typeface.BOLD));
        String historyText = r.getString(R.string.format_label_history_rank, entity.historyRank);
        end = entity.historyRank.length();
        mHistoryRank.setText(ParseText.arrangeContentStyle(historyText, 0, end, Color.BLACK, sp12Size, Typeface.BOLD));
        String m30rank = r.getString(R.string.format_label_30_rank, entity.day30Rank);
        end = entity.day30Rank.length();
        m30DayRank.setText(ParseText.arrangeContentStyle(m30rank, 0, end, Color.BLACK, sp12Size, Typeface.BOLD));
    }

    private static class ShowLoaderHandler extends Handler
    {
        private SoftReference<PeoplePictureActivity> reference;

        ShowLoaderHandler(PeoplePictureActivity activity)
        {
            reference = new SoftReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 200)
            {
                UserEntity entity = Parcels.unwrap(msg.getData().getParcelable("user.photos"));
                reference.get().setProplePictureDatas(entity);
            } else
                reference.get().setProplePictureDatas(null);
        }
    }

    private final ShowLoaderHandler mHandler = new ShowLoaderHandler(this);

    private final class PeopleRunnable implements Runnable
    {
        @Override
        public void run()
        {
            Message message = mHandler.obtainMessage();
            try
            {
                UserEntity entity = onParseProplePictures(getIntent().getStringExtra(App.Key.KEY_EXTRA_DATA));
                message.getData().putParcelable("user.photos", Parcels.wrap(entity));
                message.what = 200;
            } catch (IOException e)
            {
                e.printStackTrace();
                message.what = 500;
            }
            mHandler.sendMessage(message);
        }
    }

    private UserEntity onParseProplePictures(String url) throws IOException
    {
        final String baseUrl = "https://www.pexels.com" + url;
        Document doc = Jsoup.connect(baseUrl).get();
        UserEntity entity = new UserEntity();
        Element element = doc.selectFirst("div[class*=profile-header__user-info__avatar__container]");
        entity.author = element.selectFirst("img").attr("alt");
        entity.avatar = element.selectFirst("img").attr("src");
        Elements statsElements = doc.select("span[class=profile-header__fact]");
        for (Element linkElement : statsElements)
        {
            String html = linkElement.html();
            String value = linkElement.selectFirst("strong").text();
            if (html.contains("Total"))
                entity.totalViews = value;
            if (html.contains("All-time"))
                entity.historyRank = value;
            if (html.contains("30"))
                entity.day30Rank = value;
        }
        Elements pageElement = doc.selectFirst("div[class*=rd__tabs]").select("a[class*=rd__tabs__tab]");
        for (Element pElement : pageElement)
        {
            String text = pElement.text();
            String linkUrl = pElement.attr("href");
            if (text.contains("Photo"))
                data.putString("page.photo", linkUrl);
            if (text.contains("Collections"))
                data.putString("page.collect", linkUrl);
            if (text.contains("Stats"))
                data.putString("page.stats", linkUrl);
        }
        return entity;
    }

    @Override
    protected boolean isNeedPackLayout()
    {
        return true;
    }
}
