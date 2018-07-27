package com.bobby.pictures.util;

import android.text.TextUtils;
import android.util.SparseArray;

import com.bobby.pictures.entity.PhotoEntity;
import com.bobby.pictures.entity.PopularEntity;
import com.bobby.pictures.entity.UserEntity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 定义不同的数据请求API操作方法，并完成请求操作
 * <p>
 * Created by Bobby on 2018/07/18.
 */
public final class ExecuteApi
{
    public enum Apis
    {
        HOME_LIST(0xff15),
        POPULAR_LIST(0xff16),
        SHOW_IMG(0xff17),
        PEOPLE_IMAGES(0xff18),
        POPULAR_IMAGES(0xff19);

        int id;

        Apis(int id)
        {
            this.id = id;
        }
    }

    public static class Params
    {
        int page;
        String url;

        public static class Builder
        {
            private int page;
            private String url;

            public Builder addPage(int page)
            {
                this.page = page;
                return this;
            }

            public Builder addUrl(String url)
            {
                this.url = url;
                return this;
            }

            public Params build()
            {
                Params params = new Params();
                params.page = page;
                params.url = url;
                return params;
            }
        }
    }

    private final SparseArray<List<String>> apivalues = new SparseArray<>();

    public void get(Apis api, Params params, AsynchronousManager.OnAsynchronousCallback callback) throws IOException
    {
        if (null == apivalues.get(api.id))
            apivalues.put(api.id, new ArrayList<String>());
        switch (api)
        {
            case HOME_LIST:
                callback.onSuccessful(getHomeImages(params.page));
                break;
            case POPULAR_LIST:
                callback.onSuccessful(getPopluarList(params.page));
                break;
            case SHOW_IMG:
                callback.onSuccessful(getImage(params.url));
                break;
            case PEOPLE_IMAGES:
                callback.onSuccessful(getPeopleImages(params.page, params.url));
                break;
            case POPULAR_IMAGES:
                callback.onSuccessful(getPopularImages(params.page, params.url));
                break;
        }
    }

    /**
     * 清空所有API请求接口获取到的临时数据
     */
    protected void clear()
    {
        apivalues.clear();
    }

    /**
     * 清空某一个API接口获取到的临时数据
     *
     * @param api 指定具体的API
     */
    protected void refreshApiValues(Apis api)
    {
        if (apivalues.get(api.id) != null)
            apivalues.get(api.id).clear();
    }

    private final String baseUrl = "https://www.pexels.com";

    private ArrayList<PhotoEntity> getHomeImages(int page) throws IOException
    {
        ArrayList<PhotoEntity> images = new ArrayList<>();
        Document doc = Jsoup.connect(baseUrl + "?page=" + page).get();
        Elements elements = doc.select("article[class*=photo-item photo-item--overlay]");
        for (Element element : elements)
        {
            String id = element.selectFirst("button").attr("data-photo-id");
            if (apivalues.get(Apis.HOME_LIST.id).contains(id))
                continue;
            apivalues.get(Apis.HOME_LIST.id).add(id);
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
            String widthText = imageEl.attr("width").trim();
            if (!widthText.matches("\\d+"))
                widthText = "0";
            photo.width = Integer.parseInt(widthText);
            String heightText = imageEl.attr("height").trim();
            if (!heightText.matches("\\d+"))
                heightText = "0";
            photo.height = Integer.parseInt(heightText);
            String style = imageEl.attr("style");
            String[] rgbs = style.replaceAll("[a-zA-Z():]", "").split(",");
            int[] rgb = new int[rgbs.length];
            for (int i = 0; i < rgbs.length; i++)
            {
                String text = rgbs[i].trim();
                if (!text.matches("\\d{1,3}"))
                    text = "0";
                rgb[i] = Integer.parseInt(text);
            }
            photo.rgb = rgb;
            photo.deatilPage = element.selectFirst("a[class*=js-photo-link]").attr("href");
            UserEntity user = new UserEntity();
            user.avatar = element.selectFirst("img[class*=photo-item__avatar]").attr("src");
            user.author = element.selectFirst("span[class*=photo-item__name]").text();
            photo.user = user;
            images.add(photo);
        }
        return images;
    }

    private ArrayList<PopularEntity> getPopluarList(int page) throws IOException
    {
        ArrayList<PopularEntity> entities = new ArrayList<>();
        Document doc = Jsoup.connect(baseUrl + "/popular-searches?page=" + page).get();
        Elements elements = doc.select("div[class*=l-lg-3 l-md-4 l-sm-6 search-medium]");
        for (Element element : elements)
        {
            Element linkElement = element.selectFirst("a[class*=search-medium__link]");
            if (null == linkElement)
                continue;
            Element imageElement = element.selectFirst("img[class*=search-medium__image]");
            String n = imageElement.attr("alt");
            if (apivalues.get(Apis.POPULAR_LIST.id).contains(n))
                continue;
            apivalues.get(Apis.POPULAR_LIST.id).add(n);

            PopularEntity entity = new PopularEntity();
            entity.pageUrl = linkElement.attr("href");

            if (TextUtils.isEmpty(n))
                n = " ";
            if (n.toLowerCase().equals("iphone"))
                entity.name = "iPhone";
            else
                entity.name = n.substring(0, 1).toUpperCase().concat(n.substring(1, n.length()));
            entity.thumbnail = imageElement.attr("src");
            entities.add(entity);
        }
        return entities;
    }

    private PhotoEntity getImage(String url) throws IOException
    {
        Document doc = Jsoup.connect(baseUrl + url).get();

        PhotoEntity image = new PhotoEntity();
        Element picElementByImg = doc.selectFirst("picture[class*=image-section__picture]").selectFirst("img");
        this.parseImageTags(image, picElementByImg);

        image.bigSrc = picElementByImg.attr("data-zoom-src");
        image.downloadUrl = doc.selectFirst("a[download]").attr("href");
        Element divActionElement = doc.selectFirst("div[class*=box image-section__actions]");
        image.id = divActionElement.selectFirst("button").attr("data-photo-id");

        Element profileboxEl = doc.selectFirst("div[class*=mini-profile box]");
        Element imgElement = profileboxEl.selectFirst("img[class*=mini-profile__img]");
        UserEntity user = new UserEntity();
        user.author = imgElement.attr("alt");
        user.avatar = imgElement.attr("src");
        user.userid = profileboxEl.selectFirst("button").attr("data-user-id");
        Element alinkEl = profileboxEl.selectFirst("a[class*=mini-profile__link]");
        if (alinkEl != null)
        {
            user.userPage = alinkEl.attr("href");
            user.userPageTitle = alinkEl.text();
        }
        image.user = user;
        ArrayList<PhotoEntity> similarPhotos = new ArrayList<>();
        Elements photoElements = doc.select("article[class*=photo-item]");
        for (Element photoEl : photoElements)
        {
            PhotoEntity childImage = new PhotoEntity();
            childImage.id = photoEl.selectFirst("button").attr("data-photo-id");
            Element pageEl = photoEl.selectFirst("a[class*=js-photo-link]");
            childImage.deatilPage = pageEl.attr("href");
            this.parseImageTags(childImage, photoEl.selectFirst("img[class*=photo-item__img]"));
            childImage.downloadUrl = photoEl.selectFirst("a[download]").attr("href");
            similarPhotos.add(childImage);
        }
        image.mSimilarPhotos = similarPhotos;
        return image;
    }

    private void parseImageTags(PhotoEntity entity, Element imgElement)
    {
        String childSrcset = imgElement.attr("srcset");
        if (childSrcset.contains(","))
        {
            String[] split = childSrcset.split(",");
            entity.thumbnail1x = split[0];
            entity.thumbnail2x = split[1];
        }
        entity.title = imgElement.attr("alt");
        entity.bigSrc = imgElement.attr("data-big-src");
        entity.largeSrc = imgElement.attr("data-large-src");
        entity.smallSrc = imgElement.attr("src");
        entity.pinSrc = imgElement.attr("data-pin-media");
        String widthText = imgElement.attr("width");
        if (!widthText.matches("\\d+"))
            widthText = "0";
        entity.width = Integer.parseInt(widthText);
        String heightText = imgElement.attr("height");
        if (!heightText.matches("\\d+"))
            heightText = "0";
        entity.height = Integer.parseInt(heightText);
        String style = imgElement.attr("style");
        String[] rgbs = style.replaceAll("[a-zA-Z():]", "").split(",");
        int[] rgb = new int[rgbs.length];
        for (int i = 0; i < rgbs.length; i++)
        {
            String text = rgbs[i].trim();
            if (!text.matches("\\d{1,3}"))
                text = "0";
            rgb[i] = Integer.parseInt(text);
        }
        entity.rgb = rgb;
    }

    public ArrayList<PhotoEntity> getPeopleImages(int page, String photoUrl) throws IOException
    {
        ArrayList<PhotoEntity> images = new ArrayList<>();
        final String requestUrl = baseUrl + photoUrl + "?page=" + page;
        Document doc = Jsoup.connect(requestUrl).get();
        Elements elements = doc.select("article[class*=photo-item]");
        for (Element element : elements)
        {
            PhotoEntity entity = new PhotoEntity();
            Element linkElement = element.selectFirst("a[class*=js-photo-link]");
            entity.deatilPage = linkElement.attr("href");
            entity.title = linkElement.attr("title");
            Element imgElement = element.selectFirst("img[class*=photo-item__img]");
            String srcset = imgElement.attr("srcset");
            if (srcset.contains(","))
            {
                String[] split = srcset.split(",");
                entity.thumbnail1x = split[0];
                entity.thumbnail2x = split[1];
            }
            entity.bigSrc = imgElement.attr("data-big-src");
            entity.largeSrc = imgElement.attr("data-large-src");
            entity.smallSrc = imgElement.attr("src");
            entity.pinSrc = imgElement.attr("data-pin-media");
            String widthText = imgElement.attr("width").trim();
            if (!widthText.matches("\\d+"))
                widthText = "0";
            entity.width = Integer.parseInt(widthText);
            String heightText = imgElement.attr("height").trim();
            if (!heightText.matches("\\d+"))
                heightText = "0";
            entity.height = Integer.parseInt(heightText);
            String style = imgElement.attr("style");
            String[] rgbs = style.replaceAll("[a-zA-Z():]", "").split(",");
            int[] rgb = new int[rgbs.length];
            for (int i = 0; i < rgbs.length; i++)
            {
                String text = rgbs[i].trim();
                if (!text.matches("\\d{1,3}"))
                    text = "0";
                rgb[i] = Integer.parseInt(text);
            }
            entity.rgb = rgb;
            images.add(entity);
        }
        return images;
    }

    public ArrayList<PhotoEntity> getPopularImages(int page, String photoUrl) throws IOException
    {
        return getPeopleImages(page, photoUrl);
    }
}
