## 缩放

翻译：[English](zoom.md)

> [!TIP]
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

### 特点

* 支持[单指双击上下滑动缩放](#单指缩放)
  、双指捏合缩放、[双击循环缩放](#双击缩放)以及通过 [scale()](#scale) 方法缩放到指定的倍数
* [支持橡皮筋效果](#橡皮筋效果).
  手势连续缩放时（单指/双指缩放）超过最大或最小范围时可以继续缩放，但有阻尼效果，松手后会回弹到最大或最小缩放倍数
* [动态缩放范围](#minscale-mediumscale-maxscale). 默认根据
  containerSize、contentSize、contentOriginSize 动态计算 mediumScale 和 maxScale
* [支持动画](#动画). scale() 方法和双击缩放均支持动画
* [支持全部的 ContentScale, 和 Alignment](#contentscale-alignment)，ZoomImageView 也支持 ContentScale
  和 Alignment
* [支持禁用手势](#禁用手势). 支持分别禁用双击缩放、双指缩放、单指缩放、拖动等手势
* 仅 containerSize 改变时（桌面平台上拖动调整窗口大小），ZoomImage 会保持缩放比例和 content 可见中心点不变
* 页面重建时（屏幕旋转、App 在后台被回收）会重置缩放和偏移
* [开放 Modifier.zoom() 函数](#modifierzoom)，可以应用在任意组件上
* [支持读取相关信息](#可访问属性). 支持读取当前缩放倍数、最小/中间/最大缩放倍数等缩放相关信息

### ContentScale, Alignment

ZoomImage 支持所有的 [ContentScale] 和 [Alignment]，并且得益于 compose 版本和 view 版本使用的是同一套算法，view
版本的组件在支持 [ScaleType] 之外也支持 [ContentScale] 和 [Alignment]

示例：

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)

sketchZoomImageView.zoomable.contentScale = ContentScaleCompat.None
sketchZoomImageView.zoomable.alignment = AlignmentCompat.BottomEnd
```

### minScale, mediumScale, maxScale

ZoomImage 在缩放的过程中始终受 minScale、mediumScale、maxScale 三个参数的控制：

* `minScale`：最小缩放倍数，用于限制 ZoomImage 在缩放过程中的最小值，计算公式为：
    ```kotlin
    ContentScale.computeScaleFactor(srcSize, dstSize).scaleX
    ```
* `mediumScale`：中间缩放倍数，专门用于双击缩放，取值受 scalesCalculator 参数控制
* `maxScale`：最大缩放倍数，用于限制 ZoomImage 在缩放过程中的最大值，取值受 scalesCalculator 参数控制

#### ScalesCalculator

[ScalesCalculator] 专门用来计算 mediumScale 和 maxScale，ZoomImage 有两个内置的 [ScalesCalculator]：

> [!TIP]
> * minMediumScale = `minScale * multiple`
> * fillContainerScale = `max(containerSize.width / contentSize.width.toFloat(),
    containerSize.height / contentSize.height.toFloat())`
> * contentOriginScale = `max(contentOriginSize.width / contentSize.width.toFloat(),
    contentOriginSize.height / contentSize.height.toFloat())`
> * initialScale 通常由 ReadMode 计算
> * multiple 默认值为 3f

* [ScalesCalculator].Dynamic：
    * mediumScale 计算规则如下：
        * 如果 contentScale 是 FillBounds，则始终是 minMediumScale
        * 如果 initialScale 大于 minScale 则始终是 initialScale
        * 否则在 minMediumScale、fillContainerScale、contentOriginScale 当中取最大的
    * maxScale 计算规则如下：
        * 如果 contentScale 是 FillBounds，则始终是 `mediumScale * multiple`
        * 否则在 `mediumScale * multiple`, contentOriginScale 当中取最大的
* [ScalesCalculator].Fixed：
    * mediumScale 计算规则如下：
        * 如果 contentScale 是 FillBounds，则始终是 minMediumScale
        * 如果 initialScale 大于 minScale 则始终是 initialScale
        * 否则始终是 minMediumScale
    * maxScale 始终是 `mediumScale * multiple`

scalesCalculator 默认值为 [ScalesCalculator].Dynamic，你可以将它修改为 Fixed 或自定义的实现

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    zoomState.zoomable.scalesCalculator = ScalesCalculator.Fixed
    // 或
    zoomState.zoomable.scalesCalculator = MyScalesCalculator()
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### 双击缩放

双击图像时 ZoomImage 会缩放到下一个缩放倍数，默认总是在 minScale 和 mediumScale 之间循环

如果你想在 minScale、mediumScale 和 maxScale 之间循环，可以修改 threeStepScale 属性为 true

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    zoomState.zoomable.threeStepScale = true
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

双击缩放调用的是 ZoomImage 的 `switchScale()` 方法，你也可以在需要的时候调用 `switchScale()`
方法来切换缩放倍数，它有两个参数：

* `centroidContentPoint: IntOffset = contentVisibleRect.center`：content 上的缩放中心点，原点是
  content 的左上角，默认是 content 当前可见区域的中心
* `animated: Boolean = false`：是否使用动画，默认为 false

> [!TIP]
> 注意：centroidContentPoint 一定要是 content 上的点

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)

val coroutineScope = rememberCoroutineScope()
Button(
    onClick = {
        coroutineScope.launch {
            zoomState.zoomable.switchScale(animated = true)
        }
    }
) {
    Text(text = "switch scale")
}
```

你还可以调用 `getNextStepScale()` 方法来获取下一个缩放倍数

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

zoomState.zoomable.getNextStepScale()
```

### 单指缩放

ZoomImage 支持单指缩放图像，双击后按住屏幕上下滑动即可缩放图像。
此功能默认开启，你可以通过 [禁用手势](#禁用手势) 给你关闭它

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // 关闭单指缩放手势
    zoomState.zoomable.disabledGestureTypes = GestureType.ONE_FINGER_SCALE
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### scale()

ZoomImage 提供了 scale() 方法用来缩放图像到指定的倍数，它有三个参数：

* `targetScale: Float`: 目标缩放倍数
* `centroidContentPoint: IntOffset = contentVisibleRect.center`: content 上的缩放中心点，原点是
  content 的左上角， 默认是 content 当前可见区域的中心
* `animated: Boolean = false`: 是否使用动画，默认为 false

> [!TIP]
> 注意：centroidContentPoint 一定要是 content 上的点

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)

val coroutineScope = rememberCoroutineScope()
Button(
    onClick = {
        coroutineScope.launch {
            val targetScale = zoomState.zoomable.transform.scaleX + 0.2f
            zoomState.zoomable.scale(targetScale = targetScale, animated = true)
        }
    }
) {
    Text(text = "scale + 0.2")
}

Button(
    onClick = {
        coroutineScope.launch {
            val targetScale = zoomState.zoomable.transform.scaleX - 0.2f
            zoomState.zoomable.scale(targetScale = targetScale, animated = true)
        }
    }
) {
    Text(text = "scale - 0.2")
}
```

### 橡皮筋效果

ZoomImage 会将缩放倍数限制在 `minScale` 和 `maxScale`之间，单指或双指缩放时如果超过了这个范围依然可以继续缩放，
但会有类似橡皮筋的阻尼效果，松手后会回弹到 `minScale`或 `maxScale`
，此功能默认开启，你可通过 `rubberBandScale` 属性关闭它

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    zoomState.zoomable.rubberBandScale = false
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### 动画

ZoomImage 提供了 `animationSpec` 参数用来修改缩放动画的时长、Easing 以及初始速度

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    zoomState.animationSpec = ZoomAnimationSpec(
        durationMillis = 500,
        easing = LinearOutSlowInEasing,
        initialVelocity = 10f
    )

    // 或者在默认值的基础上修改部分参数
    zoomState.animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = 500)
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### 禁用手势

ZoomImage 支持双击缩放、双指缩放、单指缩放、拖动等手势，这些手势除单指缩放外默认都是开启的，你可以通过
`disabledGestureTypes` 属性来禁用它们

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // 关闭所有缩放手势，只保留拖动手势
    zoomState.zoomable.disabledGestureTypes =
        GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE or GestureType.DOUBLE_TAP_SCALE
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### Modifier.zoom()

Compose 版本的 ZoomImage 组件依赖 `Modifier.zoom()` 实现缩放，它还可以用在任意 Compose 组件上

示例：

```kotlin
val logger = rememberZoomImageLogger()
val zoomState = rememberZoomableState(logger)
val text = remember {
    """
    六王毕，四海一，蜀山兀，阿房出。覆压三百余里，隔离天日。骊山北构而西折，直走咸阳。二川溶溶，流入宫墙。五步一楼，十步一阁；廊腰缦回，檐牙高啄；各抱地势，钩心斗角。盘盘焉，囷囷焉，蜂房水涡，矗不知其几千万落。长桥卧波，未云何龙？复道行空，不霁何虹？高低冥迷，不知西东。歌台暖响，春光融融；舞殿冷袖，风雨凄凄。一日之内，一宫之间，而气候不齐。　　

    妃嫔媵嫱，王子皇孙，辞楼下殿，辇来于秦。朝歌夜弦，为秦宫人。明星荧荧，开妆镜也；绿云扰扰，梳晓鬟也；渭流涨腻，弃脂水也；烟斜雾横，焚椒兰也。雷霆乍惊，宫车过也；辘辘远听，杳不知其所之也。一肌一容，尽态极妍，缦立远视，而望幸焉。有不见者三十六年。燕赵之收藏，韩魏之经营，齐楚之精英，几世几年，剽掠其人，倚叠如山。一旦不能有，输来其间。鼎铛玉石，金块珠砾，弃掷逦迤，秦人视之，亦不甚惜。
　  
    嗟乎！一人之心，千万人之心也。秦爱纷奢，人亦念其家。奈何取之尽锱铢，用之如泥沙？使负栋之柱，多于南亩之农夫；架梁之椽，多于机上之工女；钉头磷磷，多于在庾之粟粒；瓦缝参差，多于周身之帛缕；直栏横槛，多于九土之城郭；管弦呕哑，多于市人之言语。使天下之人，不敢言而敢怒。独夫之心，日益骄固。戍卒叫，函谷举，楚人一炬，可怜焦土！　　
    
    呜呼！灭六国者六国也，非秦也；族秦者秦也，非天下也。嗟乎！使六国各爱其人，则足以拒秦；使秦复爱六国之人，则递三世可至万世而为君，谁得而族灭也？秦人不暇自哀，而后人哀之；后人哀之而不鉴之，亦使后人而复哀后人也。

                                ——唐代·杜牧《阿房宫赋》
""".trimIndent()
}
Box(
    modifier = Modifier
        .fillMaxSize()
        .zoom(logger, zoomState)
) {
    Text(
        text = text,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(10.dp)
    )
}
```

### 可访问属性

```kotlin
// compose
val zoomState: ZoomState by rememberZoomState()
SketchZoomAsyncImage(zoomState = zoomState)
val zoomable: ZoomableState = zoomState.zoomable

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
```

> [!TIP]
> * 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀

* `zoomable.transform.scale: ScaleFactor`: 当前缩放比例（baseTransform.scale * userTransform.scale）
* `zoomable.baseTransform.scale: ScaleFactor`: 当前基础缩放比例，受 contentScale 参数影响
* `zoomable.userTransform.scale: ScaleFactor`: 当前用户缩放比例，受 scale()、locate() 以及用户手势缩放、双击等操作影响
* `zoomable.minScale: Float`: 最小缩放比例，用于缩放时限制最小缩放比例以及双击缩放时的一个循环缩放比例
* `zoomable.mediumScale: Float`: 中间缩放比例，用于双击缩放时的一个循环缩放比例
* `zoomable.maxScale: Float`: 最大缩放比例，用于缩放时限制最大缩放比例以及双击缩放时的一个循环缩放比例

#### 监听属性变化

* compose 版本的相关属性是用 State 包装的，在 Composable 函数中直接读取它即可实现监听
* view 的相关属性是用 StateFlow 包装，调用其 collect 函数即可实现监听

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[ScalesCalculator]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ScalesCalculator.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment

[ScaleType]: https://developer.android.com/reference/android/widget/ImageView.ScaleType