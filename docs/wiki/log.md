## Log

Translations: [简体中文](log_zh.md)

> [!TIP]
> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

ZoomImage generates some logs during its run, which can help you find the problem when something
goes wrong and help you understand it
How ZoomImage works.

### Logger

The [Logger] class encapsulates the print, level control, and output pipelines of logs

#### level

The [Logger].level property controls the print level of the log, the default is INFO, you can modify
it to expand the output range of the log

example：

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

The [Logger].pipeline property controls the output pipeline of the log, and the default is
AndroidLogPipeline to output to Android
console, you can modify it to output logs elsewhere

example：

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