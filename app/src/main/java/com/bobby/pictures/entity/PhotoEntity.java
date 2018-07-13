package com.bobby.pictures.entity;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 照片基本属性
 * <p>
 * Created by Bobby on 2018/07/12.
 */
@Parcel
public class PhotoEntity
{
    public String id;
    public String title;
    public String thumbnail1x;
    public String thumbnail2x;
    public String bigSrc;
    public String largeSrc;
    public String pinSrc;
    public String smallSrc;
    public int width;
    public int height;
    public int[] rgb;

    public UserEntity user;

    public String deatilPage;

    public ArrayList<PhotoEntity> mSimilarPhotos;

    @Override
    public String toString()
    {
        return "id=" + id + "｜title=" + title + "｜thumbnail1x=" + thumbnail1x + "｜thumbnail2x=" + thumbnail2x +
                "｜largeSrc=" + largeSrc + "｜bigSrc=" + bigSrc + "｜pinSrc=" + pinSrc +
                "｜smallSrc=" + smallSrc + "｜detailPage=" + deatilPage +
                "｜width=" + width + "｜height=" + height + "｜rgb=" + Arrays.toString(rgb) +
                "｜user=" + user.toString();
    }
}
