## Compose Multiplatform

Translations: [简体中文](multiplatform_zh.md)

Both ZoomImage's `zoomimage-core` and `zoomimage-compose` modules are cross-platform and can be
found in
Used on Android, macOS, Linux, Windows and other platforms (iOS is not supported at the moment)

When configuring dependencies, you only need to add `zoomimage-core` or `zoomimage-compose`
dependencies to the project, without specifying the platform, Gradle The corresponding package is
automatically loaded according to the current platform

### Platform support progress

* Android：Stable and available
* Desktop：
    * Functionality has been developed, but there are still bugs to be resolved
    * Compared with the Android version, except for memory caching and Bitmap reuse, other functions
      are supported
* iOS：Not supported at the moment
    * Reason 1: Compose multiplatform’s support for iOS is still in alpha stage
    * Reason 2: I won't iOS, and I have no plans to learn it in the short term
    * Welcome party for iOS students to contribute code

### Desktop version pending bugs

1. When the subsampling function slides to the bottom and right, the content of the component will
   disappear due to failure to draw the tiles. What the user sees is that the bottom and right are
   never able to display clear images. BUG
   Features are as follows:
    * When a problem occurs, all the content of the component will disappear. It cannot be drawn
      together with the base map, otherwise the base map will also disappear. Therefore, the
      subsampling tiles are currently drawn in a separate component.
    * It needs to be magnified to a certain magnification. This problem will not occur at smaller
      magnifications.
    * When a problem occurs, dragging the lower right corner of the window to enlarge the window can
      solve the problem. There is no fixed window size that can solve the problem. Different
      pictures have different window sizes that can solve the problem under different zoom factors.
    * This BUG only exists in the desktop version, the Android version does not have this problem
    * Reported to the official, issue https://github.com/JetBrains/compose-multiplatform/issues/3904
2. There will be a visible gap between subsampling tiles
    * This BUG only exists in the desktop version, the Android version does not have this problem
    * Reported to the official, issue https://github.com/JetBrains/compose-multiplatform/issues/3917