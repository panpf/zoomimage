package com.github.panpf.zoomimage.compose.test

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.zoomimage.Centroid
import com.github.panpf.zoomimage.internal.Translation
import com.github.panpf.zoomimage.internal.computeContainerCentroidByTouchPosition
import com.github.panpf.zoomimage.internal.computeContainerVisibleRect
import com.github.panpf.zoomimage.internal.computeContentInContainerRect
import com.github.panpf.zoomimage.internal.computeContentInContainerVisibleRect
import com.github.panpf.zoomimage.internal.computeContentVisibleRect
import com.github.panpf.zoomimage.internal.computeScaleTargetTranslation
//import com.github.panpf.zoomimage.internal.computeScrollEdge
import com.github.panpf.zoomimage.internal.computeSupportTranslationBounds
import com.github.panpf.zoomimage.internal.containerCentroidToContentCentroid
import com.github.panpf.zoomimage.internal.contentCentroidToContainerCentroid
import com.github.panpf.zoomimage.internal.name
import com.github.panpf.zoomimage.internal.toShortString
import com.github.panpf.zoomimage.toShortString
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeZoomUtilsTest {

    data class Item(
        val contentScale: ContentScale,
        val contentAlignment: Alignment,
        val expected: Rect
    ) {
        fun getMessage(containerSize: Size, contentSize: Size): String {
            return "Item(containerSize=${containerSize.toShortString()}, contentSize=${contentSize.toShortString()}, contentScale=${contentScale.name}, contentAlignment=${contentAlignment.name})"
        }

        fun getMessage(containerSize: Size, contentSize: Size, scale: Float): String {
            return "Item(containerSize=${containerSize.toShortString()}, contentSize=${contentSize.toShortString()}, contentScale=${contentScale.name}, contentAlignment=${contentAlignment.name}, scale=${scale})"
        }
    }

    private fun List<Item>.printlnExpectedMessage(computeExpected: (Item) -> Rect): List<Item> {
        this.map {
            val expected = computeExpected(it)
            "Item(ContentScale.${it.contentScale.name}, Alignment.${it.contentAlignment.name}, Rect(${expected.run { "${left}f,${top}f,${right}f,${bottom}f" }}))"
        }.apply {
            Assert.fail(joinToString(separator = ", \n", postfix = ","))
        }
        return this
    }

    data class Item2(
        val contentScale: ContentScale,
        val contentAlignment: Alignment,
        val expected: Centroid
    ) {
        fun getMessage(
            containerSize: Size,
            contentSize: Size,
            containerCentroid: Centroid
        ): String {
            return "Item2(containerSize=${containerSize.toShortString()}, contentSize=${contentSize.toShortString()}, contentScale=${contentScale.name}, contentAlignment=${contentAlignment.name}, containerCentroid=${containerCentroid.toShortString()})"
        }
    }

    private fun List<Item2>.printlnExpectedMessage2(computeExpected: (Item2) -> Centroid): List<Item2> {
        this.map {
            val expected = computeExpected(it)
            "Item2(ContentScale.${it.contentScale.name}, Alignment.${it.contentAlignment.name}, Centroid(${expected.run { "${x}f,${y}f" }}))"
        }.apply {
            Assert.fail(joinToString(separator = ", \n", postfix = ","))
        }
        return this
    }

    data class Item3(
        val translation: Translation,
        val expected: Rect
    ) {
        fun getMessage(
            containerSize: Size,
            contentSize: Size,
            contentScale: ContentScale,
            contentAlignment: Alignment,
            scale: Float
        ): String {
            return "Item3(containerSize=${containerSize.toShortString()}, contentSize=${contentSize.toShortString()}, contentScale=${contentScale.name}, contentAlignment=${contentAlignment.name}, scale=$scale, translation=${translation.toShortString()})"
        }
    }

    private fun List<Item3>.printlnExpectedMessage3(computeExpected: (Item3) -> Rect): List<Item3> {
        this.map {
            val visibleRect = computeExpected(it)
            "Translation(${it.translation.translationX}f, ${it.translation.translationY}f) to Rect(${visibleRect.left}f, ${visibleRect.top}f, ${visibleRect.right}f, ${visibleRect.bottom}f)"
        }.apply {
            Assert.fail(joinToString(separator = ", \n"))
        }
        return this
    }

    @Test
    fun testComputeContentInContainerRect() {
        val containerSize = Size(1000f, 1000f)

        var contentSize = Size(800f, 400f)
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(100f, 0f, 900f, 400f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(200f, 0f, 1000f, 400f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0f, 300f, 800f, 700f)),
            Item(ContentScale.None, Alignment.Center, Rect(100f, 300f, 900f, 700f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(200f, 300f, 1000f, 700f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0f, 600f, 800f, 1000f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(100f, 600f, 900f, 1000f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(200f, 600f, 1000f, 1000f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(100f, 0f, 900f, 400f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(200f, 0f, 1000f, 400f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0f, 300f, 800f, 700f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(100f, 300f, 900f, 700f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(200f, 300f, 1000f, 700f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0f, 600f, 800f, 1000f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(100f, 600f, 900f, 1000f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(200f, 600f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0f, 0f, 1000f, 500f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(0f, 0f, 1000f, 500f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(0f, 0f, 1000f, 500f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0f, 250f, 1000f, 750f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(0f, 250f, 1000f, 750f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(0f, 250f, 1000f, 750f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0f, 500f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(0f, 500f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(0f, 500f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0f, 0f, 1000f, 500f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0f, 0f, 1000f, 500f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0f, 0f, 1000f, 500f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0f, 250f, 1000f, 750f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0f, 250f, 1000f, 750f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0f, 250f, 1000f, 750f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0f, 500f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0f, 500f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0f, 500f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeContentInContainerRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize),
                it.expected,
                computeContentInContainerRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                )
            )
        }

        contentSize = Size(400f, 800f)
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(300f, 0f, 700f, 800f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(600f, 0f, 1000f, 800f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0f, 100f, 400f, 900f)),
            Item(ContentScale.None, Alignment.Center, Rect(300f, 100f, 700f, 900f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(600f, 100f, 1000f, 900f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0f, 200f, 400f, 1000f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(300f, 200f, 700f, 1000f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(600f, 200f, 1000f, 1000f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(300f, 0f, 700f, 800f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(600f, 0f, 1000f, 800f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0f, 100f, 400f, 900f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(300f, 100f, 700f, 900f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(600f, 100f, 1000f, 900f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0f, 200f, 400f, 1000f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(300f, 200f, 700f, 1000f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(600f, 200f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0f, 0f, 500f, 1000f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(250f, 0f, 750f, 1000f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(500f, 0f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0f, 0f, 500f, 1000f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(250f, 0f, 750f, 1000f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(500f, 0f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0f, 0f, 500f, 1000f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(250f, 0f, 750f, 1000f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(500f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0f, 0f, 500f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(250f, 0f, 750f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(500f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0f, 0f, 500f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(250f, 0f, 750f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(500f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0f, 0f, 500f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(250f, 0f, 750f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(500f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeContentInContainerRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize),
                it.expected,
                computeContentInContainerRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                )
            )
        }

        contentSize = Size(1600f, 1200f)
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0f, 0f, 1000f, 750f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(0f, 0f, 1000f, 750f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(0f, 0f, 1000f, 750f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0f, 125f, 1000f, 875f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(0f, 125f, 1000f, 875f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(0f, 125f, 1000f, 875f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0f, 250f, 1000f, 1000f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(0f, 250f, 1000f, 1000f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(0f, 250f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0f, 0f, 1000f, 750f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(0f, 0f, 1000f, 750f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(0f, 0f, 1000f, 750f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0f, 125f, 1000f, 875f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(0f, 125f, 1000f, 875f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(0f, 125f, 1000f, 875f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0f, 250f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(0f, 250f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(0f, 250f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0f, 0f, 1000f, 750f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0f, 0f, 1000f, 750f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0f, 0f, 1000f, 750f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0f, 125f, 1000f, 875f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0f, 125f, 1000f, 875f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0f, 125f, 1000f, 875f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0f, 250f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0f, 250f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0f, 250f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeContentInContainerRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize),
                it.expected,
                computeContentInContainerRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                )
            )
        }

        contentSize = Size(1200f, 1600f)
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0f, 0f, 750f, 1000f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(125f, 0f, 875f, 1000f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(250f, 0f, 1000f, 1000f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0f, 0f, 750f, 1000f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(125f, 0f, 875f, 1000f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(250f, 0f, 1000f, 1000f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0f, 0f, 750f, 1000f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(125f, 0f, 875f, 1000f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(250f, 0f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0f, 0f, 750f, 1000f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(125f, 0f, 875f, 1000f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(250f, 0f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0f, 0f, 750f, 1000f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(125f, 0f, 875f, 1000f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(250f, 0f, 1000f, 1000f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0f, 0f, 750f, 1000f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(125f, 0f, 875f, 1000f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(250f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0f, 0f, 750f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(125f, 0f, 875f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(250f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0f, 0f, 750f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(125f, 0f, 875f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(250f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0f, 0f, 750f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(125f, 0f, 875f, 1000f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(250f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(0f, 0f, 1000f, 1000f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeContentInContainerRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize),
                it.expected,
                computeContentInContainerRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                )
            )
        }
    }

    @Test
    fun testComputeContentInContainerVisibleRect() {
        val containerSize = Size(1000f, 1000f)

        var contentSize = Size(800f, 400f)
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.None, Alignment.Center, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(200f, 0f, 600f, 400f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(400f, 0f, 800f, 400f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(200f, 0f, 600f, 400f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(400f, 0f, 800f, 400f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(200f, 0f, 600f, 400f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(400f, 0f, 800f, 400f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0f, 0f, 800f, 400f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(200f, 0f, 600f, 400f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(400f, 0f, 800f, 400f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(200f, 0f, 600f, 400f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(400f, 0f, 800f, 400f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(200f, 0f, 600f, 400f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(400f, 0f, 800f, 400f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeContentInContainerVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize),
                it.expected,
                computeContentInContainerVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                )
            )
        }

        contentSize = Size(400f, 800f)
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.None, Alignment.Center, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0f, 200f, 400f, 600f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0f, 200f, 400f, 600f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0f, 200f, 400f, 600f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0f, 400f, 400f, 800f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0f, 400f, 400f, 800f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0f, 400f, 400f, 800f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0f, 0f, 400f, 800f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(0f, 0f, 400f, 400f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0f, 200f, 400f, 600f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(0f, 200f, 400f, 600f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(0f, 200f, 400f, 600f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0f, 400f, 400f, 800f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(0f, 400f, 400f, 800f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(0f, 400f, 400f, 800f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeContentInContainerVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize),
                it.expected,
                computeContentInContainerVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                )
            )
        }

        contentSize = Size(1600f, 1200f)
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(300f, 0f, 1300f, 1000f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(600f, 0f, 1600f, 1000f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0f, 100f, 1000f, 1100f)),
            Item(ContentScale.None, Alignment.Center, Rect(300f, 100f, 1300f, 1100f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(600f, 100f, 1600f, 1100f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0f, 200f, 1000f, 1200f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(300f, 200f, 1300f, 1200f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(600f, 200f, 1600f, 1200f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(199.99995f, 0f, 1400f, 1200f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(399.9999f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(199.99995f, 0f, 1400f, 1200f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(399.9999f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0f, 0f, 1200f, 1200f)),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomCenter,
                Rect(199.99995f, 0f, 1400f, 1200f)
            ),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(399.9999f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0f, 0f, 1600f, 1200f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(199.99995f, 0f, 1400f, 1200f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(399.9999f, 0f, 1600f, 1200f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(199.99995f, 0f, 1400f, 1200f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(399.9999f, 0f, 1600f, 1200f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(199.99995f, 0f, 1400f, 1200f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(399.9999f, 0f, 1600f, 1200f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeContentInContainerVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize),
                it.expected,
                computeContentInContainerVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                )
            )
        }

        contentSize = Size(1200f, 1600f)
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0f, 0f, 1000f, 1000f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(100f, 0f, 1100f, 1000f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(200f, 0f, 1200f, 1000f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0f, 300f, 1000f, 1300f)),
            Item(ContentScale.None, Alignment.Center, Rect(100f, 300f, 1100f, 1300f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(200f, 300f, 1200f, 1300f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0f, 600f, 1000f, 1600f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(100f, 600f, 1100f, 1600f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(200f, 600f, 1200f, 1600f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0f, 199.99995f, 1200f, 1400f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0f, 199.99995f, 1200f, 1400f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0f, 199.99995f, 1200f, 1400f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0f, 399.9999f, 1200f, 1600f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0f, 399.9999f, 1200f, 1600f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0f, 399.9999f, 1200f, 1600f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0f, 0f, 1200f, 1600f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(0f, 0f, 1200f, 1200f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0f, 199.99995f, 1200f, 1400f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(0f, 199.99995f, 1200f, 1400f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(0f, 199.99995f, 1200f, 1400f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0f, 399.9999f, 1200f, 1600f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(0f, 399.9999f, 1200f, 1600f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(0f, 399.9999f, 1200f, 1600f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeContentInContainerVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize),
                it.expected,
                computeContentInContainerVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                )
            )
        }
    }

    @Test
    fun testComputeScaleTargetTranslation() {
        val containerSize = Size(1000f, 2000f)

        var scale = 1f
        listOf(
            Centroid(0.25f, 0.25f) to Translation(0f, 0f),
            Centroid(0.75f, 0.25f) to Translation(-250f, 0f),
            Centroid(0.5f, 0.5f) to Translation(-0f, -0f),
            Centroid(0.25f, 0.75f) to Translation(0f, -500f),
            Centroid(0.75f, 0.75f) to Translation(-250f, -500f),
        ).forEach { (containerCentroid, expected) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, containerCentroid=$containerCentroid",
                expected,
                computeScaleTargetTranslation(containerSize, scale, containerCentroid)
            )
        }

        scale = 2f
        listOf(
            Centroid(0.25f, 0.25f) to Translation(-0f, -0f),
            Centroid(0.75f, 0.25f) to Translation(-1000f, -0f),
            Centroid(0.5f, 0.5f) to Translation(-500f, -1000f),
            Centroid(0.25f, 0.75f) to Translation(-0f, -2000f),
            Centroid(0.75f, 0.75f) to Translation(-1000f, -2000f),
        ).forEach { (containerCentroid, expected) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, containerCentroid=$containerCentroid",
                expected,
                computeScaleTargetTranslation(containerSize, scale, containerCentroid)
            )
        }
    }

    @Test
    fun testComputeSupportTranslationBounds() {
        val containerSize = Size(1000f, 1000f)

        var contentSize = Size(800f, 400f)
        var scale = 1f
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeTranslationBounds(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                    scale = scale,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, scale),
                it.expected,
                computeSupportTranslationBounds(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                    supportScale = scale,
                )
            )
        }

        contentSize = Size(400f, 800f)
        scale = 1f
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeTranslationBounds(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                    scale = scale,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, scale),
                it.expected,
                computeSupportTranslationBounds(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                    supportScale = scale,
                )
            )
        }

        contentSize = Size(1600f, 1200f)
        scale = 1f
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeTranslationBounds(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                    scale = scale,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, scale),
                it.expected,
                computeSupportTranslationBounds(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                    supportScale = scale,
                )
            )
        }

        contentSize = Size(1200f, 1600f)
        scale = 1f
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillHeight, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.FillBounds, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(0.0f, 0.0f, 0.0f, 0.0f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeTranslationBounds(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                    scale = scale,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, scale),
                it.expected,
                computeSupportTranslationBounds(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                    supportScale = scale,
                )
            )
        }

        contentSize = Size(800f, 400f)
        scale = 2f
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(-600.0f, 0.0f, -0.0f, 0.0f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(-800.0f, 0.0f, -200.0f, 0.0f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(-1000.0f, 0.0f, -400.0f, 0.0f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(-600.0f, -500.0f, -0.0f, -500.0f)),
            Item(ContentScale.None, Alignment.Center, Rect(-800.0f, -500.0f, -200.0f, -500.0f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(-1000.0f, -500.0f, -400.0f, -500.0f)),
            Item(
                ContentScale.None,
                Alignment.BottomStart,
                Rect(-600.0f, -1000.0f, -0.0f, -1000.0f)
            ),
            Item(
                ContentScale.None,
                Alignment.BottomCenter,
                Rect(-800.0f, -1000.0f, -200.0f, -1000.0f)
            ),
            Item(
                ContentScale.None,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -400.0f, -1000.0f)
            ),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(-600.0f, 0.0f, -0.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(-800.0f, 0.0f, -200.0f, 0.0f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(-1000.0f, 0.0f, -400.0f, 0.0f)),
            Item(
                ContentScale.Inside,
                Alignment.CenterStart,
                Rect(-600.0f, -500.0f, -0.0f, -500.0f)
            ),
            Item(ContentScale.Inside, Alignment.Center, Rect(-800.0f, -500.0f, -200.0f, -500.0f)),
            Item(
                ContentScale.Inside,
                Alignment.CenterEnd,
                Rect(-1000.0f, -500.0f, -400.0f, -500.0f)
            ),
            Item(
                ContentScale.Inside,
                Alignment.BottomStart,
                Rect(-600.0f, -1000.0f, -0.0f, -1000.0f)
            ),
            Item(
                ContentScale.Inside,
                Alignment.BottomCenter,
                Rect(-800.0f, -1000.0f, -200.0f, -1000.0f)
            ),
            Item(
                ContentScale.Inside,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -400.0f, -1000.0f)
            ),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(-1000.0f, 0.0f, -0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(-1000.0f, 0.0f, -0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(-1000.0f, 0.0f, -0.0f, 0.0f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(-1000.0f, -500.0f, -0.0f, -500.0f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(-1000.0f, -500.0f, -0.0f, -500.0f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(-1000.0f, -500.0f, -0.0f, -500.0f)),
            Item(
                ContentScale.Fit,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -1000.0f)
            ),
            Item(
                ContentScale.Fit,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -1000.0f)
            ),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(-1000.0f, -1000.0f, -0.0f, -1000.0f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(-1000.0f, 0.0f, -0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopCenter, Rect(-1000.0f, 0.0f, -0.0f, 0.0f)),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(-1000.0f, 0.0f, -0.0f, 0.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.CenterStart,
                Rect(-1000.0f, -500.0f, -0.0f, -500.0f)
            ),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(-1000.0f, -500.0f, -0.0f, -500.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.CenterEnd,
                Rect(-1000.0f, -500.0f, -0.0f, -500.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -1000.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -1000.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -1000.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.TopStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.TopCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillHeight,
                Alignment.CenterStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillHeight,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.TopStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.TopCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillBounds,
                Alignment.CenterStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillBounds,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeTranslationBounds(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                    scale = scale,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, scale),
                it.expected,
                computeSupportTranslationBounds(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                    supportScale = scale,
                )
            )
        }

        contentSize = Size(400f, 800f)
        scale = 2f
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(0.0f, -600.0f, 0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(-500.0f, -600.0f, -500.0f, -0.0f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(-1000.0f, -600.0f, -1000.0f, -0.0f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(0.0f, -800.0f, 0.0f, -200.0f)),
            Item(ContentScale.None, Alignment.Center, Rect(-500.0f, -800.0f, -500.0f, -200.0f)),
            Item(
                ContentScale.None,
                Alignment.CenterEnd,
                Rect(-1000.0f, -800.0f, -1000.0f, -200.0f)
            ),
            Item(ContentScale.None, Alignment.BottomStart, Rect(0.0f, -1000.0f, 0.0f, -400.0f)),
            Item(
                ContentScale.None,
                Alignment.BottomCenter,
                Rect(-500.0f, -1000.0f, -500.0f, -400.0f)
            ),
            Item(
                ContentScale.None,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -1000.0f, -400.0f)
            ),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(0.0f, -600.0f, 0.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(-500.0f, -600.0f, -500.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(-1000.0f, -600.0f, -1000.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(0.0f, -800.0f, 0.0f, -200.0f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(-500.0f, -800.0f, -500.0f, -200.0f)),
            Item(
                ContentScale.Inside,
                Alignment.CenterEnd,
                Rect(-1000.0f, -800.0f, -1000.0f, -200.0f)
            ),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(0.0f, -1000.0f, 0.0f, -400.0f)),
            Item(
                ContentScale.Inside,
                Alignment.BottomCenter,
                Rect(-500.0f, -1000.0f, -500.0f, -400.0f)
            ),
            Item(
                ContentScale.Inside,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -1000.0f, -400.0f)
            ),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(0.0f, -1000.0f, 0.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(-500.0f, -1000.0f, -500.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -1000.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(0.0f, -1000.0f, 0.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(-500.0f, -1000.0f, -500.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(-1000.0f, -1000.0f, -1000.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(0.0f, -1000.0f, 0.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(-500.0f, -1000.0f, -500.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(-1000.0f, -1000.0f, -1000.0f, -0.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.TopStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.TopCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.CenterStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillHeight, Alignment.TopStart, Rect(0.0f, -1000.0f, 0.0f, -0.0f)),
            Item(
                ContentScale.FillHeight,
                Alignment.TopCenter,
                Rect(-500.0f, -1000.0f, -500.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.TopEnd,
                Rect(-1000.0f, -1000.0f, -1000.0f, -0.0f)
            ),
            Item(ContentScale.FillHeight, Alignment.CenterStart, Rect(0.0f, -1000.0f, 0.0f, -0.0f)),
            Item(
                ContentScale.FillHeight,
                Alignment.Center,
                Rect(-500.0f, -1000.0f, -500.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -1000.0f, -0.0f)
            ),
            Item(ContentScale.FillHeight, Alignment.BottomStart, Rect(0.0f, -1000.0f, 0.0f, -0.0f)),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomCenter,
                Rect(-500.0f, -1000.0f, -500.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -1000.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.TopStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.TopCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillBounds,
                Alignment.CenterStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillBounds,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeTranslationBounds(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                    scale = scale,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, scale),
                it.expected,
                computeSupportTranslationBounds(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                    supportScale = scale,
                )
            )
        }

        contentSize = Size(1600f, 1200f)
        scale = 2f
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(-1000.0f, -500.0f, -0.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(-1000.0f, -500.0f, -0.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(-1000.0f, -500.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.Inside,
                Alignment.CenterStart,
                Rect(-1000.0f, -750.0f, -0.0f, -250.0f)
            ),
            Item(ContentScale.Inside, Alignment.Center, Rect(-1000.0f, -750.0f, -0.0f, -250.0f)),
            Item(ContentScale.Inside, Alignment.CenterEnd, Rect(-1000.0f, -750.0f, -0.0f, -250.0f)),
            Item(
                ContentScale.Inside,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -500.0f)
            ),
            Item(
                ContentScale.Inside,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -500.0f)
            ),
            Item(
                ContentScale.Inside,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -500.0f)
            ),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(-1000.0f, -500.0f, -0.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(-1000.0f, -500.0f, -0.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(-1000.0f, -500.0f, -0.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(-1000.0f, -750.0f, -0.0f, -250.0f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(-1000.0f, -750.0f, -0.0f, -250.0f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(-1000.0f, -750.0f, -0.0f, -250.0f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(-1000.0f, -1000.0f, -0.0f, -500.0f)),
            Item(
                ContentScale.Fit,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -500.0f)
            ),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(-1000.0f, -1000.0f, -0.0f, -500.0f)),
            Item(ContentScale.FillWidth, Alignment.TopStart, Rect(-1000.0f, -500.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.TopCenter,
                Rect(-1000.0f, -500.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(-1000.0f, -500.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.CenterStart,
                Rect(-1000.0f, -750.0f, -0.0f, -250.0f)
            ),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(-1000.0f, -750.0f, -0.0f, -250.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.CenterEnd,
                Rect(-1000.0f, -750.0f, -0.0f, -250.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -500.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -500.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -500.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.TopStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.TopCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillHeight, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillHeight,
                Alignment.CenterStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillHeight, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillHeight,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.TopStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.TopCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillBounds,
                Alignment.CenterStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillBounds,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeTranslationBounds(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                    scale = scale,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, scale),
                it.expected,
                computeSupportTranslationBounds(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                    supportScale = scale,
                )
            )
        }

        contentSize = Size(1200f, 1600f)
        scale = 2f
        listOf(
            Item(ContentScale.None, Alignment.TopStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.TopCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.CenterStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.CenterEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.BottomStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.BottomCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.None, Alignment.BottomEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.TopStart, Rect(-500.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.TopCenter, Rect(-750.0f, -1000.0f, -250.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -500.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.CenterStart, Rect(-500.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Inside, Alignment.Center, Rect(-750.0f, -1000.0f, -250.0f, -0.0f)),
            Item(
                ContentScale.Inside,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -500.0f, -0.0f)
            ),
            Item(ContentScale.Inside, Alignment.BottomStart, Rect(-500.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.Inside,
                Alignment.BottomCenter,
                Rect(-750.0f, -1000.0f, -250.0f, -0.0f)
            ),
            Item(
                ContentScale.Inside,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -500.0f, -0.0f)
            ),
            Item(ContentScale.Fit, Alignment.TopStart, Rect(-500.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.TopCenter, Rect(-750.0f, -1000.0f, -250.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -500.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.CenterStart, Rect(-500.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.Center, Rect(-750.0f, -1000.0f, -250.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.CenterEnd, Rect(-1000.0f, -1000.0f, -500.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.BottomStart, Rect(-500.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.BottomCenter, Rect(-750.0f, -1000.0f, -250.0f, -0.0f)),
            Item(ContentScale.Fit, Alignment.BottomEnd, Rect(-1000.0f, -1000.0f, -500.0f, -0.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.TopStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.TopCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillWidth, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.CenterStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillWidth, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillWidth,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillWidth,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.TopStart,
                Rect(-500.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.TopCenter,
                Rect(-750.0f, -1000.0f, -250.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.TopEnd,
                Rect(-1000.0f, -1000.0f, -500.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.CenterStart,
                Rect(-500.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.Center,
                Rect(-750.0f, -1000.0f, -250.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -500.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomStart,
                Rect(-500.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomCenter,
                Rect(-750.0f, -1000.0f, -250.0f, -0.0f)
            ),
            Item(
                ContentScale.FillHeight,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -500.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.TopStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.TopCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillBounds, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillBounds,
                Alignment.CenterStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.FillBounds, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(
                ContentScale.FillBounds,
                Alignment.CenterEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomStart,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomCenter,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(
                ContentScale.FillBounds,
                Alignment.BottomEnd,
                Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)
            ),
            Item(ContentScale.Crop, Alignment.TopStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.TopCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.TopEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.CenterStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.Center, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.CenterEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomStart, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomCenter, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
            Item(ContentScale.Crop, Alignment.BottomEnd, Rect(-1000.0f, -1000.0f, -0.0f, -0.0f)),
//        ).printlnExpectedMessage(
//            computeExpected =  {
//                computeTranslationBounds(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    contentAlignment = it.contentAlignment,
//                    scale = scale,
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, scale),
                it.expected,
                computeSupportTranslationBounds(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = it.contentScale,
                    contentAlignment = it.contentAlignment,
                    supportScale = scale,
                )
            )
        }
    }

    @Test
    fun testComputeContainerVisibleRect() {
        val containerSize = Size(1000f, 2000f)

        var scale = 1f
        listOf(
            Translation(0f, 0f) to Rect(0f, 0f, 1000f, 2000f),
            Translation(250f, 500f) to Rect(0f, 0f, 750f, 1500f),
            Translation(750f, 500f) to Rect(0f, 0f, 250f, 1500f),
            Translation(250f, 1500f) to Rect(0f, 0f, 750f, 500f),
            Translation(750f, 1500f) to Rect(0f, 0f, 250f, 500f),
            Translation(1000f, 2000f) to Rect(0f, 0f, 0f, 0f),
            Translation(-250f, -500f) to Rect(250f, 500f, 1000f, 2000f),
            Translation(-750f, -500f) to Rect(750f, 500f, 1000f, 2000f),
            Translation(-250f, -1500f) to Rect(250f, 1500f, 1000f, 2000f),
            Translation(-750f, -1500f) to Rect(750f, 1500f, 1000f, 2000f),
            Translation(-1000f, -2000f) to Rect(0f, 0f, 0f, 0f),
        ).forEach { (translation, expectedVisibleRect) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, translation=$translation",
                expectedVisibleRect,
                computeContainerVisibleRect(containerSize, scale, translation)
            )
        }

        scale = 2f
        listOf(
            Translation(0f, 0f) to Rect(0f, 0f, 500f, 1000f),
            Translation(250f, 500f) to Rect(0f, 0f, 375f, 750f),
            Translation(750f, 500f) to Rect(0f, 0f, 125f, 750f),
            Translation(250f, 1500f) to Rect(0f, 0f, 375f, 250f),
            Translation(750f, 1500f) to Rect(0f, 0f, 125f, 250f),
            Translation(1000f, 2000f) to Rect(0f, 0f, 0f, 0f),
            Translation(-250f, -500f) to Rect(125f, 250f, 625f, 1250f),
            Translation(-750f, -500f) to Rect(375f, 250f, 875f, 1250f),
            Translation(-250f, -1500f) to Rect(125f, 750f, 625f, 1750f),
            Translation(-750f, -1500f) to Rect(375f, 750f, 875f, 1750f),
            Translation(-1000f, -2000f) to Rect(500f, 1000f, 1000f, 2000f),
        ).forEach { (translation, expectedVisibleRect) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, translation=$translation",
                expectedVisibleRect,
                computeContainerVisibleRect(containerSize, scale, translation)
            )
        }
    }

    @Test
    fun testComputeContentVisibleRect() {
        var containerSize = Size(1000f, 2000f)
        var contentSize = Size(800f, 1200f)
        var contentScale = ContentScale.Fit
        var contentAlignment = Alignment.Center
        var scale = 1f
        listOf(
            Item3(Translation(0f, 0f), Rect(0f, 0f, 800f, 1200f)),
            Item3(Translation(250f, 500f), Rect(0f, 0f, 600f, 1000f)),
            Item3(Translation(750f, 500f), Rect(0f, 0f, 200f, 1000f)),
            Item3(Translation(250f, 1500f), Rect(0f, 0f, 600f, 200f)),
            Item3(Translation(750f, 1500f), Rect(0f, 0f, 200f, 200f)),
            Item3(Translation(1000f, 2000f), Rect(0f, 0f, 0f, 0f)),
            Item3(Translation(-250f, -500f), Rect(200f, 200f, 800f, 1200f)),
            Item3(Translation(-750f, -500f), Rect(600f, 200f, 800f, 1200f)),
            Item3(Translation(-250f, -1500f), Rect(200f, 1000f, 800f, 1200f)),
            Item3(Translation(-750f, -1500f), Rect(600f, 1000f, 800f, 1200f)),
            Item3(Translation(-1000f, -2000f), Rect(0f, 0f, 0f, 0f)),
//        ).printlnExpectedMessage3(
//            computeExpected = {
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    contentAlignment = contentAlignment,
//                    scale = scale,
//                    translation = it.translation
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, contentScale, contentAlignment, scale),
                it.expected,
                computeContentVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = contentScale,
                    contentAlignment = contentAlignment,
                    scale = scale,
                    translation = it.translation
                )
            )
        }

        containerSize = Size(1000f, 2000f)
        contentSize = Size(800f, 1200f)
        contentScale = ContentScale.Fit
        contentAlignment = Alignment.Center
        scale = 2f
        listOf(
            Item3(Translation(0f, 0f), Rect(0f, 0f, 400f, 600f)),
            Item3(Translation(250f, 500f), Rect(0f, 0f, 300f, 400f)),
            Item3(Translation(750f, 500f), Rect(0f, 0f, 100f, 400f)),
            Item3(Translation(250f, 1500f), Rect(0f, 0f, 0f, 0f)),
            Item3(Translation(750f, 1500f), Rect(0f, 0f, 0f, 0f)),
            Item3(Translation(1000f, 2000f), Rect(0f, 0f, 0f, 0f)),
            Item3(Translation(-250f, -500f), Rect(100f, 0f, 500f, 800f)),
            Item3(Translation(-750f, -500f), Rect(300f, 0f, 700f, 800f)),
            Item3(Translation(-250f, -1500f), Rect(100f, 400f, 500f, 1200f)),
            Item3(Translation(-750f, -1500f), Rect(300f, 400f, 700f, 1200f)),
            Item3(Translation(-1000f, -2000f), Rect(400f, 600f, 800f, 1200f)),
//        ).printlnExpectedMessage3(
//            computeExpected = {
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    contentAlignment = contentAlignment,
//                    scale = scale,
//                    translation = it.translation
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, contentScale, contentAlignment, scale),
                it.expected,
                computeContentVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = contentScale,
                    contentAlignment = contentAlignment,
                    scale = scale,
                    translation = it.translation
                )
            )
        }

        containerSize = Size(1000f, 2000f)
        contentSize = Size(800f, 1200f)
        contentScale = ContentScale.Inside
        contentAlignment = Alignment.Center
        scale = 1f
        listOf(
            Item3(Translation(0f, 0f), Rect(0f, 0f, 800f, 1200f)),
            Item3(Translation(250f, 500f), Rect(0f, 0f, 650f, 1100f)),
            Item3(Translation(750f, 500f), Rect(0f, 0f, 150f, 1100f)),
            Item3(Translation(250f, 1500f), Rect(0f, 0f, 650f, 100f)),
            Item3(Translation(750f, 1500f), Rect(0f, 0f, 150f, 100f)),
            Item3(Translation(1000f, 2000f), Rect(0f, 0f, 0f, 0f)),
            Item3(Translation(-250f, -500f), Rect(150f, 100f, 800f, 1200f)),
            Item3(Translation(-750f, -500f), Rect(650f, 100f, 800f, 1200f)),
            Item3(Translation(-250f, -1500f), Rect(150f, 1100f, 800f, 1200f)),
            Item3(Translation(-750f, -1500f), Rect(650f, 1100f, 800f, 1200f)),
            Item3(Translation(-1000f, -2000f), Rect(0f, 0f, 0f, 0f)),
//        ).printlnExpectedMessage3(
//            computeExpected = {
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    contentAlignment = contentAlignment,
//                    scale = scale,
//                    translation = it.translation
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, contentScale, contentAlignment, scale),
                it.expected,
                computeContentVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = contentScale,
                    contentAlignment = contentAlignment,
                    scale = scale,
                    translation = it.translation
                )
            )
        }

        containerSize = Size(1000f, 2000f)
        contentSize = Size(800f, 1200f)
        contentScale = ContentScale.Inside
        contentAlignment = Alignment.Center
        scale = 2f
        listOf(
            Item3(Translation(0f, 0f), Rect(0f, 0f, 400f, 600f)),
            Item3(Translation(250f, 500f), Rect(0f, 0f, 275f, 350f)),
            Item3(Translation(750f, 500f), Rect(0f, 0f, 25f, 350f)),
            Item3(Translation(250f, 1500f), Rect(0f, 0f, 0f, 0f)),
            Item3(Translation(750f, 1500f), Rect(0f, 0f, 0f, 0f)),
            Item3(Translation(1000f, 2000f), Rect(0f, 0f, 0f, 0f)),
            Item3(Translation(-250f, -500f), Rect(25f, 0f, 525f, 850f)),
            Item3(Translation(-750f, -500f), Rect(275f, 0f, 775f, 850f)),
            Item3(Translation(-250f, -1500f), Rect(25f, 350f, 525f, 1200f)),
            Item3(Translation(-750f, -1500f), Rect(275f, 350f, 775f, 1200f)),
            Item3(Translation(-1000f, -2000f), Rect(400f, 600f, 800f, 1200f)),
//        ).printlnExpectedMessage3(
//            computeExpected = {
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    contentAlignment = contentAlignment,
//                    scale = scale,
//                    translation = it.translation
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, contentScale, contentAlignment, scale),
                it.expected,
                computeContentVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = contentScale,
                    contentAlignment = contentAlignment,
                    scale = scale,
                    translation = it.translation
                )
            )
        }

        containerSize = Size(1000f, 2000f)
        contentSize = Size(800f, 1200f)
        contentScale = ContentScale.Crop
        contentAlignment = Alignment.Center
        scale = 1f
        listOf(
            Item3(Translation(0f, 0f), Rect(99.99998f, 0f, 700f, 1200f)),
            Item3(Translation(250f, 500f), Rect(99.99998f, 0f, 550f, 900f)),
            Item3(Translation(750f, 500f), Rect(99.99998f, 0f, 249.99997f, 900f)),
            Item3(Translation(250f, 1500f), Rect(99.99998f, 0f, 550f, 300f)),
            Item3(Translation(750f, 1500f), Rect(99.99998f, 0f, 249.99997f, 300f)),
            Item3(Translation(1000f, 2000f), Rect(0f, 0f, 0f, 0f)),
            Item3(Translation(-250f, -500f), Rect(249.99997f, 300f, 700f, 1200f)),
            Item3(Translation(-750f, -500f), Rect(550f, 300f, 700f, 1200f)),
            Item3(Translation(-250f, -1500f), Rect(249.99997f, 900f, 700f, 1200f)),
            Item3(Translation(-750f, -1500f), Rect(550f, 900f, 700f, 1200f)),
            Item3(Translation(-1000f, -2000f), Rect(0f, 0f, 0f, 0f)),
//        ).printlnExpectedMessage3(
//            computeExpected = {
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    contentAlignment = contentAlignment,
//                    scale = scale,
//                    translation = it.translation
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, contentScale, contentAlignment, scale),
                it.expected,
                computeContentVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = contentScale,
                    contentAlignment = contentAlignment,
                    scale = scale,
                    translation = it.translation
                )
            )
        }

        containerSize = Size(1000f, 2000f)
        contentSize = Size(800f, 1200f)
        contentScale = ContentScale.Crop
        contentAlignment = Alignment.Center
        scale = 2f
        listOf(
            Item3(Translation(0.0f, 0.0f), Rect(99.99998f, 0.0f, 399.99997f, 600.0f)),
            Item3(Translation(250.0f, 500.0f), Rect(99.99998f, 0.0f, 324.99997f, 450.0f)),
            Item3(Translation(750.0f, 500.0f), Rect(99.99998f, 0.0f, 174.99997f, 450.0f)),
            Item3(Translation(250.0f, 1500.0f), Rect(99.99998f, 0.0f, 324.99997f, 150.0f)),
            Item3(Translation(750.0f, 1500.0f), Rect(99.99998f, 0.0f, 174.99997f, 150.0f)),
            Item3(Translation(1000.0f, 2000.0f), Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item3(Translation(-250.0f, -500.0f), Rect(174.99997f, 150.0f, 474.99997f, 750.0f)),
            Item3(Translation(-750.0f, -500.0f), Rect(324.99997f, 150.0f, 625.0f, 750.0f)),
            Item3(Translation(-250.0f, -1500.0f), Rect(174.99997f, 450.0f, 474.99997f, 1050.0f)),
            Item3(Translation(-750.0f, -1500.0f), Rect(324.99997f, 450.0f, 625.0f, 1050.0f)),
            Item3(Translation(-1000.0f, -2000.0f), Rect(399.99997f, 600.0f, 700.0f, 1200.0f)),
//        ).printlnExpectedMessage3(
//            computeExpected = {
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    contentAlignment = contentAlignment,
//                    scale = scale,
//                    translation = it.translation
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, contentScale, contentAlignment, scale),
                it.expected,
                computeContentVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = contentScale,
                    contentAlignment = contentAlignment,
                    scale = scale,
                    translation = it.translation
                )
            )
        }

        containerSize = Size(1000f, 2000f)
        contentSize = Size(containerSize.width * 1.5f, containerSize.height * 1.3f)
        contentScale = ContentScale.None
        contentAlignment = Alignment.Center
        scale = 1f
        listOf(
            Item3(Translation(0.0f, 0.0f), Rect(250.0f, 300.0f, 1250.0f, 2300.0f)),
            Item3(Translation(250.0f, 500.0f), Rect(250.0f, 300.0f, 1000.0f, 1800.0f)),
            Item3(Translation(750.0f, 500.0f), Rect(250.0f, 300.0f, 500.0f, 1800.0f)),
            Item3(Translation(250.0f, 1500.0f), Rect(250.0f, 300.0f, 1000.0f, 800.0f)),
            Item3(Translation(750.0f, 1500.0f), Rect(250.0f, 300.0f, 500.0f, 800.0f)),
            Item3(Translation(1000.0f, 2000.0f), Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item3(Translation(-250.0f, -500.0f), Rect(500.0f, 800.0f, 1250.0f, 2300.0f)),
            Item3(Translation(-750.0f, -500.0f), Rect(1000.0f, 800.0f, 1250.0f, 2300.0f)),
            Item3(Translation(-250.0f, -1500.0f), Rect(500.0f, 1800.0f, 1250.0f, 2300.0f)),
            Item3(Translation(-750.0f, -1500.0f), Rect(1000.0f, 1800.0f, 1250.0f, 2300.0f)),
            Item3(Translation(-1000.0f, -2000.0f), Rect(0.0f, 0.0f, 0.0f, 0.0f)),
//        ).printlnExpectedMessage3(
//            computeExpected = {
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    contentAlignment = contentAlignment,
//                    scale = scale,
//                    translation = it.translation
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, contentScale, contentAlignment, scale),
                it.expected,
                computeContentVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = contentScale,
                    contentAlignment = contentAlignment,
                    scale = scale,
                    translation = it.translation
                )
            )
        }

        containerSize = Size(1000f, 2000f)
        contentSize = Size(containerSize.width * 1.5f, containerSize.height * 1.3f)
        contentScale = ContentScale.None
        contentAlignment = Alignment.Center
        scale = 2f
        listOf(
            Item3(Translation(0.0f, 0.0f), Rect(250.0f, 300.0f, 750.0f, 1300.0f)),
            Item3(Translation(250.0f, 500.0f), Rect(250.0f, 300.0f, 625.0f, 1050.0f)),
            Item3(Translation(750.0f, 500.0f), Rect(250.0f, 300.0f, 375.0f, 1050.0f)),
            Item3(Translation(250.0f, 1500.0f), Rect(250.0f, 300.0f, 625.0f, 550.0f)),
            Item3(Translation(750.0f, 1500.0f), Rect(250.0f, 300.0f, 375.0f, 550.0f)),
            Item3(Translation(1000.0f, 2000.0f), Rect(0.0f, 0.0f, 0.0f, 0.0f)),
            Item3(Translation(-250.0f, -500.0f), Rect(375.0f, 550.0f, 875.0f, 1550.0f)),
            Item3(Translation(-750.0f, -500.0f), Rect(625.0f, 550.0f, 1125.0f, 1550.0f)),
            Item3(Translation(-250.0f, -1500.0f), Rect(375.0f, 1050.0f, 875.0f, 2050.0f)),
            Item3(Translation(-750.0f, -1500.0f), Rect(625.0f, 1050.0f, 1125.0f, 2050.0f)),
            Item3(Translation(-1000.0f, -2000.0f), Rect(750.0f, 1300.0f, 1250.0f, 2300.0f)),
//        ).printlnExpectedMessage3(
//            computeExpected = {
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    contentAlignment = contentAlignment,
//                    scale = scale,
//                    translation = it.translation
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, contentScale, contentAlignment, scale),
                it.expected,
                computeContentVisibleRect(
                    containerSize = containerSize,
                    contentSize = contentSize,
                    contentScale = contentScale,
                    contentAlignment = contentAlignment,
                    scale = scale,
                    translation = it.translation
                )
            )
        }
    }


    @Test
    fun testComputeContainerCentroidByTouchPosition() {
        var containerSize = Size(1080f, 1920f)

        var scale = 1f
        var translation = Translation(0f, 0f)
        listOf(
            Offset(216f, 960f) to Centroid(0.2f, 0.5f),
            Offset(540f, 384f) to Centroid(0.5f, 0.2f),
            Offset(864f, 960f) to Centroid(0.8f, 0.5f),
            Offset(540f, 1536f) to Centroid(0.5f, 0.8f)
        ).forEach { (touchPosition, targetPercentageCentroidOfContent) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, translation=$translation, touchPosition=$touchPosition",
                targetPercentageCentroidOfContent,
                computeContainerCentroidByTouchPosition(
                    containerSize, scale, translation, touchPosition
                )
            )
        }

        scale = 1f
        translation = Translation(540f, 960f)
        listOf(
            Offset(216f, 960f) to Centroid(0f, 0f),
            Offset(540f, 384f) to Centroid(0f, 0f),
            Offset(864f, 960f) to Centroid(0.3f, 0f),
            Offset(540f, 1536f) to Centroid(0f, 0.3f)
        ).forEach { (touchPosition, targetPercentageCentroidOfContent) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, translation=$translation, touchPosition=$touchPosition",
                targetPercentageCentroidOfContent,
                computeContainerCentroidByTouchPosition(
                    containerSize, scale, translation, touchPosition
                )
            )
        }

        scale = 1f
        translation = Translation(-540f, -960f)
        listOf(
            Offset(216f, 960f) to Centroid(0.7f, 1f),
            Offset(540f, 384f) to Centroid(1f, 0.7f),
            Offset(864f, 960f) to Centroid(1f, 1f),
            Offset(540f, 1536f) to Centroid(1f, 1f)
        ).forEach { (touchPosition, targetPercentageCentroidOfContent) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, translation=$translation, touchPosition=$touchPosition",
                targetPercentageCentroidOfContent,
                computeContainerCentroidByTouchPosition(
                    containerSize, scale, translation, touchPosition
                )
            )
        }

        scale = 2f
        translation = Translation(0f, 0f)
        listOf(
            Offset(216f, 960f) to Centroid(0.1f, 0.25f),
            Offset(540f, 384f) to Centroid(0.25f, 0.1f),
            Offset(864f, 960f) to Centroid(0.4f, 0.25f),
            Offset(540f, 1536f) to Centroid(0.25f, 0.4f)
        ).forEach { (touchPosition, targetPercentageCentroidOfContent) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, translation=$translation, touchPosition=$touchPosition",
                targetPercentageCentroidOfContent,
                computeContainerCentroidByTouchPosition(
                    containerSize, scale, translation, touchPosition
                )
            )
        }

        scale = 2f
        translation = Translation(540f, 960f)
        listOf(
            Offset(216f, 960f) to Centroid(0f, 0f),
            Offset(540f, 384f) to Centroid(0f, 0f),
            Offset(864f, 960f) to Centroid(0.15f, 0f),
            Offset(540f, 1536f) to Centroid(0f, 0.15f)
        ).forEach { (touchPosition, targetPercentageCentroidOfContent) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, translation=$translation, touchPosition=$touchPosition",
                targetPercentageCentroidOfContent,
                computeContainerCentroidByTouchPosition(
                    containerSize, scale, translation, touchPosition
                )
            )
        }

        scale = 2f
        translation = Translation(-540f, -960f)
        listOf(
            Offset(216f, 960f) to Centroid(0.35f, 0.5f),
            Offset(540f, 384f) to Centroid(0.5f, 0.35f),
            Offset(864f, 960f) to Centroid(0.65f, 0.5f),
            Offset(540f, 1536f) to Centroid(0.5f, 0.65f)
        ).forEach { (touchPosition, targetPercentageCentroidOfContent) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, translation=$translation, touchPosition=$touchPosition",
                targetPercentageCentroidOfContent,
                computeContainerCentroidByTouchPosition(
                    containerSize, scale, translation, touchPosition
                )
            )
        }

        containerSize = Size.Unspecified
        scale = 1f
        translation = Translation(0f, 0f)
        listOf(
            Offset(216f, 960f) to Centroid(0f, 0f),
            Offset(540f, 384f) to Centroid(0f, 0f),
            Offset(864f, 960f) to Centroid(0f, 0f),
            Offset(540f, 1536f) to Centroid(0f, 0f)
        ).forEach { (touchPosition, targetPercentageCentroidOfContent) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, translation=$translation, touchPosition=$touchPosition",
                targetPercentageCentroidOfContent,
                computeContainerCentroidByTouchPosition(
                    containerSize, scale, translation, touchPosition
                )
            )
        }
    }

    @Test
    fun testContainerCentroidToContentCentroid() {
        var containerSize = Size(1000f, 1000f)
        var contentSize = Size(800f, 200f)
        var containerCentroid = Centroid(0.5f, 0.5f)
        listOf(
            Item2(ContentScale.None, Alignment.TopStart, Centroid(0.625f, 1.0f)),
            Item2(ContentScale.None, Alignment.TopCenter, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.None, Alignment.TopEnd, Centroid(0.375f, 1.0f)),
            Item2(ContentScale.None, Alignment.CenterStart, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.None, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.None, Alignment.CenterEnd, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.None, Alignment.BottomStart, Centroid(0.625f, 0.0f)),
            Item2(ContentScale.None, Alignment.BottomCenter, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.None, Alignment.BottomEnd, Centroid(0.375f, 0.0f)),
            Item2(ContentScale.Inside, Alignment.TopStart, Centroid(0.625f, 1.0f)),
            Item2(ContentScale.Inside, Alignment.TopCenter, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.Inside, Alignment.TopEnd, Centroid(0.375f, 1.0f)),
            Item2(ContentScale.Inside, Alignment.CenterStart, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.CenterEnd, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomStart, Centroid(0.625f, 0.0f)),
            Item2(ContentScale.Inside, Alignment.BottomCenter, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.Inside, Alignment.BottomEnd, Centroid(0.375f, 0.0f)),
            Item2(ContentScale.Fit, Alignment.TopStart, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.Fit, Alignment.TopCenter, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.Fit, Alignment.TopEnd, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.Fit, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomStart, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.Fit, Alignment.BottomCenter, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.Fit, Alignment.BottomEnd, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.FillWidth, Alignment.TopStart, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.FillWidth, Alignment.TopCenter, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.FillWidth, Alignment.TopEnd, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.FillWidth, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.BottomStart, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.FillHeight, Alignment.TopStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.CenterStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.CenterEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomEnd, Centroid(0.875f, 0.5f)),
//        ).printlnExpectedMessage2(
//            computeExpected = {
//                containerCentroidToContentCentroid(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.contentAlignment,
//                    containerCentroid
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, containerCentroid),
                it.expected,
                containerCentroidToContentCentroid(
                    containerSize,
                    contentSize,
                    it.contentScale,
                    it.contentAlignment,
                    containerCentroid
                )
            )
        }

        containerSize = Size(1000f, 1000f)
        contentSize = Size(200f, 800f)
        containerCentroid = Centroid(0.5f, 0.5f)
        listOf(
            Item2(ContentScale.None, Alignment.TopStart, Centroid(1.0f, 0.625f)),
            Item2(ContentScale.None, Alignment.TopCenter, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.None, Alignment.TopEnd, Centroid(0.0f, 0.625f)),
            Item2(ContentScale.None, Alignment.CenterStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.None, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.None, Alignment.CenterEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.None, Alignment.BottomStart, Centroid(1.0f, 0.375f)),
            Item2(ContentScale.None, Alignment.BottomCenter, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.None, Alignment.BottomEnd, Centroid(0.0f, 0.375f)),
            Item2(ContentScale.Inside, Alignment.TopStart, Centroid(1.0f, 0.625f)),
            Item2(ContentScale.Inside, Alignment.TopCenter, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.Inside, Alignment.TopEnd, Centroid(0.0f, 0.625f)),
            Item2(ContentScale.Inside, Alignment.CenterStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.CenterEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomStart, Centroid(1.0f, 0.375f)),
            Item2(ContentScale.Inside, Alignment.BottomCenter, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.Inside, Alignment.BottomEnd, Centroid(0.0f, 0.375f)),
            Item2(ContentScale.Fit, Alignment.TopStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.TopEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.TopStart, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.FillWidth, Alignment.TopCenter, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.FillWidth, Alignment.TopEnd, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.FillWidth, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.BottomStart, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.FillHeight, Alignment.TopStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopStart, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.Crop, Alignment.TopCenter, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.Crop, Alignment.TopEnd, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.Crop, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomStart, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.Crop, Alignment.BottomCenter, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.Crop, Alignment.BottomEnd, Centroid(0.5f, 0.875f)),
//        ).printlnExpectedMessage2(
//            computeExpected = {
//                containerCentroidToContentCentroid(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.contentAlignment,
//                    containerCentroid
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, containerCentroid),
                it.expected,
                containerCentroidToContentCentroid(
                    containerSize,
                    contentSize,
                    it.contentScale,
                    it.contentAlignment,
                    containerCentroid
                )
            )
        }

        containerSize = Size(1000f, 1000f)
        contentSize = Size(1600f, 1200f)
        containerCentroid = Centroid(0.5f, 0.5f)
        listOf(
            Item2(ContentScale.None, Alignment.TopStart, Centroid(0.3125f, 0.41666666f)),
            Item2(ContentScale.None, Alignment.TopCenter, Centroid(0.5f, 0.41666666f)),
            Item2(ContentScale.None, Alignment.TopEnd, Centroid(0.6875f, 0.41666666f)),
            Item2(ContentScale.None, Alignment.CenterStart, Centroid(0.3125f, 0.5f)),
            Item2(ContentScale.None, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.None, Alignment.CenterEnd, Centroid(0.6875f, 0.5f)),
            Item2(ContentScale.None, Alignment.BottomStart, Centroid(0.3125f, 0.5833333f)),
            Item2(ContentScale.None, Alignment.BottomCenter, Centroid(0.5f, 0.5833333f)),
            Item2(ContentScale.None, Alignment.BottomEnd, Centroid(0.6875f, 0.5833333f)),
            Item2(ContentScale.Inside, Alignment.TopStart, Centroid(0.5f, 0.6666667f)),
            Item2(ContentScale.Inside, Alignment.TopCenter, Centroid(0.5f, 0.6666667f)),
            Item2(ContentScale.Inside, Alignment.TopEnd, Centroid(0.5f, 0.6666667f)),
            Item2(ContentScale.Inside, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomStart, Centroid(0.5f, 0.33333334f)),
            Item2(ContentScale.Inside, Alignment.BottomCenter, Centroid(0.5f, 0.33333334f)),
            Item2(ContentScale.Inside, Alignment.BottomEnd, Centroid(0.5f, 0.33333334f)),
            Item2(ContentScale.Fit, Alignment.TopStart, Centroid(0.5f, 0.6666667f)),
            Item2(ContentScale.Fit, Alignment.TopCenter, Centroid(0.5f, 0.6666667f)),
            Item2(ContentScale.Fit, Alignment.TopEnd, Centroid(0.5f, 0.6666667f)),
            Item2(ContentScale.Fit, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomStart, Centroid(0.5f, 0.33333334f)),
            Item2(ContentScale.Fit, Alignment.BottomCenter, Centroid(0.5f, 0.33333334f)),
            Item2(ContentScale.Fit, Alignment.BottomEnd, Centroid(0.5f, 0.33333334f)),
            Item2(ContentScale.FillWidth, Alignment.TopStart, Centroid(0.5f, 0.6666667f)),
            Item2(ContentScale.FillWidth, Alignment.TopCenter, Centroid(0.5f, 0.6666667f)),
            Item2(ContentScale.FillWidth, Alignment.TopEnd, Centroid(0.5f, 0.6666667f)),
            Item2(ContentScale.FillWidth, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.BottomStart, Centroid(0.5f, 0.33333334f)),
            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Centroid(0.5f, 0.33333334f)),
            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Centroid(0.5f, 0.33333334f)),
            Item2(ContentScale.FillHeight, Alignment.TopStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopCenter, Centroid(0.49999997f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopEnd, Centroid(0.62499994f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.Center, Centroid(0.49999997f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Centroid(0.62499994f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Centroid(0.49999997f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Centroid(0.62499994f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopCenter, Centroid(0.49999997f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopEnd, Centroid(0.62499994f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.CenterStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.Center, Centroid(0.49999997f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.CenterEnd, Centroid(0.62499994f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomCenter, Centroid(0.49999997f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomEnd, Centroid(0.62499994f, 0.5f)),
//        ).printlnExpectedMessage2(
//            computeExpected = {
//                containerCentroidToContentCentroid(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.contentAlignment,
//                    containerCentroid
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, containerCentroid),
                it.expected,
                containerCentroidToContentCentroid(
                    containerSize,
                    contentSize,
                    it.contentScale,
                    it.contentAlignment,
                    containerCentroid
                )
            )
        }

        containerSize = Size(1000f, 1000f)
        contentSize = Size(1200f, 1600f)
        containerCentroid = Centroid(0.5f, 0.5f)
        listOf(
            Item2(ContentScale.None, Alignment.TopStart, Centroid(0.41666666f, 0.3125f)),
            Item2(ContentScale.None, Alignment.TopCenter, Centroid(0.5f, 0.3125f)),
            Item2(ContentScale.None, Alignment.TopEnd, Centroid(0.5833333f, 0.3125f)),
            Item2(ContentScale.None, Alignment.CenterStart, Centroid(0.41666666f, 0.5f)),
            Item2(ContentScale.None, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.None, Alignment.CenterEnd, Centroid(0.5833333f, 0.5f)),
            Item2(ContentScale.None, Alignment.BottomStart, Centroid(0.41666666f, 0.6875f)),
            Item2(ContentScale.None, Alignment.BottomCenter, Centroid(0.5f, 0.6875f)),
            Item2(ContentScale.None, Alignment.BottomEnd, Centroid(0.5833333f, 0.6875f)),
            Item2(ContentScale.Inside, Alignment.TopStart, Centroid(0.6666667f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.TopEnd, Centroid(0.33333334f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.CenterStart, Centroid(0.6666667f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.CenterEnd, Centroid(0.33333334f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomStart, Centroid(0.6666667f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomEnd, Centroid(0.33333334f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.TopStart, Centroid(0.6666667f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.TopEnd, Centroid(0.33333334f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterStart, Centroid(0.6666667f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterEnd, Centroid(0.33333334f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomStart, Centroid(0.6666667f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomEnd, Centroid(0.33333334f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.TopStart, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.FillWidth, Alignment.TopCenter, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.FillWidth, Alignment.TopEnd, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.FillWidth, Alignment.CenterStart, Centroid(0.5f, 0.49999997f)),
            Item2(ContentScale.FillWidth, Alignment.Center, Centroid(0.5f, 0.49999997f)),
            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Centroid(0.5f, 0.49999997f)),
            Item2(ContentScale.FillWidth, Alignment.BottomStart, Centroid(0.5f, 0.62499994f)),
            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Centroid(0.5f, 0.62499994f)),
            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Centroid(0.5f, 0.62499994f)),
            Item2(ContentScale.FillHeight, Alignment.TopStart, Centroid(0.6666667f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopEnd, Centroid(0.33333334f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterStart, Centroid(0.6666667f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Centroid(0.33333334f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomStart, Centroid(0.6666667f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Centroid(0.33333334f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopStart, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.Crop, Alignment.TopCenter, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.Crop, Alignment.TopEnd, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.Crop, Alignment.CenterStart, Centroid(0.5f, 0.49999997f)),
            Item2(ContentScale.Crop, Alignment.Center, Centroid(0.5f, 0.49999997f)),
            Item2(ContentScale.Crop, Alignment.CenterEnd, Centroid(0.5f, 0.49999997f)),
            Item2(ContentScale.Crop, Alignment.BottomStart, Centroid(0.5f, 0.62499994f)),
            Item2(ContentScale.Crop, Alignment.BottomCenter, Centroid(0.5f, 0.62499994f)),
            Item2(ContentScale.Crop, Alignment.BottomEnd, Centroid(0.5f, 0.62499994f)),
//        ).printlnExpectedMessage2(
//            computeExpected = {
//                containerCentroidToContentCentroid(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.contentAlignment,
//                    containerCentroid
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, containerCentroid),
                it.expected,
                containerCentroidToContentCentroid(
                    containerSize,
                    contentSize,
                    it.contentScale,
                    it.contentAlignment,
                    containerCentroid
                )
            )
        }
    }

    @Test
    fun testContentCentroidToContainerCentroid() {
        var containerSize = Size(1000f, 1000f)
        var contentSize = Size(800f, 200f)
        var containerCentroid = Centroid(0.5f, 0.5f)
        listOf(
            Item2(ContentScale.None, Alignment.TopStart, Centroid(0.4f, 0.1f)),
            Item2(ContentScale.None, Alignment.TopCenter, Centroid(0.5f, 0.1f)),
            Item2(ContentScale.None, Alignment.TopEnd, Centroid(0.6f, 0.1f)),
            Item2(ContentScale.None, Alignment.CenterStart, Centroid(0.4f, 0.5f)),
            Item2(ContentScale.None, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.None, Alignment.CenterEnd, Centroid(0.6f, 0.5f)),
            Item2(ContentScale.None, Alignment.BottomStart, Centroid(0.4f, 0.9f)),
            Item2(ContentScale.None, Alignment.BottomCenter, Centroid(0.5f, 0.9f)),
            Item2(ContentScale.None, Alignment.BottomEnd, Centroid(0.6f, 0.9f)),
            Item2(ContentScale.Inside, Alignment.TopStart, Centroid(0.4f, 0.1f)),
            Item2(ContentScale.Inside, Alignment.TopCenter, Centroid(0.5f, 0.1f)),
            Item2(ContentScale.Inside, Alignment.TopEnd, Centroid(0.6f, 0.1f)),
            Item2(ContentScale.Inside, Alignment.CenterStart, Centroid(0.4f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.CenterEnd, Centroid(0.6f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomStart, Centroid(0.4f, 0.9f)),
            Item2(ContentScale.Inside, Alignment.BottomCenter, Centroid(0.5f, 0.9f)),
            Item2(ContentScale.Inside, Alignment.BottomEnd, Centroid(0.6f, 0.9f)),
            Item2(ContentScale.Fit, Alignment.TopStart, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.Fit, Alignment.TopCenter, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.Fit, Alignment.TopEnd, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.Fit, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomStart, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.Fit, Alignment.BottomCenter, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.Fit, Alignment.BottomEnd, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.FillWidth, Alignment.TopStart, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.FillWidth, Alignment.TopCenter, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.FillWidth, Alignment.TopEnd, Centroid(0.5f, 0.125f)),
            Item2(ContentScale.FillWidth, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.BottomStart, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Centroid(0.5f, 0.875f)),
            Item2(ContentScale.FillHeight, Alignment.TopStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.CenterStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.CenterEnd, Centroid(0.0f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomStart, Centroid(1.0f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomEnd, Centroid(0.0f, 0.5f)),
//        ).printlnExpectedMessage2(
//            computeExpected = {
//                contentCentroidToContainerCentroid(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.contentAlignment,
//                    containerCentroid
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, containerCentroid),
                it.expected,
                contentCentroidToContainerCentroid(
                    containerSize,
                    contentSize,
                    it.contentScale,
                    it.contentAlignment,
                    containerCentroid
                )
            )
        }

        containerSize = Size(1000f, 1000f)
        contentSize = Size(200f, 800f)
        containerCentroid = Centroid(0.5f, 0.5f)
        listOf(
            Item2(ContentScale.None, Alignment.TopStart, Centroid(0.1f, 0.4f)),
            Item2(ContentScale.None, Alignment.TopCenter, Centroid(0.5f, 0.4f)),
            Item2(ContentScale.None, Alignment.TopEnd, Centroid(0.9f, 0.4f)),
            Item2(ContentScale.None, Alignment.CenterStart, Centroid(0.1f, 0.5f)),
            Item2(ContentScale.None, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.None, Alignment.CenterEnd, Centroid(0.9f, 0.5f)),
            Item2(ContentScale.None, Alignment.BottomStart, Centroid(0.1f, 0.6f)),
            Item2(ContentScale.None, Alignment.BottomCenter, Centroid(0.5f, 0.6f)),
            Item2(ContentScale.None, Alignment.BottomEnd, Centroid(0.9f, 0.6f)),
            Item2(ContentScale.Inside, Alignment.TopStart, Centroid(0.1f, 0.4f)),
            Item2(ContentScale.Inside, Alignment.TopCenter, Centroid(0.5f, 0.4f)),
            Item2(ContentScale.Inside, Alignment.TopEnd, Centroid(0.9f, 0.4f)),
            Item2(ContentScale.Inside, Alignment.CenterStart, Centroid(0.1f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.CenterEnd, Centroid(0.9f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomStart, Centroid(0.1f, 0.6f)),
            Item2(ContentScale.Inside, Alignment.BottomCenter, Centroid(0.5f, 0.6f)),
            Item2(ContentScale.Inside, Alignment.BottomEnd, Centroid(0.9f, 0.6f)),
            Item2(ContentScale.Fit, Alignment.TopStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.TopEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.TopStart, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.FillWidth, Alignment.TopCenter, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.FillWidth, Alignment.TopEnd, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.FillWidth, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.BottomStart, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.FillHeight, Alignment.TopStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomStart, Centroid(0.125f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Centroid(0.875f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopStart, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.Crop, Alignment.TopCenter, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.Crop, Alignment.TopEnd, Centroid(0.5f, 1.0f)),
            Item2(ContentScale.Crop, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomStart, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.Crop, Alignment.BottomCenter, Centroid(0.5f, 0.0f)),
            Item2(ContentScale.Crop, Alignment.BottomEnd, Centroid(0.5f, 0.0f)),
//        ).printlnExpectedMessage2(
//            computeExpected = {
//                contentCentroidToContainerCentroid(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.contentAlignment,
//                    containerCentroid
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, containerCentroid),
                it.expected,
                contentCentroidToContainerCentroid(
                    containerSize,
                    contentSize,
                    it.contentScale,
                    it.contentAlignment,
                    containerCentroid
                )
            )
        }

        containerSize = Size(1000f, 1000f)
        contentSize = Size(1600f, 1200f)
        containerCentroid = Centroid(0.5f, 0.5f)
        listOf(
            Item2(ContentScale.None, Alignment.TopStart, Centroid(0.8f, 0.6f)),
            Item2(ContentScale.None, Alignment.TopCenter, Centroid(0.5f, 0.6f)),
            Item2(ContentScale.None, Alignment.TopEnd, Centroid(0.2f, 0.6f)),
            Item2(ContentScale.None, Alignment.CenterStart, Centroid(0.8f, 0.5f)),
            Item2(ContentScale.None, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.None, Alignment.CenterEnd, Centroid(0.2f, 0.5f)),
            Item2(ContentScale.None, Alignment.BottomStart, Centroid(0.8f, 0.4f)),
            Item2(ContentScale.None, Alignment.BottomCenter, Centroid(0.5f, 0.4f)),
            Item2(ContentScale.None, Alignment.BottomEnd, Centroid(0.2f, 0.4f)),
            Item2(ContentScale.Inside, Alignment.TopStart, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.Inside, Alignment.TopCenter, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.Inside, Alignment.TopEnd, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.Inside, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomStart, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.Inside, Alignment.BottomCenter, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.Inside, Alignment.BottomEnd, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.Fit, Alignment.TopStart, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.Fit, Alignment.TopCenter, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.Fit, Alignment.TopEnd, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.Fit, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomStart, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.Fit, Alignment.BottomCenter, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.Fit, Alignment.BottomEnd, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.FillWidth, Alignment.TopStart, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.FillWidth, Alignment.TopCenter, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.FillWidth, Alignment.TopEnd, Centroid(0.5f, 0.375f)),
            Item2(ContentScale.FillWidth, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.BottomStart, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Centroid(0.5f, 0.625f)),
            Item2(ContentScale.FillHeight, Alignment.TopStart, Centroid(0.6666666f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopCenter, Centroid(0.50000006f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopEnd, Centroid(0.3333334f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterStart, Centroid(0.6666666f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.Center, Centroid(0.50000006f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Centroid(0.3333334f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomStart, Centroid(0.6666666f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Centroid(0.50000006f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Centroid(0.3333334f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopStart, Centroid(0.6666666f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopCenter, Centroid(0.50000006f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopEnd, Centroid(0.3333334f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.CenterStart, Centroid(0.6666666f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.Center, Centroid(0.50000006f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.CenterEnd, Centroid(0.3333334f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomStart, Centroid(0.6666666f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomCenter, Centroid(0.50000006f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.BottomEnd, Centroid(0.3333334f, 0.5f)),
//        ).printlnExpectedMessage2(
//            computeExpected = {
//                contentCentroidToContainerCentroid(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.contentAlignment,
//                    containerCentroid
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, containerCentroid),
                it.expected,
                contentCentroidToContainerCentroid(
                    containerSize,
                    contentSize,
                    it.contentScale,
                    it.contentAlignment,
                    containerCentroid
                )
            )
        }

        containerSize = Size(1000f, 1000f)
        contentSize = Size(1200f, 1600f)
        containerCentroid = Centroid(0.5f, 0.5f)
        listOf(
            Item2(ContentScale.None, Alignment.TopStart, Centroid(0.6f, 0.8f)),
            Item2(ContentScale.None, Alignment.TopCenter, Centroid(0.5f, 0.8f)),
            Item2(ContentScale.None, Alignment.TopEnd, Centroid(0.4f, 0.8f)),
            Item2(ContentScale.None, Alignment.CenterStart, Centroid(0.6f, 0.5f)),
            Item2(ContentScale.None, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.None, Alignment.CenterEnd, Centroid(0.4f, 0.5f)),
            Item2(ContentScale.None, Alignment.BottomStart, Centroid(0.6f, 0.2f)),
            Item2(ContentScale.None, Alignment.BottomCenter, Centroid(0.5f, 0.2f)),
            Item2(ContentScale.None, Alignment.BottomEnd, Centroid(0.4f, 0.2f)),
            Item2(ContentScale.Inside, Alignment.TopStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.TopEnd, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.CenterStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.CenterEnd, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Inside, Alignment.BottomEnd, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.TopStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.TopEnd, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.CenterEnd, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Fit, Alignment.BottomEnd, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.FillWidth, Alignment.TopStart, Centroid(0.5f, 0.6666666f)),
            Item2(ContentScale.FillWidth, Alignment.TopCenter, Centroid(0.5f, 0.6666666f)),
            Item2(ContentScale.FillWidth, Alignment.TopEnd, Centroid(0.5f, 0.6666666f)),
            Item2(ContentScale.FillWidth, Alignment.CenterStart, Centroid(0.5f, 0.50000006f)),
            Item2(ContentScale.FillWidth, Alignment.Center, Centroid(0.5f, 0.50000006f)),
            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Centroid(0.5f, 0.50000006f)),
            Item2(ContentScale.FillWidth, Alignment.BottomStart, Centroid(0.5f, 0.3333334f)),
            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Centroid(0.5f, 0.3333334f)),
            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Centroid(0.5f, 0.3333334f)),
            Item2(ContentScale.FillHeight, Alignment.TopStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.TopEnd, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomStart, Centroid(0.375f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Centroid(0.625f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.TopEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.Center, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomStart, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Centroid(0.5f, 0.5f)),
            Item2(ContentScale.Crop, Alignment.TopStart, Centroid(0.5f, 0.6666666f)),
            Item2(ContentScale.Crop, Alignment.TopCenter, Centroid(0.5f, 0.6666666f)),
            Item2(ContentScale.Crop, Alignment.TopEnd, Centroid(0.5f, 0.6666666f)),
            Item2(ContentScale.Crop, Alignment.CenterStart, Centroid(0.5f, 0.50000006f)),
            Item2(ContentScale.Crop, Alignment.Center, Centroid(0.5f, 0.50000006f)),
            Item2(ContentScale.Crop, Alignment.CenterEnd, Centroid(0.5f, 0.50000006f)),
            Item2(ContentScale.Crop, Alignment.BottomStart, Centroid(0.5f, 0.3333334f)),
            Item2(ContentScale.Crop, Alignment.BottomCenter, Centroid(0.5f, 0.3333334f)),
            Item2(ContentScale.Crop, Alignment.BottomEnd, Centroid(0.5f, 0.3333334f)),
//        ).printlnExpectedMessage2(
//            computeExpected = {
//                contentCentroidToContainerCentroid(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.contentAlignment,
//                    containerCentroid
//                )
//            }
        ).forEach {
            Assert.assertEquals(
                it.getMessage(containerSize, contentSize, containerCentroid),
                it.expected,
                contentCentroidToContainerCentroid(
                    containerSize,
                    contentSize,
                    it.contentScale,
                    it.contentAlignment,
                    containerCentroid
                )
            )
        }
    }

//    @Test
//    fun testComputeScrollEdge() {
//        val contentSize = Size(1000f, 1000f)
//
//        listOf(
//            (Rect(0f, 0f, 1000f, 1000f) to true) to BOTH,
//            (Rect(0f, 0f, 1000f, 1000f) to false) to BOTH,
//
//            (Rect(0f, 0f, 500f, 500f) to true) to START,
//            (Rect(0f, 0f, 500f, 500f) to false) to START,
//            (Rect(200f, 0f, 800f, 500f) to true) to NONE,
//            (Rect(200f, 0f, 800f, 500f) to false) to START,
//            (Rect(500f, 0f, 1000f, 1000f) to true) to END,
//            (Rect(500f, 0f, 1000f, 500f) to false) to START,
//
//            (Rect(0f, 200f, 500f, 800f) to true) to START,
//            (Rect(0f, 200f, 500f, 800f) to false) to NONE,
//            (Rect(200f, 200f, 800f, 800f) to true) to NONE,
//            (Rect(200f, 200f, 800f, 800f) to false) to NONE,
//            (Rect(500f, 200f, 1000f, 800f) to true) to END,
//            (Rect(500f, 200f, 1000f, 800f) to false) to NONE,
//
//            (Rect(0f, 500f, 500f, 1000f) to true) to START,
//            (Rect(0f, 500f, 500f, 1000f) to false) to END,
//            (Rect(200f, 500f, 800f, 1000f) to true) to NONE,
//            (Rect(200f, 500f, 800f, 1000f) to false) to END,
//            (Rect(500f, 500f, 1000f, 1000f) to true) to END,
//            (Rect(500f, 500f, 1000f, 1000f) to false) to END,
//        ).forEach {
//            val visibleRect = it.first.first
//            val horizontal = it.first.second
//            val expected = it.second
//            Assert.assertEquals(
//                "contentSize=${contentSize.toShortString()}, visibleRect=${visibleRect.toShortString()}, horizontal=$horizontal",
//                expected,
//                computeScrollEdge(contentSize, visibleRect, horizontal = horizontal)
//            )
//        }
//    }
}