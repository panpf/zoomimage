## Log/日志

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

ZoomImage generates some logs during its run, which can help you find the problem when something
goes wrong and help you understand it
How ZoomImage works.
<br>-----------</br>
ZoomImage 在运行的过程中会产生一些日志，这些日志可以在出现问题时帮你查找问题所在，也可以帮你理解
ZoomImage 的运行机制。

### Logger

The [Logger] class encapsulates the print, level control, and output pipelines of logs
<br>-----------</br>
[Logger] 类封装了日志的打印、级别控制与输出管道

#### level

The [Logger].level property controls the print level of the log, the default is INFO, you can modify
it to expand the output range of the log
<br>-----------</br>
[Logger].level 属性用来控制日志的打印级别，默认是 INFO，你可以修改它来扩大日志的输出范围

example/示例：

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

The [Logger].pipeline property controls the output pipeline of the log, and the default is
AndroidLogPipeline to output to Android
console, you can modify it to output logs elsewhere
<br>-----------</br>
[Logger].pipeline 属性用来控制日志的输出管道，默认是 AndroidLogPipeline 表示输出到 Android
的控制台，你可以修改它来将日志输出到磁盘等别的地方

example/示例：

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