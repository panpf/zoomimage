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

### Platform support progress/平台支持进展

* Android：Stable and available. 稳定可用
* Desktop：
    * Functionality has been developed. 功能已开发完毕
    * There are a few bugs to be fixed. 有少量 bug 待修复
    * Compared with the Android version, except for memory caching and Bitmap reuse, other functions
      are supported. 对比 Android 版本除内存缓存和 Bitmap 复用外，其它功能都支持
* iOS：Not supported at the moment. 暂不支持
    * Reason 1: Kotlin multiplatform’s support for iOS is still in alpha stage. 原因 1：kotlin
      multiplatform 对 iOS 的支持还处于 alpha 阶段
    * Reason 2: I won't iOS. 原因 2：我不会 iOS 开发