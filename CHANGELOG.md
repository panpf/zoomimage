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