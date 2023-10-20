## Compose Multiplatform/Compose 多平台

Both ZoomImage's `zoomimage-core` and `zoomimage-compose` modules are cross-platform and can be
found in
Used on Android, macOS, Linux, Windows and other platforms (iOS is not supported at the moment)
<br>-----------------</br>
ZoomImage 的 `zoomimage-core` 和 `zoomimage-compose` 两个模块都是跨平台的，可以在
Android、macOS、Linux、Windows 等平台上使用（暂不支持 iOS）

When configuring dependencies, you only need to add `zoomimage-core` or `zoomimage-compose`
dependencies to the project, without specifying the platform, Gradle The corresponding package is
automatically loaded according to the current platform
<br>-----------------</br>
配置依赖时只需要在项目中添加 `zoomimage-core` 或 `zoomimage-compose` 依赖即可，不需指定平台，Gradle
会自动根据当前平台加载对应的包

The desktop platform does not currently support subsampling because the desktop platform does not
currently have an API similar to BitmapRegionDecoder in Android
<br>-----------------</br>
桌面平台目前不支持子采样，因为桌面平台目前没有类似 Android 中 BitmapRegionDecoder 的 API