# Change Log

Translations: [简体中文](CHANGELOG_zh.md)

## new

* change: Remove ignoreExifOrientation attribute
* change: Remove disabledTileBitmapReuse and TileBitmapPool attributes
* change: Non-Android platforms now use Skia to decode images
* change: Use Jetbrains Lifecycle instead of StoppedController
* change: ImageSource now uses okio's Source instead of InputStream
* change: ImageSource.fromResource() on desktop platform changed to fromKotlinResource()
* change: ImageSource.fromFile(File) is now a JVM platform-specific extension function
* remove: Remove the showThreadName parameter of Logger
* new: New ImageSource.fromKotlinResource() function on ios platform
* new: TileBitmap adds bitmapFrom attribute
* change: zoomimage-view-sketch and zoomimage-compose-sketch modules upgraded to sketch4, while
  adding zoomimage-view-sketch3 and zoomimage-compose-sketch3 modules to continue to support sketch3
* change: zoomimage-view-coil and zoomimage-compose-coil modules are upgraded to coil3, and new
  The zoomimage-view-coil2 and zoomimage-compose-coil2 modules continue to support coil2

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
