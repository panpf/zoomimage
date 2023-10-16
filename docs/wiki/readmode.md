## Read Mode/阅读模式

> * The following example takes precedence over the Compose version component for demonstration
> * The API of [ZoomImageView] is exactly the same as [ZoomImage], except that the entrance is
    different
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomImageView] 的 API 和 [ZoomImage] 一模一样，只是入口不一样
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

For long text images, their height is usually very large, if the initial state shows the full image,
then the text content in the image is not clear, the user must double-click to zoom in and slide to
the start position to start reading
<br>-----------</br>
对于文字类长图片，他们的高度通常非常大，如果初始状态显示全貌，那么图片里的文字内容什么也看不清楚，用户必须双击一下放大再滑动到开始位置才能开始阅读

For such images, ZoomImage provides a reading mode to fill the screen at the initial state of the
image and locate it to the beginning position, similar to [ContentScale]
. Crop plus [alignment]. combination of TopStart, so that users can directly start reading the
content of the long text image
<br>-----------</br>
针对这样的图片 ZoomImage 提供了阅读模式让图片初始状态时就充满屏幕，并定位到开始位置，
类似 [ContentScale].Crop 加 [Alignment] .TopStart 的组合，这样用户就能直接开始阅读文字长图的内容了

### Enabled Read Mode/开启阅读模式

```kotlin
val state: ZoomState by rememberZoomState()

state.zoomable.readmode = ReadMode.Default

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### ReadMode Class/ReadMode 类

The [ReadMode] class is used to control the read mode, and it has two parameters:
<br>-----------</br>
[ReadMode] 类用来控制阅读模式，它有两个参数：

* sizeType: Int = `ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL`。
    * The reading mode can be used to control which size type of image, the default is both
      horizontal and vertical charts, and the value is  `ReadMode.SIZE_TYPE_HORIZONTA`
      or `ReadMode.SIZE_TYPE_VERTICAL`
      <br>-----------</br>
    * 用来控制哪种尺寸类型的图片可以使用阅读模式，默认是横图和竖图都可以，取值为 `ReadMode.SIZE_TYPE_HORIZONTA`
    或 `ReadMode.SIZE_TYPE_VERTICAL`
* decider: ReadMode.Decider = ReadMode.Decider.Default。
    * decider determines whether read mode can be used based on contentSize and containerSize, and
      the default implementation is ReadMode.LongImageDecider, which uses read mode only for long
      images
      <br>-----------</br>
    * decider 根据 contentSize 和 containerSize 来判断是否可以使用阅读模式，默认实现是
      ReadMode.LongImageDecider，仅对长图使用阅读模式

> * The default configuration for ReadMode is ReadMode.Default
> * You can implement the ReadMode.Decider interface to implement your own decision rules
    <br>-----------</br>
> * ReadMode 的默认配置是 ReadMode.Default
> * 你可以实现 ReadMode.Decider 接口实现你自己的判定规则

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ReadMode]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ReadMode.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment