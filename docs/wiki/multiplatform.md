## Compose Multiplatform

Translations: [简体中文](multiplatform_zh.md)

Both ZoomImage's `zoomimage-core` and `zoomimage-compose` modules are cross-platform and can be
found in
Used on Android, macOS, Linux, Windows and other platforms (iOS is not supported at the moment)

When configuring dependencies, you only need to add `zoomimage-core` or `zoomimage-compose`
dependencies to the project, without specifying the platform, Gradle The corresponding package is
automatically loaded according to the current platform

### Multiplatform support

| Platform/Function | Zoom | Subsampling | Integrated Image Loader |
|:------------------|:----:|:-----------:|:-----------------------:|
| Android           |  ✅   |      ✅      |            ✅            |
| Desktop           |  ✅   |      ✅      |            ❌            |
| iOS               |  🚧  |     🚧      |           🚧            |
| Web               |  🚧  |     🚧      |           🚧            |

* Android：
    * Integrate Sketch, Coil, Glide, and Picasso image loading frameworks
    * Subsampling supports memory caching and Bitmap reuse
* Desktop：
    * The image loading framework is not integrated (because there is currently no mature image
      loading framework that can be used on desktop platforms)
    * Subsampling does not support memory caching and Bitmap reuse (requires image loading framework
      support)
    * The function is stable and available, but the experience is not perfect due to the bug of
      Compose itself. The bug details are as follows:
        1. When sliding to the bottom and right, the content of the component will all disappear due
           to failure to draw the subsampled tile (out of bounds)
        2. Basemaps and tiles are currently drawn as two components, so users will now always see a
           blurry basemap when the problem occurs
        3. It needs to be magnified to a certain magnification. This problem will not occur at
           smaller magnifications.
        4. When a problem occurs, dragging the lower right corner of the window to make the window
           larger can temporarily solve the problem, but there is no fixed window size that can
           solve the problem.
        5. Reported to the official,
           issue https://github.com/JetBrains/compose-multiplatform/issues/3904
* iOS：In development
* Web：In development