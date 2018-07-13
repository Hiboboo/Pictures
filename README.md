##这是一个图片浏览器
'''
private final String baseUrl = "https://www.pexels.com";

    public Image showImage(String url) throws IOException
    {
        Document doc = Jsoup.connect(url).get();
        Element profileboxEl = doc.selectFirst("div[class*=mini-profile box]");
        Element imgElement = profileboxEl.selectFirst("img[class*=mini-profile__img]");
        Image image = new Image();
        image.author = imgElement.attr("alt");
        image.avatar = imgElement.attr("src");
        image.userid = profileboxEl.selectFirst("button").attr("data-user-id");
        Element alinkEl = profileboxEl.selectFirst("a[class*=mini-profile__link]");
        image.userPage = baseUrl + alinkEl.attr("href");
        image.userPageTitle = alinkEl.text();
        List<Image> similarPhotos = new ArrayList<>();
        Elements photoElements = doc.select("article[class*=photo-item]");
        for (Element photoEl : photoElements)
        {
            Element imageEl = photoEl.selectFirst("img[class*=photo-item__img]");
            Image childImage = new Image();
            childImage.id = photoEl.selectFirst("button").attr("data-photo-id");
            Element pageEl = photoEl.selectFirst("a[class*=js-photo-link]");
            childImage.title = pageEl.attr("title");
            childImage.deatilPage = baseUrl + pageEl.attr("href");
            String srcset = imageEl.attr("srcset");
            if (srcset.contains(","))
            {
                String[] split = srcset.split(",");
                childImage.thumbnail1x = split[0];
                childImage.thumbnail2x = split[1];
            }
            childImage.bigSrc = imageEl.attr("data-big-src");
            childImage.largeSrc = imageEl.attr("data-large-src");
            childImage.smallSrc = imageEl.attr("src");
            childImage.pinSrc = imageEl.attr("data-pin-media");
            similarPhotos.add(childImage);
        }
        image.mSimilarPhotos = similarPhotos;
        return image;
    }
'''