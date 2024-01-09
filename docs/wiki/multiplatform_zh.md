## Compose Multiplatform

翻译：[English](multiplatform.md)

ZoomImage 的 `zoomimage-core` 和 `zoomimage-compose` 两个模块都是跨平台的，可以在
Android、macOS、Linux、Windows 等平台上使用（暂不支持 iOS）

配置依赖时只需要在项目中添加 `zoomimage-core` 或 `zoomimage-compose` 依赖即可，不需指定平台，Gradle
会自动根据当前平台加载对应的包

### 多平台支持

| 平台/功能   | 缩放 | 子采样 | 集成图片加载框架 |
|:--------|:--:|:---:|:--------:|
| Android | ✅  |  ✅  |    ✅     |
| Desktop | ✅  |  ✅  |    ❌     |
| iOS     | 🚧 | 🚧  |    🚧    |
| Web     | 🚧 | 🚧  |    🚧    |

* Android：
    * 集成 Sketch、Coil、Glide、Picasso 图片加载框架
    * 子采样支持内存缓存和 Bitmap 复用
* Desktop：
    * 未集成图片加载框架（因为目前没有成熟的可在桌面平台用的图片加载框架）
    * 子采样不支持内存缓存和 Bitmap 复用（需要图片加载框架支持）
    * 功能已稳定可用，但受 Compose 自身的 bug 影响体验不完美，bug 详情如下：
        1. 在滑动到底部和右边时会因绘制子采样图块失败（超出边界）而导致组件的内容全部消失
        2. 目前将底图和图块分成两个组件绘制，因此现在出现问题时用户会始终看到模糊的底图
        3. 需要放大到一定倍数，较小放大倍数时不会出现此问题
        4. 出现问题时拖动窗口右下角调大窗口就能暂时解决问题，但没有固定能解决问题的窗口大小
        5. 已报告官方，issue https://github.com/JetBrains/compose-multiplatform/issues/3904
* iOS：开发中
* Web：开发中