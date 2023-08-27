## Read Mode/阅读模式

> * The following example takes precedence over the Compose version component for demonstration
> * The API of [ZoomImageView] is exactly the same as [ZoomImage], except that the entrance is
    different
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomAbility
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsamplingAbility
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomImageView] 的 API 和 [ZoomImage] 一模一样，只是入口不一样
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomAbility
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsamplingAbility

对于文字类长图片，他们的高度通常非常大，如果初始状态显示全貌，那么图片里的文字内容什么也看不清楚，用户必须双击一下放大才能开始阅读

针对这样的图片 zoomimage 提供了阅读模式让其初始时就充满屏幕，并移动到开头位置，类似 [ContentScale].Crop
加 [Alignment].TopStart 的组合，这样用户就能直接开始阅读文字长图的内容了

### 开启阅读模式

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

### ReadMode 类

[ReadMode] 类用来控制阅读模式，它有两个参数：

* sizeType: Int = `ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL`
  。用来控制哪种尺寸类型的图片可以使用阅读模式，默认是横图和竖图都可以，取值为 `ReadMode.SIZE_TYPE_HORIZONTA`
  和 `ReadMode.SIZE_TYPE_VERTICAL`
* decider: ReadMode.Decider = ReadMode.Decider.Default。decider 根据 contentSize 和 containerSize
  来判断是否可以使用阅读模式，默认实现是 ReadMode.LongImageDecider，仅对长图使用阅读模式

> * ReadMode 的默认配置是 ReadMode.Default
> * 你可以实现 ReadMode.Decider 接口实现你自己的判定规则

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/ZoomState.kt

[ReadMode]: ../../zoomimage-core/src/main/java/com/github/panpf/zoomimage/ReadMode.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment