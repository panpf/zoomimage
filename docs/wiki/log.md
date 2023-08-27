## Log/日志

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

zoomimage 在运行的过程中会产生一些日志，这些日志可以在出现问题时帮你查找问题所在，也可以帮你理解
zoomimage 的运行机制。

### Logger

[Logger] 类封装了日志的打印、级别控制与输出管道

#### level

[Logger].level 属性来控制日志的打印级别，默认是 INFO，你可以修改它来扩大日志的输出范围，如下：

```kotlin
val state: ZoomState by rememberZoomState()

state.logger.level = Logger.DEBUG

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

#### pipeline

[Logger].pipeline 属性来控制日志的输出管道，默认是 AndroidLogPipeline 表示输出到 Android
的控制台，你可以修改它来将日志的输出到别的地方，如下：

```kotlin
val state: ZoomState by rememberZoomState()

state.logger.pipeline = MyLoggerPipeline()

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/ZoomState.kt

[Logger]: ../../zoomimage-core/src/main/java/com/github/panpf/zoomimage/Logger.kt