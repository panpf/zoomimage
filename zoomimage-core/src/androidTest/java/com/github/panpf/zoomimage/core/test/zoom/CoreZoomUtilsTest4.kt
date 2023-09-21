package com.github.panpf.zoomimage.core.test.zoom

import com.github.panpf.zoomimage.util.IntOffsetCompat as IntOffset
import com.github.panpf.zoomimage.util.IntRectCompat as IntRect
import com.github.panpf.zoomimage.util.IntSizeCompat as IntSize
import com.github.panpf.zoomimage.zoom.AlignmentCompat as Alignment
import com.github.panpf.zoomimage.zoom.ContentScaleCompat as ContentScale
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.zoomimage.core.test.internal.A
import com.github.panpf.zoomimage.core.test.internal.printlnBatchBuildExpression
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.AlignmentCompat.Companion.Center
import com.github.panpf.zoomimage.zoom.ContentScaleCompat.Companion.None
import com.github.panpf.zoomimage.zoom.containerPointToContentPoint
import com.github.panpf.zoomimage.zoom.contentPointToContainerPoint
import com.github.panpf.zoomimage.zoom.name
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreZoomUtilsTest4 {

    // TODO touchPointToContainerPoint
    // TODO containerPointToTouchPoint

    @Test
    fun testContainerPointToContentPoint() {
        val containerSize = IntSize(1000, 1000)

        val printBatchBuildExpression = false
//        val printBatchBuildExpression = true
        if (printBatchBuildExpression) {
            printlnBatchBuildExpression(
                p1s = listOf(
                    IntSize(1000, 400),
                    IntSize(400, 1000),
                    IntSize(500, 200),
                    IntSize(200, 500),
                    IntSize(2000, 800),
                    IntSize(800, 2000),
                ),
                p2s = listOf(None),
                p3s = listOf(Center),
                p4s = listOf(
                    IntOffset(500, 500),
                    IntOffset(200, 500),
                    IntOffset(500, 200),
                    IntOffset(800, 500),
                    IntOffset(500, 800),
                    IntOffset(-200, 500),
                    IntOffset(500, -200),
                    IntOffset(1200, 500),
                    IntOffset(500, 1200),
                ),
                buildItem = { p1, p2, p3, p4 ->
                    Item9(p1, p2, p3, p4, IntOffset.Zero)
                },
            ) { item ->
                containerPointToContentPoint(
                    containerSize = containerSize,
                    contentSize = item.contentSize,
                    contentScale = item.contentScale,
                    alignment = item.alignment,
                    rotation = 0,
                    containerPoint = item.inputPoint.toOffset(),
                ).round()
            }
        }

        listOf(
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(500, 200)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(200, 200)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(500, 0)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(800, 200)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(500, 400)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(0, 200)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(500, 0)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(1000, 200)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(500, 400)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(200, 500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(0, 500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(200, 200)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(400, 500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(200, 800)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(0, 500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(200, 0)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(400, 500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(200, 1000)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(250, 100)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(0, 100)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(250, 0)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(500, 100)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(250, 200)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(0, 100)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(250, 0)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(500, 100)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(250, 200)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(100, 250)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(0, 250)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(100, 0)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(200, 250)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(100, 500)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(0, 250)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(100, 0)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(200, 250)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(100, 500)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(1000, 400)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(700, 400)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(1000, 100)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(1300, 400)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(1000, 700)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(300, 400)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(1000, 0)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(1700, 400)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(1000, 800)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(400, 1000)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(100, 1000)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(400, 700)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(700, 1000)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(400, 1300)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(0, 1000)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(400, 300)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(800, 1000)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(400, 1700)
            ),
        ).forEach { item ->
            val result = containerPointToContentPoint(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentScale = item.contentScale,
                alignment = item.alignment,
                rotation = 0,
                containerPoint = item.inputPoint.toOffset(),
            ).round()
            Assert.assertEquals(item.getMessage(containerSize), item.expected, result)
        }
    }

    @Test
    fun testContentPointToContainerPoint() {
        val containerSize = IntSize(1000, 1000)

        val printBatchBuildExpression = false
//        val printBatchBuildExpression = true
        if (printBatchBuildExpression) {
            printlnBatchBuildExpression(
                p1s = listOf(
                    IntSize(1000, 400),
                    IntSize(400, 1000),
                    IntSize(500, 200),
                    IntSize(200, 500),
                    IntSize(2000, 800),
                    IntSize(800, 2000),
                ),
                p2s = listOf(None),
                p3s = listOf(Center),
                p4s = listOf(
                    IntOffset(500, 500),
                    IntOffset(200, 500),
                    IntOffset(500, 200),
                    IntOffset(800, 500),
                    IntOffset(500, 800),
                    IntOffset(-200, 500),
                    IntOffset(500, -200),
                    IntOffset(1200, 500),
                    IntOffset(500, 1200),
                ),
                buildItem = { p1, p2, p3, p4 ->
                    Item9(p1, p2, p3, p4, IntOffset.Zero)
                },
            ) { item ->
                contentPointToContainerPoint(
                    containerSize = containerSize,
                    contentSize = item.contentSize,
                    contentScale = item.contentScale,
                    alignment = item.alignment,
                    rotation = 0,
                    contentPoint = item.inputPoint.toOffset(),
                ).round()
            }
        }

        listOf(
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(500, 800)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(200, 800)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(500, 500)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(800, 800)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(500, 1100)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(-200, 800)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(500, 100)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(1200, 800)
            ),
            Item9(
                IntSize(1000, 400),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(500, 1500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(800, 500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(500, 500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(800, 200)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(1100, 500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(800, 800)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(100, 500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(800, -200)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(1500, 500)
            ),
            Item9(
                IntSize(400, 1000),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(800, 1200)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(750, 900)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(450, 900)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(750, 600)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(1050, 900)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(750, 1200)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(50, 900)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(750, 200)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(1450, 900)
            ),
            Item9(
                IntSize(500, 200),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(750, 1600)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(900, 750)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(600, 750)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(900, 450)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(1200, 750)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(900, 1050)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(200, 750)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(900, 50)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(1600, 750)
            ),
            Item9(
                IntSize(200, 500),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(900, 1450)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(0, 600)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(-300, 600)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(0, 300)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(300, 600)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(0, 900)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(-700, 600)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(0, -100)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(700, 600)
            ),
            Item9(
                IntSize(2000, 800),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(0, 1300)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(500, 500),
                IntOffset(600, 0)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(200, 500),
                IntOffset(300, 0)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(500, 200),
                IntOffset(600, -300)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(800, 500),
                IntOffset(900, 0)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(500, 800),
                IntOffset(600, 300)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(-200, 500),
                IntOffset(-100, 0)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(500, -200),
                IntOffset(600, -700)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(1200, 500),
                IntOffset(1300, 0)
            ),
            Item9(
                IntSize(800, 2000),
                None,
                Center,
                IntOffset(500, 1200),
                IntOffset(600, 700)
            ),
        ).forEach { item ->
            val result = contentPointToContainerPoint(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentScale = item.contentScale,
                alignment = item.alignment,
                rotation = 0,
                contentPoint = item.inputPoint.toOffset(),
            ).round()
            Assert.assertEquals(item.getMessage(containerSize), item.expected, result)
        }
    }

    // TODO touchPointToContentPoint
    // TODO contentPointToTouchPoint

    data class Item6(
        val contentSize: IntSize,
        val contentScale: ContentScale,
        val alignment: Alignment,
        override val expected: IntOffset
    ) : A<IntOffset> {
        override fun getMessage(containerSize: IntSize): String {
            return "Item6(" +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}" +
                    ")"
        }

        override fun getBuildExpression(r: IntOffset): String {
            return "Item6(" +
                    "IntSize(${contentSize.width}, ${contentSize.height}), " +
                    "${contentScale.name}, " +
                    "${alignment.name}, " +
                    "IntOffset(${r.x}, ${r.y})" +
                    ")"
        }
    }

    data class Item7(
        val contentSize: IntSize,
        val contentScale: ContentScale,
        val alignment: Alignment,
        val scale: Float,
        override val expected: IntRect
    ) : A<IntRect> {
        override fun getMessage(containerSize: IntSize): String {
            return "Item7(" +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}, " +
                    "scale=${scale}" +
                    ")"
        }

        override fun getBuildExpression(r: IntRect): String {
            return "Item7(" +
                    "IntSize(${contentSize.width}, ${contentSize.height}), " +
                    "${contentScale.name}, " +
                    "${alignment.name}, " +
                    "${scale}f, " +
                    "IntRect(${r.left}, ${r.top}, ${r.right}, ${r.bottom})" +
                    ")"
        }
    }

    data class Item9(
        val contentSize: IntSize,
        val contentScale: ContentScale,
        val alignment: Alignment,
        val inputPoint: IntOffset,
        override val expected: IntOffset,
    ) : A<IntOffset> {
        override fun getMessage(containerSize: IntSize): String {
            return "Item9(" +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}," +
                    "inputPoint=${inputPoint.toShortString()}" +
                    ")"
        }

        override fun getBuildExpression(r: IntOffset): String {
            return "Item9(" +
                    "IntSize(${contentSize.width}, ${contentSize.height}), " +
                    "${contentScale.name}, " +
                    "${alignment.name}, " +
                    "IntOffset(${inputPoint.x}, ${inputPoint.y})," +
                    "IntOffset(${r.x}, ${r.y})" +
                    ")"
        }
    }
}