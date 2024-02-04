# 更新日志

翻译：[English](CHANGELOG.md)

## v1.0.2-SNAPSHOT

* fix: 修复 ZoomImageView 在 API 30 及以下版本因 TypedArray.close() 方法崩溃的
  bug. [#15](https://github.com/panpf/zoomimage/issues/15)
* fix: 修复 GlideZoomAsyncImage 和 GlideZoomImageView 不支持 'file:///android_asset/' 和 'file:
  ///sdcard/sample.jpeg' 类型 model 的 bug. [#16](https://github.com/panpf/zoomimage/issues/16)
* improve: 改进对 Picasso 的支持
* improve: 改进 ScalesCalculator.dynamic()，现在阅读模式可用时 mediumScale 始终是阅读模式的初始缩放倍数

## v1.0.1

* fix: 修复 ZoomImageView 在单指拖动后不松手再按下一根手指执行双指缩放手势时图像会跳动的
  bug [#12](https://github.com/panpf/zoomimage/issues/12)

## 1.0.0

初始稳定版发行

## v1.0.0-rc01

zoom:

* improve: 改进 ZoomableState、ZoomableEngine、SubsamplingState 和
  SubsamplingEngine，现在它们都是在被记住或附到窗口时才会开始工作

other:

* depend: 升级 sketch 3.3.0 stable

## v1.0.0-beta11

zoom:

* change: ZoomableEngine 的 contentSizeState 属性现在为空时会使用 containerSizeState

other:

* depend: 升级 sketch 3.3.0-beta06
* change: 重命名 `zoomimage-compose-coil-base` 模块为 `zoomimage-compose-coil-core`
* improve: GlideZoomAsyncImage 和 GlideZoomImageView 现在支持 GlideUrl

## 1.0.0-beta10

* fix: 修复 ZoomImage 无法显示滚动条的 bug
* fix: 修复 SketchZoomAsyncImage 和 CoilZoomAsyncImage 在遇到没有大小的 Drawable 时会崩溃的 bug
* fix: 修复 SketchZoomAsyncImage 和 CoilZoomAsyncImage 在 placeholder 和 result 过渡时 placeholder
  会被意外的缩小的 bug
* improve: composed 迁移到 Modifier.Node
* depend: 升级 Sketch 3.3.0-beta04

## 1.0.0-beta09

* fix: 修复在 Android 解码不支持的图片格式时因为 BitmapFactory.Options.outMimeType 为 null 导致的崩溃问题
* change: `zoomimage-core` 模块 minSdk 从 21 改为 16

## 1.0.0-beta08

* fix: 修复 `zoomimage-compose-glide` 模块没有配置混淆导致崩溃的 bug
* change: 现在只有 `zoomimage-core` 模块生成 BuildConfig
* change: ZoomableEngine 的 scale(), rotate() 等方法加了 suspend 修饰符
* new: 新增 `zoomimage-compose-coil-base` 和 `zoomimage-compose-sketch-core` 模块，分别依赖了 Coil 和
  Sketch 的非单例模块
* depend: 升级 Sketch 3.3.0-beta02

## 1.0.0-beta07

zoom:

* fix: 现在单指缩放和长按回调不会再同时触发
* fix: 修复 View 版本双击事件和长按事件以及双指缩放事件会同时触发的 bug
* fix: 修复 Compose 版的组件在 Pager 中放大并且在边缘位置触发 Pager 的滑动后往回滑动会打断 Pager
  的滑动的 bug
* change: 单指缩放手势改为双击后上下拖动，并且现在默认开启
* change: OnViewTapListener 和 OnViewLongPressListener 的 x, y 参数合并成一个 OffsetCompat
* improve: 改进 calculateTiles() 函数，现在最后一格的 right 和 bottom 始终是 `width-1` 和 `height -1`
* improve: 改进手势

subsampling:

* fix: 修复桌面平台上图块之间存在间隙的 bug

## 1.0.0-beta06

zoom:

* fix: 修复 View 版本初始时能看到图像从顶部快速移到中间的过程
* fix: 修复 Compose 版本初始时能看到图像从顶部快速移到中间的过程
* fix: 修复 SketchZoomAsyncImage 和 CoilZoomAsyncImage 组件在 Pager 中快速切换时边缘部分会出现诡异的闪现图片内容的
  bug
* new: 增加 HeartbeatHapticFeedback，触发单指缩放时会有类似心跳的效果

subsampling:

* fix: 修复双击放大时，因为没有及时取消未完成的背景图块导致同一个图块会加载多次的 bug
* fix: 修复 View 版本的子采样图块因没有正确设置显示计数，导致对应位置图像显示黑色的 bug
* fix: 修复 View 版本从内存缓存中读取的图块被意外的丢弃，导致对应位置的图像模糊的 bug

other:

* change: rememberZoomImageLogger() 函数增加 level 和 pipeline 参数

## 1.0.0-beta05

zoom:

* fix: 修复了在用户操作变换又恢复到初始状态后再调整窗口大小时偶尔内容不随窗口大小变化而缩放的 bug

subsampling:

* remove: 删除 TileBitmap 的 size 属性
* improve: 子采样图块现在绘制在单独的组件上，避免了在桌面平台上因平台的 bug
  导致内容全部丢失的问题 https://github.com/JetBrains/compose-multiplatform/issues/3904
* improve: 降低由于 containerSize 变化而导致 TileManager 重置的频率
* new: 桌面平台支持二次采样
* new: 桌面平台支持 Exif Orientation

## 1.0.0-beta04

zoom:

* change: LongPressSlide 重命名为 OneFingerScale
* improve: 现在仅容器大小变化时保持内容的缩放和可见中心不变
* new: 支持禁用手势
* new: 添加了 Modifier.zoom() 函数，可以轻松为其他组件添加缩放功能

subsampling:

* change: 添加了 stopController 属性替代 setLifecycle() 方法
* change: 改进了一些 API 名称

other:

* new: 支持 Compose 多平台
* change: 移除 rememberZoomImageLogger 函数的 level 参数

## 1.0.0-beta03

zoom:

* fix: 修复了calculateUserOffsetBounds函数在缩放到全屏时可能返回错误边界，导致崩溃的错误
* fix: 修复了 GlideZoomAsyncImage 会加载大于视图的位图，从而导致崩溃的错误
* improve: 升级 GlideImage 1.0.0-beta01
* improve: NoClipImage 组件重命名为 NoClipContentImage

## 1.0.0-beta02

zoom:

fix: 修复了切换 Pager 后 ZoomImage 无法缩放的 bug
new: 支持单指缩放功能

## 1.0.0-beta01

zoom:

* fix: 修复了双击缩放时 ZoomImageView 有时没有动画的错误
* change: ZoomableEngine 的公开属性现在封装在 StateFlow 中，可以直接监听
* improve: 避免在最小缩放倍数和多指触摸时触发 Pager 的滑动

subsampling:

* fix: 修复了子采样可能失败的错误
* fix: tileGridSizeMap 属性现在返回正确的大小
* fix: 修复了 calculateTileGridMap() 函数在遇到特别小的 tileMaxSize 时引发 OutOfMemoryError 的错误
* change: SubsamplingEngine 的公开属性现在封装在 StateFlow 中，可以直接监听
* change: ImageSource.openInputStream() 方法删除 suspend 修饰符
* change: pauseWhenTransforming 改为 pausedContinuousTransformType
* improve: 改进 calculateImageLoadRect() 的结果，现在不会超过 imageSize
* improve: 图块并发加载从 4 个减少到 2 个，以减少内存占用并提高 UI 性能
* new: SubsamplingState 和 SubsamplingEngine 添加 tileGridSizeMap 属性
* new: 现在切换采样大小时，清晰度会逐渐变化，不再总是从底图过渡

other:

* fix: 修复 level 判断错误导致日志不打印的 bug
* build: 升级 compileSdk 到 34，kotlinx-coroutines 到 1.7.3，compose 到 1.5.0

## 1.0.0-alpha03

zoom:

* fix: 修复了使用两根手指缩放时抬起一根手指后 ZoomImageView 会明显平移图像的错误
* fix: 修复 rotate 方法遇到负旋转角度异常的 bug
* fix: 修复了动画时间为 0 时可能崩溃的错误
* change: ScalesCalculator 现在与 ReadMode 兼容
* change: onViewTapListener 和 onViewLongPressListener 替换 registerOnViewTapListener 和
  registerOnViewLongPressListener
* change: ZoomImageView 的 zoomAbility 属性重命名为 zoomable，subsamplingAbility 重命名为 subsampling
* improve: 兼容机型 MIX4、ROM版 本 14.0.6.0、Android 版本 13 环境下启用导航栏但 App 不主动适配导航栏，屏幕解锁后因
  View 尺寸变化而触发重置的问题
* improve: ZoomImageView 现在支持 scaleType 是 MATRIX
* new: ZoomImageView 添加 xml 属性支持

other:

* change: ZoomableState、SubsamplingState、ZoomableEngine 和 SubsamplingEngine 的 logger 参数改为私有

## 1.0.0-alpha02

zoom:

* change: ZoomableState 和 ZoomAbility 的 location() 方法重命名为 locate()
* new: ZoomImageView 添加 OnResetListener

subsampling:

* change: SubsamplingAbility 的 registerOnTileChangedListener()、unregisterOnTileChangedListener()
  方法重命名为 registerOnTileChangeListener()、unregisterOnTileChangeListener()
* change: 子采样的暂停属性重命名为停止
* new: 二次采样的图块支持动画
* new: 二次采样添加了 pauseWhenTransforming 属性

## 1.0.0-alpha01

初始发行
