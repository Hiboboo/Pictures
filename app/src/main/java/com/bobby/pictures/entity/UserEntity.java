package com.bobby.pictures.entity;

import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * 用户信息
 * <p>
 * Created by Bobby on 2018/07/12.
 */
@Parcel
public class UserEntity
{
    public String userid;
    public String author;
    public String avatar;
    public String userPage;
    public String userPageTitle;
    public String totalViews;
    public String historyRank;
    public String day30Rank;

    public ArrayList<PhotoEntity> pictures;

    @Override
    public String toString()
    {
        return "userid=" + userid + "｜author=" + author + "｜avatar=" + avatar
                + "｜userpage=" + userPage + "｜upagetitle=" + userPageTitle;
    }
}
