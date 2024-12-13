# 更新日志

翻译：[English](CHANGELOG.md)

## 1.1.0 Stable

> [!CAUTION]
> 为实现支持 js 和 iOS 平台，所有 `remove` 和 `change` 标签的变更都是破坏性的

zoom:

* fix: 修复 ZoomableEngine 的 contentSizeState 属性 collect 和 value 返回的值不一致的
  bug。 [#37](https://github.com/panpf/zoomimage/issues/37)
* fix: 修复在 LayoutDirection 为 RTL 模式下偏移错误的
  bug. [#65](https://github.com/panpf/zoomimage/issues/65)
* remove: 移除 'remember\*ZoomState()' 系列函数的 logger 参数
* change: ZoomImage 系列组件禁用双指拖动手势，避免快速双指缩放后又触发 fling
  导致图片漂移。 [#28](https://github.com/panpf/zoomimage/issues/28)
* change: ZoomableState 和 ZoomableEngine 的 `contentSize` 属性现在为空时不再返回 `containerSize`
* change: 滚动条的最小长度现在是 10.dp
* improve: ZoomImageView
  系列组件双指缩放手势现在只需滑动比较短的距离就可以触发. [#61](https://github.com/panpf/zoomimage/issues/61)
* improve: 为所有组件的 alignment 属性提供 LayoutDirection RTL
  支持. [#66](https://github.com/panpf/zoomimage/issues/66)
* new: 新增支持鼠标滚轮缩放功能。 [#35](https://github.com/panpf/zoomimage/issues/35)
* new: 新增支持键盘缩放和拖动功能。 [#42](https://github.com/panpf/zoomimage/issues/42)
* new: 'remember\*ZoomState()' 系列函数增加 'logLevel: Level' 参数
* new: 新增 `containerWhitespaceMultiple` 和 `containerWhitespace`
  参数用于在图像边缘和容器边缘之间留出空白区域。 [#45](https://github.com/panpf/zoomimage/issues/45)

subsampling:

* fix: 修复在遇到极端尺寸（一边特别大一边特别小）的图片时导致 Tile 地图计算异常而崩溃的
  bug。 [#32](https://github.com/panpf/zoomimage/issues/32)
* fix: 修复 coil 和 glide 系列组件不支持 '/sdcard/sample.jpeg' 类型 model 的
  bug。 [#34](https://github.com/panpf/zoomimage/issues/34)
* remove: 移除 TileBitmapCache 的 put() 方法的 disallowReuseBitmap 参数
* remove: 移除 BitmapFrom
* change: 移除 ignoreExifOrientation 属性
* change: 移除 disabledTileBitmapReuse 和 TileBitmapPool 属性
* change: 非安卓平台现在使用 Skia 解码图片
* change: 用 Jetbrains Lifecycle 代替 StoppedController
* change: ImageSource 现在使用 okio 的 Source 代替 InputStream
* change: 桌面平台的 ImageSource.fromResource() 改为 fromKotlinResource()
* change: ImageSource.fromFile(File) 现在是 JVM 平台专属的扩展函数
* change: 移除 ImageSource 的 openSource() 方法的 suspend 修饰符，需要 suspend 的话请使用
  ImageSource.Factory
* change: 现在不再根据缩略图的图像请求的内存缓存设置来决定是否禁用 Tile 的内存缓存
* change: SubsamplingState.disabledTileBitmapCache 重命名为 disabledTileImageCache
* change: SubsamplingState.tileBitmapCache 重命名为 tileImageCache
* change: SubsamplingEngine.disabledTileBitmapCacheState 重命名为 disabledTileImageCacheState
* change: SubsamplingEngine.tileBitmapCacheState 重命名为 tileImageCacheState
* improve: 放宽缩略图和原图的宽高比限制。 [#22](https://github.com/panpf/zoomimage/issues/22)
* improve: 改进
  BitmapRegionDecoderDecodeHelper，现在只会在销毁时关闭输入流。 [#29](https://github.com/panpf/zoomimage/issues/29)
* improve: 现在从内存中加载的图块在显示时也会有动画效果
* improve: 现在非 Android 平台在子采样时会直接忽略 gif 图片
* improve: 现在只要缩略图的任意一边超过原图就无法使用子采样
* improve: 改进 SketchZoomImageview、CoilZoomImageView、GlideZoomImageView、PicassoZoomImageView，现在设置
  ImageSource 时只要失败就会清空 ImageSource
* improve: 改进 sketch
  系列组件，子采样初始化完成后不会再重置变焦。 [#50](https://github.com/panpf/zoomimage/issues/50)
* improve: 现在 Sketch、Coil、Glide 系列组件在设置子采样时会主动过滤动图
* new: 新增支持 js、wasmJs、iOS 平台
* new: TileBitmap 增加 bitmapFrom 属性
* new: GlideZoomAsyncImage、GlideZoomImageView、PicassoZoomImageView 支持扩展 ImageSource
* new: 新增 ImageSource.Factory 接口，用于创建 ImageSource
* new: ZoomState 和 ZoomImageView 增加 `setSubsamplingImage()` 方法，用于设置 ImageSource

other:

* remove: 移除 Logger 的 showThreadName 参数
* remove: 移除 Logger 的 module 属性
* change: sketch-compose-coil 模块重命名为 sketch-compose-coil2，并新增 sketch-compose-coil3 模块
* change: sketch-compose-sketch 模块重命名为 sketch-compose-sketch3，并新增 sketch-compose-sketch4 模块
* change: sketch-core-coil 模块重命名为 sketch-compose-coil2，并新增 sketch-core-coil3 模块
* change: sketch-core-sketch 模块重命名为 sketch-compose-sketch3，并新增 sketch-core-sketch4 模块
* change: sketch-view-coil 模块重命名为 sketch-compose-coil2，并新增 sketch-view-coil3 模块
* change: sketch-view-sketch 模块重命名为 sketch-compose-sketch3，并新增 sketch-view-sketch4 模块
* change: Logger.DEBUG 改为 Logger.Level.Debug
* change: 现在每个组件的日志的 tag 都是单独的
* change: ZoomImage、SketchZoomAsyncImage、CoilZoomAsyncImage、GlideZoomAsyncImage
  等函数的 `state: ZoomState` 参数的名字改为 `zoomState: ZoomState`
* depend: 升级 kotlin 2.0.21, kotlinx coroutines 1.9.0
* depend: 升级 jetbrains compose 1.7.0, jetbrains lifecycle 2.8.3
* depend: 升级 coil 2.7.0

## 1.1.0-rc03

zoom:

* fix: 修复 containerWhitespace 导致崩溃的 bug. [#63](https://github.com/panpf/zoomimage/issues/63)
* fix: 修复在 LayoutDirection 为 RTL 模式下偏移错误的
  bug. [#65](https://github.com/panpf/zoomimage/issues/65)
* improve: 为所有组件的 alignment 和 containerWhitespace 属性提供 LayoutDirection RTL
  支持. [#66](https://github.com/panpf/zoomimage/issues/66)

depend:

* depend: 升级 coil v3.0.4 版本

## 1.1.0-rc02

zoom:

* fix: 'zoomimage-compose-sketch4' 模块的 SketchZoomAsyncImage
  组件始终首先在左上角显示图像，然后立即将图像移动到屏幕中央. [#60](https://github.com/panpf/zoomimage/issues/60)
* improve: ZoomImageView
  系列组件双指缩放手势现在只需滑动比较短的距离就可以触发. [#61](https://github.com/panpf/zoomimage/issues/61)
* new: 新增 containerWhitespace
  属性用于以像素值的方式设置容器容器四周的空白. [#59](https://github.com/panpf/zoomimage/issues/59)

subsampling:

* improve: 改进根据 mimeType 判断是否支持区域解码，非 image 类型直接返回 false，非 Android 平台上根据
  skiko 版本判断是否支持 heic、heif、avif 类型

depend:

* depend: 升级 sketch4 v4.0.0-rc01 版本

## 1.1.0-rc01

zoom:

* new: 新增 `containerWhitespaceMultiple`
  参数用于在图像边缘和容器边缘之间留出空白区域。 [#45](https://github.com/panpf/zoomimage/issues/45)

subsampling:

* remove: 移除 BitmapFrom
* change: SubsamplingState.disabledTileBitmapCache 重命名为 disabledTileImageCache
* change: SubsamplingState.tileBitmapCache 重命名为 tileImageCache
* change: SubsamplingEngine.disabledTileBitmapCacheState 重命名为 disabledTileImageCacheState
* change: SubsamplingEngine.tileBitmapCacheState 重命名为 tileImageCacheState
* improve: 改进 sketch
  系列组件，子采样初始化完成后不会再重置变焦。 [#50](https://github.com/panpf/zoomimage/issues/50)
* improve: 现在 Sketch、Coil、Glide 系列组件在设置子采样时会主动过滤动图
* new: \*ZoomState 和 \*ZoomImageView 新增 setSubsamplingImage() 方法用于替代 setImageSource() 方法

other:

* change: sketch-compose-coil 模块重命名为 sketch-compose-coil3
* change: sketch-compose-coil-core 模块重命名为 sketch-compose-coil3-core
* change: sketch-compose-sketch 模块重命名为 sketch-compose-sketch4
* change: sketch-compose-sketch-core 模块重命名为 sketch-compose-sketch4-core
* change: sketch-core-coil 模块重命名为 sketch-core-coil3
* change: sketch-core-coil-core 模块重命名为 sketch-core-coil3-core
* change: sketch-core-sketch 模块重命名为 sketch-core-sketch4
* change: sketch-core-sketch-core 模块重命名为 sketch-core-sketch4-core
* change: sketch-view-coil 模块重命名为 sketch-view-coil3
* change: sketch-view-coil-core 模块重命名为 sketch-view-coil3-core
* change: sketch-view-sketch 模块重命名为 sketch-view-sketch4
* change: sketch-view-sketch-core 模块重命名为 sketch-view-sketch4-core
* depend: 升级 jetbrains compose 1.7.0, jetbrains lifecycle 2.8.3

## 1.1.0-beta01

zoom:

* change: ZoomableState 和 ZoomableEngine 的 `contentSize` 属性现在为空时不再返回 `containerSize`
* change: 滚动条的最小长度现在是 10.dp
* new: 'remember\*ZoomState()' 系列函数增加 'logLevel: Level' 参数

subsampling:

* change: 现在不再根据缩略图的图像请求的内存缓存设置来决定是否禁用 Tile 的内存缓存
* improve: 改进 SketchZoomImageview、CoilZoomImageView、GlideZoomImageView、PicassoZoomImageView，现在设置
  ImageSource 时只要失败就会清空 ImageSource
* new: ZoomState 和 ZoomImageView 增加 `setImageSource()` 方法，用于设置 ImageSource

## 1.1.0-alpha06

zoom:

* broken: 移除 'remember\*ZoomState()' 系列函数的 logger 参数
* new: 新增支持鼠标滚轮缩放功能。 [#35](https://github.com/panpf/zoomimage/issues/35)
* new: 新增支持键盘缩放和拖动功能。 [#42](https://github.com/panpf/zoomimage/issues/42)

subsampling:

* broken: CoilModelToImageSource、GlideModelToImageSource、PicassoDataToImageSource 接口的
  \*ToImageSource 方法添加 suspend 修饰符
* broken: 移除 SubsamplingEngine 的构造函数的 view 参数

## 1.1.0-alpha05

subsampling:

* change: CoilZomState 和 GlideZoomState 的 \*ModelToImageSource 改为在创建时提供
* new: coil、glide、picasso 系列组件现在支持 'android.resource://example.package.name/drawable/image'
  和 'android.resource://example.package.name/4125123' 类型的 model

zoom:

* fix: 修复 ZoomableEngine 的 contentSizeState 属性 collect 和 value 返回的值不一致的
  bug 。 [#37](https://github.com/panpf/zoomimage/issues/37)
* change: ZoomImage 系列组件禁用双指拖动手势，避免快速双指缩放后又触发 fling
  导致图片漂移。 [#28](https://github.com/panpf/zoomimage/issues/28)

other:

* upgrade: 升级 sketch 到 4.0.0-alpha05 版本

## 1.1.0-alpha04

subsampling:

* fix: 修复在遇到极端尺寸（一边特别大一边特别小）的图片时导致 Tile 地图计算异常而崩溃的
  bug。 [#32](https://github.com/panpf/zoomimage/issues/32)
* fix: 修复 coil 和 glide 系列组件不支持 '/sdcard/sample.jpeg' 类型 model 的
  bug。 [#34](https://github.com/panpf/zoomimage/issues/34)
* fix: 修复 ios 版 KotlinResourceImageSource 无法加载图片的
  bug。 [#36](https://github.com/panpf/zoomimage/issues/36)
* improve: 现在非 Android 平台在子采样时会直接忽略 gif 图片
* improve: 现在只要缩略图的任意一边超过原图就无法使用子采样

## 1.1.0-alpha03

subsampling:

* fix: 修复 coil 系列的组件从 1.1.0-alpha02 版本开始无法子采样的
  bug。 [#31](https://github.com/panpf/zoomimage/issues/31)
* improve: GlideModeToImageSource 和 PicassoDataToImageSource 现在是用户注册的优先
* improve: 改进
  BitmapRegionDecoderDecodeHelper，现在只会在销毁时关闭输入流。 [#29](https://github.com/panpf/zoomimage/issues/29)
* improve: 现在从内存中加载的图块在显示时也会有动画效果

zoom:

* remove: 移除 GestureType.NONE 和 ContinuousTransformType.NONE 属性
* change: SubsamplingState 的 pausedContinuousTransformType 属性名称改为
  pausedContinuousTransformTypes，SubsamplingEngine 的 pausedContinuousTransformTypeState 属性名称改为
  pausedContinuousTransformTypesState
* change: ZoomableState 的 disabledGestureType 属性名称改为 disabledGestureTypes，ZoomableEngine 的
  disabledGestureTypeState 属性名称改为 disabledGestureTypesState

## 1.1.0-alpha02

subsampling:

* fix: 修复子采样并发控制失效导致短时间内多次打开 ImageSource 的
  bug。 [#29](https://github.com/panpf/zoomimage/issues/29)
* change: 移除 ImageSource 的 openSource() 方法的 suspend 修饰符，需要 suspend 的话请使用
  ImageSource.Factory
* improve: 放宽缩略图和原图的宽高比限制。 [#22](https://github.com/panpf/zoomimage/issues/22)
* improve: SketchImageSource、CoilImageSource、GlideHttpImageSource、PicassoHttpImageSource 现在支持从网络下载图片
* new: 新增 ImageSource.Factory 接口，用于创建 ImageSource

## 1.1.0-alpha01

> [!CAUTION]
> 为实现支持 js 和 iOS 平台，所有 `remove` 和 `change` 标签的变更都是破坏性的

subsampling:

* remove: 移除 TileBitmapCache 的 put() 方法的 disallowReuseBitmap 参数
* change: 移除 ignoreExifOrientation 属性
* change: 移除 disabledTileBitmapReuse 和 TileBitmapPool 属性
* change: 非安卓平台现在使用 Skia 解码图片
* change: 用 Jetbrains Lifecycle 代替 StoppedController
* change: ImageSource 现在使用 okio 的 Source 代替 InputStream
* change: 桌面平台的 ImageSource.fromResource() 改为 fromKotlinResource()
* change: ImageSource.fromFile(File) 现在是 JVM 平台专属的扩展函数
* new: 新增支持 js、wasmJs、iOS 平台
* new: TileBitmap 增加 bitmapFrom 属性
* new: GlideZoomAsyncImage、GlideZoomImageView、PicassoZoomImageView 支持扩展 ImageSource

other:

* remove: 移除 Logger 的 showThreadName 参数
* remove: 移除 Logger 的 module 属性
* change: Logger.DEBUG 改为 Logger.Level.Debug
* change: 现在每个组件的日志的 tag 都是单独的
* change: zoomimage-view-sketch 和 zoomimage-compose-sketch 模块升级到 sketch4，同时新增
  zoomimage-view-sketch3 和 zoomimage-compose-sketch3 模块继续支持 sketch3
* change: zoomimage-view-coil 和 zoomimage-compose-coil 模块升级到 coil3，同时新增
  zoomimage-view-coil2 和 zoomimage-compose-coil2 模块继续支持 coil2
* change: ZoomImage、SketchZoomAsyncImage、CoilZoomAsyncImage、GlideZoomAsyncImage
  等函数的 `state: ZoomState` 参数的名字改为 `zoomState: ZoomState`

## v1.0.2

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
