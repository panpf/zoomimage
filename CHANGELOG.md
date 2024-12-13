# Change Log

Translations: [简体中文](CHANGELOG_zh.md)

## 1.1.0 Stable

> [!CAUTION]
> To support js and iOS platforms, all `remove` and `change` tag changes are destructive

zoom:

* fix: Fix the inconsistent values returned by the contentSizeState property collect and value of
  ZoomableEngine
  bug. [#37](https://github.com/panpf/zoomimage/issues/37)
* fix: Fixed offset error when LayoutDirection is in RTL mode
  bug. [#65](https://github.com/panpf/zoomimage/issues/65)
* remove: Remove the logger parameter of the 'remember\*ZoomState()' series of functions
* change: The ZoomImage series components disable two-finger dragging gestures to avoid triggering
  fling after rapid two-finger zooming, causing the image to
  drift. [#28](https://github.com/panpf/zoomimage/issues/28)
* change: The `contentSize` properties of ZoomableState and ZoomableEngine now no longer return
  `containerSize` when empty
* change: The minimum length of the scrollbar is now 10.dp
* improve: The two-finger zoom gesture of ZoomImageView series components can now be triggered by
  sliding a relatively short distance. [#61](https://github.com/panpf/zoomimage/issues/61)
* improve: Provides LayoutDirection RTL support for the alignment property of all
  components. [#66](https://github.com/panpf/zoomimage/issues/66)
* new: Added support for mouse wheel zoom
  function. [#35](https://github.com/panpf/zoomimage/issues/35)
* new: Added support for keyboard zoom and drag
  functions. [#42](https://github.com/panpf/zoomimage/issues/42)
* new: 'remember\*ZoomState()' series of functions add 'logLevel: Level' parameter
* new: New `containerWhitespaceMultiple` and `containerWhitespace` parameters are added to leave a
  white space between the edge of the image and the edge of the
  container. [#45](https://github.com/panpf/zoomimage/issues/45)

subsampling:

* fix: Fixed a bug that caused the Tile map to calculate abnormally and crash when encountering
  images of extreme sizes (one side is extremely large and the other is extremely
  small). [#32](https://github.com/panpf/zoomimage/issues/32)
* fix: Fixed the bug that coil and glide series components do not support '/sdcard/sample.jpeg' type
  model. [#34](https://github.com/panpf/zoomimage/issues/34)
* remove: Remove the disallowReuseBitmap parameter of TileBitmapCache's put() method
* remove: Remove BitmapFrom
* change: Remove ignoreExifOrientation attribute
* change: Remove disabledTileBitmapReuse and TileBitmapPool attributes
* change: Non-Android platforms now use Skia to decode images
* change: Use Jetbrains Lifecycle instead of StoppedController
* change: ImageSource now uses okio's Source instead of InputStream
* change: ImageSource.fromResource() on desktop platform changed to fromKotlinResource()
* change: ImageSource.fromFile(File) is now a JVM platform-specific extension function
* change: Remove the suspend modifier of ImageSource's openSource() method. If you need to suspend,
  please use ImageSource.Factory
* change: Decisions about whether to disable Tile's memory cache are now no longer based on the
  memory cache setting of the thumbnail's image request
* change: SubsamplingState.disabledTileBitmapCache renamed to disabledTileImageCache
* change: SubsamplingState.tileBitmapCache renamed to tileImageCache
* change: SubsamplingEngine.disabledTileBitmapCacheState renamed to disabledTileImageCacheState
* change: SubsamplingEngine.tileBitmapCacheState renamed to tileImageCacheState
* improve: Relax the aspect ratio restrictions between thumbnails and original
  images. [#22](https://github.com/panpf/zoomimage/issues/22)
* improve: Improved BitmapRegionDecoderDecodeHelper, now only closes the input stream on
  destruction. [#29](https://github.com/panpf/zoomimage/issues/29)
* improve: Tiles loaded from memory now also animate when displayed
* improve: Now non-Android platforms will directly ignore gif images when subsampling
* improve: Now subsampling cannot be used as long as either side of the thumbnail exceeds the
  original image
* improve: Improved SketchZoomImageview, CoilZoomImageView, GlideZoomImageView,
  PicassoZoomImageView, now when setting ImageSource, the ImageSource will be cleared whenever it
  fails.
* improve: Improve the sketch series components so that the zoom will no longer be reset after
  subsampling initialization is completed. [#50](https://github.com/panpf/zoomimage/issues/50)
* improve: Now Sketch, Coil, and Glide series components will actively filter animations when
  setting subsampling.
* new: Added support for js, wasmJs, and iOS platforms
* new: TileBitmap adds bitmapFrom attribute
* new: GlideZoomAsyncImage, GlideZoomImageView, and PicassoZoomImageView support extended
  ImageSource
* new: Added ImageSource.Factory interface for creating ImageSource
* new: ZoomState and ZoomImageView add `setSubsamplingImage()` method for setting ImageSource

other:

* remove: Remove the showThreadName parameter of Logger
* remove: Remove the module attribute of Logger
* change: The sketch-compose-coil module is renamed to sketch-compose-coil2, and the
  sketch-compose-coil3 module is added
* change: The sketch-compose-sketch module is renamed to sketch-compose-sketch3, and the
  sketch-compose-sketch4 module is added
* change: The sketch-core-coil module is renamed to sketch-compose-coil2, and the sketch-core-coil3
  module is added
* change: The sketch-core-sketch module is renamed to sketch-compose-sketch3, and the
  sketch-core-sketch4 module is added
* change: The sketch-view-coil module is renamed to sketch-compose-coil2, and the sketch-view-coil3
  module is added
* change: The sketch-view-sketch module is renamed to sketch-compose-sketch3, and the
  sketch-view-sketch4 module is added
* change: Logger.DEBUG changed to Logger.Level.Debug
* change: Now each component's log tag is separate
* change: The name of the `state: ZoomState` parameter of ZoomImage, SketchZoomAsyncImage,
  CoilZoomAsyncImage, GlideZoomAsyncImage and other functions has been changed to
  `zoomState: ZoomState`
* depend: Upgrade kotlin 2.0.21, kotlinx coroutines 1.9.0
* depend: Upgrade jetbrains compose 1.7.0, jetbrains lifecycle 2.8.3
* depend: Upgrade coil 2.7.0

## 1.1.0-rc03

zoom:

* fix: Fix containerWhitespace crash bug. [#63](https://github.com/panpf/zoomimage/issues/63)
* fix: Fixed the bug of offset error when LayoutDirection is in RTL
  mode. [#65](https://github.com/panpf/zoomimage/issues/65)
* improve: Provide LayoutDirection RTL support for alignment and containerWhitespace properties of
  all components. [#66](https://github.com/panpf/zoomimage/issues/66)

depend:

* depend: Upgrade coil v3.0.4 version

## 1.1.0-rc02

zoom:

* fix: The SketchZoomAsyncImage component of the 'zoomimage-compose-sketch4' module always displays
  the image in the upper left corner first, and then instantly moves the image to the center of the
  screen. [#60](https://github.com/panpf/zoomimage/issues/60)
* improve: The two-finger zoom gesture of ZoomImageView series components can now be triggered by
  sliding a relatively short distance. [#61](https://github.com/panpf/zoomimage/issues/61)
* new: The new containerWhitespace attribute is used to set the white space around the container in
  pixel values. [#59](https://github.com/panpf/zoomimage/issues/59)

subsampling:

* improve: Improve to determine whether regional decoding is supported based on mimeType. Non-image
  types directly return false. On non-Android platforms, based on the skiko version, it is
  determined whether heic, heif, and avi types are supported.

depend:

* depend: Upgrade sketch to version 4.0.0-rc01

## 1.1.0-rc01

zoom:

* new: New `containerWhitespaceMultiple` parameter is added to leave a white space between the edge
  of the image and the edge of the container. [#45](https://github.com/panpf/zoomimage/issues/45)

subsampling:

* remove: Remove BitmapFrom
* change: SubsamplingState.disabledTileBitmapCache rename to disabledTileImageCache
* change: SubsamplingState.tileBitmapCache rename to tileImageCache
* change: SubsamplingEngine.disabledTileBitmapCacheState rename to disabledTileImageCacheState
* change: SubsamplingEngine.tileBitmapCacheState rename to tileImageCacheState
* improve: Improve the sketch series components so that the zoom will no longer be reset after
  subsampling initialization is completed. [#50](https://github.com/panpf/zoomimage/issues/50)
* improve: Now Sketch, Coil, Glide series components will actively filter the animation diagram when
  setting subsampling.
* new: \*ZoomState and \*ZoomImageView add a new setSubsamplingImage() method to replace the
  setImageSource() method

other:

* change: sketch-compose-coil module renamed to sketch-compose-coil3
* change: sketch-compose-coil-core module renamed to sketch-compose-coil3-core
* change: sketch-compose-sketch module renamed to sketch-compose-sketch4
* change: sketch-compose-sketch-core module renamed to sketch-compose-sketch4-core
* change: sketch-core-coil module renamed to sketch-core-coil3
* change: sketch-core-coil-core module renamed to sketch-core-coil3-core
* change: sketch-core-sketch module renamed to sketch-core-sketch4
* change: sketch-core-sketch-core module renamed to sketch-core-sketch4-core
* change: sketch-view-coil module renamed to sketch-view-coil3
* change: sketch-view-coil-core module renamed to sketch-view-coil3-core
* change: sketch-view-sketch module renamed to sketch-view-sketch4
* change: sketch-view-sketch-core module renamed to sketch-view-sketch4-core
* depend: Upgrade jetbrains compose 1.7.0, jetbrains lifecycle 2.8.3

## 1.1.0-beta01

zoom:

* change: The `contentSize` properties of ZoomableState and ZoomableEngine now no longer return
  `containerSize` when empty
* change: The minimum length of the scrollbar is now 10.dp
* new: 'remember\*ZoomState()' series of functions add 'logLevel: Level' parameter

subsampling:

* change: Decisions about whether to disable Tile's memory cache are no longer based on the memory
  cache setting of the thumbnail's image request
* improve: Improve SketchZoomImageview, CoilZoomImageView, GlideZoomImageView, PicassoZoomImageView.
  Now when setting ImageSource, the ImageSource will be cleared whenever it fails.
* new: ZoomState and ZoomImageView add `setImageSource()` method for setting ImageSource

## 1.1.0-alpha06

zoom:

* broken: Remove the logger parameter of the 'remember\*ZoomState()' series of functions
* new: Added support for mouse wheel scale
  function. [#35](https://github.com/panpf/zoomimage/issues/35)
* new: Added support for keyboard scale and drag
  functions. [#42](https://github.com/panpf/zoomimage/issues/42)

subsampling:

* broken: Add the suspend modifier to the \*ToImageSource methods of the CoilModelToImageSource,
  GlideModelToImageSource, and PicassoDataToImageSource interfaces
* broken: Remove the view parameter of SubsamplingEngine's constructor

## 1.1.0-alpha05

subsampling:

* change: \*ModelToImageSource for CoilZomState and GlideZoomState changed to provided at create
  time
* new: coil, glide, picasso series components now support 'android.resource:
  //example.package.name/drawable/image' and 'android.resource://example.package.name/4125123' types
  of models

zoom:

* fix: Fix the bug that the values returned by ZoomableEngine's contentSizeState property collect
  and value are inconsistent. [#37](https://github.com/panpf/zoomimage/issues/37)
* change: The ZoomImage series components disable two-finger dragging gestures to avoid triggering
  fling after rapid two-finger zooming, causing the image to
  drift. [#28](https://github.com/panpf/zoomimage/issues/28)

other:

* upgrade: Upgrade sketch to version 4.0.0-alpha05

## 1.1.0-alpha04

images of extreme sizes (one side is extremely large and the other is extremely
small). [#32](https://github.com/panpf/zoomimage/issues/32)

* fix: Fix the bug that coil and glide series components do not support '/sdcard/sample.jpeg' type
  model. [#34](https://github.com/panpf/zoomimage/issues/34)
* fix: Fixed a bug that caused the Tile map to calculate abnormally and crash when encountering
* fix: Fixed the bug where KotlinResourceImageSource on ios could not load
  images. [#36](https://github.com/panpf/zoomimage/issues/36)
* improve: Now non-Android platforms will directly ignore gif images when subsampling panpf Moments
  ago
* improve: Now subsampling cannot be used as long as either side of the thumbnail exceeds the
  original image

## 1.1.0-alpha03

subsampling:

* fix: Fixed the bug that coil series components cannot be subsampling starting from version
  1.1.0-alpha02. [#31](https://github.com/panpf/zoomimage/issues/31)
* improve: GlideModeToImageSource and PicassoDataToImageSource are now priority for user
  registration
* improve: Improve BitmapRegionDecoderDecodeHelper. Input streams will now only be closed on
  destruction. [#29](https://github.com/panpf/zoomimage/issues/29)
* improve: Tiles loaded from memory now also animate when displayed

zoom:

* remove: Removed GestureType.NONE and ContinuousTransformType.NONE properties
* change: The pausedContinuousTransformType property name of SubsamplingState is changed to
  pausedContinuousTransformTypes, and the pausedContinuousTransformTypeState property name of
  SubsamplingEngine is changed to pausedContinuousTransformTypesState
* change: The disabledGestureType property name of ZoomableState is changed to disabledGestureTypes,
  and the disabledGestureTypeState property name of ZoomableEngine is changed to
  disabledGestureTypesState

## 1.1.0-alpha02

subsampling:

* fix: Fixed a bug that caused the failure of subsampling concurrency control to open ImageSource
  multiple times in a short period of time。 [#29](https://github.com/panpf/zoomimage/issues/29)
* change: Remove the suspend modifier of ImageSource's openSource() method. If you need to suspend,
  please use ImageSource.Factory.
* improve: Relax the aspect ratio restrictions between thumbnails and original
  images. [#22](https://github.com/panpf/zoomimage/issues/22)
* improve: SketchImageSource, CoilImageSource, GlideHttpImageSource, PicassoHttpImageSource now
  support downloading images from the internet
* new: Added ImageSource.Factory interface for creating ImageSource

## 1.1.0-alpha01

> [!CAUTION]
> To support js and iOS platforms, all `remove` and `change` tag changes are destructive

subsampling:

* remove: Remove the disallowReuseBitmap parameter of the put() method of TileBitmapCache
* change: Remove ignoreExifOrientation attribute
* change: Remove disabledTileBitmapReuse and TileBitmapPool attributes
* change: Non-Android platforms now use Skia to decode images
* change: Use Jetbrains Lifecycle instead of StoppedController
* change: ImageSource now uses okio's Source instead of InputStream
* change: ImageSource.fromResource() on desktop platform changed to fromKotlinResource()
* change: ImageSource.fromFile(File) is now a JVM platform-specific extension function
* new: Added support for js, wasmJs, and iOS platforms
* new: TileBitmap adds bitmapFrom attribute
* new: GlideZoomAsyncImage, GlideZoomImageView, and PicassoZoomImageView support extended
  ImageSource

other:

* remove: Remove the showThreadName parameter of Logger
* remove: Remove the module attribute of Logger
* change: Logger.DEBUG change to Logger.Level.Debug
* change: Now each component's log tag is separate
* change: zoomimage-view-sketch and zoomimage-compose-sketch modules upgraded to sketch4, while
  adding zoomimage-view-sketch3 and zoomimage-compose-sketch3 modules to continue to support sketch3
* change: zoomimage-view-coil and zoomimage-compose-coil modules are upgraded to coil3, and new
  The zoomimage-view-coil2 and zoomimage-compose-coil2 modules continue to support coil2
* change: The name of the `state: ZoomState` parameter of ZoomImage, SketchZoomAsyncImage,
  CoilZoomAsyncImage, GlideZoomAsyncImage and other functions is changed to `zoomState: ZoomState`

## v1.0.2

* fix: Fix the bug that ZoomImageView crashes due to the TypedArray.close() method in API 30 and
  below versions. [#15](https://github.com/panpf/zoomimage/issues/15)
* fix: Fix the bug that GlideZoomAsyncImage and GlideZoomImageView do not support 'file:
  ///android_asset/' and 'file:///sdcard/sample.jpeg' type
  model. [#16](https://github.com/panpf/zoomimage/issues/16)
* improve: Improved support for Picasso
* improve: Improved ScalesCalculator.dynamic(), now when reading mode is available mediumScale is
  always the initial scaling multiplier of reading mode

## v1.0.1

* fix: Fixed the bug that the image of ZoomImageView would jump when you do not release your hand
  after dragging with one finger and then press another finger to perform a two-finger zoom
  gesture. [#12](https://github.com/panpf/zoomimage/issues/12)

## v1.0.0

Initial stable release

## v1.0.0-rc01

zoom:

* improve: Improved ZoomableState, ZoomableEngine, SubsamplingState and SubsamplingEngine, now they
  all start working when remembered or attached to a window

other:

* depend: Upgrade sketch 3.3.0 stable

## v1.0.0-beta11

zoom:

* change: ZoomableEngine's contentSizeState property now uses containerSizeState when it is empty

other:

* depend: Upgrade sketch 3.3.0-beta06
* change: Rename `zoomimage-compose-coil-base` module to `zoomimage-compose-coil-core
* improve: GlideZoomAsyncImage and GlideZoomImageView now support GlideUrl

## 1.0.0-beta10

* fix: Fixed a bug where ZoomImage could not display scrollbars
* fix: Fixed a bug where SketchZoomAsyncImage and CoilZoomAsyncImage would crash when they
  encountered a drawable without size
* fix: Fixed the bug where placeholder would be accidentally shrunk when placeholder and result
  transitioned between SketchZoomAsyncImage and CoilZoomAsyncImage.
* improve: composed migrated to Modifier.Node
* depend: Upgrade Sketch 3.3.0-beta04

## 1.0.0-beta09

* fix: Fixed a crash caused by BitmapFactory.Options.outMimeType being null when Android decodes
  unsupported image formats
* change: `zoomimage-core` module minSdk changed from 21 to 16

## 1.0.0-beta08

other:

* fix: Fixed a bug that caused the `zoomimage-compose-glide` module to crash due to no configuration
  confusion.
* change: Now only the `zoomimage-core` module generates BuildConfig
* change: ZoomableEngine's scale(), rotate() and other methods add the suspend modifier
* new: Added `zoomimage-compose-coil-base` and `zoomimage-compose-sketch-core` modules, which depend
  on the non-singleton modules of Coil and Sketch respectively.
* depend: Upgrade Sketch 3.3.0-beta02

## 1.0.0-beta07

zoom:

* fix: Now the one-finger scale and long press callbacks will no longer be triggered at the same
  time.
* fix: Fixed a bug where the View version's double-tap event, long-press event, and two-finger scale
  event would be triggered at the same time
* fix: Fixed the bug that when the Compose version of the component is enlarged in the Pager and
  triggers the Pager's sliding at the edge position, sliding back will interrupt the Pager's
  sliding.
* change: The one-finger zoom gesture has been changed to double-click and then drag up and down,
  and is now enabled by default
* change: The x, y parameters of OnViewTapListener and OnViewLongPressListener are combined into one
  OffsetCompat
* improve: Improve the calculateTiles() function, now the right and bottom of the last cell are
  always `width-1` and `height -1`
* improve: Improve gesture

subsampling:

* fix: Fixed a bug with gaps between tiles on desktop platforms

## 1.0.0-beta06

zoom:

* fix: Fixed the issue where the image can be seen quickly moving from the top to the center in the
  initial View version.
* fix: Fixed the issue where the image can be seen quickly moving from the top to the center in the
  initial Compose version.
* fix: Fixed a bug where weird flashing image content would appear at the edge of the
  SketchZoomAsyncImage and CoilZoomAsyncImage components when they are quickly switched in Pager.
* new: Added HeartbeatHapticFeedback, which will have a heartbeat-like effect when triggering
  single-finger zoom.

subsampling:

* fix: Fixed a bug where the same tile would be loaded multiple times due to failure to cancel
  unfinished background tiles in time when double-clicking to enlarge.
* fix: Fixed a bug where the View version of the subsampling tile did not set the display count
  correctly, causing the image at the corresponding position to appear black.
* fix: Fixed a bug where the tiles read from the memory cache in the View version were accidentally
  discarded, causing the image at the corresponding position to be blurred.

other:

* change: rememberZoomImageLogger() function adds level and pipeline parameters

## 1.0.0-beta05

zoom:

* fix: Fixed a bug where the content did not change with the window due to a slight difference
  between the user and the initial value when the user operated and returned to the starting state
  and then adjusted the window size

subsampling:

* remove: Remove the size property of TileBitmap
* improve: Subsampling tiles are drawn on separate components
* improve: Reduce the frequency of TileManager due to containerSize changes
* new: The Desktop platform supports subsampling
* new: The Desktop platform supports exif orientation

## 1.0.0-beta04

zoom:

* change: LongPressSlide rename to OneFingerScale
* improve: Now keep the scale and visible center unchanged of content only when the container size
  changes
* new: Support for disabling gestures
* new: Added the Modifier.zoom() function to easily add zoom functionality to other components

subsampling:

* change: Added stoppedController attribute instead of setLifecycle() method
* change: Improved some API names

other:

* new: Support for Compose Multiplatform
* change: The rememberZoomImageLogger function removes the level parameters

## 1.0.0-beta03

zoom:

* fix: Fixed a bug where the calculateUserOffsetBounds function could return the wrong bounds when
  zooming to a full screen, causing a crash
* fix: Fixed a bug where GlideZoomAsyncImage would load bitmaps larger than the view, causing a
  crash
* improve: Upgrade GlideImage 1.0.0-beta01
* improve: NoClipImage rename to NoClipContentImage

## 1.0.0-beta02

zoom:

fix: Fixed a bug where ZoomImage could not be scaled after switching in Pager
new: Supports one finger scale function

## 1.0.0-beta01

zoom:

* fix: Fixed a bug where ZoomImageView sometimes did not animate when double-clicking to zoom
* change: The external properties of ZoomableEngine are now wrapped in StateFlow and can be listened
  to directly
* improve: Avoid triggering Pager's swipe at the smallest zoom factor and multi-finger touch

subsampling:

* fix: Fixed a bug where subsampling could fail
* fix: The tileGridSizeMap property now returns the correct size
* fix: Fixed a bug where the calculateTileGridMap() function raised OutOfMemoryError when it
  encountered a particularly small tileMaxSize
* change: The external properties of SubsamplingEngine are now wrapped in StateFlow and can be
  listened to directly
* change: The ImageSource.openInputStream() method removes the suspend modifier
* change: pauseWhenTransforming change to pausedContinuousTransformType
* improve: Improved calculateImageLoadRect() calculations that now do not exceed imageSize
* improve: Tile concurrent loads reduced from 4 to 2 to reduce memory footprint and improve UI
  performance
* new: SubsamplingState and SubsamplingEngine add tileGridSizeMap properties
* new: Subsampling now changes in sharpness continuously when switching sampleSize and no longer
  always transitions from the base image

other:

* fix: Fixed the bug that the level judgment error caused the log not to be printed
* build: Upgrade compileSdk to 34, kotlinx-coroutines to 1.7.3, compose to 1.5.0

## 1.0.0-alpha03

zoom:

* fix: Fixed a bug where ZoomImageView would significantly pan the image after lifting one finger
  when zooming with two fingers
* fix: Fixed a bug where the rotate method was unusually when it encountered a negative rotation
  angle
* fix: Fixed a bug where zoom animation time could crash when the animation time was 0
* change: ScalesCalculator is now compatible with ReadMode
* change: onViewTapListener and onViewLongPressListener replace registerOnViewTapListener and
  registerOnViewLongPressListener
* change: ZoomImageView's zoomAbility property rename to zoomable, subsamplingAbility rename to
  subsampling
* improve: Compatible with models MIX4, ROM version 14.0.6.0, Android version 13 environment when
  the navigation bar is enabled but the app does not actively adapt to the navigation bar, an issue
  that triggers a reset due to a change in View size after the screen is unlocked
* improve: ZoomImageView now supports scaleType is MATRIX
* new: ZoomImageView adds xml attribute support

other:

* change: The logger parameter for ZoomableState, SubsamplingState, ZoomableEngine, and
  SubsamplingEngine was changed to private

## 1.0.0-alpha02

zoom:

* change: The location() methods of ZoomableState and ZoomAbility rename to locate()
* new: ZoomImageView added OnResetListener

subsampling:

* change: SubsamplingAbility's registerOnTileChangedListener(), unregisterOnTileChangedListener()
  methods renamed to registerOnTileChangeListener(), unregisterOnTileChangeListener()
* change: Subsampling's paused property rename to stopped
* new: Subsampling supports tile animation
* new: Subsampling added the pauseWhenTransforming property

## 1.0.0-alpha01

Initial release
