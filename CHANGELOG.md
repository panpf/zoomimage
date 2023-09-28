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
* improve: ZoomImageView now supports scaleType as Matrix
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