## 阅读模式

翻译：[English](readmode.md)

> [!TIP]
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

对于文字类长图片，他们的高度通常非常大，如果初始状态显示全貌，那么图片里的文字内容什么也看不清楚，用户必须双击一下放大再滑动到开始位置才能开始阅读

针对这样的图片 ZoomImage 提供了阅读模式让图片初始状态时就充满屏幕，并定位到开始位置，
类似 [ContentScale].Crop 加 [Alignment].TopStart 的组合，这样用户就能直接开始阅读文字长图的内容了

### 开启阅读模式

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.zommable) {
    zoomState.zoomable.readMode = ReadMode.Default
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### ReadMode

[ReadMode] 类用来控制阅读模式，它有两个参数：

* `sizeType: Int = ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL`:
  用来控制哪种尺寸类型的图片可以使用阅读模式，默认是横图和竖图都可以，取值为 `ReadMode.SIZE_TYPE_HORIZONTA`
  或 `ReadMode.SIZE_TYPE_VERTICAL`
* `decider: ReadMode.Decider = ReadMode.Decider.Default`: decider 根据 contentSize 和 containerSize
  来判断是否可以使用阅读模式，默认实现是 ReadMode.LongImageDecider，仅对长图使用阅读模式

> [!TIP]
> * ReadMode 的默认配置是 ReadMode.Default
> * 你可以实现 ReadMode.Decider 接口实现你自己的判定规则

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ReadMode]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ReadMode.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment