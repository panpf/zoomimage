## Compose Multiplatform

翻译：[English](multiplatform.md)

ZoomImage 的 `zoomimage-core` 和 `zoomimage-compose` 两个模块都是跨平台的，可以在
Android、macOS、Linux、Windows 等平台上使用（暂不支持 iOS）

配置依赖时只需要在项目中添加 `zoomimage-core` 或 `zoomimage-compose` 依赖即可，不需指定平台，Gradle
会自动根据当前平台加载对应的包

### 平台支持进展

* Android：稳定可用
* Desktop：
    * 功能已开发完毕，但还有 bug 待解决
    * 对比 Android 版本除内存缓存和 Bitmap 复用外，其它功能都支持
* iOS：暂不支持
    * 原因 1：compose multiplatform 对 iOS 的支持还处于 alpha 阶段
    * 原因 2：我不会 iOS 开发，短期内也没有学的计划
    * 欢迎会 iOS 的同学贡献代码

### 桌面版待解决 bug

1. 子采样功能在滑动到底部和右边时会因绘制图块失败而导致组件的内容全部消失，用户看到的就是底部和右边始终无法展示清晰的图像，BUG
   特点如下：
    * 出现问题时会导致组件的内容全部消失，不能与底图一起绘制，否则会导致底图也消失，所以目前将子采样图块在单独的组件绘制
    * 需要放大到一定倍数，较小放大倍数时不会出现此问题
    * 出现问题时拖动窗口右下角调大窗口就能解决问题，没有固定能解决问题的窗口大小，不同图片不同缩放倍数下能解决问题的窗口大小不一样
    * 此 BUG 仅在桌面版存在，Android 版本没有此问题
    * 已报告官方，issue https://github.com/JetBrains/compose-multiplatform/issues/3904