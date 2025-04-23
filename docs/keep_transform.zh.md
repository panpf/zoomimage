## 保持变换状态

翻译：[English](keep_transform.md)

ZoomImage 在以下属性改变时会直接重置变换状态：

* rotation
* contentScale
* alignment
* readMode
* scalesCalculator
* limitOffsetWithinBaseVisibleRect
* containerWhitespaceMultiple
* containerWhitespace

### containerSize 改变

仅当 containerSize 属性改变时，ZoomImage 会根据新的 containerSize 和当前变换状态
恢复变换状态，确保用户当前可见的中心点始终位于屏幕中央，这样当你在桌面平台上改变窗口大小时，ZoomImage
就会自动适应新的窗口大小并保持可见中心不变，可见范围也会随之变化

> [!TIP]
> 当 containerSize 属性改变的同时有上述其它属性也发生了改变，ZoomImage 会直接重置变换状态

### contentSize 或 contentOriginSize 改变

默认情况下仅当 contentSize 或 contentOriginSize 属性改变时，ZoomImage 会直接重置变换状态

但在你将 `keepTransformWhenSameAspectRatioContentSizeChanged` 属性设置为 true 后，ZoomImage
会恢复变换状态，确保用户可见的范围始终保持不变。这样在先显示缩略图，然后用户操作了变换，最后再显示原图的场景下就不会重置变换状态了

`keepTransformWhenSameAspectRatioContentSizeChanged`
属性仅在切换相同宽高比的图像时才会工作，因此此功能仅适用于缩略图和原图切换时的场景，所以它默认没有开启

> [!TIP]
> 当 contentSize 或 contentOriginSize 属性改变的同时有上述其它属性也发生了改变，ZoomImage 会直接重置变换状态