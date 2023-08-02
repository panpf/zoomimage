package com.github.panpf.zoomimage.compose.test

import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.layout.ContentScale.Companion.FillBounds
import androidx.compose.ui.layout.ContentScale.Companion.FillHeight
import androidx.compose.ui.layout.ContentScale.Companion.FillWidth
import androidx.compose.ui.layout.ContentScale.Companion.Fit
import androidx.compose.ui.layout.ContentScale.Companion.Inside
import androidx.compose.ui.layout.ContentScale.Companion.None
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.roundToIntRect
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.zoomimage.compose.internal.name
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.zoom.internal.computeAlignmentIntOffset
import com.github.panpf.zoomimage.compose.zoom.internal.computeContainerVisibleRect
import com.github.panpf.zoomimage.compose.zoom.internal.computeContentInContainerInnerRect
import com.github.panpf.zoomimage.compose.zoom.internal.computeContentInContainerRect
import com.github.panpf.zoomimage.compose.zoom.internal.computeLocationUserOffset
import com.github.panpf.zoomimage.compose.zoom.internal.computeUserOffsetBounds
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.toShortString
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class ComposeZoomUtilsTest {

    @Test
    fun testComputeAlignmentOffset() {
        val containerSize = IntSize(1080, 1656)
        val printBatchBuildExpression = false
//        val printBatchBuildExpression = true

        if (printBatchBuildExpression) {
            printlnBatchBuildExpression(
                p1s = listOf(
                    IntSize(7500, 232), IntSize(173, 3044), IntSize(575, 427), IntSize(551, 1038),
                ),
                p2s = listOf(None, Inside, Fit, FillWidth, FillHeight, FillBounds, Crop),
                p3s = listOf(
                    TopStart, TopCenter, TopEnd,
                    CenterStart, Center, CenterEnd,
                    BottomStart, BottomCenter, BottomEnd,
                ),
                buildItem = { p1, p2, p3 ->
                    Item6(p1, p2, p3, IntOffset.Zero)
                },
            ) {
                computeAlignmentIntOffset(
                    containerSize = containerSize,
                    contentSize = it.contentSize,
                    contentScale = it.contentScale,
                    alignment = it.alignment,
                )
            }
        }

        listOf(
            Item6(IntSize(7500, 232), None, TopStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), None, TopCenter, IntOffset(-3210, 0)),
            Item6(IntSize(7500, 232), None, TopEnd, IntOffset(-6420, 0)),
            Item6(IntSize(7500, 232), None, CenterStart, IntOffset(0, 712)),
            Item6(IntSize(7500, 232), None, Center, IntOffset(-3210, 712)),
            Item6(IntSize(7500, 232), None, CenterEnd, IntOffset(-6420, 712)),
            Item6(IntSize(7500, 232), None, BottomStart, IntOffset(0, 1424)),
            Item6(IntSize(7500, 232), None, BottomCenter, IntOffset(-3210, 1424)),
            Item6(IntSize(7500, 232), None, BottomEnd, IntOffset(-6420, 1424)),
            Item6(IntSize(7500, 232), Inside, TopStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), Inside, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), Inside, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), Inside, CenterStart, IntOffset(0, 812)),
            Item6(IntSize(7500, 232), Inside, Center, IntOffset(0, 812)),
            Item6(IntSize(7500, 232), Inside, CenterEnd, IntOffset(0, 812)),
            Item6(IntSize(7500, 232), Inside, BottomStart, IntOffset(0, 1623)),
            Item6(IntSize(7500, 232), Inside, BottomCenter, IntOffset(0, 1623)),
            Item6(IntSize(7500, 232), Inside, BottomEnd, IntOffset(0, 1623)),
            Item6(IntSize(7500, 232), Fit, TopStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), Fit, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), Fit, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), Fit, CenterStart, IntOffset(0, 812)),
            Item6(IntSize(7500, 232), Fit, Center, IntOffset(0, 812)),
            Item6(IntSize(7500, 232), Fit, CenterEnd, IntOffset(0, 812)),
            Item6(IntSize(7500, 232), Fit, BottomStart, IntOffset(0, 1623)),
            Item6(IntSize(7500, 232), Fit, BottomCenter, IntOffset(0, 1623)),
            Item6(IntSize(7500, 232), Fit, BottomEnd, IntOffset(0, 1623)),
            Item6(IntSize(7500, 232), FillWidth, TopStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillWidth, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillWidth, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillWidth, CenterStart, IntOffset(0, 812)),
            Item6(IntSize(7500, 232), FillWidth, Center, IntOffset(0, 812)),
            Item6(IntSize(7500, 232), FillWidth, CenterEnd, IntOffset(0, 812)),
            Item6(IntSize(7500, 232), FillWidth, BottomStart, IntOffset(0, 1623)),
            Item6(IntSize(7500, 232), FillWidth, BottomCenter, IntOffset(0, 1623)),
            Item6(IntSize(7500, 232), FillWidth, BottomEnd, IntOffset(0, 1623)),
            Item6(IntSize(7500, 232), FillHeight, TopStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillHeight, TopCenter, IntOffset(-26227, 0)),
            Item6(IntSize(7500, 232), FillHeight, TopEnd, IntOffset(-52454, 0)),
            Item6(IntSize(7500, 232), FillHeight, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillHeight, Center, IntOffset(-26227, 0)),
            Item6(IntSize(7500, 232), FillHeight, CenterEnd, IntOffset(-52454, 0)),
            Item6(IntSize(7500, 232), FillHeight, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillHeight, BottomCenter, IntOffset(-26227, 0)),
            Item6(IntSize(7500, 232), FillHeight, BottomEnd, IntOffset(-52454, 0)),
            Item6(IntSize(7500, 232), FillBounds, TopStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillBounds, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillBounds, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillBounds, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillBounds, Center, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillBounds, CenterEnd, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillBounds, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillBounds, BottomCenter, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), FillBounds, BottomEnd, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), Crop, TopStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), Crop, TopCenter, IntOffset(-26227, 0)),
            Item6(IntSize(7500, 232), Crop, TopEnd, IntOffset(-52454, 0)),
            Item6(IntSize(7500, 232), Crop, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), Crop, Center, IntOffset(-26227, 0)),
            Item6(IntSize(7500, 232), Crop, CenterEnd, IntOffset(-52454, 0)),
            Item6(IntSize(7500, 232), Crop, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(7500, 232), Crop, BottomCenter, IntOffset(-26227, 0)),
            Item6(IntSize(7500, 232), Crop, BottomEnd, IntOffset(-52454, 0)),
            Item6(IntSize(173, 3044), None, TopStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), None, TopCenter, IntOffset(454, 0)),
            Item6(IntSize(173, 3044), None, TopEnd, IntOffset(907, 0)),
            Item6(IntSize(173, 3044), None, CenterStart, IntOffset(0, -694)),
            Item6(IntSize(173, 3044), None, Center, IntOffset(454, -694)),
            Item6(IntSize(173, 3044), None, CenterEnd, IntOffset(907, -694)),
            Item6(IntSize(173, 3044), None, BottomStart, IntOffset(0, -1388)),
            Item6(IntSize(173, 3044), None, BottomCenter, IntOffset(454, -1388)),
            Item6(IntSize(173, 3044), None, BottomEnd, IntOffset(907, -1388)),
            Item6(IntSize(173, 3044), Inside, TopStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), Inside, TopCenter, IntOffset(493, 0)),
            Item6(IntSize(173, 3044), Inside, TopEnd, IntOffset(986, 0)),
            Item6(IntSize(173, 3044), Inside, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), Inside, Center, IntOffset(493, 0)),
            Item6(IntSize(173, 3044), Inside, CenterEnd, IntOffset(986, 0)),
            Item6(IntSize(173, 3044), Inside, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), Inside, BottomCenter, IntOffset(493, 0)),
            Item6(IntSize(173, 3044), Inside, BottomEnd, IntOffset(986, 0)),
            Item6(IntSize(173, 3044), Fit, TopStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), Fit, TopCenter, IntOffset(493, 0)),
            Item6(IntSize(173, 3044), Fit, TopEnd, IntOffset(986, 0)),
            Item6(IntSize(173, 3044), Fit, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), Fit, Center, IntOffset(493, 0)),
            Item6(IntSize(173, 3044), Fit, CenterEnd, IntOffset(986, 0)),
            Item6(IntSize(173, 3044), Fit, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), Fit, BottomCenter, IntOffset(493, 0)),
            Item6(IntSize(173, 3044), Fit, BottomEnd, IntOffset(986, 0)),
            Item6(IntSize(173, 3044), FillWidth, TopStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillWidth, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillWidth, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillWidth, CenterStart, IntOffset(0, -8673)),
            Item6(IntSize(173, 3044), FillWidth, Center, IntOffset(0, -8673)),
            Item6(IntSize(173, 3044), FillWidth, CenterEnd, IntOffset(0, -8673)),
            Item6(IntSize(173, 3044), FillWidth, BottomStart, IntOffset(0, -17347)),
            Item6(IntSize(173, 3044), FillWidth, BottomCenter, IntOffset(0, -17347)),
            Item6(IntSize(173, 3044), FillWidth, BottomEnd, IntOffset(0, -17347)),
            Item6(IntSize(173, 3044), FillHeight, TopStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillHeight, TopCenter, IntOffset(493, 0)),
            Item6(IntSize(173, 3044), FillHeight, TopEnd, IntOffset(986, 0)),
            Item6(IntSize(173, 3044), FillHeight, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillHeight, Center, IntOffset(493, 0)),
            Item6(IntSize(173, 3044), FillHeight, CenterEnd, IntOffset(986, 0)),
            Item6(IntSize(173, 3044), FillHeight, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillHeight, BottomCenter, IntOffset(493, 0)),
            Item6(IntSize(173, 3044), FillHeight, BottomEnd, IntOffset(986, 0)),
            Item6(IntSize(173, 3044), FillBounds, TopStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillBounds, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillBounds, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillBounds, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillBounds, Center, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillBounds, CenterEnd, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillBounds, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillBounds, BottomCenter, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), FillBounds, BottomEnd, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), Crop, TopStart, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), Crop, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), Crop, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(173, 3044), Crop, CenterStart, IntOffset(0, -8673)),
            Item6(IntSize(173, 3044), Crop, Center, IntOffset(0, -8673)),
            Item6(IntSize(173, 3044), Crop, CenterEnd, IntOffset(0, -8673)),
            Item6(IntSize(173, 3044), Crop, BottomStart, IntOffset(0, -17347)),
            Item6(IntSize(173, 3044), Crop, BottomCenter, IntOffset(0, -17347)),
            Item6(IntSize(173, 3044), Crop, BottomEnd, IntOffset(0, -17347)),
            Item6(IntSize(575, 427), None, TopStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), None, TopCenter, IntOffset(253, 0)),
            Item6(IntSize(575, 427), None, TopEnd, IntOffset(505, 0)),
            Item6(IntSize(575, 427), None, CenterStart, IntOffset(0, 615)),
            Item6(IntSize(575, 427), None, Center, IntOffset(253, 615)),
            Item6(IntSize(575, 427), None, CenterEnd, IntOffset(505, 615)),
            Item6(IntSize(575, 427), None, BottomStart, IntOffset(0, 1229)),
            Item6(IntSize(575, 427), None, BottomCenter, IntOffset(253, 1229)),
            Item6(IntSize(575, 427), None, BottomEnd, IntOffset(505, 1229)),
            Item6(IntSize(575, 427), Inside, TopStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), Inside, TopCenter, IntOffset(253, 0)),
            Item6(IntSize(575, 427), Inside, TopEnd, IntOffset(505, 0)),
            Item6(IntSize(575, 427), Inside, CenterStart, IntOffset(0, 615)),
            Item6(IntSize(575, 427), Inside, Center, IntOffset(253, 615)),
            Item6(IntSize(575, 427), Inside, CenterEnd, IntOffset(505, 615)),
            Item6(IntSize(575, 427), Inside, BottomStart, IntOffset(0, 1229)),
            Item6(IntSize(575, 427), Inside, BottomCenter, IntOffset(253, 1229)),
            Item6(IntSize(575, 427), Inside, BottomEnd, IntOffset(505, 1229)),
            Item6(IntSize(575, 427), Fit, TopStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), Fit, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(575, 427), Fit, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(575, 427), Fit, CenterStart, IntOffset(0, 427)),
            Item6(IntSize(575, 427), Fit, Center, IntOffset(0, 427)),
            Item6(IntSize(575, 427), Fit, CenterEnd, IntOffset(0, 427)),
            Item6(IntSize(575, 427), Fit, BottomStart, IntOffset(0, 854)),
            Item6(IntSize(575, 427), Fit, BottomCenter, IntOffset(0, 854)),
            Item6(IntSize(575, 427), Fit, BottomEnd, IntOffset(0, 854)),
            Item6(IntSize(575, 427), FillWidth, TopStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillWidth, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillWidth, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillWidth, CenterStart, IntOffset(0, 427)),
            Item6(IntSize(575, 427), FillWidth, Center, IntOffset(0, 427)),
            Item6(IntSize(575, 427), FillWidth, CenterEnd, IntOffset(0, 427)),
            Item6(IntSize(575, 427), FillWidth, BottomStart, IntOffset(0, 854)),
            Item6(IntSize(575, 427), FillWidth, BottomCenter, IntOffset(0, 854)),
            Item6(IntSize(575, 427), FillWidth, BottomEnd, IntOffset(0, 854)),
            Item6(IntSize(575, 427), FillHeight, TopStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillHeight, TopCenter, IntOffset(-575, 0)),
            Item6(IntSize(575, 427), FillHeight, TopEnd, IntOffset(-1150, 0)),
            Item6(IntSize(575, 427), FillHeight, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillHeight, Center, IntOffset(-575, 0)),
            Item6(IntSize(575, 427), FillHeight, CenterEnd, IntOffset(-1150, 0)),
            Item6(IntSize(575, 427), FillHeight, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillHeight, BottomCenter, IntOffset(-575, 0)),
            Item6(IntSize(575, 427), FillHeight, BottomEnd, IntOffset(-1150, 0)),
            Item6(IntSize(575, 427), FillBounds, TopStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillBounds, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillBounds, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillBounds, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillBounds, Center, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillBounds, CenterEnd, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillBounds, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillBounds, BottomCenter, IntOffset(0, 0)),
            Item6(IntSize(575, 427), FillBounds, BottomEnd, IntOffset(0, 0)),
            Item6(IntSize(575, 427), Crop, TopStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), Crop, TopCenter, IntOffset(-575, 0)),
            Item6(IntSize(575, 427), Crop, TopEnd, IntOffset(-1150, 0)),
            Item6(IntSize(575, 427), Crop, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), Crop, Center, IntOffset(-575, 0)),
            Item6(IntSize(575, 427), Crop, CenterEnd, IntOffset(-1150, 0)),
            Item6(IntSize(575, 427), Crop, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(575, 427), Crop, BottomCenter, IntOffset(-575, 0)),
            Item6(IntSize(575, 427), Crop, BottomEnd, IntOffset(-1150, 0)),
            Item6(IntSize(551, 1038), None, TopStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), None, TopCenter, IntOffset(265, 0)),
            Item6(IntSize(551, 1038), None, TopEnd, IntOffset(529, 0)),
            Item6(IntSize(551, 1038), None, CenterStart, IntOffset(0, 309)),
            Item6(IntSize(551, 1038), None, Center, IntOffset(265, 309)),
            Item6(IntSize(551, 1038), None, CenterEnd, IntOffset(529, 309)),
            Item6(IntSize(551, 1038), None, BottomStart, IntOffset(0, 618)),
            Item6(IntSize(551, 1038), None, BottomCenter, IntOffset(265, 618)),
            Item6(IntSize(551, 1038), None, BottomEnd, IntOffset(529, 618)),
            Item6(IntSize(551, 1038), Inside, TopStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), Inside, TopCenter, IntOffset(265, 0)),
            Item6(IntSize(551, 1038), Inside, TopEnd, IntOffset(529, 0)),
            Item6(IntSize(551, 1038), Inside, CenterStart, IntOffset(0, 309)),
            Item6(IntSize(551, 1038), Inside, Center, IntOffset(265, 309)),
            Item6(IntSize(551, 1038), Inside, CenterEnd, IntOffset(529, 309)),
            Item6(IntSize(551, 1038), Inside, BottomStart, IntOffset(0, 618)),
            Item6(IntSize(551, 1038), Inside, BottomCenter, IntOffset(265, 618)),
            Item6(IntSize(551, 1038), Inside, BottomEnd, IntOffset(529, 618)),
            Item6(IntSize(551, 1038), Fit, TopStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), Fit, TopCenter, IntOffset(101, 0)),
            Item6(IntSize(551, 1038), Fit, TopEnd, IntOffset(201, 0)),
            Item6(IntSize(551, 1038), Fit, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), Fit, Center, IntOffset(101, 0)),
            Item6(IntSize(551, 1038), Fit, CenterEnd, IntOffset(201, 0)),
            Item6(IntSize(551, 1038), Fit, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), Fit, BottomCenter, IntOffset(101, 0)),
            Item6(IntSize(551, 1038), Fit, BottomEnd, IntOffset(201, 0)),
            Item6(IntSize(551, 1038), FillWidth, TopStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillWidth, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillWidth, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillWidth, CenterStart, IntOffset(0, -189)),
            Item6(IntSize(551, 1038), FillWidth, Center, IntOffset(0, -189)),
            Item6(IntSize(551, 1038), FillWidth, CenterEnd, IntOffset(0, -189)),
            Item6(IntSize(551, 1038), FillWidth, BottomStart, IntOffset(0, -379)),
            Item6(IntSize(551, 1038), FillWidth, BottomCenter, IntOffset(0, -379)),
            Item6(IntSize(551, 1038), FillWidth, BottomEnd, IntOffset(0, -379)),
            Item6(IntSize(551, 1038), FillHeight, TopStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillHeight, TopCenter, IntOffset(101, 0)),
            Item6(IntSize(551, 1038), FillHeight, TopEnd, IntOffset(201, 0)),
            Item6(IntSize(551, 1038), FillHeight, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillHeight, Center, IntOffset(101, 0)),
            Item6(IntSize(551, 1038), FillHeight, CenterEnd, IntOffset(201, 0)),
            Item6(IntSize(551, 1038), FillHeight, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillHeight, BottomCenter, IntOffset(101, 0)),
            Item6(IntSize(551, 1038), FillHeight, BottomEnd, IntOffset(201, 0)),
            Item6(IntSize(551, 1038), FillBounds, TopStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillBounds, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillBounds, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillBounds, CenterStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillBounds, Center, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillBounds, CenterEnd, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillBounds, BottomStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillBounds, BottomCenter, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), FillBounds, BottomEnd, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), Crop, TopStart, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), Crop, TopCenter, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), Crop, TopEnd, IntOffset(0, 0)),
            Item6(IntSize(551, 1038), Crop, CenterStart, IntOffset(0, -189)),
            Item6(IntSize(551, 1038), Crop, Center, IntOffset(0, -189)),
            Item6(IntSize(551, 1038), Crop, CenterEnd, IntOffset(0, -189)),
            Item6(IntSize(551, 1038), Crop, BottomStart, IntOffset(0, -379)),
            Item6(IntSize(551, 1038), Crop, BottomCenter, IntOffset(0, -379)),
            Item6(IntSize(551, 1038), Crop, BottomEnd, IntOffset(0, -379)),
        ).forEach { item ->
            val result = computeAlignmentIntOffset(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentScale = item.contentScale,
                alignment = item.alignment,
            )
            Assert.assertEquals(
                /* message = */ item.getMessage(containerSize),
                /* expected = */ item.expected,
                /* actual = */ result,
            )
        }
    }

    @Test
    fun testComputeLocationOffset() {
        val containerSize = IntSize(1000, 2000)

        var scale = 1f
        listOf(
            TransformOriginCompat(0.25f, 0.25f) to Offset(0f, 0f),
            TransformOriginCompat(0.75f, 0.25f) to Offset(-250f, 0f),
            TransformOriginCompat(0.5f, 0.5f) to Offset(-0f, -0f),
            TransformOriginCompat(0.25f, 0.75f) to Offset(0f, -500f),
            TransformOriginCompat(0.75f, 0.75f) to Offset(-250f, -500f),
        ).forEach { (containerOrigin, expected) ->
            val containerPoint = IntOffset(
                x = (containerOrigin.pivotFractionX * containerSize.width).roundToInt(),
                y = (containerOrigin.pivotFractionY * containerSize.height).roundToInt(),
            )
            Assert.assertEquals(
                /* message = */ "containerSize=$containerSize, scale=$scale, containerOrigin=$containerOrigin",
                /* expected = */ expected,
                /* actual = */ computeLocationUserOffset(containerSize, containerPoint, scale)
            )
        }

        scale = 2f
        listOf(
            TransformOriginCompat(0.25f, 0.25f) to Offset(-0f, -0f),
            TransformOriginCompat(0.75f, 0.25f) to Offset(-1000f, -0f),
            TransformOriginCompat(0.5f, 0.5f) to Offset(-500f, -1000f),
            TransformOriginCompat(0.25f, 0.75f) to Offset(-0f, -2000f),
            TransformOriginCompat(0.75f, 0.75f) to Offset(-1000f, -2000f),
        ).forEach { (containerOrigin, expected) ->
            val containerPoint = IntOffset(
                x = (containerOrigin.pivotFractionX * containerSize.width).roundToInt(),
                y = (containerOrigin.pivotFractionY * containerSize.height).roundToInt(),
            )
            Assert.assertEquals(
                /* message = */ "containerSize=$containerSize, scale=$scale, containerOrigin=$containerOrigin",
                /* expected = */ expected,
                /* actual = */ computeLocationUserOffset(containerSize, containerPoint, scale)
            )
        }
    }


    @Test
    fun testComputeContentInContainerRect() {
        val containerSize = IntSize(1080, 1656)
        val printBatchBuildExpression = false
//        val printBatchBuildExpression = true

        if (printBatchBuildExpression) {
            printlnBatchBuildExpression(
                p1s = listOf(
                    IntSize(7500, 232), IntSize(173, 3044), IntSize(575, 427), IntSize(551, 1038),
                ),
                p2s = listOf(None, Inside, Fit, FillWidth, FillHeight, FillBounds, Crop),
                p3s = listOf(
                    TopStart, TopCenter, TopEnd,
                    CenterStart, Center, CenterEnd,
                    BottomStart, BottomCenter, BottomEnd,
                ),
                buildItem = { p1, p2, p3 ->
                    Item5(p1, p2, p3, IntRect.Zero)
                },
            ) {
                computeContentInContainerRect(
                    containerSize = containerSize,
                    contentSize = it.contentSize,
                    contentScale = it.contentScale,
                    alignment = it.alignment,
                ).roundToIntRect()
            }
        }

        listOf(
            Item5(IntSize(7500, 232), None, TopStart, IntRect(0, 0, 7500, 232)),
            Item5(IntSize(7500, 232), None, TopCenter, IntRect(-3210, 0, 4290, 232)),
            Item5(IntSize(7500, 232), None, TopEnd, IntRect(-6420, 0, 1080, 232)),
            Item5(IntSize(7500, 232), None, CenterStart, IntRect(0, 712, 7500, 944)),
            Item5(IntSize(7500, 232), None, Center, IntRect(-3210, 712, 4290, 944)),
            Item5(IntSize(7500, 232), None, CenterEnd, IntRect(-6420, 712, 1080, 944)),
            Item5(IntSize(7500, 232), None, BottomStart, IntRect(0, 1424, 7500, 1656)),
            Item5(IntSize(7500, 232), None, BottomCenter, IntRect(-3210, 1424, 4290, 1656)),
            Item5(IntSize(7500, 232), None, BottomEnd, IntRect(-6420, 1424, 1080, 1656)),
            Item5(IntSize(7500, 232), Inside, TopStart, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Inside, TopCenter, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Inside, TopEnd, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Inside, CenterStart, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Inside, Center, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Inside, CenterEnd, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Inside, BottomStart, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), Inside, BottomCenter, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), Inside, BottomEnd, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), Fit, TopStart, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Fit, TopCenter, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Fit, TopEnd, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Fit, CenterStart, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Fit, Center, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Fit, CenterEnd, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Fit, BottomStart, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), Fit, BottomCenter, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), Fit, BottomEnd, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), FillWidth, TopStart, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), FillWidth, TopCenter, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), FillWidth, TopEnd, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), FillWidth, CenterStart, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), FillWidth, Center, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), FillWidth, CenterEnd, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), FillWidth, BottomStart, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), FillWidth, BottomCenter, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), FillWidth, BottomEnd, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, TopStart, IntRect(0, 0, 53534, 1656)),
            Item5(IntSize(7500, 232), FillHeight, TopCenter, IntRect(-26227, 0, 27307, 1656)),
            Item5(IntSize(7500, 232), FillHeight, TopEnd, IntRect(-52454, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, CenterStart, IntRect(0, 0, 53534, 1656)),
            Item5(IntSize(7500, 232), FillHeight, Center, IntRect(-26227, 0, 27307, 1656)),
            Item5(IntSize(7500, 232), FillHeight, CenterEnd, IntRect(-52454, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, BottomStart, IntRect(0, 0, 53534, 1656)),
            Item5(IntSize(7500, 232), FillHeight, BottomCenter, IntRect(-26227, 0, 27307, 1656)),
            Item5(IntSize(7500, 232), FillHeight, BottomEnd, IntRect(-52454, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, TopStart, IntRect(0, 0, 53534, 1656)),
            Item5(IntSize(7500, 232), Crop, TopCenter, IntRect(-26227, 0, 27307, 1656)),
            Item5(IntSize(7500, 232), Crop, TopEnd, IntRect(-52454, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, CenterStart, IntRect(0, 0, 53534, 1656)),
            Item5(IntSize(7500, 232), Crop, Center, IntRect(-26227, 0, 27307, 1656)),
            Item5(IntSize(7500, 232), Crop, CenterEnd, IntRect(-52454, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, BottomStart, IntRect(0, 0, 53534, 1656)),
            Item5(IntSize(7500, 232), Crop, BottomCenter, IntRect(-26227, 0, 27307, 1656)),
            Item5(IntSize(7500, 232), Crop, BottomEnd, IntRect(-52454, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), None, TopStart, IntRect(0, 0, 173, 3044)),
            Item5(IntSize(173, 3044), None, TopCenter, IntRect(454, 0, 627, 3044)),
            Item5(IntSize(173, 3044), None, TopEnd, IntRect(907, 0, 1080, 3044)),
            Item5(IntSize(173, 3044), None, CenterStart, IntRect(0, -694, 173, 2350)),
            Item5(IntSize(173, 3044), None, Center, IntRect(454, -694, 627, 2350)),
            Item5(IntSize(173, 3044), None, CenterEnd, IntRect(907, -694, 1080, 2350)),
            Item5(IntSize(173, 3044), None, BottomStart, IntRect(0, -1388, 173, 1656)),
            Item5(IntSize(173, 3044), None, BottomCenter, IntRect(454, -1388, 627, 1656)),
            Item5(IntSize(173, 3044), None, BottomEnd, IntRect(907, -1388, 1080, 1656)),
            Item5(IntSize(173, 3044), Inside, TopStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Inside, TopCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Inside, TopEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Inside, CenterStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Inside, Center, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Inside, CenterEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Inside, BottomStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Inside, BottomCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Inside, BottomEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Fit, TopStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Fit, TopCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Fit, TopEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Fit, CenterStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Fit, Center, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Fit, CenterEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Fit, BottomStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Fit, BottomCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Fit, BottomEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, TopStart, IntRect(0, 0, 1080, 19003)),
            Item5(IntSize(173, 3044), FillWidth, TopCenter, IntRect(0, 0, 1080, 19003)),
            Item5(IntSize(173, 3044), FillWidth, TopEnd, IntRect(0, 0, 1080, 19003)),
            Item5(IntSize(173, 3044), FillWidth, CenterStart, IntRect(0, -8673, 1080, 10330)),
            Item5(IntSize(173, 3044), FillWidth, Center, IntRect(0, -8673, 1080, 10330)),
            Item5(IntSize(173, 3044), FillWidth, CenterEnd, IntRect(0, -8673, 1080, 10330)),
            Item5(IntSize(173, 3044), FillWidth, BottomStart, IntRect(0, -17347, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, BottomCenter, IntRect(0, -17347, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, BottomEnd, IntRect(0, -17347, 1080, 1656)),
            Item5(IntSize(173, 3044), FillHeight, TopStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), FillHeight, TopCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), FillHeight, TopEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillHeight, CenterStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), FillHeight, Center, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), FillHeight, CenterEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillHeight, BottomStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), FillHeight, BottomCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), FillHeight, BottomEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, TopStart, IntRect(0, 0, 1080, 19003)),
            Item5(IntSize(173, 3044), Crop, TopCenter, IntRect(0, 0, 1080, 19003)),
            Item5(IntSize(173, 3044), Crop, TopEnd, IntRect(0, 0, 1080, 19003)),
            Item5(IntSize(173, 3044), Crop, CenterStart, IntRect(0, -8673, 1080, 10330)),
            Item5(IntSize(173, 3044), Crop, Center, IntRect(0, -8673, 1080, 10330)),
            Item5(IntSize(173, 3044), Crop, CenterEnd, IntRect(0, -8673, 1080, 10330)),
            Item5(IntSize(173, 3044), Crop, BottomStart, IntRect(0, -17347, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, BottomCenter, IntRect(0, -17347, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, BottomEnd, IntRect(0, -17347, 1080, 1656)),
            Item5(IntSize(575, 427), None, TopStart, IntRect(0, 0, 575, 427)),
            Item5(IntSize(575, 427), None, TopCenter, IntRect(253, 0, 828, 427)),
            Item5(IntSize(575, 427), None, TopEnd, IntRect(505, 0, 1080, 427)),
            Item5(IntSize(575, 427), None, CenterStart, IntRect(0, 615, 575, 1042)),
            Item5(IntSize(575, 427), None, Center, IntRect(253, 615, 828, 1042)),
            Item5(IntSize(575, 427), None, CenterEnd, IntRect(505, 615, 1080, 1042)),
            Item5(IntSize(575, 427), None, BottomStart, IntRect(0, 1229, 575, 1656)),
            Item5(IntSize(575, 427), None, BottomCenter, IntRect(253, 1229, 828, 1656)),
            Item5(IntSize(575, 427), None, BottomEnd, IntRect(505, 1229, 1080, 1656)),
            Item5(IntSize(575, 427), Inside, TopStart, IntRect(0, 0, 575, 427)),
            Item5(IntSize(575, 427), Inside, TopCenter, IntRect(253, 0, 828, 427)),
            Item5(IntSize(575, 427), Inside, TopEnd, IntRect(505, 0, 1080, 427)),
            Item5(IntSize(575, 427), Inside, CenterStart, IntRect(0, 615, 575, 1042)),
            Item5(IntSize(575, 427), Inside, Center, IntRect(253, 615, 828, 1042)),
            Item5(IntSize(575, 427), Inside, CenterEnd, IntRect(505, 615, 1080, 1042)),
            Item5(IntSize(575, 427), Inside, BottomStart, IntRect(0, 1229, 575, 1656)),
            Item5(IntSize(575, 427), Inside, BottomCenter, IntRect(253, 1229, 828, 1656)),
            Item5(IntSize(575, 427), Inside, BottomEnd, IntRect(505, 1229, 1080, 1656)),
            Item5(IntSize(575, 427), Fit, TopStart, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), Fit, TopCenter, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), Fit, TopEnd, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), Fit, CenterStart, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), Fit, Center, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), Fit, CenterEnd, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), Fit, BottomStart, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), Fit, BottomCenter, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), Fit, BottomEnd, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), FillWidth, TopStart, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), FillWidth, TopCenter, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), FillWidth, TopEnd, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), FillWidth, CenterStart, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), FillWidth, Center, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), FillWidth, CenterEnd, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), FillWidth, BottomStart, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), FillWidth, BottomCenter, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), FillWidth, BottomEnd, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, TopStart, IntRect(0, 0, 2230, 1656)),
            Item5(IntSize(575, 427), FillHeight, TopCenter, IntRect(-575, 0, 1655, 1656)),
            Item5(IntSize(575, 427), FillHeight, TopEnd, IntRect(-1150, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, CenterStart, IntRect(0, 0, 2230, 1656)),
            Item5(IntSize(575, 427), FillHeight, Center, IntRect(-575, 0, 1655, 1656)),
            Item5(IntSize(575, 427), FillHeight, CenterEnd, IntRect(-1150, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, BottomStart, IntRect(0, 0, 2230, 1656)),
            Item5(IntSize(575, 427), FillHeight, BottomCenter, IntRect(-575, 0, 1655, 1656)),
            Item5(IntSize(575, 427), FillHeight, BottomEnd, IntRect(-1150, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, TopStart, IntRect(0, 0, 2230, 1656)),
            Item5(IntSize(575, 427), Crop, TopCenter, IntRect(-575, 0, 1655, 1656)),
            Item5(IntSize(575, 427), Crop, TopEnd, IntRect(-1150, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, CenterStart, IntRect(0, 0, 2230, 1656)),
            Item5(IntSize(575, 427), Crop, Center, IntRect(-575, 0, 1655, 1656)),
            Item5(IntSize(575, 427), Crop, CenterEnd, IntRect(-1150, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, BottomStart, IntRect(0, 0, 2230, 1656)),
            Item5(IntSize(575, 427), Crop, BottomCenter, IntRect(-575, 0, 1655, 1656)),
            Item5(IntSize(575, 427), Crop, BottomEnd, IntRect(-1150, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), None, TopStart, IntRect(0, 0, 551, 1038)),
            Item5(IntSize(551, 1038), None, TopCenter, IntRect(265, 0, 816, 1038)),
            Item5(IntSize(551, 1038), None, TopEnd, IntRect(529, 0, 1080, 1038)),
            Item5(IntSize(551, 1038), None, CenterStart, IntRect(0, 309, 551, 1347)),
            Item5(IntSize(551, 1038), None, Center, IntRect(265, 309, 816, 1347)),
            Item5(IntSize(551, 1038), None, CenterEnd, IntRect(529, 309, 1080, 1347)),
            Item5(IntSize(551, 1038), None, BottomStart, IntRect(0, 618, 551, 1656)),
            Item5(IntSize(551, 1038), None, BottomCenter, IntRect(265, 618, 816, 1656)),
            Item5(IntSize(551, 1038), None, BottomEnd, IntRect(529, 618, 1080, 1656)),
            Item5(IntSize(551, 1038), Inside, TopStart, IntRect(0, 0, 551, 1038)),
            Item5(IntSize(551, 1038), Inside, TopCenter, IntRect(265, 0, 816, 1038)),
            Item5(IntSize(551, 1038), Inside, TopEnd, IntRect(529, 0, 1080, 1038)),
            Item5(IntSize(551, 1038), Inside, CenterStart, IntRect(0, 309, 551, 1347)),
            Item5(IntSize(551, 1038), Inside, Center, IntRect(265, 309, 816, 1347)),
            Item5(IntSize(551, 1038), Inside, CenterEnd, IntRect(529, 309, 1080, 1347)),
            Item5(IntSize(551, 1038), Inside, BottomStart, IntRect(0, 618, 551, 1656)),
            Item5(IntSize(551, 1038), Inside, BottomCenter, IntRect(265, 618, 816, 1656)),
            Item5(IntSize(551, 1038), Inside, BottomEnd, IntRect(529, 618, 1080, 1656)),
            Item5(IntSize(551, 1038), Fit, TopStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), Fit, TopCenter, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), Fit, TopEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Fit, CenterStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), Fit, Center, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), Fit, CenterEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Fit, BottomStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), Fit, BottomCenter, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), Fit, BottomEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, TopStart, IntRect(0, 0, 1080, 2035)),
            Item5(IntSize(551, 1038), FillWidth, TopCenter, IntRect(0, 0, 1080, 2035)),
            Item5(IntSize(551, 1038), FillWidth, TopEnd, IntRect(0, 0, 1080, 2035)),
            Item5(IntSize(551, 1038), FillWidth, CenterStart, IntRect(0, -189, 1080, 1846)),
            Item5(IntSize(551, 1038), FillWidth, Center, IntRect(0, -189, 1080, 1846)),
            Item5(IntSize(551, 1038), FillWidth, CenterEnd, IntRect(0, -189, 1080, 1846)),
            Item5(IntSize(551, 1038), FillWidth, BottomStart, IntRect(0, -379, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, BottomCenter, IntRect(0, -379, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, BottomEnd, IntRect(0, -379, 1080, 1656)),
            Item5(IntSize(551, 1038), FillHeight, TopStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), FillHeight, TopCenter, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), FillHeight, TopEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillHeight, CenterStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), FillHeight, Center, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), FillHeight, CenterEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillHeight, BottomStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), FillHeight, BottomCenter, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), FillHeight, BottomEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, TopStart, IntRect(0, 0, 1080, 2035)),
            Item5(IntSize(551, 1038), Crop, TopCenter, IntRect(0, 0, 1080, 2035)),
            Item5(IntSize(551, 1038), Crop, TopEnd, IntRect(0, 0, 1080, 2035)),
            Item5(IntSize(551, 1038), Crop, CenterStart, IntRect(0, -189, 1080, 1846)),
            Item5(IntSize(551, 1038), Crop, Center, IntRect(0, -189, 1080, 1846)),
            Item5(IntSize(551, 1038), Crop, CenterEnd, IntRect(0, -189, 1080, 1846)),
            Item5(IntSize(551, 1038), Crop, BottomStart, IntRect(0, -379, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, BottomCenter, IntRect(0, -379, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, BottomEnd, IntRect(0, -379, 1080, 1656)),
        ).forEach { item ->
            val result = computeContentInContainerRect(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentScale = item.contentScale,
                alignment = item.alignment,
            ).roundToIntRect()
            Assert.assertEquals(
                /* message = */ item.getMessage(containerSize),
                /* expected = */ item.expected,
                /* actual = */ result,
            )
        }
    }

    @Test
    fun testComputeContentInContainerInnerRect() {
        val containerSize = IntSize(1080, 1656)
        val printBatchBuildExpression = false
//        val printBatchBuildExpression = true

        if (printBatchBuildExpression) {
            printlnBatchBuildExpression(
                p1s = listOf(
                    IntSize(7500, 232), IntSize(173, 3044), IntSize(575, 427), IntSize(551, 1038),
                ),
                p2s = listOf(None, Inside, Fit, FillWidth, FillHeight, FillBounds, Crop),
                p3s = listOf(
                    TopStart, TopCenter, TopEnd,
                    CenterStart, Center, CenterEnd,
                    BottomStart, BottomCenter, BottomEnd,
                ),
                buildItem = { p1, p2, p3 ->
                    Item5(p1, p2, p3, IntRect.Zero)
                },
            ) {
                computeContentInContainerInnerRect(
                    containerSize = containerSize,
                    contentSize = it.contentSize,
                    contentScale = it.contentScale,
                    alignment = it.alignment,
                ).roundToIntRect()
            }
        }

        listOf(
            Item5(IntSize(7500, 232), None, TopStart, IntRect(0, 0, 1080, 232)),
            Item5(IntSize(7500, 232), None, TopCenter, IntRect(0, 0, 1080, 232)),
            Item5(IntSize(7500, 232), None, TopEnd, IntRect(0, 0, 1080, 232)),
            Item5(IntSize(7500, 232), None, CenterStart, IntRect(0, 712, 1080, 944)),
            Item5(IntSize(7500, 232), None, Center, IntRect(0, 712, 1080, 944)),
            Item5(IntSize(7500, 232), None, CenterEnd, IntRect(0, 712, 1080, 944)),
            Item5(IntSize(7500, 232), None, BottomStart, IntRect(0, 1424, 1080, 1656)),
            Item5(IntSize(7500, 232), None, BottomCenter, IntRect(0, 1424, 1080, 1656)),
            Item5(IntSize(7500, 232), None, BottomEnd, IntRect(0, 1424, 1080, 1656)),
            Item5(IntSize(7500, 232), Inside, TopStart, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Inside, TopCenter, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Inside, TopEnd, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Inside, CenterStart, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Inside, Center, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Inside, CenterEnd, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Inside, BottomStart, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), Inside, BottomCenter, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), Inside, BottomEnd, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), Fit, TopStart, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Fit, TopCenter, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Fit, TopEnd, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), Fit, CenterStart, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Fit, Center, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Fit, CenterEnd, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), Fit, BottomStart, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), Fit, BottomCenter, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), Fit, BottomEnd, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), FillWidth, TopStart, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), FillWidth, TopCenter, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), FillWidth, TopEnd, IntRect(0, 0, 1080, 33)),
            Item5(IntSize(7500, 232), FillWidth, CenterStart, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), FillWidth, Center, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), FillWidth, CenterEnd, IntRect(0, 812, 1080, 845)),
            Item5(IntSize(7500, 232), FillWidth, BottomStart, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), FillWidth, BottomCenter, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), FillWidth, BottomEnd, IntRect(0, 1623, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillHeight, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), FillBounds, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(7500, 232), Crop, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), None, TopStart, IntRect(0, 0, 173, 1656)),
            Item5(IntSize(173, 3044), None, TopCenter, IntRect(454, 0, 627, 1656)),
            Item5(IntSize(173, 3044), None, TopEnd, IntRect(907, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), None, CenterStart, IntRect(0, 0, 173, 1656)),
            Item5(IntSize(173, 3044), None, Center, IntRect(454, 0, 627, 1656)),
            Item5(IntSize(173, 3044), None, CenterEnd, IntRect(907, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), None, BottomStart, IntRect(0, 0, 173, 1656)),
            Item5(IntSize(173, 3044), None, BottomCenter, IntRect(454, 0, 627, 1656)),
            Item5(IntSize(173, 3044), None, BottomEnd, IntRect(907, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Inside, TopStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Inside, TopCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Inside, TopEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Inside, CenterStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Inside, Center, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Inside, CenterEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Inside, BottomStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Inside, BottomCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Inside, BottomEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Fit, TopStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Fit, TopCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Fit, TopEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Fit, CenterStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Fit, Center, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Fit, CenterEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Fit, BottomStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), Fit, BottomCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), Fit, BottomEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillWidth, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillHeight, TopStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), FillHeight, TopCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), FillHeight, TopEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillHeight, CenterStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), FillHeight, Center, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), FillHeight, CenterEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillHeight, BottomStart, IntRect(0, 0, 94, 1656)),
            Item5(IntSize(173, 3044), FillHeight, BottomCenter, IntRect(493, 0, 587, 1656)),
            Item5(IntSize(173, 3044), FillHeight, BottomEnd, IntRect(986, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), FillBounds, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(173, 3044), Crop, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), None, TopStart, IntRect(0, 0, 575, 427)),
            Item5(IntSize(575, 427), None, TopCenter, IntRect(253, 0, 828, 427)),
            Item5(IntSize(575, 427), None, TopEnd, IntRect(505, 0, 1080, 427)),
            Item5(IntSize(575, 427), None, CenterStart, IntRect(0, 615, 575, 1042)),
            Item5(IntSize(575, 427), None, Center, IntRect(253, 615, 828, 1042)),
            Item5(IntSize(575, 427), None, CenterEnd, IntRect(505, 615, 1080, 1042)),
            Item5(IntSize(575, 427), None, BottomStart, IntRect(0, 1229, 575, 1656)),
            Item5(IntSize(575, 427), None, BottomCenter, IntRect(253, 1229, 828, 1656)),
            Item5(IntSize(575, 427), None, BottomEnd, IntRect(505, 1229, 1080, 1656)),
            Item5(IntSize(575, 427), Inside, TopStart, IntRect(0, 0, 575, 427)),
            Item5(IntSize(575, 427), Inside, TopCenter, IntRect(253, 0, 828, 427)),
            Item5(IntSize(575, 427), Inside, TopEnd, IntRect(505, 0, 1080, 427)),
            Item5(IntSize(575, 427), Inside, CenterStart, IntRect(0, 615, 575, 1042)),
            Item5(IntSize(575, 427), Inside, Center, IntRect(253, 615, 828, 1042)),
            Item5(IntSize(575, 427), Inside, CenterEnd, IntRect(505, 615, 1080, 1042)),
            Item5(IntSize(575, 427), Inside, BottomStart, IntRect(0, 1229, 575, 1656)),
            Item5(IntSize(575, 427), Inside, BottomCenter, IntRect(253, 1229, 828, 1656)),
            Item5(IntSize(575, 427), Inside, BottomEnd, IntRect(505, 1229, 1080, 1656)),
            Item5(IntSize(575, 427), Fit, TopStart, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), Fit, TopCenter, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), Fit, TopEnd, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), Fit, CenterStart, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), Fit, Center, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), Fit, CenterEnd, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), Fit, BottomStart, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), Fit, BottomCenter, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), Fit, BottomEnd, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), FillWidth, TopStart, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), FillWidth, TopCenter, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), FillWidth, TopEnd, IntRect(0, 0, 1080, 802)),
            Item5(IntSize(575, 427), FillWidth, CenterStart, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), FillWidth, Center, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), FillWidth, CenterEnd, IntRect(0, 427, 1080, 1229)),
            Item5(IntSize(575, 427), FillWidth, BottomStart, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), FillWidth, BottomCenter, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), FillWidth, BottomEnd, IntRect(0, 854, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillHeight, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), FillBounds, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(575, 427), Crop, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), None, TopStart, IntRect(0, 0, 551, 1038)),
            Item5(IntSize(551, 1038), None, TopCenter, IntRect(265, 0, 816, 1038)),
            Item5(IntSize(551, 1038), None, TopEnd, IntRect(529, 0, 1080, 1038)),
            Item5(IntSize(551, 1038), None, CenterStart, IntRect(0, 309, 551, 1347)),
            Item5(IntSize(551, 1038), None, Center, IntRect(265, 309, 816, 1347)),
            Item5(IntSize(551, 1038), None, CenterEnd, IntRect(529, 309, 1080, 1347)),
            Item5(IntSize(551, 1038), None, BottomStart, IntRect(0, 618, 551, 1656)),
            Item5(IntSize(551, 1038), None, BottomCenter, IntRect(265, 618, 816, 1656)),
            Item5(IntSize(551, 1038), None, BottomEnd, IntRect(529, 618, 1080, 1656)),
            Item5(IntSize(551, 1038), Inside, TopStart, IntRect(0, 0, 551, 1038)),
            Item5(IntSize(551, 1038), Inside, TopCenter, IntRect(265, 0, 816, 1038)),
            Item5(IntSize(551, 1038), Inside, TopEnd, IntRect(529, 0, 1080, 1038)),
            Item5(IntSize(551, 1038), Inside, CenterStart, IntRect(0, 309, 551, 1347)),
            Item5(IntSize(551, 1038), Inside, Center, IntRect(265, 309, 816, 1347)),
            Item5(IntSize(551, 1038), Inside, CenterEnd, IntRect(529, 309, 1080, 1347)),
            Item5(IntSize(551, 1038), Inside, BottomStart, IntRect(0, 618, 551, 1656)),
            Item5(IntSize(551, 1038), Inside, BottomCenter, IntRect(265, 618, 816, 1656)),
            Item5(IntSize(551, 1038), Inside, BottomEnd, IntRect(529, 618, 1080, 1656)),
            Item5(IntSize(551, 1038), Fit, TopStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), Fit, TopCenter, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), Fit, TopEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Fit, CenterStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), Fit, Center, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), Fit, CenterEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Fit, BottomStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), Fit, BottomCenter, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), Fit, BottomEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillWidth, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillHeight, TopStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), FillHeight, TopCenter, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), FillHeight, TopEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillHeight, CenterStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), FillHeight, Center, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), FillHeight, CenterEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillHeight, BottomStart, IntRect(0, 0, 879, 1656)),
            Item5(IntSize(551, 1038), FillHeight, BottomCenter, IntRect(101, 0, 980, 1656)),
            Item5(IntSize(551, 1038), FillHeight, BottomEnd, IntRect(201, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), FillBounds, BottomEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, TopStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, TopCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, TopEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, CenterStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, Center, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, CenterEnd, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, BottomStart, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, BottomCenter, IntRect(0, 0, 1080, 1656)),
            Item5(IntSize(551, 1038), Crop, BottomEnd, IntRect(0, 0, 1080, 1656)),
        ).forEach { item ->
            val result = computeContentInContainerInnerRect(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentScale = item.contentScale,
                alignment = item.alignment,
            ).roundToIntRect()
            Assert.assertEquals(
                /* message = */ item.getMessage(containerSize),
                /* expected = */ item.expected,
                /* actual = */ result,
            )
        }
    }


    @Test
    fun testComputeContainerVisibleRect() {
        val containerSize = IntSize(1000, 2000)

        var scale = 1f
        listOf(
            Offset(0f, 0f) to IntRect(0, 0, 1000, 2000),
            Offset(250f, 500f) to IntRect(0, 0, 750, 1500),
            Offset(750f, 500f) to IntRect(0, 0, 250, 1500),
            Offset(250f, 1500f) to IntRect(0, 0, 750, 500),
            Offset(750f, 1500f) to IntRect(0, 0, 250, 500),
            Offset(1000f, 2000f) to IntRect(0, 0, 0, 0),
            Offset(-250f, -500f) to IntRect(250, 500, 1000, 2000),
            Offset(-750f, -500f) to IntRect(750, 500, 1000, 2000),
            Offset(-250f, -1500f) to IntRect(250, 1500, 1000, 2000),
            Offset(-750f, -1500f) to IntRect(750, 1500, 1000, 2000),
            Offset(-1000f, -2000f) to IntRect(0, 0, 0, 0),
        ).forEach { (offset, expectedVisibleRect) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, offset=$offset",
                expectedVisibleRect,
                computeContainerVisibleRect(containerSize, scale, offset).roundToIntRect()
            )
        }

        scale = 2f
        listOf(
            Offset(0f, 0f) to IntRect(0, 0, 500, 1000),
            Offset(250f, 500f) to IntRect(0, 0, 375, 750),
            Offset(750f, 500f) to IntRect(0, 0, 125, 750),
            Offset(250f, 1500f) to IntRect(0, 0, 375, 250),
            Offset(750f, 1500f) to IntRect(0, 0, 125, 250),
            Offset(1000f, 2000f) to IntRect(0, 0, 0, 0),
            Offset(-250f, -500f) to IntRect(125, 250, 625, 1250),
            Offset(-750f, -500f) to IntRect(375, 250, 875, 1250),
            Offset(-250f, -1500f) to IntRect(125, 750, 625, 1750),
            Offset(-750f, -1500f) to IntRect(375, 750, 875, 1750),
            Offset(-1000f, -2000f) to IntRect(500, 1000, 1000, 2000),
        ).forEach { (offset, expectedVisibleRect) ->
            Assert.assertEquals(
                "containerSize=$containerSize, scale=$scale, offset=$offset",
                expectedVisibleRect,
                computeContainerVisibleRect(containerSize, scale, offset).roundToIntRect()
            )
        }
    }

    // todo test

    //    @Test
//    fun testComputeContentInContainerVisibleRect() {
//        val containerSize = IntSize(1000, 1000)
//
//        var contentSize = IntSize(800, 400)
//        listOf(
//            Item(ContentScale.None, Alignment.TopStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.None, Alignment.TopCenter, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.None, Alignment.TopEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.None, Alignment.CenterStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.None, Alignment.Center, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.None, Alignment.CenterEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.None, Alignment.BottomStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.None, Alignment.BottomCenter, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.None, Alignment.BottomEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Inside, Alignment.TopStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Inside, Alignment.TopCenter, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Inside, Alignment.TopEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Inside, Alignment.CenterStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Inside, Alignment.Center, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Inside, Alignment.CenterEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Inside, Alignment.BottomStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Inside, Alignment.BottomCenter, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Inside, Alignment.BottomEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Fit, Alignment.TopStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Fit, Alignment.TopCenter, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Fit, Alignment.TopEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Fit, Alignment.CenterStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Fit, Alignment.Center, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Fit, Alignment.CenterEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Fit, Alignment.BottomStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Fit, Alignment.BottomCenter, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Fit, Alignment.BottomEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillWidth, Alignment.TopStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillWidth, Alignment.TopCenter, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillWidth, Alignment.TopEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillWidth, Alignment.CenterStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillWidth, Alignment.Center, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillWidth, Alignment.CenterEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillWidth, Alignment.BottomStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillWidth, Alignment.BottomCenter, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillWidth, Alignment.BottomEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillHeight, Alignment.TopStart, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.FillHeight, Alignment.TopCenter, IntRect(200, 0, 600, 400)),
//            Item(ContentScale.FillHeight, Alignment.TopEnd, IntRect(400, 0, 800, 400)),
//            Item(ContentScale.FillHeight, Alignment.CenterStart, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.FillHeight, Alignment.Center, IntRect(200, 0, 600, 400)),
//            Item(ContentScale.FillHeight, Alignment.CenterEnd, IntRect(400, 0, 800, 400)),
//            Item(ContentScale.FillHeight, Alignment.BottomStart, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.FillHeight, Alignment.BottomCenter, IntRect(200, 0, 600, 400)),
//            Item(ContentScale.FillHeight, Alignment.BottomEnd, IntRect(400, 0, 800, 400)),
//            Item(ContentScale.FillBounds, Alignment.TopStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillBounds, Alignment.TopCenter, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillBounds, Alignment.TopEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillBounds, Alignment.CenterStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillBounds, Alignment.Center, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillBounds, Alignment.CenterEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillBounds, Alignment.BottomStart, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillBounds, Alignment.BottomCenter, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.FillBounds, Alignment.BottomEnd, IntRect(0, 0, 800, 400)),
//            Item(ContentScale.Crop, Alignment.TopStart, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.Crop, Alignment.TopCenter, IntRect(200, 0, 600, 400)),
//            Item(ContentScale.Crop, Alignment.TopEnd, IntRect(400, 0, 800, 400)),
//            Item(ContentScale.Crop, Alignment.CenterStart, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.Crop, Alignment.Center, IntRect(200, 0, 600, 400)),
//            Item(ContentScale.Crop, Alignment.CenterEnd, IntRect(400, 0, 800, 400)),
//            Item(ContentScale.Crop, Alignment.BottomStart, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.Crop, Alignment.BottomCenter, IntRect(200, 0, 600, 400)),
//            Item(ContentScale.Crop, Alignment.BottomEnd, IntRect(400, 0, 800, 400)),
////        ).printlnExpectedMessage(
////            computeExpected =  {
////                computeContentInContainerVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = it.contentScale,
////                    alignment = it.alignment,
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize),
//                it.expected,
//                computeContentInContainerVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    alignment = it.alignment,
//                )
//            )
//        }
//
//        contentSize = IntSize(400, 800)
//        listOf(
//            Item(ContentScale.None, Alignment.TopStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.None, Alignment.TopCenter, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.None, Alignment.TopEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.None, Alignment.CenterStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.None, Alignment.Center, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.None, Alignment.CenterEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.None, Alignment.BottomStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.None, Alignment.BottomCenter, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.None, Alignment.BottomEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Inside, Alignment.TopStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Inside, Alignment.TopCenter, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Inside, Alignment.TopEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Inside, Alignment.CenterStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Inside, Alignment.Center, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Inside, Alignment.CenterEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Inside, Alignment.BottomStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Inside, Alignment.BottomCenter, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Inside, Alignment.BottomEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Fit, Alignment.TopStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Fit, Alignment.TopCenter, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Fit, Alignment.TopEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Fit, Alignment.CenterStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Fit, Alignment.Center, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Fit, Alignment.CenterEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Fit, Alignment.BottomStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Fit, Alignment.BottomCenter, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Fit, Alignment.BottomEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillWidth, Alignment.TopStart, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.FillWidth, Alignment.TopCenter, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.FillWidth, Alignment.TopEnd, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.FillWidth, Alignment.CenterStart, IntRect(0, 200, 400, 600)),
//            Item(ContentScale.FillWidth, Alignment.Center, IntRect(0, 200, 400, 600)),
//            Item(ContentScale.FillWidth, Alignment.CenterEnd, IntRect(0, 200, 400, 600)),
//            Item(ContentScale.FillWidth, Alignment.BottomStart, IntRect(0, 400, 400, 800)),
//            Item(ContentScale.FillWidth, Alignment.BottomCenter, IntRect(0, 400, 400, 800)),
//            Item(ContentScale.FillWidth, Alignment.BottomEnd, IntRect(0, 400, 400, 800)),
//            Item(ContentScale.FillHeight, Alignment.TopStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillHeight, Alignment.TopCenter, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillHeight, Alignment.TopEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillHeight, Alignment.CenterStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillHeight, Alignment.Center, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillHeight, Alignment.CenterEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillHeight, Alignment.BottomStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillHeight, Alignment.BottomCenter, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillHeight, Alignment.BottomEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillBounds, Alignment.TopStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillBounds, Alignment.TopCenter, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillBounds, Alignment.TopEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillBounds, Alignment.CenterStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillBounds, Alignment.Center, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillBounds, Alignment.CenterEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillBounds, Alignment.BottomStart, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillBounds, Alignment.BottomCenter, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.FillBounds, Alignment.BottomEnd, IntRect(0, 0, 400, 800)),
//            Item(ContentScale.Crop, Alignment.TopStart, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.Crop, Alignment.TopCenter, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.Crop, Alignment.TopEnd, IntRect(0, 0, 400, 400)),
//            Item(ContentScale.Crop, Alignment.CenterStart, IntRect(0, 200, 400, 600)),
//            Item(ContentScale.Crop, Alignment.Center, IntRect(0, 200, 400, 600)),
//            Item(ContentScale.Crop, Alignment.CenterEnd, IntRect(0, 200, 400, 600)),
//            Item(ContentScale.Crop, Alignment.BottomStart, IntRect(0, 400, 400, 800)),
//            Item(ContentScale.Crop, Alignment.BottomCenter, IntRect(0, 400, 400, 800)),
//            Item(ContentScale.Crop, Alignment.BottomEnd, IntRect(0, 400, 400, 800)),
////        ).printlnExpectedMessage(
////            computeExpected =  {
////                computeContentInContainerVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = it.contentScale,
////                    alignment = it.alignment,
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize),
//                it.expected,
//                computeContentInContainerVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    alignment = it.alignment,
//                )
//            )
//        }
//
//        contentSize = IntSize(1600, 1200)
//        listOf(
//            Item(ContentScale.None, Alignment.TopStart, IntRect(0, 0, 1000, 1000)),
//            Item(ContentScale.None, Alignment.TopCenter, IntRect(300, 0, 1300, 1000)),
//            Item(ContentScale.None, Alignment.TopEnd, IntRect(600, 0, 1600, 1000)),
//            Item(ContentScale.None, Alignment.CenterStart, IntRect(0, 100, 1000, 1100)),
//            Item(ContentScale.None, Alignment.Center, IntRect(300, 100, 1300, 1100)),
//            Item(ContentScale.None, Alignment.CenterEnd, IntRect(600, 100, 1600, 1100)),
//            Item(ContentScale.None, Alignment.BottomStart, IntRect(0, 200, 1000, 1200)),
//            Item(ContentScale.None, Alignment.BottomCenter, IntRect(300, 200, 1300, 1200)),
//            Item(ContentScale.None, Alignment.BottomEnd, IntRect(600, 200, 1600, 1200)),
//            Item(ContentScale.Inside, Alignment.TopStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Inside, Alignment.TopCenter, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Inside, Alignment.TopEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Inside, Alignment.CenterStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Inside, Alignment.Center, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Inside, Alignment.CenterEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Inside, Alignment.BottomStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Inside, Alignment.BottomCenter, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Inside, Alignment.BottomEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Fit, Alignment.TopStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Fit, Alignment.TopCenter, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Fit, Alignment.TopEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Fit, Alignment.CenterStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Fit, Alignment.Center, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Fit, Alignment.CenterEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Fit, Alignment.BottomStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Fit, Alignment.BottomCenter, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Fit, Alignment.BottomEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillWidth, Alignment.TopStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillWidth, Alignment.TopCenter, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillWidth, Alignment.TopEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillWidth, Alignment.CenterStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillWidth, Alignment.Center, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillWidth, Alignment.CenterEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillWidth, Alignment.BottomStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillWidth, Alignment.BottomCenter, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillWidth, Alignment.BottomEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillHeight, Alignment.TopStart, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.FillHeight, Alignment.TopCenter, IntRect(199, 0, 1400, 1200)),
//            Item(ContentScale.FillHeight, Alignment.TopEnd, IntRect(399, 0, 1600, 1200)),
//            Item(ContentScale.FillHeight, Alignment.CenterStart, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.FillHeight, Alignment.Center, IntRect(199, 0, 1400, 1200)),
//            Item(ContentScale.FillHeight, Alignment.CenterEnd, IntRect(399, 0, 1600, 1200)),
//            Item(ContentScale.FillHeight, Alignment.BottomStart, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.FillHeight, Alignment.BottomCenter, IntRect(199, 0, 1400, 1200)),
//            Item(ContentScale.FillHeight, Alignment.BottomEnd, IntRect(399, 0, 1600, 1200)),
//            Item(ContentScale.FillBounds, Alignment.TopStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillBounds, Alignment.TopCenter, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillBounds, Alignment.TopEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillBounds, Alignment.CenterStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillBounds, Alignment.Center, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillBounds, Alignment.CenterEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillBounds, Alignment.BottomStart, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillBounds, Alignment.BottomCenter, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.FillBounds, Alignment.BottomEnd, IntRect(0, 0, 1600, 1200)),
//            Item(ContentScale.Crop, Alignment.TopStart, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.Crop, Alignment.TopCenter, IntRect(199, 0, 1400, 1200)),
//            Item(ContentScale.Crop, Alignment.TopEnd, IntRect(399, 0, 1600, 1200)),
//            Item(ContentScale.Crop, Alignment.CenterStart, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.Crop, Alignment.Center, IntRect(199, 0, 1400, 1200)),
//            Item(ContentScale.Crop, Alignment.CenterEnd, IntRect(399, 0, 1600, 1200)),
//            Item(ContentScale.Crop, Alignment.BottomStart, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.Crop, Alignment.BottomCenter, IntRect(199, 0, 1400, 1200)),
//            Item(ContentScale.Crop, Alignment.BottomEnd, IntRect(399, 0, 1600, 1200)),
////        ).printlnExpectedMessage(
////            computeExpected =  {
////                computeContentInContainerVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = it.contentScale,
////                    alignment = it.alignment,
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize),
//                it.expected,
//                computeContentInContainerVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    alignment = it.alignment,
//                )
//            )
//        }
//
//        contentSize = IntSize(1200, 1600)
//        listOf(
//            Item(ContentScale.None, Alignment.TopStart, IntRect(0, 0, 1000, 1000)),
//            Item(ContentScale.None, Alignment.TopCenter, IntRect(100, 0, 1100, 1000)),
//            Item(ContentScale.None, Alignment.TopEnd, IntRect(200, 0, 1200, 1000)),
//            Item(ContentScale.None, Alignment.CenterStart, IntRect(0, 300, 1000, 1300)),
//            Item(ContentScale.None, Alignment.Center, IntRect(100, 300, 1100, 1300)),
//            Item(ContentScale.None, Alignment.CenterEnd, IntRect(200, 300, 1200, 1300)),
//            Item(ContentScale.None, Alignment.BottomStart, IntRect(0, 600, 1000, 1600)),
//            Item(ContentScale.None, Alignment.BottomCenter, IntRect(100, 600, 1100, 1600)),
//            Item(ContentScale.None, Alignment.BottomEnd, IntRect(200, 600, 1200, 1600)),
//            Item(ContentScale.Inside, Alignment.TopStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Inside, Alignment.TopCenter, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Inside, Alignment.TopEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Inside, Alignment.CenterStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Inside, Alignment.Center, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Inside, Alignment.CenterEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Inside, Alignment.BottomStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Inside, Alignment.BottomCenter, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Inside, Alignment.BottomEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Fit, Alignment.TopStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Fit, Alignment.TopCenter, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Fit, Alignment.TopEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Fit, Alignment.CenterStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Fit, Alignment.Center, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Fit, Alignment.CenterEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Fit, Alignment.BottomStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Fit, Alignment.BottomCenter, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Fit, Alignment.BottomEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillWidth, Alignment.TopStart, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.FillWidth, Alignment.TopCenter, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.FillWidth, Alignment.TopEnd, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.FillWidth, Alignment.CenterStart, IntRect(0, 199, 1200, 1400)),
//            Item(ContentScale.FillWidth, Alignment.Center, IntRect(0, 199, 1200, 1400)),
//            Item(ContentScale.FillWidth, Alignment.CenterEnd, IntRect(0, 199, 1200, 1400)),
//            Item(ContentScale.FillWidth, Alignment.BottomStart, IntRect(0, 399, 1200, 1600)),
//            Item(ContentScale.FillWidth, Alignment.BottomCenter, IntRect(0, 399, 1200, 1600)),
//            Item(ContentScale.FillWidth, Alignment.BottomEnd, IntRect(0, 399, 1200, 1600)),
//            Item(ContentScale.FillHeight, Alignment.TopStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillHeight, Alignment.TopCenter, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillHeight, Alignment.TopEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillHeight, Alignment.CenterStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillHeight, Alignment.Center, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillHeight, Alignment.CenterEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillHeight, Alignment.BottomStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillHeight, Alignment.BottomCenter, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillHeight, Alignment.BottomEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillBounds, Alignment.TopStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillBounds, Alignment.TopCenter, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillBounds, Alignment.TopEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillBounds, Alignment.CenterStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillBounds, Alignment.Center, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillBounds, Alignment.CenterEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillBounds, Alignment.BottomStart, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillBounds, Alignment.BottomCenter, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.FillBounds, Alignment.BottomEnd, IntRect(0, 0, 1200, 1600)),
//            Item(ContentScale.Crop, Alignment.TopStart, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.Crop, Alignment.TopCenter, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.Crop, Alignment.TopEnd, IntRect(0, 0, 1200, 1200)),
//            Item(ContentScale.Crop, Alignment.CenterStart, IntRect(0, 199, 1200, 1400)),
//            Item(ContentScale.Crop, Alignment.Center, IntRect(0, 199, 1200, 1400)),
//            Item(ContentScale.Crop, Alignment.CenterEnd, IntRect(0, 199, 1200, 1400)),
//            Item(ContentScale.Crop, Alignment.BottomStart, IntRect(0, 399, 1200, 1600)),
//            Item(ContentScale.Crop, Alignment.BottomCenter, IntRect(0, 399, 1200, 1600)),
//            Item(ContentScale.Crop, Alignment.BottomEnd, IntRect(0, 399, 1200, 1600)),
////        ).printlnExpectedMessage(
////            computeExpected =  {
////                computeContentInContainerVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = it.contentScale,
////                    alignment = it.alignment,
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize),
//                it.expected,
//                computeContentInContainerVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = it.contentScale,
//                    alignment = it.alignment,
//                )
//            )
//        }
//    }
//
    @Test
    fun testComputeOffsetBounds() {
        val containerSize = IntSize(1080, 1656)
        val printBatchBuildExpression = false
//        val printBatchBuildExpression = true

        if (printBatchBuildExpression) {
            printlnBatchBuildExpression(
                p1s = listOf(
                    IntSize(7500, 232), IntSize(173, 3044), IntSize(575, 427), IntSize(551, 1038),
                ),
                p2s = listOf(None, Inside, Fit, FillWidth, FillHeight, FillBounds, Crop),
                p3s = listOf(
                    TopStart, TopCenter, TopEnd,
                    CenterStart, Center, CenterEnd,
                    BottomStart, BottomCenter, BottomEnd,
                ),
                p4s = listOf(1.0f, 2.0f),
                buildItem = { p1, p2, p3, p4 ->
                    Item7(p1, p2, p3, p4, IntRect.Zero)
                },
            ) {
                computeUserOffsetBounds(
                    containerSize = containerSize,
                    contentSize = it.contentSize,
                    contentScale = it.contentScale,
                    alignment = it.alignment,
                    userScale = it.scale,
                ).roundToIntRect()
            }
        }

        listOf(
            Item7(IntSize(7500, 232), None, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, TopStart, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, TopCenter, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, TopEnd, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, CenterStart, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), None, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, Center, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), None, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, CenterEnd, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), None, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, BottomStart, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(7500, 232), None, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(7500, 232), None, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), None, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(7500, 232), Inside, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, TopStart, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, TopCenter, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, TopEnd, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, CenterStart, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), Inside, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, Center, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), Inside, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, CenterEnd, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), Inside, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, BottomStart, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(7500, 232), Inside, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(7500, 232), Inside, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Inside, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(7500, 232), Fit, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, TopStart, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, TopCenter, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, TopEnd, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, CenterStart, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), Fit, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, Center, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), Fit, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, CenterEnd, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), Fit, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, BottomStart, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(7500, 232), Fit, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(7500, 232), Fit, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Fit, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(7500, 232), FillWidth, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillWidth, TopStart, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillWidth, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillWidth, TopCenter, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillWidth, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillWidth, TopEnd, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillWidth, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillWidth, CenterStart, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), FillWidth, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillWidth, Center, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), FillWidth, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillWidth, CenterEnd, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(7500, 232), FillWidth, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(
                IntSize(7500, 232),
                FillWidth,
                BottomStart,
                2.0f,
                IntRect(-1080, -1656, 0, -1656)
            ),
            Item7(IntSize(7500, 232), FillWidth, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(
                IntSize(7500, 232),
                FillWidth,
                BottomCenter,
                2.0f,
                IntRect(-1080, -1656, 0, -1656)
            ),
            Item7(IntSize(7500, 232), FillWidth, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillWidth, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(7500, 232), FillHeight, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, BottomStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillHeight, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, BottomStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), FillBounds, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), Crop, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Crop, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), Crop, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Crop, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), Crop, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Crop, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), Crop, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Crop, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), Crop, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Crop, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), Crop, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Crop, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), Crop, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Crop, BottomStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), Crop, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Crop, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(7500, 232), Crop, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(7500, 232), Crop, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), None, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), None, TopStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), None, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), None, TopCenter, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), None, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), None, TopEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), None, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), None, CenterStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), None, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), None, Center, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), None, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), None, CenterEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), None, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), None, BottomStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), None, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), None, BottomCenter, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), None, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), None, BottomEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), Inside, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Inside, TopStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Inside, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Inside, TopCenter, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), Inside, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Inside, TopEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), Inside, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Inside, CenterStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Inside, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Inside, Center, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), Inside, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Inside, CenterEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), Inside, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Inside, BottomStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Inside, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Inside, BottomCenter, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), Inside, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Inside, BottomEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), Fit, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Fit, TopStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Fit, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Fit, TopCenter, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), Fit, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Fit, TopEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), Fit, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Fit, CenterStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Fit, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Fit, Center, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), Fit, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Fit, CenterEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), Fit, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Fit, BottomStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Fit, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Fit, BottomCenter, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), Fit, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Fit, BottomEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), FillWidth, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, BottomStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillWidth, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, TopStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, TopCenter, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), FillHeight, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, TopEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), FillHeight, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, CenterStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, Center, 2.0f, IntRect(-540, -1656, -540, 0)),
            Item7(IntSize(173, 3044), FillHeight, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, CenterEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), FillHeight, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, BottomStart, 2.0f, IntRect(0, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(
                IntSize(173, 3044),
                FillHeight,
                BottomCenter,
                2.0f,
                IntRect(-540, -1656, -540, 0)
            ),
            Item7(IntSize(173, 3044), FillHeight, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillHeight, BottomEnd, 2.0f, IntRect(-1080, -1656, -1080, 0)),
            Item7(IntSize(173, 3044), FillBounds, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, BottomStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), FillBounds, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Crop, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Crop, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Crop, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Crop, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Crop, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Crop, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Crop, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Crop, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Crop, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Crop, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Crop, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Crop, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Crop, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Crop, BottomStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Crop, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Crop, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(173, 3044), Crop, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(173, 3044), Crop, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), None, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), None, TopStart, 2.0f, IntRect(-70, 0, 0, 0)),
            Item7(IntSize(575, 427), None, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), None, TopCenter, 2.0f, IntRect(-576, 0, -506, 0)),
            Item7(IntSize(575, 427), None, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), None, TopEnd, 2.0f, IntRect(-1080, 0, -1010, 0)),
            Item7(IntSize(575, 427), None, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), None, CenterStart, 2.0f, IntRect(-70, -828, 0, -828)),
            Item7(IntSize(575, 427), None, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), None, Center, 2.0f, IntRect(-576, -828, -506, -828)),
            Item7(IntSize(575, 427), None, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), None, CenterEnd, 2.0f, IntRect(-1080, -828, -1010, -828)),
            Item7(IntSize(575, 427), None, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), None, BottomStart, 2.0f, IntRect(-70, -1656, 0, -1656)),
            Item7(IntSize(575, 427), None, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), None, BottomCenter, 2.0f, IntRect(-576, -1656, -506, -1656)),
            Item7(IntSize(575, 427), None, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), None, BottomEnd, 2.0f, IntRect(-1080, -1656, -1010, -1656)),
            Item7(IntSize(575, 427), Inside, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Inside, TopStart, 2.0f, IntRect(-70, 0, 0, 0)),
            Item7(IntSize(575, 427), Inside, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Inside, TopCenter, 2.0f, IntRect(-576, 0, -506, 0)),
            Item7(IntSize(575, 427), Inside, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Inside, TopEnd, 2.0f, IntRect(-1080, 0, -1010, 0)),
            Item7(IntSize(575, 427), Inside, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Inside, CenterStart, 2.0f, IntRect(-70, -828, 0, -828)),
            Item7(IntSize(575, 427), Inside, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Inside, Center, 2.0f, IntRect(-576, -828, -506, -828)),
            Item7(IntSize(575, 427), Inside, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Inside, CenterEnd, 2.0f, IntRect(-1080, -828, -1010, -828)),
            Item7(IntSize(575, 427), Inside, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Inside, BottomStart, 2.0f, IntRect(-70, -1656, 0, -1656)),
            Item7(IntSize(575, 427), Inside, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Inside, BottomCenter, 2.0f, IntRect(-576, -1656, -506, -1656)),
            Item7(IntSize(575, 427), Inside, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Inside, BottomEnd, 2.0f, IntRect(-1080, -1656, -1010, -1656)),
            Item7(IntSize(575, 427), Fit, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, TopStart, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, TopCenter, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, TopEnd, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, CenterStart, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(575, 427), Fit, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, Center, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(575, 427), Fit, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, CenterEnd, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(575, 427), Fit, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, BottomStart, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(575, 427), Fit, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(575, 427), Fit, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Fit, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(575, 427), FillWidth, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, TopStart, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, TopCenter, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, TopEnd, 2.0f, IntRect(-1080, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, CenterStart, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(575, 427), FillWidth, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, Center, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(575, 427), FillWidth, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, CenterEnd, 2.0f, IntRect(-1080, -828, 0, -828)),
            Item7(IntSize(575, 427), FillWidth, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, BottomStart, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(575, 427), FillWidth, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(
                IntSize(575, 427),
                FillWidth,
                BottomCenter,
                2.0f,
                IntRect(-1080, -1656, 0, -1656)
            ),
            Item7(IntSize(575, 427), FillWidth, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillWidth, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, -1656)),
            Item7(IntSize(575, 427), FillHeight, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, BottomStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillHeight, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, BottomStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), FillBounds, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), Crop, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Crop, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), Crop, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Crop, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), Crop, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Crop, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), Crop, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Crop, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), Crop, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Crop, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), Crop, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Crop, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), Crop, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Crop, BottomStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), Crop, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Crop, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(575, 427), Crop, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(575, 427), Crop, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), None, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), None, TopStart, 2.0f, IntRect(-22, -420, 0, 0)),
            Item7(IntSize(551, 1038), None, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), None, TopCenter, 2.0f, IntRect(-552, -420, -530, 0)),
            Item7(IntSize(551, 1038), None, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), None, TopEnd, 2.0f, IntRect(-1080, -420, -1058, 0)),
            Item7(IntSize(551, 1038), None, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), None, CenterStart, 2.0f, IntRect(-22, -1038, 0, -618)),
            Item7(IntSize(551, 1038), None, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), None, Center, 2.0f, IntRect(-552, -1038, -530, -618)),
            Item7(IntSize(551, 1038), None, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), None, CenterEnd, 2.0f, IntRect(-1080, -1038, -1058, -618)),
            Item7(IntSize(551, 1038), None, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), None, BottomStart, 2.0f, IntRect(-22, -1656, 0, -1236)),
            Item7(IntSize(551, 1038), None, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), None, BottomCenter, 2.0f, IntRect(-552, -1656, -530, -1236)),
            Item7(IntSize(551, 1038), None, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), None, BottomEnd, 2.0f, IntRect(-1080, -1656, -1058, -1236)),
            Item7(IntSize(551, 1038), Inside, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Inside, TopStart, 2.0f, IntRect(-22, -420, 0, 0)),
            Item7(IntSize(551, 1038), Inside, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Inside, TopCenter, 2.0f, IntRect(-552, -420, -530, 0)),
            Item7(IntSize(551, 1038), Inside, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Inside, TopEnd, 2.0f, IntRect(-1080, -420, -1058, 0)),
            Item7(IntSize(551, 1038), Inside, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Inside, CenterStart, 2.0f, IntRect(-22, -1038, 0, -618)),
            Item7(IntSize(551, 1038), Inside, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Inside, Center, 2.0f, IntRect(-552, -1038, -530, -618)),
            Item7(IntSize(551, 1038), Inside, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Inside, CenterEnd, 2.0f, IntRect(-1080, -1038, -1058, -618)),
            Item7(IntSize(551, 1038), Inside, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Inside, BottomStart, 2.0f, IntRect(-22, -1656, 0, -1236)),
            Item7(IntSize(551, 1038), Inside, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(
                IntSize(551, 1038),
                Inside,
                BottomCenter,
                2.0f,
                IntRect(-552, -1656, -530, -1236)
            ),
            Item7(IntSize(551, 1038), Inside, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Inside, BottomEnd, 2.0f, IntRect(-1080, -1656, -1058, -1236)),
            Item7(IntSize(551, 1038), Fit, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Fit, TopStart, 2.0f, IntRect(-678, -1656, 0, 0)),
            Item7(IntSize(551, 1038), Fit, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Fit, TopCenter, 2.0f, IntRect(-880, -1656, -202, 0)),
            Item7(IntSize(551, 1038), Fit, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Fit, TopEnd, 2.0f, IntRect(-1080, -1656, -402, 0)),
            Item7(IntSize(551, 1038), Fit, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Fit, CenterStart, 2.0f, IntRect(-678, -1656, 0, 0)),
            Item7(IntSize(551, 1038), Fit, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Fit, Center, 2.0f, IntRect(-880, -1656, -202, 0)),
            Item7(IntSize(551, 1038), Fit, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Fit, CenterEnd, 2.0f, IntRect(-1080, -1656, -402, 0)),
            Item7(IntSize(551, 1038), Fit, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Fit, BottomStart, 2.0f, IntRect(-678, -1656, 0, 0)),
            Item7(IntSize(551, 1038), Fit, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Fit, BottomCenter, 2.0f, IntRect(-880, -1656, -202, 0)),
            Item7(IntSize(551, 1038), Fit, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Fit, BottomEnd, 2.0f, IntRect(-1080, -1656, -402, 0)),
            Item7(IntSize(551, 1038), FillWidth, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, BottomStart, 2.0f, IntRect(-1080, -1655, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, BottomCenter, 2.0f, IntRect(-1080, -1655, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillWidth, BottomEnd, 2.0f, IntRect(-1080, -1655, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, TopStart, 2.0f, IntRect(-678, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, TopCenter, 2.0f, IntRect(-880, -1656, -202, 0)),
            Item7(IntSize(551, 1038), FillHeight, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, TopEnd, 2.0f, IntRect(-1080, -1656, -402, 0)),
            Item7(IntSize(551, 1038), FillHeight, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, CenterStart, 2.0f, IntRect(-678, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, Center, 2.0f, IntRect(-880, -1656, -202, 0)),
            Item7(IntSize(551, 1038), FillHeight, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, CenterEnd, 2.0f, IntRect(-1080, -1656, -402, 0)),
            Item7(IntSize(551, 1038), FillHeight, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, BottomStart, 2.0f, IntRect(-678, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(
                IntSize(551, 1038),
                FillHeight,
                BottomCenter,
                2.0f,
                IntRect(-880, -1656, -202, 0)
            ),
            Item7(IntSize(551, 1038), FillHeight, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillHeight, BottomEnd, 2.0f, IntRect(-1080, -1656, -402, 0)),
            Item7(IntSize(551, 1038), FillBounds, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, BottomStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, BottomCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), FillBounds, BottomEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), Crop, TopStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Crop, TopStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), Crop, TopCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Crop, TopCenter, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), Crop, TopEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Crop, TopEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), Crop, CenterStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Crop, CenterStart, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), Crop, Center, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Crop, Center, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), Crop, CenterEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Crop, CenterEnd, 2.0f, IntRect(-1080, -1656, 0, 0)),
            Item7(IntSize(551, 1038), Crop, BottomStart, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Crop, BottomStart, 2.0f, IntRect(-1080, -1655, 0, 0)),
            Item7(IntSize(551, 1038), Crop, BottomCenter, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Crop, BottomCenter, 2.0f, IntRect(-1080, -1655, 0, 0)),
            Item7(IntSize(551, 1038), Crop, BottomEnd, 1.0f, IntRect(0, 0, 0, 0)),
            Item7(IntSize(551, 1038), Crop, BottomEnd, 2.0f, IntRect(-1080, -1655, 0, 0)),
        ).forEach { item ->
            val result = computeUserOffsetBounds(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentScale = item.contentScale,
                alignment = item.alignment,
                userScale = item.scale,
            ).roundToIntRect()
            Assert.assertEquals(
                /* message = */ item.getMessage(containerSize),
                /* expected = */ item.expected,
                /* actual = */ result,
            )
        }
    }

//    @Test
//    fun testComputeContentVisibleRect() {
//        var containerSize = IntSize(1000, 2000)
//        var contentSize = IntSize(800, 1200)
//        var contentScale = ContentScale.Fit
//        var alignment = Alignment.Center
//        var scale = 1f
//        listOf(
//            Item3(IntOffset(0, 0), IntRect(0, 0, 800, 1200)),
//            Item3(IntOffset(250, 500), IntRect(0, 0, 600, 1000)),
//            Item3(IntOffset(750, 500), IntRect(0, 0, 200, 1000)),
//            Item3(IntOffset(250, 1500), IntRect(0, 0, 600, 200)),
//            Item3(IntOffset(750, 1500), IntRect(0, 0, 200, 200)),
//            Item3(IntOffset(1000, 2000), IntRect(0, 0, 0, 0)),
//            Item3(IntOffset(-250, -500), IntRect(200, 200, 800, 1200)),
//            Item3(IntOffset(-750, -500), IntRect(600, 200, 800, 1200)),
//            Item3(IntOffset(-250, -1500), IntRect(200, 1000, 800, 1200)),
//            Item3(IntOffset(-750, -1500), IntRect(600, 1000, 800, 1200)),
//            Item3(IntOffset(-1000, -2000), IntRect(0, 0, 0, 0)),
////        ).printlnExpectedMessage3(
////            computeExpected = {
////                computeContentVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = contentScale,
////                    alignment = alignment,
////                    scale = scale,
////                    offset = it.offset
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, contentScale, alignment, scale),
//                it.expected,
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    alignment = alignment,
//                    scale = scale,
//                    offset = it.offset
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 2000)
//        contentSize = IntSize(800, 1200)
//        contentScale = ContentScale.Fit
//        alignment = Alignment.Center
//        scale = 2f
//        listOf(
//            Item3(IntOffset(0, 0), IntRect(0, 0, 400, 600)),
//            Item3(IntOffset(250, 500), IntRect(0, 0, 300, 400)),
//            Item3(IntOffset(750, 500), IntRect(0, 0, 100, 400)),
//            Item3(IntOffset(250, 1500), IntRect(0, 0, 0, 0)),
//            Item3(IntOffset(750, 1500), IntRect(0, 0, 0, 0)),
//            Item3(IntOffset(1000, 2000), IntRect(0, 0, 0, 0)),
//            Item3(IntOffset(-250, -500), IntRect(100, 0, 500, 800)),
//            Item3(IntOffset(-750, -500), IntRect(300, 0, 700, 800)),
//            Item3(IntOffset(-250, -1500), IntRect(100, 400, 500, 1200)),
//            Item3(IntOffset(-750, -1500), IntRect(300, 400, 700, 1200)),
//            Item3(IntOffset(-1000, -2000), IntRect(400, 600, 800, 1200)),
////        ).printlnExpectedMessage3(
////            computeExpected = {
////                computeContentVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = contentScale,
////                    alignment = alignment,
////                    scale = scale,
////                    offset = it.offset
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, contentScale, alignment, scale),
//                it.expected,
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    alignment = alignment,
//                    scale = scale,
//                    offset = it.offset
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 2000)
//        contentSize = IntSize(800, 1200)
//        contentScale = ContentScale.Inside
//        alignment = Alignment.Center
//        scale = 1f
//        listOf(
//            Item3(IntOffset(0, 0), IntRect(0, 0, 800, 1200)),
//            Item3(IntOffset(250, 500), IntRect(0, 0, 650, 1100)),
//            Item3(IntOffset(750, 500), IntRect(0, 0, 150, 1100)),
//            Item3(IntOffset(250, 1500), IntRect(0, 0, 650, 100)),
//            Item3(IntOffset(750, 1500), IntRect(0, 0, 150, 100)),
//            Item3(IntOffset(1000, 2000), IntRect(0, 0, 0, 0)),
//            Item3(IntOffset(-250, -500), IntRect(150, 100, 800, 1200)),
//            Item3(IntOffset(-750, -500), IntRect(650, 100, 800, 1200)),
//            Item3(IntOffset(-250, -1500), IntRect(150, 1100, 800, 1200)),
//            Item3(IntOffset(-750, -1500), IntRect(650, 1100, 800, 1200)),
//            Item3(IntOffset(-1000, -2000), IntRect(0, 0, 0, 0)),
////        ).printlnExpectedMessage3(
////            computeExpected = {
////                computeContentVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = contentScale,
////                    alignment = alignment,
////                    scale = scale,
////                    offset = it.offset
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, contentScale, alignment, scale),
//                it.expected,
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    alignment = alignment,
//                    scale = scale,
//                    offset = it.offset
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 2000)
//        contentSize = IntSize(800, 1200)
//        contentScale = ContentScale.Inside
//        alignment = Alignment.Center
//        scale = 2f
//        listOf(
//            Item3(IntOffset(0, 0), IntRect(0, 0, 400, 600)),
//            Item3(IntOffset(250, 500), IntRect(0, 0, 275, 350)),
//            Item3(IntOffset(750, 500), IntRect(0, 0, 25, 350)),
//            Item3(IntOffset(250, 1500), IntRect(0, 0, 0, 0)),
//            Item3(IntOffset(750, 1500), IntRect(0, 0, 0, 0)),
//            Item3(IntOffset(1000, 2000), IntRect(0, 0, 0, 0)),
//            Item3(IntOffset(-250, -500), IntRect(25, 0, 525, 850)),
//            Item3(IntOffset(-750, -500), IntRect(275, 0, 775, 850)),
//            Item3(IntOffset(-250, -1500), IntRect(25, 350, 525, 1200)),
//            Item3(IntOffset(-750, -1500), IntRect(275, 350, 775, 1200)),
//            Item3(IntOffset(-1000, -2000), IntRect(400, 600, 800, 1200)),
////        ).printlnExpectedMessage3(
////            computeExpected = {
////                computeContentVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = contentScale,
////                    alignment = alignment,
////                    scale = scale,
////                    offset = it.offset
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, contentScale, alignment, scale),
//                it.expected,
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    alignment = alignment,
//                    scale = scale,
//                    offset = it.offset
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 2000)
//        contentSize = IntSize(800, 1200)
//        contentScale = ContentScale.Crop
//        alignment = Alignment.Center
//        scale = 1f
//        listOf(
//            Item3(IntOffset(0, 0), IntRect(998, 0, 700, 1200)),
//            Item3(IntOffset(250, 500), IntRect(998, 0, 550, 900)),
//            Item3(IntOffset(750, 500), IntRect(998, 0, 2497, 900)),
//            Item3(IntOffset(250, 1500), IntRect(998, 0, 550, 300)),
//            Item3(IntOffset(750, 1500), IntRect(998, 0, 2497, 300)),
//            Item3(IntOffset(1000, 2000), IntRect(0, 0, 0, 0)),
//            Item3(IntOffset(-250, -500), IntRect(2497, 300, 700, 1200)),
//            Item3(IntOffset(-750, -500), IntRect(550, 300, 700, 1200)),
//            Item3(IntOffset(-250, -1500), IntRect(2497, 900, 700, 1200)),
//            Item3(IntOffset(-750, -1500), IntRect(550, 900, 700, 1200)),
//            Item3(IntOffset(-1000, -2000), IntRect(0, 0, 0, 0)),
////        ).printlnExpectedMessage3(
////            computeExpected = {
////                computeContentVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = contentScale,
////                    alignment = alignment,
////                    scale = scale,
////                    offset = it.offset
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, contentScale, alignment, scale),
//                it.expected,
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    alignment = alignment,
//                    scale = scale,
//                    offset = it.offset
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 2000)
//        contentSize = IntSize(800, 1200)
//        contentScale = ContentScale.Crop
//        alignment = Alignment.Center
//        scale = 2f
//        listOf(
//            Item3(IntOffset(0.0, 0.0), IntRect(998, 0.0, 3997, 600.0)),
//            Item3(IntOffset(250.0, 500.0), IntRect(998, 0.0, 3247, 450.0)),
//            Item3(IntOffset(750.0, 500.0), IntRect(998, 0.0, 1747, 450.0)),
//            Item3(IntOffset(250.0, 1500.0), IntRect(998, 0.0, 3247, 150.0)),
//            Item3(IntOffset(750.0, 1500.0), IntRect(998, 0.0, 1747, 150.0)),
//            Item3(IntOffset(1000.0, 2000.0), IntRect(0.0, 0.0, 0.0, 0.0)),
//            Item3(IntOffset(-250.0, -500.0), IntRect(1747, 150.0, 4747, 750.0)),
//            Item3(IntOffset(-750.0, -500.0), IntRect(3247, 150.0, 625.0, 750.0)),
//            Item3(IntOffset(-250.0, -1500.0), IntRect(1747, 450.0, 4747, 1050.0)),
//            Item3(IntOffset(-750.0, -1500.0), IntRect(3247, 450.0, 625.0, 1050.0)),
//            Item3(IntOffset(-1000.0, -2000.0), IntRect(3997, 600.0, 700.0, 1200.0)),
////        ).printlnExpectedMessage3(
////            computeExpected = {
////                computeContentVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = contentScale,
////                    alignment = alignment,
////                    scale = scale,
////                    offset = it.offset
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, contentScale, alignment, scale),
//                it.expected,
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    alignment = alignment,
//                    scale = scale,
//                    offset = it.offset
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 2000)
//        contentSize = IntSize(containerSize.width * 1.5, containerSize.height * 1.3)
//        contentScale = ContentScale.None
//        alignment = Alignment.Center
//        scale = 1f
//        listOf(
//            Item3(IntOffset(0.0, 0.0), IntRect(250.0, 300.0, 1250.0, 2300.0)),
//            Item3(IntOffset(250.0, 500.0), IntRect(250.0, 300.0, 1000.0, 1800.0)),
//            Item3(IntOffset(750.0, 500.0), IntRect(250.0, 300.0, 500.0, 1800.0)),
//            Item3(IntOffset(250.0, 1500.0), IntRect(250.0, 300.0, 1000.0, 800.0)),
//            Item3(IntOffset(750.0, 1500.0), IntRect(250.0, 300.0, 500.0, 800.0)),
//            Item3(IntOffset(1000.0, 2000.0), IntRect(0.0, 0.0, 0.0, 0.0)),
//            Item3(IntOffset(-250.0, -500.0), IntRect(500.0, 800.0, 1250.0, 2300.0)),
//            Item3(IntOffset(-750.0, -500.0), IntRect(1000.0, 800.0, 1250.0, 2300.0)),
//            Item3(IntOffset(-250.0, -1500.0), IntRect(500.0, 1800.0, 1250.0, 2300.0)),
//            Item3(IntOffset(-750.0, -1500.0), IntRect(1000.0, 1800.0, 1250.0, 2300.0)),
//            Item3(IntOffset(-1000.0, -2000.0), IntRect(0.0, 0.0, 0.0, 0.0)),
////        ).printlnExpectedMessage3(
////            computeExpected = {
////                computeContentVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = contentScale,
////                    alignment = alignment,
////                    scale = scale,
////                    offset = it.offset
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, contentScale, alignment, scale),
//                it.expected,
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    alignment = alignment,
//                    scale = scale,
//                    offset = it.offset
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 2000)
//        contentSize = IntSize(containerSize.width * 1.5, containerSize.height * 1.3)
//        contentScale = ContentScale.None
//        alignment = Alignment.Center
//        scale = 2f
//        listOf(
//            Item3(IntOffset(0.0, 0.0), IntRect(250.0, 300.0, 750.0, 1300.0)),
//            Item3(IntOffset(250.0, 500.0), IntRect(250.0, 300.0, 625.0, 1050.0)),
//            Item3(IntOffset(750.0, 500.0), IntRect(250.0, 300.0, 375.0, 1050.0)),
//            Item3(IntOffset(250.0, 1500.0), IntRect(250.0, 300.0, 625.0, 550.0)),
//            Item3(IntOffset(750.0, 1500.0), IntRect(250.0, 300.0, 375.0, 550.0)),
//            Item3(IntOffset(1000.0, 2000.0), IntRect(0.0, 0.0, 0.0, 0.0)),
//            Item3(IntOffset(-250.0, -500.0), IntRect(375.0, 550.0, 875.0, 1550.0)),
//            Item3(IntOffset(-750.0, -500.0), IntRect(625.0, 550.0, 1125.0, 1550.0)),
//            Item3(IntOffset(-250.0, -1500.0), IntRect(375.0, 1050.0, 875.0, 2050.0)),
//            Item3(IntOffset(-750.0, -1500.0), IntRect(625.0, 1050.0, 1125.0, 2050.0)),
//            Item3(IntOffset(-1000.0, -2000.0), IntRect(750.0, 1300.0, 1250.0, 2300.0)),
////        ).printlnExpectedMessage3(
////            computeExpected = {
////                computeContentVisibleRect(
////                    containerSize = containerSize,
////                    contentSize = contentSize,
////                    contentScale = contentScale,
////                    alignment = alignment,
////                    scale = scale,
////                    offset = it.offset
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, contentScale, alignment, scale),
//                it.expected,
//                computeContentVisibleRect(
//                    containerSize = containerSize,
//                    contentSize = contentSize,
//                    contentScale = contentScale,
//                    alignment = alignment,
//                    scale = scale,
//                    offset = it.offset
//                )
//            )
//        }
//    }
//
//
//    @Test
//    fun testComputeContainerOriginByTouchPosition() {
//        var containerSize = IntSize(1080, 1920)
//
//        var scale = 1f
//        var offset = IntOffset(0, 0)
//        listOf(
//            IntOffset(216, 960) to Origin(0.2, 0.5),
//            IntOffset(540, 384) to Origin(0.5, 0.2),
//            IntOffset(864, 960) to Origin(0.8, 0.5),
//            IntOffset(540, 1536) to Origin(0.5, 0.8)
//        ).forEach { (touchPosition, targetPercentageOriginOfContent) ->
//            Assert.assertEquals(
//                "containerSize=$containerSize, scale=$scale, offset=$offset, touchPosition=$touchPosition",
//                targetPercentageOriginOfContent,
//                computeContainerOriginByTouchPosition(
//                    containerSize, scale, offset, touchPosition
//                )
//            )
//        }
//
//        scale = 1f
//        offset = IntOffset(540, 960)
//        listOf(
//            IntOffset(216, 960) to Origin(0, 0),
//            IntOffset(540, 384) to Origin(0, 0),
//            IntOffset(864, 960) to Origin(0.3, 0),
//            IntOffset(540, 1536) to Origin(0, 0.3)
//        ).forEach { (touchPosition, targetPercentageOriginOfContent) ->
//            Assert.assertEquals(
//                "containerSize=$containerSize, scale=$scale, offset=$offset, touchPosition=$touchPosition",
//                targetPercentageOriginOfContent,
//                computeContainerOriginByTouchPosition(
//                    containerSize, scale, offset, touchPosition
//                )
//            )
//        }
//
//        scale = 1f
//        offset = IntOffset(-540, -960)
//        listOf(
//            IntOffset(216, 960) to Origin(0.7, 1),
//            IntOffset(540, 384) to Origin(1, 0.7),
//            IntOffset(864, 960) to Origin(1, 1),
//            IntOffset(540, 1536) to Origin(1, 1)
//        ).forEach { (touchPosition, targetPercentageOriginOfContent) ->
//            Assert.assertEquals(
//                "containerSize=$containerSize, scale=$scale, offset=$offset, touchPosition=$touchPosition",
//                targetPercentageOriginOfContent,
//                computeContainerOriginByTouchPosition(
//                    containerSize, scale, offset, touchPosition
//                )
//            )
//        }
//
//        scale = 2f
//        offset = IntOffset(0, 0)
//        listOf(
//            IntOffset(216, 960) to Origin(0.1, 0.25),
//            IntOffset(540, 384) to Origin(0.25, 0.1),
//            IntOffset(864, 960) to Origin(0.4, 0.25),
//            IntOffset(540, 1536) to Origin(0.25, 0.4)
//        ).forEach { (touchPosition, targetPercentageOriginOfContent) ->
//            Assert.assertEquals(
//                "containerSize=$containerSize, scale=$scale, offset=$offset, touchPosition=$touchPosition",
//                targetPercentageOriginOfContent,
//                computeContainerOriginByTouchPosition(
//                    containerSize, scale, offset, touchPosition
//                )
//            )
//        }
//
//        scale = 2f
//        offset = IntOffset(540, 960)
//        listOf(
//            IntOffset(216, 960) to Origin(0, 0),
//            IntOffset(540, 384) to Origin(0, 0),
//            IntOffset(864, 960) to Origin(0.15, 0),
//            IntOffset(540, 1536) to Origin(0, 0.15)
//        ).forEach { (touchPosition, targetPercentageOriginOfContent) ->
//            Assert.assertEquals(
//                "containerSize=$containerSize, scale=$scale, offset=$offset, touchPosition=$touchPosition",
//                targetPercentageOriginOfContent,
//                computeContainerOriginByTouchPosition(
//                    containerSize, scale, offset, touchPosition
//                )
//            )
//        }
//
//        scale = 2f
//        offset = IntOffset(-540, -960)
//        listOf(
//            IntOffset(216, 960) to Origin(0.35, 0.5),
//            IntOffset(540, 384) to Origin(0.5, 0.35),
//            IntOffset(864, 960) to Origin(0.65, 0.5),
//            IntOffset(540, 1536) to Origin(0.5, 0.65)
//        ).forEach { (touchPosition, targetPercentageOriginOfContent) ->
//            Assert.assertEquals(
//                "containerSize=$containerSize, scale=$scale, offset=$offset, touchPosition=$touchPosition",
//                targetPercentageOriginOfContent,
//                computeContainerOriginByTouchPosition(
//                    containerSize, scale, offset, touchPosition
//                )
//            )
//        }
//
//        containerSize = IntSize.Unspecified
//        scale = 1f
//        offset = IntOffset(0, 0)
//        listOf(
//            IntOffset(216, 960) to Origin(0, 0),
//            IntOffset(540, 384) to Origin(0, 0),
//            IntOffset(864, 960) to Origin(0, 0),
//            IntOffset(540, 1536) to Origin(0, 0)
//        ).forEach { (touchPosition, targetPercentageOriginOfContent) ->
//            Assert.assertEquals(
//                "containerSize=$containerSize, scale=$scale, offset=$offset, touchPosition=$touchPosition",
//                targetPercentageOriginOfContent,
//                computeContainerOriginByTouchPosition(
//                    containerSize, scale, offset, touchPosition
//                )
//            )
//        }
//    }
//
//    @Test
//    fun testContainerOriginToContentOrigin() {
//        var containerSize = IntSize(1000, 1000)
//        var contentSize = IntSize(800, 200)
//        var containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScale.None, Alignment.TopStart, Origin(0.625, 1.0)),
//            Item2(ContentScale.None, Alignment.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScale.None, Alignment.TopEnd, Origin(0.375, 1.0)),
//            Item2(ContentScale.None, Alignment.CenterStart, Origin(0.625, 0.5)),
//            Item2(ContentScale.None, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.None, Alignment.CenterEnd, Origin(0.375, 0.5)),
//            Item2(ContentScale.None, Alignment.BottomStart, Origin(0.625, 0.0)),
//            Item2(ContentScale.None, Alignment.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScale.None, Alignment.BottomEnd, Origin(0.375, 0.0)),
//            Item2(ContentScale.Inside, Alignment.TopStart, Origin(0.625, 1.0)),
//            Item2(ContentScale.Inside, Alignment.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScale.Inside, Alignment.TopEnd, Origin(0.375, 1.0)),
//            Item2(ContentScale.Inside, Alignment.CenterStart, Origin(0.625, 0.5)),
//            Item2(ContentScale.Inside, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.CenterEnd, Origin(0.375, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomStart, Origin(0.625, 0.0)),
//            Item2(ContentScale.Inside, Alignment.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScale.Inside, Alignment.BottomEnd, Origin(0.375, 0.0)),
//            Item2(ContentScale.Fit, Alignment.TopStart, Origin(0.5, 1.0)),
//            Item2(ContentScale.Fit, Alignment.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScale.Fit, Alignment.TopEnd, Origin(0.5, 1.0)),
//            Item2(ContentScale.Fit, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomStart, Origin(0.5, 0.0)),
//            Item2(ContentScale.Fit, Alignment.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScale.Fit, Alignment.BottomEnd, Origin(0.5, 0.0)),
//            Item2(ContentScale.FillWidth, Alignment.TopStart, Origin(0.5, 1.0)),
//            Item2(ContentScale.FillWidth, Alignment.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScale.FillWidth, Alignment.TopEnd, Origin(0.5, 1.0)),
//            Item2(ContentScale.FillWidth, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.BottomStart, Origin(0.5, 0.0)),
//            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Origin(0.5, 0.0)),
//            Item2(ContentScale.FillHeight, Alignment.TopStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.Crop, Alignment.CenterStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.Crop, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.CenterEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomEnd, Origin(0.875, 0.5)),
////        ).printlnExpectedMessage2(
////            computeExpected = {
////                containerOriginToContentOrigin(
////                    containerSize,
////                    contentSize,
////                    it.contentScale,
////                    it.alignment,
////                    containerOrigin
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, containerOrigin),
//                it.expected,
//                containerOriginToContentOrigin(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.alignment,
//                    containerOrigin
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 1000)
//        contentSize = IntSize(200, 800)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScale.None, Alignment.TopStart, Origin(1.0, 0.625)),
//            Item2(ContentScale.None, Alignment.TopCenter, Origin(0.5, 0.625)),
//            Item2(ContentScale.None, Alignment.TopEnd, Origin(0.0, 0.625)),
//            Item2(ContentScale.None, Alignment.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.None, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.None, Alignment.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.None, Alignment.BottomStart, Origin(1.0, 0.375)),
//            Item2(ContentScale.None, Alignment.BottomCenter, Origin(0.5, 0.375)),
//            Item2(ContentScale.None, Alignment.BottomEnd, Origin(0.0, 0.375)),
//            Item2(ContentScale.Inside, Alignment.TopStart, Origin(1.0, 0.625)),
//            Item2(ContentScale.Inside, Alignment.TopCenter, Origin(0.5, 0.625)),
//            Item2(ContentScale.Inside, Alignment.TopEnd, Origin(0.0, 0.625)),
//            Item2(ContentScale.Inside, Alignment.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.Inside, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomStart, Origin(1.0, 0.375)),
//            Item2(ContentScale.Inside, Alignment.BottomCenter, Origin(0.5, 0.375)),
//            Item2(ContentScale.Inside, Alignment.BottomEnd, Origin(0.0, 0.375)),
//            Item2(ContentScale.Fit, Alignment.TopStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.Fit, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.TopEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.Fit, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.TopStart, Origin(0.5, 0.125)),
//            Item2(ContentScale.FillWidth, Alignment.TopCenter, Origin(0.5, 0.125)),
//            Item2(ContentScale.FillWidth, Alignment.TopEnd, Origin(0.5, 0.125)),
//            Item2(ContentScale.FillWidth, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.BottomStart, Origin(0.5, 0.875)),
//            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Origin(0.5, 0.875)),
//            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Origin(0.5, 0.875)),
//            Item2(ContentScale.FillHeight, Alignment.TopStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopStart, Origin(0.5, 0.125)),
//            Item2(ContentScale.Crop, Alignment.TopCenter, Origin(0.5, 0.125)),
//            Item2(ContentScale.Crop, Alignment.TopEnd, Origin(0.5, 0.125)),
//            Item2(ContentScale.Crop, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomStart, Origin(0.5, 0.875)),
//            Item2(ContentScale.Crop, Alignment.BottomCenter, Origin(0.5, 0.875)),
//            Item2(ContentScale.Crop, Alignment.BottomEnd, Origin(0.5, 0.875)),
////        ).printlnExpectedMessage2(
////            computeExpected = {
////                containerOriginToContentOrigin(
////                    containerSize,
////                    contentSize,
////                    it.contentScale,
////                    it.alignment,
////                    containerOrigin
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, containerOrigin),
//                it.expected,
//                containerOriginToContentOrigin(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.alignment,
//                    containerOrigin
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 1000)
//        contentSize = IntSize(1600, 1200)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScale.None, Alignment.TopStart, Origin(0.3125, 0.41666666)),
//            Item2(ContentScale.None, Alignment.TopCenter, Origin(0.5, 0.41666666)),
//            Item2(ContentScale.None, Alignment.TopEnd, Origin(0.6875, 0.41666666)),
//            Item2(ContentScale.None, Alignment.CenterStart, Origin(0.3125, 0.5)),
//            Item2(ContentScale.None, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.None, Alignment.CenterEnd, Origin(0.6875, 0.5)),
//            Item2(ContentScale.None, Alignment.BottomStart, Origin(0.3125, 0.5833333)),
//            Item2(ContentScale.None, Alignment.BottomCenter, Origin(0.5, 0.5833333)),
//            Item2(ContentScale.None, Alignment.BottomEnd, Origin(0.6875, 0.5833333)),
//            Item2(ContentScale.Inside, Alignment.TopStart, Origin(0.5, 0.6666667)),
//            Item2(ContentScale.Inside, Alignment.TopCenter, Origin(0.5, 0.6666667)),
//            Item2(ContentScale.Inside, Alignment.TopEnd, Origin(0.5, 0.6666667)),
//            Item2(ContentScale.Inside, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomStart, Origin(0.5, 0.33333334)),
//            Item2(ContentScale.Inside, Alignment.BottomCenter, Origin(0.5, 0.33333334)),
//            Item2(ContentScale.Inside, Alignment.BottomEnd, Origin(0.5, 0.33333334)),
//            Item2(ContentScale.Fit, Alignment.TopStart, Origin(0.5, 0.6666667)),
//            Item2(ContentScale.Fit, Alignment.TopCenter, Origin(0.5, 0.6666667)),
//            Item2(ContentScale.Fit, Alignment.TopEnd, Origin(0.5, 0.6666667)),
//            Item2(ContentScale.Fit, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomStart, Origin(0.5, 0.33333334)),
//            Item2(ContentScale.Fit, Alignment.BottomCenter, Origin(0.5, 0.33333334)),
//            Item2(ContentScale.Fit, Alignment.BottomEnd, Origin(0.5, 0.33333334)),
//            Item2(ContentScale.FillWidth, Alignment.TopStart, Origin(0.5, 0.6666667)),
//            Item2(ContentScale.FillWidth, Alignment.TopCenter, Origin(0.5, 0.6666667)),
//            Item2(ContentScale.FillWidth, Alignment.TopEnd, Origin(0.5, 0.6666667)),
//            Item2(ContentScale.FillWidth, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.BottomStart, Origin(0.5, 0.33333334)),
//            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Origin(0.5, 0.33333334)),
//            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Origin(0.5, 0.33333334)),
//            Item2(ContentScale.FillHeight, Alignment.TopStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopCenter, Origin(0.49999997, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopEnd, Origin(0.62499994, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.Center, Origin(0.49999997, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Origin(0.62499994, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Origin(0.49999997, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Origin(0.62499994, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopCenter, Origin(0.49999997, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopEnd, Origin(0.62499994, 0.5)),
//            Item2(ContentScale.Crop, Alignment.CenterStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.Crop, Alignment.Center, Origin(0.49999997, 0.5)),
//            Item2(ContentScale.Crop, Alignment.CenterEnd, Origin(0.62499994, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomCenter, Origin(0.49999997, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomEnd, Origin(0.62499994, 0.5)),
////        ).printlnExpectedMessage2(
////            computeExpected = {
////                containerOriginToContentOrigin(
////                    containerSize,
////                    contentSize,
////                    it.contentScale,
////                    it.alignment,
////                    containerOrigin
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, containerOrigin),
//                it.expected,
//                containerOriginToContentOrigin(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.alignment,
//                    containerOrigin
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 1000)
//        contentSize = IntSize(1200, 1600)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScale.None, Alignment.TopStart, Origin(0.41666666, 0.3125)),
//            Item2(ContentScale.None, Alignment.TopCenter, Origin(0.5, 0.3125)),
//            Item2(ContentScale.None, Alignment.TopEnd, Origin(0.5833333, 0.3125)),
//            Item2(ContentScale.None, Alignment.CenterStart, Origin(0.41666666, 0.5)),
//            Item2(ContentScale.None, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.None, Alignment.CenterEnd, Origin(0.5833333, 0.5)),
//            Item2(ContentScale.None, Alignment.BottomStart, Origin(0.41666666, 0.6875)),
//            Item2(ContentScale.None, Alignment.BottomCenter, Origin(0.5, 0.6875)),
//            Item2(ContentScale.None, Alignment.BottomEnd, Origin(0.5833333, 0.6875)),
//            Item2(ContentScale.Inside, Alignment.TopStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScale.Inside, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.TopEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScale.Inside, Alignment.CenterStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScale.Inside, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.CenterEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScale.Fit, Alignment.TopStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScale.Fit, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.TopEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScale.Fit, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.TopStart, Origin(0.5, 0.375)),
//            Item2(ContentScale.FillWidth, Alignment.TopCenter, Origin(0.5, 0.375)),
//            Item2(ContentScale.FillWidth, Alignment.TopEnd, Origin(0.5, 0.375)),
//            Item2(ContentScale.FillWidth, Alignment.CenterStart, Origin(0.5, 0.49999997)),
//            Item2(ContentScale.FillWidth, Alignment.Center, Origin(0.5, 0.49999997)),
//            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Origin(0.5, 0.49999997)),
//            Item2(ContentScale.FillWidth, Alignment.BottomStart, Origin(0.5, 0.62499994)),
//            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Origin(0.5, 0.62499994)),
//            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Origin(0.5, 0.62499994)),
//            Item2(ContentScale.FillHeight, Alignment.TopStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopStart, Origin(0.5, 0.375)),
//            Item2(ContentScale.Crop, Alignment.TopCenter, Origin(0.5, 0.375)),
//            Item2(ContentScale.Crop, Alignment.TopEnd, Origin(0.5, 0.375)),
//            Item2(ContentScale.Crop, Alignment.CenterStart, Origin(0.5, 0.49999997)),
//            Item2(ContentScale.Crop, Alignment.Center, Origin(0.5, 0.49999997)),
//            Item2(ContentScale.Crop, Alignment.CenterEnd, Origin(0.5, 0.49999997)),
//            Item2(ContentScale.Crop, Alignment.BottomStart, Origin(0.5, 0.62499994)),
//            Item2(ContentScale.Crop, Alignment.BottomCenter, Origin(0.5, 0.62499994)),
//            Item2(ContentScale.Crop, Alignment.BottomEnd, Origin(0.5, 0.62499994)),
////        ).printlnExpectedMessage2(
////            computeExpected = {
////                containerOriginToContentOrigin(
////                    containerSize,
////                    contentSize,
////                    it.contentScale,
////                    it.alignment,
////                    containerOrigin
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, containerOrigin),
//                it.expected,
//                containerOriginToContentOrigin(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.alignment,
//                    containerOrigin
//                )
//            )
//        }
//    }
//
//    @Test
//    fun testContentOriginToContainerOrigin() {
//        var containerSize = IntSize(1000, 1000)
//        var contentSize = IntSize(800, 200)
//        var containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScale.None, Alignment.TopStart, Origin(0.4, 0.1)),
//            Item2(ContentScale.None, Alignment.TopCenter, Origin(0.5, 0.1)),
//            Item2(ContentScale.None, Alignment.TopEnd, Origin(0.6, 0.1)),
//            Item2(ContentScale.None, Alignment.CenterStart, Origin(0.4, 0.5)),
//            Item2(ContentScale.None, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.None, Alignment.CenterEnd, Origin(0.6, 0.5)),
//            Item2(ContentScale.None, Alignment.BottomStart, Origin(0.4, 0.9)),
//            Item2(ContentScale.None, Alignment.BottomCenter, Origin(0.5, 0.9)),
//            Item2(ContentScale.None, Alignment.BottomEnd, Origin(0.6, 0.9)),
//            Item2(ContentScale.Inside, Alignment.TopStart, Origin(0.4, 0.1)),
//            Item2(ContentScale.Inside, Alignment.TopCenter, Origin(0.5, 0.1)),
//            Item2(ContentScale.Inside, Alignment.TopEnd, Origin(0.6, 0.1)),
//            Item2(ContentScale.Inside, Alignment.CenterStart, Origin(0.4, 0.5)),
//            Item2(ContentScale.Inside, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.CenterEnd, Origin(0.6, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomStart, Origin(0.4, 0.9)),
//            Item2(ContentScale.Inside, Alignment.BottomCenter, Origin(0.5, 0.9)),
//            Item2(ContentScale.Inside, Alignment.BottomEnd, Origin(0.6, 0.9)),
//            Item2(ContentScale.Fit, Alignment.TopStart, Origin(0.5, 0.125)),
//            Item2(ContentScale.Fit, Alignment.TopCenter, Origin(0.5, 0.125)),
//            Item2(ContentScale.Fit, Alignment.TopEnd, Origin(0.5, 0.125)),
//            Item2(ContentScale.Fit, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomStart, Origin(0.5, 0.875)),
//            Item2(ContentScale.Fit, Alignment.BottomCenter, Origin(0.5, 0.875)),
//            Item2(ContentScale.Fit, Alignment.BottomEnd, Origin(0.5, 0.875)),
//            Item2(ContentScale.FillWidth, Alignment.TopStart, Origin(0.5, 0.125)),
//            Item2(ContentScale.FillWidth, Alignment.TopCenter, Origin(0.5, 0.125)),
//            Item2(ContentScale.FillWidth, Alignment.TopEnd, Origin(0.5, 0.125)),
//            Item2(ContentScale.FillWidth, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.BottomStart, Origin(0.5, 0.875)),
//            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Origin(0.5, 0.875)),
//            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Origin(0.5, 0.875)),
//            Item2(ContentScale.FillHeight, Alignment.TopStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.Crop, Alignment.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.Crop, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomStart, Origin(1.0, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomEnd, Origin(0.0, 0.5)),
////        ).printlnExpectedMessage2(
////            computeExpected = {
////                contentOriginToContainerOrigin(
////                    containerSize,
////                    contentSize,
////                    it.contentScale,
////                    it.alignment,
////                    containerOrigin
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, containerOrigin),
//                it.expected,
//                contentOriginToContainerOrigin(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.alignment,
//                    containerOrigin
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 1000)
//        contentSize = IntSize(200, 800)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScale.None, Alignment.TopStart, Origin(0.1, 0.4)),
//            Item2(ContentScale.None, Alignment.TopCenter, Origin(0.5, 0.4)),
//            Item2(ContentScale.None, Alignment.TopEnd, Origin(0.9, 0.4)),
//            Item2(ContentScale.None, Alignment.CenterStart, Origin(0.1, 0.5)),
//            Item2(ContentScale.None, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.None, Alignment.CenterEnd, Origin(0.9, 0.5)),
//            Item2(ContentScale.None, Alignment.BottomStart, Origin(0.1, 0.6)),
//            Item2(ContentScale.None, Alignment.BottomCenter, Origin(0.5, 0.6)),
//            Item2(ContentScale.None, Alignment.BottomEnd, Origin(0.9, 0.6)),
//            Item2(ContentScale.Inside, Alignment.TopStart, Origin(0.1, 0.4)),
//            Item2(ContentScale.Inside, Alignment.TopCenter, Origin(0.5, 0.4)),
//            Item2(ContentScale.Inside, Alignment.TopEnd, Origin(0.9, 0.4)),
//            Item2(ContentScale.Inside, Alignment.CenterStart, Origin(0.1, 0.5)),
//            Item2(ContentScale.Inside, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.CenterEnd, Origin(0.9, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomStart, Origin(0.1, 0.6)),
//            Item2(ContentScale.Inside, Alignment.BottomCenter, Origin(0.5, 0.6)),
//            Item2(ContentScale.Inside, Alignment.BottomEnd, Origin(0.9, 0.6)),
//            Item2(ContentScale.Fit, Alignment.TopStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.Fit, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.TopEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.Fit, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.TopStart, Origin(0.5, 1.0)),
//            Item2(ContentScale.FillWidth, Alignment.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScale.FillWidth, Alignment.TopEnd, Origin(0.5, 1.0)),
//            Item2(ContentScale.FillWidth, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.BottomStart, Origin(0.5, 0.0)),
//            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Origin(0.5, 0.0)),
//            Item2(ContentScale.FillHeight, Alignment.TopStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomStart, Origin(0.125, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Origin(0.875, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopStart, Origin(0.5, 1.0)),
//            Item2(ContentScale.Crop, Alignment.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScale.Crop, Alignment.TopEnd, Origin(0.5, 1.0)),
//            Item2(ContentScale.Crop, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomStart, Origin(0.5, 0.0)),
//            Item2(ContentScale.Crop, Alignment.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScale.Crop, Alignment.BottomEnd, Origin(0.5, 0.0)),
////        ).printlnExpectedMessage2(
////            computeExpected = {
////                contentOriginToContainerOrigin(
////                    containerSize,
////                    contentSize,
////                    it.contentScale,
////                    it.alignment,
////                    containerOrigin
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, containerOrigin),
//                it.expected,
//                contentOriginToContainerOrigin(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.alignment,
//                    containerOrigin
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 1000)
//        contentSize = IntSize(1600, 1200)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScale.None, Alignment.TopStart, Origin(0.8, 0.6)),
//            Item2(ContentScale.None, Alignment.TopCenter, Origin(0.5, 0.6)),
//            Item2(ContentScale.None, Alignment.TopEnd, Origin(0.2, 0.6)),
//            Item2(ContentScale.None, Alignment.CenterStart, Origin(0.8, 0.5)),
//            Item2(ContentScale.None, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.None, Alignment.CenterEnd, Origin(0.2, 0.5)),
//            Item2(ContentScale.None, Alignment.BottomStart, Origin(0.8, 0.4)),
//            Item2(ContentScale.None, Alignment.BottomCenter, Origin(0.5, 0.4)),
//            Item2(ContentScale.None, Alignment.BottomEnd, Origin(0.2, 0.4)),
//            Item2(ContentScale.Inside, Alignment.TopStart, Origin(0.5, 0.375)),
//            Item2(ContentScale.Inside, Alignment.TopCenter, Origin(0.5, 0.375)),
//            Item2(ContentScale.Inside, Alignment.TopEnd, Origin(0.5, 0.375)),
//            Item2(ContentScale.Inside, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomStart, Origin(0.5, 0.625)),
//            Item2(ContentScale.Inside, Alignment.BottomCenter, Origin(0.5, 0.625)),
//            Item2(ContentScale.Inside, Alignment.BottomEnd, Origin(0.5, 0.625)),
//            Item2(ContentScale.Fit, Alignment.TopStart, Origin(0.5, 0.375)),
//            Item2(ContentScale.Fit, Alignment.TopCenter, Origin(0.5, 0.375)),
//            Item2(ContentScale.Fit, Alignment.TopEnd, Origin(0.5, 0.375)),
//            Item2(ContentScale.Fit, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomStart, Origin(0.5, 0.625)),
//            Item2(ContentScale.Fit, Alignment.BottomCenter, Origin(0.5, 0.625)),
//            Item2(ContentScale.Fit, Alignment.BottomEnd, Origin(0.5, 0.625)),
//            Item2(ContentScale.FillWidth, Alignment.TopStart, Origin(0.5, 0.375)),
//            Item2(ContentScale.FillWidth, Alignment.TopCenter, Origin(0.5, 0.375)),
//            Item2(ContentScale.FillWidth, Alignment.TopEnd, Origin(0.5, 0.375)),
//            Item2(ContentScale.FillWidth, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.BottomStart, Origin(0.5, 0.625)),
//            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Origin(0.5, 0.625)),
//            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Origin(0.5, 0.625)),
//            Item2(ContentScale.FillHeight, Alignment.TopStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopCenter, Origin(0.50000006, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopEnd, Origin(0.3333334, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.Center, Origin(0.50000006, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Origin(0.3333334, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Origin(0.50000006, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Origin(0.3333334, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopCenter, Origin(0.50000006, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopEnd, Origin(0.3333334, 0.5)),
//            Item2(ContentScale.Crop, Alignment.CenterStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScale.Crop, Alignment.Center, Origin(0.50000006, 0.5)),
//            Item2(ContentScale.Crop, Alignment.CenterEnd, Origin(0.3333334, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomCenter, Origin(0.50000006, 0.5)),
//            Item2(ContentScale.Crop, Alignment.BottomEnd, Origin(0.3333334, 0.5)),
////        ).printlnExpectedMessage2(
////            computeExpected = {
////                contentOriginToContainerOrigin(
////                    containerSize,
////                    contentSize,
////                    it.contentScale,
////                    it.alignment,
////                    containerOrigin
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, containerOrigin),
//                it.expected,
//                contentOriginToContainerOrigin(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.alignment,
//                    containerOrigin
//                )
//            )
//        }
//
//        containerSize = IntSize(1000, 1000)
//        contentSize = IntSize(1200, 1600)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScale.None, Alignment.TopStart, Origin(0.6, 0.8)),
//            Item2(ContentScale.None, Alignment.TopCenter, Origin(0.5, 0.8)),
//            Item2(ContentScale.None, Alignment.TopEnd, Origin(0.4, 0.8)),
//            Item2(ContentScale.None, Alignment.CenterStart, Origin(0.6, 0.5)),
//            Item2(ContentScale.None, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.None, Alignment.CenterEnd, Origin(0.4, 0.5)),
//            Item2(ContentScale.None, Alignment.BottomStart, Origin(0.6, 0.2)),
//            Item2(ContentScale.None, Alignment.BottomCenter, Origin(0.5, 0.2)),
//            Item2(ContentScale.None, Alignment.BottomEnd, Origin(0.4, 0.2)),
//            Item2(ContentScale.Inside, Alignment.TopStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.Inside, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.TopEnd, Origin(0.625, 0.5)),
//            Item2(ContentScale.Inside, Alignment.CenterStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.Inside, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.CenterEnd, Origin(0.625, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Inside, Alignment.BottomEnd, Origin(0.625, 0.5)),
//            Item2(ContentScale.Fit, Alignment.TopStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.Fit, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.TopEnd, Origin(0.625, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.Fit, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.CenterEnd, Origin(0.625, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.Fit, Alignment.BottomEnd, Origin(0.625, 0.5)),
//            Item2(ContentScale.FillWidth, Alignment.TopStart, Origin(0.5, 0.6666666)),
//            Item2(ContentScale.FillWidth, Alignment.TopCenter, Origin(0.5, 0.6666666)),
//            Item2(ContentScale.FillWidth, Alignment.TopEnd, Origin(0.5, 0.6666666)),
//            Item2(ContentScale.FillWidth, Alignment.CenterStart, Origin(0.5, 0.50000006)),
//            Item2(ContentScale.FillWidth, Alignment.Center, Origin(0.5, 0.50000006)),
//            Item2(ContentScale.FillWidth, Alignment.CenterEnd, Origin(0.5, 0.50000006)),
//            Item2(ContentScale.FillWidth, Alignment.BottomStart, Origin(0.5, 0.3333334)),
//            Item2(ContentScale.FillWidth, Alignment.BottomCenter, Origin(0.5, 0.3333334)),
//            Item2(ContentScale.FillWidth, Alignment.BottomEnd, Origin(0.5, 0.3333334)),
//            Item2(ContentScale.FillHeight, Alignment.TopStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.TopEnd, Origin(0.625, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.CenterEnd, Origin(0.625, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomStart, Origin(0.375, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillHeight, Alignment.BottomEnd, Origin(0.625, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.Center, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScale.FillBounds, Alignment.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScale.Crop, Alignment.TopStart, Origin(0.5, 0.6666666)),
//            Item2(ContentScale.Crop, Alignment.TopCenter, Origin(0.5, 0.6666666)),
//            Item2(ContentScale.Crop, Alignment.TopEnd, Origin(0.5, 0.6666666)),
//            Item2(ContentScale.Crop, Alignment.CenterStart, Origin(0.5, 0.50000006)),
//            Item2(ContentScale.Crop, Alignment.Center, Origin(0.5, 0.50000006)),
//            Item2(ContentScale.Crop, Alignment.CenterEnd, Origin(0.5, 0.50000006)),
//            Item2(ContentScale.Crop, Alignment.BottomStart, Origin(0.5, 0.3333334)),
//            Item2(ContentScale.Crop, Alignment.BottomCenter, Origin(0.5, 0.3333334)),
//            Item2(ContentScale.Crop, Alignment.BottomEnd, Origin(0.5, 0.3333334)),
////        ).printlnExpectedMessage2(
////            computeExpected = {
////                contentOriginToContainerOrigin(
////                    containerSize,
////                    contentSize,
////                    it.contentScale,
////                    it.alignment,
////                    containerOrigin
////                )
////            }
//        ).forEach {
//            Assert.assertEquals(
//                it.getMessage(containerSize, contentSize, containerOrigin),
//                it.expected,
//                contentOriginToContainerOrigin(
//                    containerSize,
//                    contentSize,
//                    it.contentScale,
//                    it.alignment,
//                    containerOrigin
//                )
//            )
//        }
//    }

//    @Test
//    fun testComputeScrollEdge() {
//        val contentSize = IntSize(1000, 1000)
//
//        listOf(
//            (IntRect(0, 0, 1000, 1000) to true) to BOTH,
//            (IntRect(0, 0, 1000, 1000) to false) to BOTH,
//
//            (IntRect(0, 0, 500, 500) to true) to START,
//            (IntRect(0, 0, 500, 500) to false) to START,
//            (IntRect(200, 0, 800, 500) to true) to NONE,
//            (IntRect(200, 0, 800, 500) to false) to START,
//            (IntRect(500, 0, 1000, 1000) to true) to END,
//            (IntRect(500, 0, 1000, 500) to false) to START,
//
//            (IntRect(0, 200, 500, 800) to true) to START,
//            (IntRect(0, 200, 500, 800) to false) to NONE,
//            (IntRect(200, 200, 800, 800) to true) to NONE,
//            (IntRect(200, 200, 800, 800) to false) to NONE,
//            (IntRect(500, 200, 1000, 800) to true) to END,
//            (IntRect(500, 200, 1000, 800) to false) to NONE,
//
//            (IntRect(0, 500, 500, 1000) to true) to START,
//            (IntRect(0, 500, 500, 1000) to false) to END,
//            (IntRect(200, 500, 800, 1000) to true) to NONE,
//            (IntRect(200, 500, 800, 1000) to false) to END,
//            (IntRect(500, 500, 1000, 1000) to true) to END,
//            (IntRect(500, 500, 1000, 1000) to false) to END,
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

    data class Item(
        val contentScale: ContentScale,
        val alignment: Alignment,
        val expected: IntRect
    ) {
        fun getMessage(containerSize: IntSize, contentSize: IntSize): String {
            return "Item(" +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}" +
                    ")"
        }

        fun getMessage(containerSize: IntSize, contentSize: IntSize, scale: Float): String {
            return "Item(" +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}, " +
                    "scale=${scale}" +
                    ")"
        }
    }

    private fun List<Item>.printlnExpectedMessage(computeExpected: (Item) -> IntRect): List<Item> {
        this.map {
            val expected = computeExpected(it)
            "Item(" +
                    "ContentScale.${it.contentScale.name}, " +
                    "Alignment.${it.alignment.name}, " +
                    "IntRect(${expected.run { "${left},${top},${right},${bottom}f" }})" +
                    ")"
        }.apply {
            Assert.fail(joinToString(separator = ", \n", postfix = ","))
        }
        return this
    }

    data class Item5(
        val contentSize: IntSize,
        val contentScale: ContentScale,
        val alignment: Alignment,
        override val expected: IntRect
    ) : A<IntRect> {
        override fun getMessage(containerSize: IntSize): String {
            return "Item5(" +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}" +
                    ")"
        }

        override fun getBuildExpression(r: IntRect): String {
            return "Item5(" +
                    "IntSize(${contentSize.width}, ${contentSize.height}), " +
                    "${contentScale.name}, " +
                    "${alignment.name}, " +
                    "IntRect(${r.left}, ${r.top}, ${r.right}, ${r.bottom})" +
                    ")"
        }
    }

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

    interface A<R> {
        val expected: R
        fun getMessage(containerSize: IntSize): String
        fun getBuildExpression(r: R): String
    }

    private fun <R, T : A<R>> List<T>.check(
        containerSize: IntSize,
        printBatchBuildExpression: Boolean = false,
        computeResult: (T) -> R
    ) {
        if (printBatchBuildExpression) {
            this.map { item ->
                val result = computeResult(item)
                item.getBuildExpression(result)
            }.apply {
                Assert.fail(joinToString(separator = ", \n", postfix = ","))
            }
        }
        this.forEach { item ->
            val result = computeResult(item)
            Assert.assertEquals(
                /* message = */ item.getMessage(containerSize),
                /* expected = */ item.expected,
                /* actual = */ result,
            )
        }
    }

    private fun <R, T : A<R>> List<T>.printlnBatchBuildExpression(computeExpected: (T) -> R): List<T> {
        this.map { item ->
            val expected = computeExpected(item)
            item.getBuildExpression(expected)
        }.apply {
            Assert.fail(joinToString(separator = ", \n", postfix = ","))
        }
        return this
    }

    private fun <P1, P2, P3, R, T : A<R>> printlnBatchBuildExpression(
        p1s: List<P1>,
        p2s: List<P2>,
        p3s: List<P3>,
        buildItem: (P1, P2, P3) -> T,
        computeExpected: (T) -> R
    ) {
        val paramList = mutableListOf<T>()
        p1s.forEach { p1 ->
            p2s.forEach { p2 ->
                p3s.forEach { p3 ->
                    paramList.add(buildItem(p1, p2, p3))
                }
            }
        }
        val buildExpression =
            paramList.joinToString(separator = ", \n", prefix = "\n", postfix = ",") { item ->
                val expected = computeExpected(item)
                item.getBuildExpression(expected)
            }
        Assert.fail(buildExpression)
    }

    private fun <P1, P2, P3, P4, R, T : A<R>> printlnBatchBuildExpression(
        p1s: List<P1>,
        p2s: List<P2>,
        p3s: List<P3>,
        p4s: List<P4>,
        buildItem: (P1, P2, P3, P4) -> T,
        computeExpected: (T) -> R
    ) {
        val paramList = mutableListOf<T>()
        p1s.forEach { p1 ->
            p2s.forEach { p2 ->
                p3s.forEach { p3 ->
                    p4s.forEach { p4 ->
                        paramList.add(buildItem(p1, p2, p3, p4))
                    }
                }
            }
        }
        val buildExpression =
            paramList.joinToString(separator = ", \n", prefix = "\n", postfix = ",") { item ->
                val expected = computeExpected(item)
                item.getBuildExpression(expected)
            }
        Assert.fail(buildExpression)
    }

    data class Item2(
        val contentScale: ContentScale,
        val alignment: Alignment,
        val expected: TransformOriginCompat
    ) {
        fun getMessage(
            containerSize: IntSize,
            contentSize: IntSize,
            containerOrigin: TransformOriginCompat
        ): String {
            return "Item2(" +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}, " +
                    "containerOrigin=${containerOrigin.toShortString()}" +
                    ")"
        }
    }

    private fun List<Item2>.printlnExpectedMessage2(computeExpected: (Item2) -> TransformOriginCompat): List<Item2> {
        this.map {
            val expected = computeExpected(it)
            "Item2(" +
                    "ContentScale.${it.contentScale.name}, " +
                    "Alignment.${it.alignment.name}, " +
                    "Origin(${expected.run { "${pivotFractionX},${pivotFractionY}f" }})" +
                    ")"
        }.apply {
            Assert.fail(joinToString(separator = ", \n", postfix = ","))
        }
        return this
    }

    data class Item3(
        val offset: IntOffset,
        val expected: IntRect
    ) {
        fun getMessage(
            containerSize: IntSize,
            contentSize: IntSize,
            contentScale: ContentScale,
            alignment: Alignment,
            scale: Float
        ): String {
            return "Item3(" +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "alignment=${alignment.name}, " +
                    "scale=$scale, " +
                    "offset=${offset.toShortString()}" +
                    ")"
        }
    }

    private fun List<Item3>.printlnExpectedMessage3(computeExpected: (Item3) -> IntRect): List<Item3> {
        this.map {
            val visibleRect = computeExpected(it)
            "IntOffset(" +
                    "${it.offset.x}, ${it.offset.y}) to IntRect(${visibleRect.left}, " +
                    "${visibleRect.top}, " +
                    "${visibleRect.right}, " +
                    "${visibleRect.bottom}" +
                    ")"
        }.apply {
            Assert.fail(joinToString(separator = ", \n"))
        }
        return this
    }
}