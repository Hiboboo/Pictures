package com.bobby.pictures.entity;

import org.parceler.Parcel;

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

    @Override
    public String toString()
    {
        return "userid=" + userid + "｜author=" + author + "｜avatar=" + avatar
                + "｜userpage=" + userPage + "｜upagetitle=" + userPageTitle;
    }
}
