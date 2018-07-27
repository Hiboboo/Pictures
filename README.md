## 这是一个网络图片的浏览器
> 这是一个利用`Jsoup`技术对[Pexels](https://www.pexels.com/)网站进行图片扒取后，又以Android应用程序的方式展现出来的一个App。

- **图片浏览**：采用瀑布流的形式展现所有能抓取到的照片，支持查看单张照片，支持查看相似照片
- **照片下载**：在查看单张照片时，支持直接点击下载到本地（以原图无压缩的形式），没有任何水印哦，绝对的高清无码
- **自动更换壁纸**：支持开启每隔一段时间自动更换桌面壁纸，具体看下边的详细介绍

#### 重点介绍一下自动更换壁纸的功能
先看截图吧

从截图中可以看到这里有几个关键性的设置

- **壁纸关键字**：
意思是说你希望都会有哪些类别的照片范围可以被设置为桌面壁纸。
比如几个关键字：girl;car;model;nature
表示希望可以自动搜索到包含有姑娘，汽车，模特以及自然风景的相关照片。
当程序自动搜索到这些照片后，会每次随机性的取其中一张作为你的新桌面壁纸。

- **更新间隔时间**：
这个我觉得不需要过多解释，就是希望程序每隔多长时间去自动更新一次壁纸。
实际上，如果你是Android系统版本在7.0以下（不好含7.0）,设置的时间甚至可以低于15分钟，至于为什么这里要强调15分钟，
懂的人自然就懂了，不懂的没关系，毕竟这并不重要。

最后要说一点的是，一般情况下，还是建议根据屏幕分辨率来下载壁纸，因为有很多原图的尺寸和体积都是非常大的。


## 最最最重要的
当然是要发表一下感谢了。
- Pexels：这个网站的数据几乎全都是免费的，允许免费下载，免费将照片应用于商业用途等等，很伟大的，赞！！
- 各个开源项目，我列举一下吧，都是非常棒的。
    - [沉浸式状态栏支持](https://github.com/gyf-dev/ImmersionBar)
    - [RecyclerView 的强大支持库](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)
    - [瀑布流布局管理器](https://github.com/google/flexbox-layout)
    - [Smart TableLayout](https://github.com/ogaclejapan/SmartTabLayout)
    - [实体类注解器](https://github.com/johncarl81/parceler)
    - [运行时权限](https://github.com/permissions-dispatcher/PermissionsDispatcher)
    - [Glide](https://github.com/bumptech/glide)
    - [个性化图片](https://github.com/SheHuan/NiceImageView)
    - [图片缩放](https://github.com/alexvasilkov/GestureViews)
    - [圆形图片](https://github.com/hdodenhof/CircleImageView)
    - [网络请求库](https://github.com/jeasonlzy/okhttp-OkGo)