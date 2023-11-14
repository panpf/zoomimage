# 更新日志

翻译：[English](CHANGELOG.md)

## 1.0.0-beta07-SNAPSHOT

* improve: 改进 calculateTiles() 函数，现在最后一格的 right 和 bottom 始终是 `width-1` 和 `height -1`

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