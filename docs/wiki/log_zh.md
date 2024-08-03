## 日志

翻译：[English](log.md)

> [!TIP]
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

ZoomImage 在运行的过程中会产生一些日志，这些日志可以在出现问题时帮你查找问题所在，也可以帮你理解
ZoomImage 的运行机制。

### Logger

[Logger] 类封装了日志的打印、级别控制与输出管道

#### level

[Logger].level 属性用来控制日志的打印级别，默认是 INFO，你可以修改它来扩大日志的输出范围

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.logger) {
    zoomState.logger.level = Logger.DEBUG
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

#### pipeline

[Logger].pipeline 属性用来控制日志的输出管道，默认是 AndroidLogPipeline 表示输出到 Android
的控制台，你可以修改它来将日志输出到磁盘等别的地方

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

zoomState.logger.pipeline = MyLoggerPipeline()

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[Logger]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/util/Logger.common.kt