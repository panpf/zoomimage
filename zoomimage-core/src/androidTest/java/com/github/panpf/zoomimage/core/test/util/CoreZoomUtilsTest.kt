package com.github.panpf.zoomimage.core.test.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.panpf.zoomimage.core.test.internal.Item5
import com.github.panpf.zoomimage.core.test.internal.printlnBatchBuildExpression
import com.github.panpf.zoomimage.util.AlignmentCompat.Companion.BottomCenter
import com.github.panpf.zoomimage.util.AlignmentCompat.Companion.BottomEnd
import com.github.panpf.zoomimage.util.AlignmentCompat.Companion.BottomStart
import com.github.panpf.zoomimage.util.AlignmentCompat.Companion.Center
import com.github.panpf.zoomimage.util.AlignmentCompat.Companion.CenterEnd
import com.github.panpf.zoomimage.util.AlignmentCompat.Companion.CenterStart
import com.github.panpf.zoomimage.util.AlignmentCompat.Companion.TopCenter
import com.github.panpf.zoomimage.util.AlignmentCompat.Companion.TopEnd
import com.github.panpf.zoomimage.util.AlignmentCompat.Companion.TopStart
import com.github.panpf.zoomimage.util.ContentScaleCompat.Companion.Crop
import com.github.panpf.zoomimage.util.ContentScaleCompat.Companion.FillBounds
import com.github.panpf.zoomimage.util.ContentScaleCompat.Companion.FillHeight
import com.github.panpf.zoomimage.util.ContentScaleCompat.Companion.FillWidth
import com.github.panpf.zoomimage.util.ContentScaleCompat.Companion.Fit
import com.github.panpf.zoomimage.util.ContentScaleCompat.Companion.Inside
import com.github.panpf.zoomimage.util.ContentScaleCompat.Companion.None
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat.Companion
import com.github.panpf.zoomimage.util.calculateNextStepScale
import com.github.panpf.zoomimage.util.computeContentBaseDisplayRect
import com.github.panpf.zoomimage.util.computeContentBaseInsideDisplayRect
import com.github.panpf.zoomimage.util.computeContentVisibleRect
import com.github.panpf.zoomimage.util.round
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreZoomUtilsTest {
    // todo Implementation tests

//    @Test
//    fun testComputeAlignmentOffset() {
//        val containerSize = IntSizeCompat(1080, 1656)
//        val printBatchBuildExpression = false
////        val printBatchBuildExpression = true
//
//        if (printBatchBuildExpression) {
//            printlnBatchBuildExpression(
//                p1s = listOf(
//                    IntSizeCompat(7500, 232), IntSizeCompat(173, 3044), IntSizeCompat(575, 427), IntSizeCompat(551, 1038),
//                ),
//                p2s = listOf(None, Inside, Fit, FillWidth, FillHeight, FillBounds, Crop),
//                p3s = listOf(
//                    TopStart, TopCenter, TopEnd,
//                    CenterStart, Center, CenterEnd,
//                    BottomStart, BottomCenter, BottomEnd,
//                ),
//                buildItem = { p1, p2, p3 ->
//                    Item6(p1, p2, p3, IntOffsetCompat.Zero)
//                },
//            ) {
//                computeAlignmentIntOffset(
//                    containerSize = containerSize,
//                    contentSize = it.contentSize,
//                    contentScale = it.contentScale,
//                    alignment = it.alignment,
//                )
//            }
//        }
//
//        listOf(
//            Item6(IntSizeCompat(7500, 232), None, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), None, TopCenter, IntOffsetCompat(-3210, 0)),
//            Item6(IntSizeCompat(7500, 232), None, TopEnd, IntOffsetCompat(-6420, 0)),
//            Item6(IntSizeCompat(7500, 232), None, CenterStart, IntOffsetCompat(0, 712)),
//            Item6(IntSizeCompat(7500, 232), None, Center, IntOffsetCompat(-3210, 712)),
//            Item6(IntSizeCompat(7500, 232), None, CenterEnd, IntOffsetCompat(-6420, 712)),
//            Item6(IntSizeCompat(7500, 232), None, BottomStart, IntOffsetCompat(0, 1424)),
//            Item6(IntSizeCompat(7500, 232), None, BottomCenter, IntOffsetCompat(-3210, 1424)),
//            Item6(IntSizeCompat(7500, 232), None, BottomEnd, IntOffsetCompat(-6420, 1424)),
//            Item6(IntSizeCompat(7500, 232), Inside, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), Inside, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), Inside, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), Inside, CenterStart, IntOffsetCompat(0, 812)),
//            Item6(IntSizeCompat(7500, 232), Inside, Center, IntOffsetCompat(0, 812)),
//            Item6(IntSizeCompat(7500, 232), Inside, CenterEnd, IntOffsetCompat(0, 812)),
//            Item6(IntSizeCompat(7500, 232), Inside, BottomStart, IntOffsetCompat(0, 1623)),
//            Item6(IntSizeCompat(7500, 232), Inside, BottomCenter, IntOffsetCompat(0, 1623)),
//            Item6(IntSizeCompat(7500, 232), Inside, BottomEnd, IntOffsetCompat(0, 1623)),
//            Item6(IntSizeCompat(7500, 232), Fit, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), Fit, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), Fit, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), Fit, CenterStart, IntOffsetCompat(0, 812)),
//            Item6(IntSizeCompat(7500, 232), Fit, Center, IntOffsetCompat(0, 812)),
//            Item6(IntSizeCompat(7500, 232), Fit, CenterEnd, IntOffsetCompat(0, 812)),
//            Item6(IntSizeCompat(7500, 232), Fit, BottomStart, IntOffsetCompat(0, 1623)),
//            Item6(IntSizeCompat(7500, 232), Fit, BottomCenter, IntOffsetCompat(0, 1623)),
//            Item6(IntSizeCompat(7500, 232), Fit, BottomEnd, IntOffsetCompat(0, 1623)),
//            Item6(IntSizeCompat(7500, 232), FillWidth, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillWidth, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillWidth, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillWidth, CenterStart, IntOffsetCompat(0, 812)),
//            Item6(IntSizeCompat(7500, 232), FillWidth, Center, IntOffsetCompat(0, 812)),
//            Item6(IntSizeCompat(7500, 232), FillWidth, CenterEnd, IntOffsetCompat(0, 812)),
//            Item6(IntSizeCompat(7500, 232), FillWidth, BottomStart, IntOffsetCompat(0, 1623)),
//            Item6(IntSizeCompat(7500, 232), FillWidth, BottomCenter, IntOffsetCompat(0, 1623)),
//            Item6(IntSizeCompat(7500, 232), FillWidth, BottomEnd, IntOffsetCompat(0, 1623)),
//            Item6(IntSizeCompat(7500, 232), FillHeight, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillHeight, TopCenter, IntOffsetCompat(-26227, 0)),
//            Item6(IntSizeCompat(7500, 232), FillHeight, TopEnd, IntOffsetCompat(-52454, 0)),
//            Item6(IntSizeCompat(7500, 232), FillHeight, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillHeight, Center, IntOffsetCompat(-26227, 0)),
//            Item6(IntSizeCompat(7500, 232), FillHeight, CenterEnd, IntOffsetCompat(-52454, 0)),
//            Item6(IntSizeCompat(7500, 232), FillHeight, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillHeight, BottomCenter, IntOffsetCompat(-26227, 0)),
//            Item6(IntSizeCompat(7500, 232), FillHeight, BottomEnd, IntOffsetCompat(-52454, 0)),
//            Item6(IntSizeCompat(7500, 232), FillBounds, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillBounds, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillBounds, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillBounds, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillBounds, Center, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillBounds, CenterEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillBounds, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillBounds, BottomCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), FillBounds, BottomEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), Crop, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), Crop, TopCenter, IntOffsetCompat(-26227, 0)),
//            Item6(IntSizeCompat(7500, 232), Crop, TopEnd, IntOffsetCompat(-52454, 0)),
//            Item6(IntSizeCompat(7500, 232), Crop, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), Crop, Center, IntOffsetCompat(-26227, 0)),
//            Item6(IntSizeCompat(7500, 232), Crop, CenterEnd, IntOffsetCompat(-52454, 0)),
//            Item6(IntSizeCompat(7500, 232), Crop, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(7500, 232), Crop, BottomCenter, IntOffsetCompat(-26227, 0)),
//            Item6(IntSizeCompat(7500, 232), Crop, BottomEnd, IntOffsetCompat(-52454, 0)),
//            Item6(IntSizeCompat(173, 3044), None, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), None, TopCenter, IntOffsetCompat(454, 0)),
//            Item6(IntSizeCompat(173, 3044), None, TopEnd, IntOffsetCompat(907, 0)),
//            Item6(IntSizeCompat(173, 3044), None, CenterStart, IntOffsetCompat(0, -694)),
//            Item6(IntSizeCompat(173, 3044), None, Center, IntOffsetCompat(454, -694)),
//            Item6(IntSizeCompat(173, 3044), None, CenterEnd, IntOffsetCompat(907, -694)),
//            Item6(IntSizeCompat(173, 3044), None, BottomStart, IntOffsetCompat(0, -1388)),
//            Item6(IntSizeCompat(173, 3044), None, BottomCenter, IntOffsetCompat(454, -1388)),
//            Item6(IntSizeCompat(173, 3044), None, BottomEnd, IntOffsetCompat(907, -1388)),
//            Item6(IntSizeCompat(173, 3044), Inside, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), Inside, TopCenter, IntOffsetCompat(493, 0)),
//            Item6(IntSizeCompat(173, 3044), Inside, TopEnd, IntOffsetCompat(986, 0)),
//            Item6(IntSizeCompat(173, 3044), Inside, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), Inside, Center, IntOffsetCompat(493, 0)),
//            Item6(IntSizeCompat(173, 3044), Inside, CenterEnd, IntOffsetCompat(986, 0)),
//            Item6(IntSizeCompat(173, 3044), Inside, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), Inside, BottomCenter, IntOffsetCompat(493, 0)),
//            Item6(IntSizeCompat(173, 3044), Inside, BottomEnd, IntOffsetCompat(986, 0)),
//            Item6(IntSizeCompat(173, 3044), Fit, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), Fit, TopCenter, IntOffsetCompat(493, 0)),
//            Item6(IntSizeCompat(173, 3044), Fit, TopEnd, IntOffsetCompat(986, 0)),
//            Item6(IntSizeCompat(173, 3044), Fit, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), Fit, Center, IntOffsetCompat(493, 0)),
//            Item6(IntSizeCompat(173, 3044), Fit, CenterEnd, IntOffsetCompat(986, 0)),
//            Item6(IntSizeCompat(173, 3044), Fit, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), Fit, BottomCenter, IntOffsetCompat(493, 0)),
//            Item6(IntSizeCompat(173, 3044), Fit, BottomEnd, IntOffsetCompat(986, 0)),
//            Item6(IntSizeCompat(173, 3044), FillWidth, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillWidth, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillWidth, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillWidth, CenterStart, IntOffsetCompat(0, -8673)),
//            Item6(IntSizeCompat(173, 3044), FillWidth, Center, IntOffsetCompat(0, -8673)),
//            Item6(IntSizeCompat(173, 3044), FillWidth, CenterEnd, IntOffsetCompat(0, -8673)),
//            Item6(IntSizeCompat(173, 3044), FillWidth, BottomStart, IntOffsetCompat(0, -17347)),
//            Item6(IntSizeCompat(173, 3044), FillWidth, BottomCenter, IntOffsetCompat(0, -17347)),
//            Item6(IntSizeCompat(173, 3044), FillWidth, BottomEnd, IntOffsetCompat(0, -17347)),
//            Item6(IntSizeCompat(173, 3044), FillHeight, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillHeight, TopCenter, IntOffsetCompat(493, 0)),
//            Item6(IntSizeCompat(173, 3044), FillHeight, TopEnd, IntOffsetCompat(986, 0)),
//            Item6(IntSizeCompat(173, 3044), FillHeight, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillHeight, Center, IntOffsetCompat(493, 0)),
//            Item6(IntSizeCompat(173, 3044), FillHeight, CenterEnd, IntOffsetCompat(986, 0)),
//            Item6(IntSizeCompat(173, 3044), FillHeight, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillHeight, BottomCenter, IntOffsetCompat(493, 0)),
//            Item6(IntSizeCompat(173, 3044), FillHeight, BottomEnd, IntOffsetCompat(986, 0)),
//            Item6(IntSizeCompat(173, 3044), FillBounds, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillBounds, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillBounds, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillBounds, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillBounds, Center, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillBounds, CenterEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillBounds, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillBounds, BottomCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), FillBounds, BottomEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), Crop, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), Crop, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), Crop, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(173, 3044), Crop, CenterStart, IntOffsetCompat(0, -8673)),
//            Item6(IntSizeCompat(173, 3044), Crop, Center, IntOffsetCompat(0, -8673)),
//            Item6(IntSizeCompat(173, 3044), Crop, CenterEnd, IntOffsetCompat(0, -8673)),
//            Item6(IntSizeCompat(173, 3044), Crop, BottomStart, IntOffsetCompat(0, -17347)),
//            Item6(IntSizeCompat(173, 3044), Crop, BottomCenter, IntOffsetCompat(0, -17347)),
//            Item6(IntSizeCompat(173, 3044), Crop, BottomEnd, IntOffsetCompat(0, -17347)),
//            Item6(IntSizeCompat(575, 427), None, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), None, TopCenter, IntOffsetCompat(253, 0)),
//            Item6(IntSizeCompat(575, 427), None, TopEnd, IntOffsetCompat(505, 0)),
//            Item6(IntSizeCompat(575, 427), None, CenterStart, IntOffsetCompat(0, 615)),
//            Item6(IntSizeCompat(575, 427), None, Center, IntOffsetCompat(253, 615)),
//            Item6(IntSizeCompat(575, 427), None, CenterEnd, IntOffsetCompat(505, 615)),
//            Item6(IntSizeCompat(575, 427), None, BottomStart, IntOffsetCompat(0, 1229)),
//            Item6(IntSizeCompat(575, 427), None, BottomCenter, IntOffsetCompat(253, 1229)),
//            Item6(IntSizeCompat(575, 427), None, BottomEnd, IntOffsetCompat(505, 1229)),
//            Item6(IntSizeCompat(575, 427), Inside, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), Inside, TopCenter, IntOffsetCompat(253, 0)),
//            Item6(IntSizeCompat(575, 427), Inside, TopEnd, IntOffsetCompat(505, 0)),
//            Item6(IntSizeCompat(575, 427), Inside, CenterStart, IntOffsetCompat(0, 615)),
//            Item6(IntSizeCompat(575, 427), Inside, Center, IntOffsetCompat(253, 615)),
//            Item6(IntSizeCompat(575, 427), Inside, CenterEnd, IntOffsetCompat(505, 615)),
//            Item6(IntSizeCompat(575, 427), Inside, BottomStart, IntOffsetCompat(0, 1229)),
//            Item6(IntSizeCompat(575, 427), Inside, BottomCenter, IntOffsetCompat(253, 1229)),
//            Item6(IntSizeCompat(575, 427), Inside, BottomEnd, IntOffsetCompat(505, 1229)),
//            Item6(IntSizeCompat(575, 427), Fit, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), Fit, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), Fit, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), Fit, CenterStart, IntOffsetCompat(0, 427)),
//            Item6(IntSizeCompat(575, 427), Fit, Center, IntOffsetCompat(0, 427)),
//            Item6(IntSizeCompat(575, 427), Fit, CenterEnd, IntOffsetCompat(0, 427)),
//            Item6(IntSizeCompat(575, 427), Fit, BottomStart, IntOffsetCompat(0, 854)),
//            Item6(IntSizeCompat(575, 427), Fit, BottomCenter, IntOffsetCompat(0, 854)),
//            Item6(IntSizeCompat(575, 427), Fit, BottomEnd, IntOffsetCompat(0, 854)),
//            Item6(IntSizeCompat(575, 427), FillWidth, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillWidth, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillWidth, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillWidth, CenterStart, IntOffsetCompat(0, 427)),
//            Item6(IntSizeCompat(575, 427), FillWidth, Center, IntOffsetCompat(0, 427)),
//            Item6(IntSizeCompat(575, 427), FillWidth, CenterEnd, IntOffsetCompat(0, 427)),
//            Item6(IntSizeCompat(575, 427), FillWidth, BottomStart, IntOffsetCompat(0, 854)),
//            Item6(IntSizeCompat(575, 427), FillWidth, BottomCenter, IntOffsetCompat(0, 854)),
//            Item6(IntSizeCompat(575, 427), FillWidth, BottomEnd, IntOffsetCompat(0, 854)),
//            Item6(IntSizeCompat(575, 427), FillHeight, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillHeight, TopCenter, IntOffsetCompat(-575, 0)),
//            Item6(IntSizeCompat(575, 427), FillHeight, TopEnd, IntOffsetCompat(-1150, 0)),
//            Item6(IntSizeCompat(575, 427), FillHeight, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillHeight, Center, IntOffsetCompat(-575, 0)),
//            Item6(IntSizeCompat(575, 427), FillHeight, CenterEnd, IntOffsetCompat(-1150, 0)),
//            Item6(IntSizeCompat(575, 427), FillHeight, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillHeight, BottomCenter, IntOffsetCompat(-575, 0)),
//            Item6(IntSizeCompat(575, 427), FillHeight, BottomEnd, IntOffsetCompat(-1150, 0)),
//            Item6(IntSizeCompat(575, 427), FillBounds, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillBounds, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillBounds, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillBounds, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillBounds, Center, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillBounds, CenterEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillBounds, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillBounds, BottomCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), FillBounds, BottomEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), Crop, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), Crop, TopCenter, IntOffsetCompat(-575, 0)),
//            Item6(IntSizeCompat(575, 427), Crop, TopEnd, IntOffsetCompat(-1150, 0)),
//            Item6(IntSizeCompat(575, 427), Crop, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), Crop, Center, IntOffsetCompat(-575, 0)),
//            Item6(IntSizeCompat(575, 427), Crop, CenterEnd, IntOffsetCompat(-1150, 0)),
//            Item6(IntSizeCompat(575, 427), Crop, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(575, 427), Crop, BottomCenter, IntOffsetCompat(-575, 0)),
//            Item6(IntSizeCompat(575, 427), Crop, BottomEnd, IntOffsetCompat(-1150, 0)),
//            Item6(IntSizeCompat(551, 1038), None, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), None, TopCenter, IntOffsetCompat(265, 0)),
//            Item6(IntSizeCompat(551, 1038), None, TopEnd, IntOffsetCompat(529, 0)),
//            Item6(IntSizeCompat(551, 1038), None, CenterStart, IntOffsetCompat(0, 309)),
//            Item6(IntSizeCompat(551, 1038), None, Center, IntOffsetCompat(265, 309)),
//            Item6(IntSizeCompat(551, 1038), None, CenterEnd, IntOffsetCompat(529, 309)),
//            Item6(IntSizeCompat(551, 1038), None, BottomStart, IntOffsetCompat(0, 618)),
//            Item6(IntSizeCompat(551, 1038), None, BottomCenter, IntOffsetCompat(265, 618)),
//            Item6(IntSizeCompat(551, 1038), None, BottomEnd, IntOffsetCompat(529, 618)),
//            Item6(IntSizeCompat(551, 1038), Inside, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), Inside, TopCenter, IntOffsetCompat(265, 0)),
//            Item6(IntSizeCompat(551, 1038), Inside, TopEnd, IntOffsetCompat(529, 0)),
//            Item6(IntSizeCompat(551, 1038), Inside, CenterStart, IntOffsetCompat(0, 309)),
//            Item6(IntSizeCompat(551, 1038), Inside, Center, IntOffsetCompat(265, 309)),
//            Item6(IntSizeCompat(551, 1038), Inside, CenterEnd, IntOffsetCompat(529, 309)),
//            Item6(IntSizeCompat(551, 1038), Inside, BottomStart, IntOffsetCompat(0, 618)),
//            Item6(IntSizeCompat(551, 1038), Inside, BottomCenter, IntOffsetCompat(265, 618)),
//            Item6(IntSizeCompat(551, 1038), Inside, BottomEnd, IntOffsetCompat(529, 618)),
//            Item6(IntSizeCompat(551, 1038), Fit, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), Fit, TopCenter, IntOffsetCompat(101, 0)),
//            Item6(IntSizeCompat(551, 1038), Fit, TopEnd, IntOffsetCompat(201, 0)),
//            Item6(IntSizeCompat(551, 1038), Fit, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), Fit, Center, IntOffsetCompat(101, 0)),
//            Item6(IntSizeCompat(551, 1038), Fit, CenterEnd, IntOffsetCompat(201, 0)),
//            Item6(IntSizeCompat(551, 1038), Fit, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), Fit, BottomCenter, IntOffsetCompat(101, 0)),
//            Item6(IntSizeCompat(551, 1038), Fit, BottomEnd, IntOffsetCompat(201, 0)),
//            Item6(IntSizeCompat(551, 1038), FillWidth, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillWidth, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillWidth, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillWidth, CenterStart, IntOffsetCompat(0, -189)),
//            Item6(IntSizeCompat(551, 1038), FillWidth, Center, IntOffsetCompat(0, -189)),
//            Item6(IntSizeCompat(551, 1038), FillWidth, CenterEnd, IntOffsetCompat(0, -189)),
//            Item6(IntSizeCompat(551, 1038), FillWidth, BottomStart, IntOffsetCompat(0, -379)),
//            Item6(IntSizeCompat(551, 1038), FillWidth, BottomCenter, IntOffsetCompat(0, -379)),
//            Item6(IntSizeCompat(551, 1038), FillWidth, BottomEnd, IntOffsetCompat(0, -379)),
//            Item6(IntSizeCompat(551, 1038), FillHeight, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillHeight, TopCenter, IntOffsetCompat(101, 0)),
//            Item6(IntSizeCompat(551, 1038), FillHeight, TopEnd, IntOffsetCompat(201, 0)),
//            Item6(IntSizeCompat(551, 1038), FillHeight, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillHeight, Center, IntOffsetCompat(101, 0)),
//            Item6(IntSizeCompat(551, 1038), FillHeight, CenterEnd, IntOffsetCompat(201, 0)),
//            Item6(IntSizeCompat(551, 1038), FillHeight, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillHeight, BottomCenter, IntOffsetCompat(101, 0)),
//            Item6(IntSizeCompat(551, 1038), FillHeight, BottomEnd, IntOffsetCompat(201, 0)),
//            Item6(IntSizeCompat(551, 1038), FillBounds, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillBounds, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillBounds, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillBounds, CenterStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillBounds, Center, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillBounds, CenterEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillBounds, BottomStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillBounds, BottomCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), FillBounds, BottomEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), Crop, TopStart, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), Crop, TopCenter, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), Crop, TopEnd, IntOffsetCompat(0, 0)),
//            Item6(IntSizeCompat(551, 1038), Crop, CenterStart, IntOffsetCompat(0, -189)),
//            Item6(IntSizeCompat(551, 1038), Crop, Center, IntOffsetCompat(0, -189)),
//            Item6(IntSizeCompat(551, 1038), Crop, CenterEnd, IntOffsetCompat(0, -189)),
//            Item6(IntSizeCompat(551, 1038), Crop, BottomStart, IntOffsetCompat(0, -379)),
//            Item6(IntSizeCompat(551, 1038), Crop, BottomCenter, IntOffsetCompat(0, -379)),
//            Item6(IntSizeCompat(551, 1038), Crop, BottomEnd, IntOffsetCompat(0, -379)),
//        ).forEach { item ->
//            val result = computeAlignmentIntOffset(
//                containerSize = containerSize,
//                contentSize = item.contentSize,
//                contentScale = item.contentScale,
//                alignment = item.alignment,
//            )
//            Assert.assertEquals(
//                /* message = */ item.getMessage(containerSize),
//                /* expected = */ item.expected,
//                /* actual = */ result,
//            )
//        }
//    }
//
//    @Test
//    fun testComputeLocationOffset() {
//        val containerSize = IntSizeCompat(1000, 2000)
//
//        var scale = 1f
//        listOf(
//            TransformOriginCompat(0.25f, 0.25f) to Offset(0f, 0f),
//            TransformOriginCompat(0.75f, 0.25f) to Offset(-250f, 0f),
//            TransformOriginCompat(0.5f, 0.5f) to Offset(-0f, -0f),
//            TransformOriginCompat(0.25f, 0.75f) to Offset(0f, -500f),
//            TransformOriginCompat(0.75f, 0.75f) to Offset(-250f, -500f),
//        ).forEach { (containerOrigin, expected) ->
//            val containerPoint = IntOffsetCompat(
//                x = (containerOrigin.pivotFractionX * containerSize.width).roundToInt(),
//                y = (containerOrigin.pivotFractionY * containerSize.height).roundToInt(),
//            )
//            Assert.assertEquals(
//                /* message = */ "containerSize=$containerSize, scale=$scale, containerOrigin=$containerOrigin",
//                /* expected = */ expected,
//                /* actual = */ computeLocationUserOffset(containerSize, containerPoint, scale)
//            )
//        }
//
//        scale = 2f
//        listOf(
//            TransformOriginCompat(0.25f, 0.25f) to Offset(-0f, -0f),
//            TransformOriginCompat(0.75f, 0.25f) to Offset(-1000f, -0f),
//            TransformOriginCompat(0.5f, 0.5f) to Offset(-500f, -1000f),
//            TransformOriginCompat(0.25f, 0.75f) to Offset(-0f, -2000f),
//            TransformOriginCompat(0.75f, 0.75f) to Offset(-1000f, -2000f),
//        ).forEach { (containerOrigin, expected) ->
//            val containerPoint = IntOffsetCompat(
//                x = (containerOrigin.pivotFractionX * containerSize.width).roundToInt(),
//                y = (containerOrigin.pivotFractionY * containerSize.height).roundToInt(),
//            )
//            Assert.assertEquals(
//                /* message = */ "containerSize=$containerSize, scale=$scale, containerOrigin=$containerOrigin",
//                /* expected = */ expected,
//                /* actual = */ computeLocationUserOffset(containerSize, containerPoint, scale)
//            )
//        }
//    }


    @Test
    fun testComputeContentBaseDisplayRect() {
        val containerSize = IntSizeCompat(1080, 1656)
        val printBatchBuildExpression = false
//        val printBatchBuildExpression = true

        if (printBatchBuildExpression) {
            printlnBatchBuildExpression(
                p1s = listOf(
                    IntSizeCompat(7500, 232),
                    IntSizeCompat(173, 3044),
                    IntSizeCompat(575, 427),
                    IntSizeCompat(551, 1038),
                ),
                p2s = listOf(None, Inside, Fit, FillWidth, FillHeight, FillBounds, Crop),
                p3s = listOf(
                    TopStart, TopCenter, TopEnd,
                    CenterStart, Center, CenterEnd,
                    BottomStart, BottomCenter, BottomEnd,
                ),
                // todo rotation
                buildItem = { p1, p2, p3 ->
                    Item5(p1, p2, p3, IntRectCompat.Zero)
                },
            ) { item ->
                computeContentBaseDisplayRect(
                    containerSize = containerSize,
                    contentSize = item.contentSize,
                    contentScale = item.contentScale,
                    alignment = item.alignment,
                    rotation = 0,
                ).round()
            }
        }

        listOf(
            Item5(IntSizeCompat(7500, 232), None, TopStart, IntRectCompat(0, 0, 7500, 232)),
            Item5(IntSizeCompat(7500, 232), None, TopCenter, IntRectCompat(-3210, 0, 4290, 232)),
            Item5(IntSizeCompat(7500, 232), None, TopEnd, IntRectCompat(-6420, 0, 1080, 232)),
            Item5(IntSizeCompat(7500, 232), None, CenterStart, IntRectCompat(0, 712, 7500, 944)),
            Item5(IntSizeCompat(7500, 232), None, Center, IntRectCompat(-3210, 712, 4290, 944)),
            Item5(IntSizeCompat(7500, 232), None, CenterEnd, IntRectCompat(-6420, 712, 1080, 944)),
            Item5(IntSizeCompat(7500, 232), None, BottomStart, IntRectCompat(0, 1424, 7500, 1656)),
            Item5(
                IntSizeCompat(7500, 232),
                None,
                BottomCenter,
                IntRectCompat(-3210, 1424, 4290, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                None,
                BottomEnd,
                IntRectCompat(-6420, 1424, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), Inside, TopStart, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Inside, TopCenter, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Inside, TopEnd, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Inside, CenterStart, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), Inside, Center, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), Inside, CenterEnd, IntRectCompat(0, 812, 1080, 845)),
            Item5(
                IntSizeCompat(7500, 232),
                Inside,
                BottomStart,
                IntRectCompat(0, 1623, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                Inside,
                BottomCenter,
                IntRectCompat(0, 1623, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), Inside, BottomEnd, IntRectCompat(0, 1623, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Fit, TopStart, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Fit, TopCenter, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Fit, TopEnd, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Fit, CenterStart, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), Fit, Center, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), Fit, CenterEnd, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), Fit, BottomStart, IntRectCompat(0, 1623, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Fit, BottomCenter, IntRectCompat(0, 1623, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Fit, BottomEnd, IntRectCompat(0, 1623, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillWidth, TopStart, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), FillWidth, TopCenter, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), FillWidth, TopEnd, IntRectCompat(0, 0, 1080, 33)),
            Item5(
                IntSizeCompat(7500, 232),
                FillWidth,
                CenterStart,
                IntRectCompat(0, 812, 1080, 845)
            ),
            Item5(IntSizeCompat(7500, 232), FillWidth, Center, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), FillWidth, CenterEnd, IntRectCompat(0, 812, 1080, 845)),
            Item5(
                IntSizeCompat(7500, 232),
                FillWidth,
                BottomStart,
                IntRectCompat(0, 1623, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillWidth,
                BottomCenter,
                IntRectCompat(0, 1623, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillWidth,
                BottomEnd,
                IntRectCompat(0, 1623, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), FillHeight, TopStart, IntRectCompat(0, 0, 53534, 1656)),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                TopCenter,
                IntRectCompat(-26227, 0, 27307, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                TopEnd,
                IntRectCompat(-52454, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                CenterStart,
                IntRectCompat(0, 0, 53534, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                Center,
                IntRectCompat(-26227, 0, 27307, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                CenterEnd,
                IntRectCompat(-52454, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                BottomStart,
                IntRectCompat(0, 0, 53534, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                BottomCenter,
                IntRectCompat(-26227, 0, 27307, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                BottomEnd,
                IntRectCompat(-52454, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), FillBounds, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillBounds, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillBounds, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(7500, 232),
                FillBounds,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), FillBounds, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillBounds, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(7500, 232),
                FillBounds,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillBounds,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), FillBounds, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, TopStart, IntRectCompat(0, 0, 53534, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, TopCenter, IntRectCompat(-26227, 0, 27307, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, TopEnd, IntRectCompat(-52454, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, CenterStart, IntRectCompat(0, 0, 53534, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, Center, IntRectCompat(-26227, 0, 27307, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, CenterEnd, IntRectCompat(-52454, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, BottomStart, IntRectCompat(0, 0, 53534, 1656)),
            Item5(
                IntSizeCompat(7500, 232),
                Crop,
                BottomCenter,
                IntRectCompat(-26227, 0, 27307, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), Crop, BottomEnd, IntRectCompat(-52454, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), None, TopStart, IntRectCompat(0, 0, 173, 3044)),
            Item5(IntSizeCompat(173, 3044), None, TopCenter, IntRectCompat(454, 0, 627, 3044)),
            Item5(IntSizeCompat(173, 3044), None, TopEnd, IntRectCompat(907, 0, 1080, 3044)),
            Item5(IntSizeCompat(173, 3044), None, CenterStart, IntRectCompat(0, -694, 173, 2350)),
            Item5(IntSizeCompat(173, 3044), None, Center, IntRectCompat(454, -694, 627, 2350)),
            Item5(IntSizeCompat(173, 3044), None, CenterEnd, IntRectCompat(907, -694, 1080, 2350)),
            Item5(IntSizeCompat(173, 3044), None, BottomStart, IntRectCompat(0, -1388, 173, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                None,
                BottomCenter,
                IntRectCompat(454, -1388, 627, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), None, BottomEnd, IntRectCompat(907, -1388, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, TopStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, TopCenter, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, TopEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, CenterStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, Center, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, CenterEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, BottomStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, BottomCenter, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, BottomEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, TopStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, TopCenter, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, TopEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, CenterStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, Center, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, CenterEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, BottomStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, BottomCenter, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, BottomEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillWidth, TopStart, IntRectCompat(0, 0, 1080, 19003)),
            Item5(IntSizeCompat(173, 3044), FillWidth, TopCenter, IntRectCompat(0, 0, 1080, 19003)),
            Item5(IntSizeCompat(173, 3044), FillWidth, TopEnd, IntRectCompat(0, 0, 1080, 19003)),
            Item5(
                IntSizeCompat(173, 3044),
                FillWidth,
                CenterStart,
                IntRectCompat(0, -8673, 1080, 10330)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                FillWidth,
                Center,
                IntRectCompat(0, -8673, 1080, 10330)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                FillWidth,
                CenterEnd,
                IntRectCompat(0, -8673, 1080, 10330)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                FillWidth,
                BottomStart,
                IntRectCompat(0, -17347, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                FillWidth,
                BottomCenter,
                IntRectCompat(0, -17347, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                FillWidth,
                BottomEnd,
                IntRectCompat(0, -17347, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillHeight, TopStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillHeight,
                TopCenter,
                IntRectCompat(493, 0, 587, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillHeight, TopEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillHeight, CenterStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), FillHeight, Center, IntRectCompat(493, 0, 587, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillHeight,
                CenterEnd,
                IntRectCompat(986, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillHeight, BottomStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillHeight,
                BottomCenter,
                IntRectCompat(493, 0, 587, 1656)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                FillHeight,
                BottomEnd,
                IntRectCompat(986, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillBounds, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillBounds, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillBounds, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillBounds,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillBounds, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillBounds, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillBounds,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                FillBounds,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillBounds, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Crop, TopStart, IntRectCompat(0, 0, 1080, 19003)),
            Item5(IntSizeCompat(173, 3044), Crop, TopCenter, IntRectCompat(0, 0, 1080, 19003)),
            Item5(IntSizeCompat(173, 3044), Crop, TopEnd, IntRectCompat(0, 0, 1080, 19003)),
            Item5(
                IntSizeCompat(173, 3044),
                Crop,
                CenterStart,
                IntRectCompat(0, -8673, 1080, 10330)
            ),
            Item5(IntSizeCompat(173, 3044), Crop, Center, IntRectCompat(0, -8673, 1080, 10330)),
            Item5(IntSizeCompat(173, 3044), Crop, CenterEnd, IntRectCompat(0, -8673, 1080, 10330)),
            Item5(
                IntSizeCompat(173, 3044),
                Crop,
                BottomStart,
                IntRectCompat(0, -17347, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                Crop,
                BottomCenter,
                IntRectCompat(0, -17347, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), Crop, BottomEnd, IntRectCompat(0, -17347, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), None, TopStart, IntRectCompat(0, 0, 575, 427)),
            Item5(IntSizeCompat(575, 427), None, TopCenter, IntRectCompat(253, 0, 828, 427)),
            Item5(IntSizeCompat(575, 427), None, TopEnd, IntRectCompat(505, 0, 1080, 427)),
            Item5(IntSizeCompat(575, 427), None, CenterStart, IntRectCompat(0, 615, 575, 1042)),
            Item5(IntSizeCompat(575, 427), None, Center, IntRectCompat(253, 615, 828, 1042)),
            Item5(IntSizeCompat(575, 427), None, CenterEnd, IntRectCompat(505, 615, 1080, 1042)),
            Item5(IntSizeCompat(575, 427), None, BottomStart, IntRectCompat(0, 1229, 575, 1656)),
            Item5(IntSizeCompat(575, 427), None, BottomCenter, IntRectCompat(253, 1229, 828, 1656)),
            Item5(IntSizeCompat(575, 427), None, BottomEnd, IntRectCompat(505, 1229, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Inside, TopStart, IntRectCompat(0, 0, 575, 427)),
            Item5(IntSizeCompat(575, 427), Inside, TopCenter, IntRectCompat(253, 0, 828, 427)),
            Item5(IntSizeCompat(575, 427), Inside, TopEnd, IntRectCompat(505, 0, 1080, 427)),
            Item5(IntSizeCompat(575, 427), Inside, CenterStart, IntRectCompat(0, 615, 575, 1042)),
            Item5(IntSizeCompat(575, 427), Inside, Center, IntRectCompat(253, 615, 828, 1042)),
            Item5(IntSizeCompat(575, 427), Inside, CenterEnd, IntRectCompat(505, 615, 1080, 1042)),
            Item5(IntSizeCompat(575, 427), Inside, BottomStart, IntRectCompat(0, 1229, 575, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                Inside,
                BottomCenter,
                IntRectCompat(253, 1229, 828, 1656)
            ),
            Item5(IntSizeCompat(575, 427), Inside, BottomEnd, IntRectCompat(505, 1229, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Fit, TopStart, IntRectCompat(0, 0, 1080, 802)),
            Item5(IntSizeCompat(575, 427), Fit, TopCenter, IntRectCompat(0, 0, 1080, 802)),
            Item5(IntSizeCompat(575, 427), Fit, TopEnd, IntRectCompat(0, 0, 1080, 802)),
            Item5(IntSizeCompat(575, 427), Fit, CenterStart, IntRectCompat(0, 427, 1080, 1229)),
            Item5(IntSizeCompat(575, 427), Fit, Center, IntRectCompat(0, 427, 1080, 1229)),
            Item5(IntSizeCompat(575, 427), Fit, CenterEnd, IntRectCompat(0, 427, 1080, 1229)),
            Item5(IntSizeCompat(575, 427), Fit, BottomStart, IntRectCompat(0, 854, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Fit, BottomCenter, IntRectCompat(0, 854, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Fit, BottomEnd, IntRectCompat(0, 854, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillWidth, TopStart, IntRectCompat(0, 0, 1080, 802)),
            Item5(IntSizeCompat(575, 427), FillWidth, TopCenter, IntRectCompat(0, 0, 1080, 802)),
            Item5(IntSizeCompat(575, 427), FillWidth, TopEnd, IntRectCompat(0, 0, 1080, 802)),
            Item5(
                IntSizeCompat(575, 427),
                FillWidth,
                CenterStart,
                IntRectCompat(0, 427, 1080, 1229)
            ),
            Item5(IntSizeCompat(575, 427), FillWidth, Center, IntRectCompat(0, 427, 1080, 1229)),
            Item5(IntSizeCompat(575, 427), FillWidth, CenterEnd, IntRectCompat(0, 427, 1080, 1229)),
            Item5(
                IntSizeCompat(575, 427),
                FillWidth,
                BottomStart,
                IntRectCompat(0, 854, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(575, 427),
                FillWidth,
                BottomCenter,
                IntRectCompat(0, 854, 1080, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillWidth, BottomEnd, IntRectCompat(0, 854, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillHeight, TopStart, IntRectCompat(0, 0, 2230, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                FillHeight,
                TopCenter,
                IntRectCompat(-575, 0, 1655, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillHeight, TopEnd, IntRectCompat(-1150, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                FillHeight,
                CenterStart,
                IntRectCompat(0, 0, 2230, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillHeight, Center, IntRectCompat(-575, 0, 1655, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                FillHeight,
                CenterEnd,
                IntRectCompat(-1150, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(575, 427),
                FillHeight,
                BottomStart,
                IntRectCompat(0, 0, 2230, 1656)
            ),
            Item5(
                IntSizeCompat(575, 427),
                FillHeight,
                BottomCenter,
                IntRectCompat(-575, 0, 1655, 1656)
            ),
            Item5(
                IntSizeCompat(575, 427),
                FillHeight,
                BottomEnd,
                IntRectCompat(-1150, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillBounds, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillBounds, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillBounds, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                FillBounds,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillBounds, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillBounds, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                FillBounds,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(575, 427),
                FillBounds,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillBounds, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, TopStart, IntRectCompat(0, 0, 2230, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, TopCenter, IntRectCompat(-575, 0, 1655, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, TopEnd, IntRectCompat(-1150, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, CenterStart, IntRectCompat(0, 0, 2230, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, Center, IntRectCompat(-575, 0, 1655, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, CenterEnd, IntRectCompat(-1150, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, BottomStart, IntRectCompat(0, 0, 2230, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, BottomCenter, IntRectCompat(-575, 0, 1655, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, BottomEnd, IntRectCompat(-1150, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), None, TopStart, IntRectCompat(0, 0, 551, 1038)),
            Item5(IntSizeCompat(551, 1038), None, TopCenter, IntRectCompat(265, 0, 816, 1038)),
            Item5(IntSizeCompat(551, 1038), None, TopEnd, IntRectCompat(529, 0, 1080, 1038)),
            Item5(IntSizeCompat(551, 1038), None, CenterStart, IntRectCompat(0, 309, 551, 1347)),
            Item5(IntSizeCompat(551, 1038), None, Center, IntRectCompat(265, 309, 816, 1347)),
            Item5(IntSizeCompat(551, 1038), None, CenterEnd, IntRectCompat(529, 309, 1080, 1347)),
            Item5(IntSizeCompat(551, 1038), None, BottomStart, IntRectCompat(0, 618, 551, 1656)),
            Item5(IntSizeCompat(551, 1038), None, BottomCenter, IntRectCompat(265, 618, 816, 1656)),
            Item5(IntSizeCompat(551, 1038), None, BottomEnd, IntRectCompat(529, 618, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Inside, TopStart, IntRectCompat(0, 0, 551, 1038)),
            Item5(IntSizeCompat(551, 1038), Inside, TopCenter, IntRectCompat(265, 0, 816, 1038)),
            Item5(IntSizeCompat(551, 1038), Inside, TopEnd, IntRectCompat(529, 0, 1080, 1038)),
            Item5(IntSizeCompat(551, 1038), Inside, CenterStart, IntRectCompat(0, 309, 551, 1347)),
            Item5(IntSizeCompat(551, 1038), Inside, Center, IntRectCompat(265, 309, 816, 1347)),
            Item5(IntSizeCompat(551, 1038), Inside, CenterEnd, IntRectCompat(529, 309, 1080, 1347)),
            Item5(IntSizeCompat(551, 1038), Inside, BottomStart, IntRectCompat(0, 618, 551, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                Inside,
                BottomCenter,
                IntRectCompat(265, 618, 816, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), Inside, BottomEnd, IntRectCompat(529, 618, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, TopStart, IntRectCompat(0, 0, 879, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, TopCenter, IntRectCompat(101, 0, 980, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, TopEnd, IntRectCompat(201, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, CenterStart, IntRectCompat(0, 0, 879, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, Center, IntRectCompat(101, 0, 980, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, CenterEnd, IntRectCompat(201, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, BottomStart, IntRectCompat(0, 0, 879, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, BottomCenter, IntRectCompat(101, 0, 980, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, BottomEnd, IntRectCompat(201, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillWidth, TopStart, IntRectCompat(0, 0, 1080, 2035)),
            Item5(IntSizeCompat(551, 1038), FillWidth, TopCenter, IntRectCompat(0, 0, 1080, 2035)),
            Item5(IntSizeCompat(551, 1038), FillWidth, TopEnd, IntRectCompat(0, 0, 1080, 2035)),
            Item5(
                IntSizeCompat(551, 1038),
                FillWidth,
                CenterStart,
                IntRectCompat(0, -189, 1080, 1846)
            ),
            Item5(IntSizeCompat(551, 1038), FillWidth, Center, IntRectCompat(0, -189, 1080, 1846)),
            Item5(
                IntSizeCompat(551, 1038),
                FillWidth,
                CenterEnd,
                IntRectCompat(0, -189, 1080, 1846)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillWidth,
                BottomStart,
                IntRectCompat(0, -379, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillWidth,
                BottomCenter,
                IntRectCompat(0, -379, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillWidth,
                BottomEnd,
                IntRectCompat(0, -379, 1080, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillHeight, TopStart, IntRectCompat(0, 0, 879, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                TopCenter,
                IntRectCompat(101, 0, 980, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillHeight, TopEnd, IntRectCompat(201, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                CenterStart,
                IntRectCompat(0, 0, 879, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillHeight, Center, IntRectCompat(101, 0, 980, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                CenterEnd,
                IntRectCompat(201, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                BottomStart,
                IntRectCompat(0, 0, 879, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                BottomCenter,
                IntRectCompat(101, 0, 980, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                BottomEnd,
                IntRectCompat(201, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillBounds, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillBounds, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillBounds, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillBounds,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillBounds, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillBounds, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillBounds,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillBounds,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillBounds, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, TopStart, IntRectCompat(0, 0, 1080, 2035)),
            Item5(IntSizeCompat(551, 1038), Crop, TopCenter, IntRectCompat(0, 0, 1080, 2035)),
            Item5(IntSizeCompat(551, 1038), Crop, TopEnd, IntRectCompat(0, 0, 1080, 2035)),
            Item5(IntSizeCompat(551, 1038), Crop, CenterStart, IntRectCompat(0, -189, 1080, 1846)),
            Item5(IntSizeCompat(551, 1038), Crop, Center, IntRectCompat(0, -189, 1080, 1846)),
            Item5(IntSizeCompat(551, 1038), Crop, CenterEnd, IntRectCompat(0, -189, 1080, 1846)),
            Item5(IntSizeCompat(551, 1038), Crop, BottomStart, IntRectCompat(0, -379, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, BottomCenter, IntRectCompat(0, -379, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, BottomEnd, IntRectCompat(0, -379, 1080, 1656)),
        ).forEach { item ->
            val result = computeContentBaseDisplayRect(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentScale = item.contentScale,
                alignment = item.alignment,
                rotation = 0,
            ).round()
            Assert.assertEquals(
                /* message = */ item.getMessage(containerSize),
                /* expected = */ item.expected,
                /* actual = */ result,
            )
        }
    }

    @Test
    fun testComputeContentBaseInsideDisplayRect() {
        val containerSize = IntSizeCompat(1080, 1656)

        val printBatchBuildExpression = false
//        val printBatchBuildExpression = true
        if (printBatchBuildExpression) {
            printlnBatchBuildExpression(
                p1s = listOf(
                    IntSizeCompat(7500, 232),
                    IntSizeCompat(173, 3044),
                    IntSizeCompat(575, 427),
                    IntSizeCompat(551, 1038),
                ),
                p2s = listOf(None, Inside, Fit, FillWidth, FillHeight, FillBounds, Crop),
                p3s = listOf(
                    TopStart, TopCenter, TopEnd,
                    CenterStart, Center, CenterEnd,
                    BottomStart, BottomCenter, BottomEnd,
                ),
                // todo rotation
                buildItem = { p1, p2, p3 ->
                    Item5(p1, p2, p3, IntRectCompat.Zero)
                },
            ) { item ->
                computeContentBaseInsideDisplayRect(
                    containerSize = containerSize,
                    contentSize = item.contentSize,
                    contentScale = item.contentScale,
                    alignment = item.alignment,
                    rotation = 0,
                ).round()
            }
        }

        listOf(
            Item5(IntSizeCompat(7500, 232), None, TopStart, IntRectCompat(0, 0, 1080, 232)),
            Item5(IntSizeCompat(7500, 232), None, TopCenter, IntRectCompat(0, 0, 1080, 232)),
            Item5(IntSizeCompat(7500, 232), None, TopEnd, IntRectCompat(0, 0, 1080, 232)),
            Item5(IntSizeCompat(7500, 232), None, CenterStart, IntRectCompat(0, 712, 1080, 944)),
            Item5(IntSizeCompat(7500, 232), None, Center, IntRectCompat(0, 712, 1080, 944)),
            Item5(IntSizeCompat(7500, 232), None, CenterEnd, IntRectCompat(0, 712, 1080, 944)),
            Item5(IntSizeCompat(7500, 232), None, BottomStart, IntRectCompat(0, 1424, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), None, BottomCenter, IntRectCompat(0, 1424, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), None, BottomEnd, IntRectCompat(0, 1424, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Inside, TopStart, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Inside, TopCenter, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Inside, TopEnd, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Inside, CenterStart, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), Inside, Center, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), Inside, CenterEnd, IntRectCompat(0, 812, 1080, 845)),
            Item5(
                IntSizeCompat(7500, 232),
                Inside,
                BottomStart,
                IntRectCompat(0, 1623, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                Inside,
                BottomCenter,
                IntRectCompat(0, 1623, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), Inside, BottomEnd, IntRectCompat(0, 1623, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Fit, TopStart, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Fit, TopCenter, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Fit, TopEnd, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), Fit, CenterStart, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), Fit, Center, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), Fit, CenterEnd, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), Fit, BottomStart, IntRectCompat(0, 1623, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Fit, BottomCenter, IntRectCompat(0, 1623, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Fit, BottomEnd, IntRectCompat(0, 1623, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillWidth, TopStart, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), FillWidth, TopCenter, IntRectCompat(0, 0, 1080, 33)),
            Item5(IntSizeCompat(7500, 232), FillWidth, TopEnd, IntRectCompat(0, 0, 1080, 33)),
            Item5(
                IntSizeCompat(7500, 232),
                FillWidth,
                CenterStart,
                IntRectCompat(0, 812, 1080, 845)
            ),
            Item5(IntSizeCompat(7500, 232), FillWidth, Center, IntRectCompat(0, 812, 1080, 845)),
            Item5(IntSizeCompat(7500, 232), FillWidth, CenterEnd, IntRectCompat(0, 812, 1080, 845)),
            Item5(
                IntSizeCompat(7500, 232),
                FillWidth,
                BottomStart,
                IntRectCompat(0, 1623, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillWidth,
                BottomCenter,
                IntRectCompat(0, 1623, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillWidth,
                BottomEnd,
                IntRectCompat(0, 1623, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), FillHeight, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillHeight, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillHeight, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), FillHeight, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillHeight, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillHeight,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), FillHeight, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillBounds, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillBounds, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillBounds, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(7500, 232),
                FillBounds,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), FillBounds, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), FillBounds, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(7500, 232),
                FillBounds,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(7500, 232),
                FillBounds,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(7500, 232), FillBounds, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, CenterStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, BottomStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, BottomCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(7500, 232), Crop, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), None, TopStart, IntRectCompat(0, 0, 173, 1656)),
            Item5(IntSizeCompat(173, 3044), None, TopCenter, IntRectCompat(454, 0, 627, 1656)),
            Item5(IntSizeCompat(173, 3044), None, TopEnd, IntRectCompat(907, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), None, CenterStart, IntRectCompat(0, 0, 173, 1656)),
            Item5(IntSizeCompat(173, 3044), None, Center, IntRectCompat(454, 0, 627, 1656)),
            Item5(IntSizeCompat(173, 3044), None, CenterEnd, IntRectCompat(907, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), None, BottomStart, IntRectCompat(0, 0, 173, 1656)),
            Item5(IntSizeCompat(173, 3044), None, BottomCenter, IntRectCompat(454, 0, 627, 1656)),
            Item5(IntSizeCompat(173, 3044), None, BottomEnd, IntRectCompat(907, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, TopStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, TopCenter, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, TopEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, CenterStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, Center, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, CenterEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, BottomStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, BottomCenter, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Inside, BottomEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, TopStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, TopCenter, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, TopEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, CenterStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, Center, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, CenterEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, BottomStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, BottomCenter, IntRectCompat(493, 0, 587, 1656)),
            Item5(IntSizeCompat(173, 3044), Fit, BottomEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillWidth, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillWidth, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillWidth, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillWidth,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillWidth, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillWidth, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillWidth,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                FillWidth,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillWidth, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillHeight, TopStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillHeight,
                TopCenter,
                IntRectCompat(493, 0, 587, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillHeight, TopEnd, IntRectCompat(986, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillHeight, CenterStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(IntSizeCompat(173, 3044), FillHeight, Center, IntRectCompat(493, 0, 587, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillHeight,
                CenterEnd,
                IntRectCompat(986, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillHeight, BottomStart, IntRectCompat(0, 0, 94, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillHeight,
                BottomCenter,
                IntRectCompat(493, 0, 587, 1656)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                FillHeight,
                BottomEnd,
                IntRectCompat(986, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillBounds, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillBounds, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillBounds, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillBounds,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillBounds, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), FillBounds, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(173, 3044),
                FillBounds,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(173, 3044),
                FillBounds,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(173, 3044), FillBounds, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Crop, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Crop, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Crop, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Crop, CenterStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Crop, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Crop, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Crop, BottomStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Crop, BottomCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(173, 3044), Crop, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), None, TopStart, IntRectCompat(0, 0, 575, 427)),
            Item5(IntSizeCompat(575, 427), None, TopCenter, IntRectCompat(253, 0, 828, 427)),
            Item5(IntSizeCompat(575, 427), None, TopEnd, IntRectCompat(505, 0, 1080, 427)),
            Item5(IntSizeCompat(575, 427), None, CenterStart, IntRectCompat(0, 615, 575, 1042)),
            Item5(IntSizeCompat(575, 427), None, Center, IntRectCompat(253, 615, 828, 1042)),
            Item5(IntSizeCompat(575, 427), None, CenterEnd, IntRectCompat(505, 615, 1080, 1042)),
            Item5(IntSizeCompat(575, 427), None, BottomStart, IntRectCompat(0, 1229, 575, 1656)),
            Item5(IntSizeCompat(575, 427), None, BottomCenter, IntRectCompat(253, 1229, 828, 1656)),
            Item5(IntSizeCompat(575, 427), None, BottomEnd, IntRectCompat(505, 1229, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Inside, TopStart, IntRectCompat(0, 0, 575, 427)),
            Item5(IntSizeCompat(575, 427), Inside, TopCenter, IntRectCompat(253, 0, 828, 427)),
            Item5(IntSizeCompat(575, 427), Inside, TopEnd, IntRectCompat(505, 0, 1080, 427)),
            Item5(IntSizeCompat(575, 427), Inside, CenterStart, IntRectCompat(0, 615, 575, 1042)),
            Item5(IntSizeCompat(575, 427), Inside, Center, IntRectCompat(253, 615, 828, 1042)),
            Item5(IntSizeCompat(575, 427), Inside, CenterEnd, IntRectCompat(505, 615, 1080, 1042)),
            Item5(IntSizeCompat(575, 427), Inside, BottomStart, IntRectCompat(0, 1229, 575, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                Inside,
                BottomCenter,
                IntRectCompat(253, 1229, 828, 1656)
            ),
            Item5(IntSizeCompat(575, 427), Inside, BottomEnd, IntRectCompat(505, 1229, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Fit, TopStart, IntRectCompat(0, 0, 1080, 802)),
            Item5(IntSizeCompat(575, 427), Fit, TopCenter, IntRectCompat(0, 0, 1080, 802)),
            Item5(IntSizeCompat(575, 427), Fit, TopEnd, IntRectCompat(0, 0, 1080, 802)),
            Item5(IntSizeCompat(575, 427), Fit, CenterStart, IntRectCompat(0, 427, 1080, 1229)),
            Item5(IntSizeCompat(575, 427), Fit, Center, IntRectCompat(0, 427, 1080, 1229)),
            Item5(IntSizeCompat(575, 427), Fit, CenterEnd, IntRectCompat(0, 427, 1080, 1229)),
            Item5(IntSizeCompat(575, 427), Fit, BottomStart, IntRectCompat(0, 854, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Fit, BottomCenter, IntRectCompat(0, 854, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Fit, BottomEnd, IntRectCompat(0, 854, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillWidth, TopStart, IntRectCompat(0, 0, 1080, 802)),
            Item5(IntSizeCompat(575, 427), FillWidth, TopCenter, IntRectCompat(0, 0, 1080, 802)),
            Item5(IntSizeCompat(575, 427), FillWidth, TopEnd, IntRectCompat(0, 0, 1080, 802)),
            Item5(
                IntSizeCompat(575, 427),
                FillWidth,
                CenterStart,
                IntRectCompat(0, 427, 1080, 1229)
            ),
            Item5(IntSizeCompat(575, 427), FillWidth, Center, IntRectCompat(0, 427, 1080, 1229)),
            Item5(IntSizeCompat(575, 427), FillWidth, CenterEnd, IntRectCompat(0, 427, 1080, 1229)),
            Item5(
                IntSizeCompat(575, 427),
                FillWidth,
                BottomStart,
                IntRectCompat(0, 854, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(575, 427),
                FillWidth,
                BottomCenter,
                IntRectCompat(0, 854, 1080, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillWidth, BottomEnd, IntRectCompat(0, 854, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillHeight, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillHeight, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillHeight, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                FillHeight,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillHeight, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillHeight, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                FillHeight,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(575, 427),
                FillHeight,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillHeight, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillBounds, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillBounds, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillBounds, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                FillBounds,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillBounds, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), FillBounds, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(575, 427),
                FillBounds,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(575, 427),
                FillBounds,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(575, 427), FillBounds, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, CenterStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, BottomStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, BottomCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(575, 427), Crop, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), None, TopStart, IntRectCompat(0, 0, 551, 1038)),
            Item5(IntSizeCompat(551, 1038), None, TopCenter, IntRectCompat(265, 0, 816, 1038)),
            Item5(IntSizeCompat(551, 1038), None, TopEnd, IntRectCompat(529, 0, 1080, 1038)),
            Item5(IntSizeCompat(551, 1038), None, CenterStart, IntRectCompat(0, 309, 551, 1347)),
            Item5(IntSizeCompat(551, 1038), None, Center, IntRectCompat(265, 309, 816, 1347)),
            Item5(IntSizeCompat(551, 1038), None, CenterEnd, IntRectCompat(529, 309, 1080, 1347)),
            Item5(IntSizeCompat(551, 1038), None, BottomStart, IntRectCompat(0, 618, 551, 1656)),
            Item5(IntSizeCompat(551, 1038), None, BottomCenter, IntRectCompat(265, 618, 816, 1656)),
            Item5(IntSizeCompat(551, 1038), None, BottomEnd, IntRectCompat(529, 618, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Inside, TopStart, IntRectCompat(0, 0, 551, 1038)),
            Item5(IntSizeCompat(551, 1038), Inside, TopCenter, IntRectCompat(265, 0, 816, 1038)),
            Item5(IntSizeCompat(551, 1038), Inside, TopEnd, IntRectCompat(529, 0, 1080, 1038)),
            Item5(IntSizeCompat(551, 1038), Inside, CenterStart, IntRectCompat(0, 309, 551, 1347)),
            Item5(IntSizeCompat(551, 1038), Inside, Center, IntRectCompat(265, 309, 816, 1347)),
            Item5(IntSizeCompat(551, 1038), Inside, CenterEnd, IntRectCompat(529, 309, 1080, 1347)),
            Item5(IntSizeCompat(551, 1038), Inside, BottomStart, IntRectCompat(0, 618, 551, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                Inside,
                BottomCenter,
                IntRectCompat(265, 618, 816, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), Inside, BottomEnd, IntRectCompat(529, 618, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, TopStart, IntRectCompat(0, 0, 879, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, TopCenter, IntRectCompat(101, 0, 980, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, TopEnd, IntRectCompat(201, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, CenterStart, IntRectCompat(0, 0, 879, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, Center, IntRectCompat(101, 0, 980, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, CenterEnd, IntRectCompat(201, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, BottomStart, IntRectCompat(0, 0, 879, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, BottomCenter, IntRectCompat(101, 0, 980, 1656)),
            Item5(IntSizeCompat(551, 1038), Fit, BottomEnd, IntRectCompat(201, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillWidth, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillWidth, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillWidth, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillWidth,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillWidth, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillWidth, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillWidth,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillWidth,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillWidth, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillHeight, TopStart, IntRectCompat(0, 0, 879, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                TopCenter,
                IntRectCompat(101, 0, 980, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillHeight, TopEnd, IntRectCompat(201, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                CenterStart,
                IntRectCompat(0, 0, 879, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillHeight, Center, IntRectCompat(101, 0, 980, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                CenterEnd,
                IntRectCompat(201, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                BottomStart,
                IntRectCompat(0, 0, 879, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                BottomCenter,
                IntRectCompat(101, 0, 980, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillHeight,
                BottomEnd,
                IntRectCompat(201, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillBounds, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillBounds, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillBounds, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillBounds,
                CenterStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillBounds, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), FillBounds, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(
                IntSizeCompat(551, 1038),
                FillBounds,
                BottomStart,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(
                IntSizeCompat(551, 1038),
                FillBounds,
                BottomCenter,
                IntRectCompat(0, 0, 1080, 1656)
            ),
            Item5(IntSizeCompat(551, 1038), FillBounds, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, TopStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, TopCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, TopEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, CenterStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, Center, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, CenterEnd, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, BottomStart, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, BottomCenter, IntRectCompat(0, 0, 1080, 1656)),
            Item5(IntSizeCompat(551, 1038), Crop, BottomEnd, IntRectCompat(0, 0, 1080, 1656)),
        ).forEach { item ->
            val result = computeContentBaseInsideDisplayRect(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentScale = item.contentScale,
                alignment = item.alignment,
                rotation = 0,
            ).round()
            Assert.assertEquals(
                /* message = */ item.getMessage(containerSize),
                /* expected = */ item.expected,
                /* actual = */ result,
            )
        }
    }


//    @Test
//    fun testComputeContainerVisibleRect() {
//        val containerSize = IntSizeCompat(1000, 2000)
//
//        var scale = 1f
//        listOf(
//            Offset(0f, 0f) to IntRectCompat(0, 0, 1000, 2000),
//            Offset(250f, 500f) to IntRectCompat(0, 0, 750, 1500),
//            Offset(750f, 500f) to IntRectCompat(0, 0, 250, 1500),
//            Offset(250f, 1500f) to IntRectCompat(0, 0, 750, 500),
//            Offset(750f, 1500f) to IntRectCompat(0, 0, 250, 500),
//            Offset(1000f, 2000f) to IntRectCompat(0, 0, 0, 0),
//            Offset(-250f, -500f) to IntRectCompat(250, 500, 1000, 2000),
//            Offset(-750f, -500f) to IntRectCompat(750, 500, 1000, 2000),
//            Offset(-250f, -1500f) to IntRectCompat(250, 1500, 1000, 2000),
//            Offset(-750f, -1500f) to IntRectCompat(750, 1500, 1000, 2000),
//            Offset(-1000f, -2000f) to IntRectCompat(0, 0, 0, 0),
//        ).forEach { (offset, expectedVisibleRect) ->
//            Assert.assertEquals(
//                "containerSize=$containerSize, scale=$scale, offset=$offset",
//                expectedVisibleRect,
//                computeContainerVisibleRect(containerSize, scale, offset).roundToIntRect()
//            )
//        }
//
//        scale = 2f
//        listOf(
//            Offset(0f, 0f) to IntRectCompat(0, 0, 500, 1000),
//            Offset(250f, 500f) to IntRectCompat(0, 0, 375, 750),
//            Offset(750f, 500f) to IntRectCompat(0, 0, 125, 750),
//            Offset(250f, 1500f) to IntRectCompat(0, 0, 375, 250),
//            Offset(750f, 1500f) to IntRectCompat(0, 0, 125, 250),
//            Offset(1000f, 2000f) to IntRectCompat(0, 0, 0, 0),
//            Offset(-250f, -500f) to IntRectCompat(125, 250, 625, 1250),
//            Offset(-750f, -500f) to IntRectCompat(375, 250, 875, 1250),
//            Offset(-250f, -1500f) to IntRectCompat(125, 750, 625, 1750),
//            Offset(-750f, -1500f) to IntRectCompat(375, 750, 875, 1750),
//            Offset(-1000f, -2000f) to IntRectCompat(500, 1000, 1000, 2000),
//        ).forEach { (offset, expectedVisibleRect) ->
//            Assert.assertEquals(
//                "containerSize=$containerSize, scale=$scale, offset=$offset",
//                expectedVisibleRect,
//                computeContainerVisibleRect(containerSize, scale, offset).roundToIntRect()
//            )
//        }
//    }
//
//        @Test
//    fun testComputeContentInContainerVisibleRect() {
//        val containerSize = IntSizeCompat(1000, 1000)
//
//        var contentSize = IntSizeCompat(800, 400)
//        listOf(
//            Item(ContentScaleCompat.None, AlignmentCompat.TopStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.None, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.None, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.None, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.None, AlignmentCompat.Center, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.None, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.Center, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.Center, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.Center, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, IntRectCompat(200, 0, 600, 400)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, IntRectCompat(400, 0, 800, 400)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.Center, IntRectCompat(200, 0, 600, 400)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, IntRectCompat(400, 0, 800, 400)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, IntRectCompat(200, 0, 600, 400)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, IntRectCompat(400, 0, 800, 400)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.Center, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 800, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopStart, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, IntRectCompat(200, 0, 600, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, IntRectCompat(400, 0, 800, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.Center, IntRectCompat(200, 0, 600, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, IntRectCompat(400, 0, 800, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, IntRectCompat(200, 0, 600, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, IntRectCompat(400, 0, 800, 400)),
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
//        contentSize = IntSizeCompat(400, 800)
//        listOf(
//            Item(ContentScaleCompat.None, AlignmentCompat.TopStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.None, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.None, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.None, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.None, AlignmentCompat.Center, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.None, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.Center, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.Center, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, IntRectCompat(0, 200, 400, 600)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.Center, IntRectCompat(0, 200, 400, 600)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, IntRectCompat(0, 200, 400, 600)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, IntRectCompat(0, 400, 400, 800)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, IntRectCompat(0, 400, 400, 800)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, IntRectCompat(0, 400, 400, 800)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.Center, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.Center, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 400, 800)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopStart, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 400, 400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, IntRectCompat(0, 200, 400, 600)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.Center, IntRectCompat(0, 200, 400, 600)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, IntRectCompat(0, 200, 400, 600)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, IntRectCompat(0, 400, 400, 800)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, IntRectCompat(0, 400, 400, 800)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, IntRectCompat(0, 400, 400, 800)),
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
//        contentSize = IntSizeCompat(1600, 1200)
//        listOf(
//            Item(ContentScaleCompat.None, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1000, 1000)),
//            Item(ContentScaleCompat.None, AlignmentCompat.TopCenter, IntRectCompat(300, 0, 1300, 1000)),
//            Item(ContentScaleCompat.None, AlignmentCompat.TopEnd, IntRectCompat(600, 0, 1600, 1000)),
//            Item(ContentScaleCompat.None, AlignmentCompat.CenterStart, IntRectCompat(0, 100, 1000, 1100)),
//            Item(ContentScaleCompat.None, AlignmentCompat.Center, IntRectCompat(300, 100, 1300, 1100)),
//            Item(ContentScaleCompat.None, AlignmentCompat.CenterEnd, IntRectCompat(600, 100, 1600, 1100)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomStart, IntRectCompat(0, 200, 1000, 1200)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomCenter, IntRectCompat(300, 200, 1300, 1200)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomEnd, IntRectCompat(600, 200, 1600, 1200)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.Center, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.Center, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.Center, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, IntRectCompat(199, 0, 1400, 1200)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, IntRectCompat(399, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.Center, IntRectCompat(199, 0, 1400, 1200)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, IntRectCompat(399, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, IntRectCompat(199, 0, 1400, 1200)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, IntRectCompat(399, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.Center, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, IntRectCompat(199, 0, 1400, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, IntRectCompat(399, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.Center, IntRectCompat(199, 0, 1400, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, IntRectCompat(399, 0, 1600, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, IntRectCompat(199, 0, 1400, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, IntRectCompat(399, 0, 1600, 1200)),
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
//        contentSize = IntSizeCompat(1200, 1600)
//        listOf(
//            Item(ContentScaleCompat.None, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1000, 1000)),
//            Item(ContentScaleCompat.None, AlignmentCompat.TopCenter, IntRectCompat(100, 0, 1100, 1000)),
//            Item(ContentScaleCompat.None, AlignmentCompat.TopEnd, IntRectCompat(200, 0, 1200, 1000)),
//            Item(ContentScaleCompat.None, AlignmentCompat.CenterStart, IntRectCompat(0, 300, 1000, 1300)),
//            Item(ContentScaleCompat.None, AlignmentCompat.Center, IntRectCompat(100, 300, 1100, 1300)),
//            Item(ContentScaleCompat.None, AlignmentCompat.CenterEnd, IntRectCompat(200, 300, 1200, 1300)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomStart, IntRectCompat(0, 600, 1000, 1600)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomCenter, IntRectCompat(100, 600, 1100, 1600)),
//            Item(ContentScaleCompat.None, AlignmentCompat.BottomEnd, IntRectCompat(200, 600, 1200, 1600)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.Center, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.Center, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, IntRectCompat(0, 199, 1200, 1400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.Center, IntRectCompat(0, 199, 1200, 1400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, IntRectCompat(0, 199, 1200, 1400)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, IntRectCompat(0, 399, 1200, 1600)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, IntRectCompat(0, 399, 1200, 1600)),
//            Item(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, IntRectCompat(0, 399, 1200, 1600)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.Center, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.Center, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, IntRectCompat(0, 0, 1200, 1600)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopStart, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, IntRectCompat(0, 0, 1200, 1200)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, IntRectCompat(0, 199, 1200, 1400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.Center, IntRectCompat(0, 199, 1200, 1400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, IntRectCompat(0, 199, 1200, 1400)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, IntRectCompat(0, 399, 1200, 1600)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, IntRectCompat(0, 399, 1200, 1600)),
//            Item(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, IntRectCompat(0, 399, 1200, 1600)),
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
//    @Test
//    fun testComputeOffsetBounds() {
//        val containerSize = IntSizeCompat(1080, 1656)
//        val printBatchBuildExpression = false
////        val printBatchBuildExpression = true
//
//        if (printBatchBuildExpression) {
//            printlnBatchBuildExpression(
//                p1s = listOf(
//                    IntSizeCompat(7500, 232), IntSizeCompat(173, 3044), IntSizeCompat(575, 427), IntSizeCompat(551, 1038),
//                ),
//                p2s = listOf(None, Inside, Fit, FillWidth, FillHeight, FillBounds, Crop),
//                p3s = listOf(
//                    TopStart, TopCenter, TopEnd,
//                    CenterStart, Center, CenterEnd,
//                    BottomStart, BottomCenter, BottomEnd,
//                ),
//                p4s = listOf(1.0f, 2.0f),
//                buildItem = { p1, p2, p3, p4 ->
//                    Item7(p1, p2, p3, p4, IntRectCompat.Zero)
//                },
//            ) {
//                computeUserOffsetBounds(
//                    containerSize = containerSize,
//                    contentSize = it.contentSize,
//                    contentScale = it.contentScale,
//                    alignment = it.alignment,
//                    userScale = it.scale,
//                ).roundToIntRect()
//            }
//        }
//
//        listOf(
//            Item7(IntSizeCompat(7500, 232), None, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, TopStart, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, TopCenter, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, TopEnd, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, CenterStart, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), None, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, Center, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), None, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, CenterEnd, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), None, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(7500, 232), None, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(7500, 232), None, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), None, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(7500, 232), Inside, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, TopStart, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, TopCenter, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, TopEnd, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, CenterStart, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), Inside, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, Center, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), Inside, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, CenterEnd, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), Inside, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(7500, 232), Inside, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(7500, 232), Inside, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Inside, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(7500, 232), Fit, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, TopStart, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, TopCenter, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, TopEnd, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, CenterStart, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), Fit, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, Center, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), Fit, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, CenterEnd, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), Fit, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(7500, 232), Fit, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(7500, 232), Fit, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Fit, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, TopStart, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, TopCenter, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, TopEnd, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, CenterStart, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, Center, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, CenterEnd, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(
//                IntSizeCompat(7500, 232),
//                FillWidth,
//                BottomStart,
//                2.0f,
//                IntRectCompat(-1080, -1656, 0, -1656)
//            ),
//            Item7(IntSizeCompat(7500, 232), FillWidth, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(
//                IntSizeCompat(7500, 232),
//                FillWidth,
//                BottomCenter,
//                2.0f,
//                IntRectCompat(-1080, -1656, 0, -1656)
//            ),
//            Item7(IntSizeCompat(7500, 232), FillWidth, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillWidth, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillHeight, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), FillBounds, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(7500, 232), Crop, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, TopStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, TopCenter, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), None, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, TopEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), None, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, CenterStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, Center, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), None, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), None, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, BottomStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, BottomCenter, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), None, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), None, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, TopStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, TopCenter, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, TopEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, CenterStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, Center, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, BottomStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, BottomCenter, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Inside, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, TopStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, TopCenter, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, TopEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, CenterStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, Center, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, BottomStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, BottomCenter, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Fit, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillWidth, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, TopStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, TopCenter, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, TopEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, CenterStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, Center, 2.0f, IntRectCompat(-540, -1656, -540, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, BottomStart, 2.0f, IntRectCompat(0, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(
//                IntSizeCompat(173, 3044),
//                FillHeight,
//                BottomCenter,
//                2.0f,
//                IntRectCompat(-540, -1656, -540, 0)
//            ),
//            Item7(IntSizeCompat(173, 3044), FillHeight, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillHeight, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, -1080, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), FillBounds, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(173, 3044), Crop, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, TopStart, 2.0f, IntRectCompat(-70, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, TopCenter, 2.0f, IntRectCompat(-576, 0, -506, 0)),
//            Item7(IntSizeCompat(575, 427), None, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, TopEnd, 2.0f, IntRectCompat(-1080, 0, -1010, 0)),
//            Item7(IntSizeCompat(575, 427), None, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, CenterStart, 2.0f, IntRectCompat(-70, -828, 0, -828)),
//            Item7(IntSizeCompat(575, 427), None, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, Center, 2.0f, IntRectCompat(-576, -828, -506, -828)),
//            Item7(IntSizeCompat(575, 427), None, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, CenterEnd, 2.0f, IntRectCompat(-1080, -828, -1010, -828)),
//            Item7(IntSizeCompat(575, 427), None, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, BottomStart, 2.0f, IntRectCompat(-70, -1656, 0, -1656)),
//            Item7(IntSizeCompat(575, 427), None, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, BottomCenter, 2.0f, IntRectCompat(-576, -1656, -506, -1656)),
//            Item7(IntSizeCompat(575, 427), None, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), None, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, -1010, -1656)),
//            Item7(IntSizeCompat(575, 427), Inside, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, TopStart, 2.0f, IntRectCompat(-70, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, TopCenter, 2.0f, IntRectCompat(-576, 0, -506, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, TopEnd, 2.0f, IntRectCompat(-1080, 0, -1010, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, CenterStart, 2.0f, IntRectCompat(-70, -828, 0, -828)),
//            Item7(IntSizeCompat(575, 427), Inside, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, Center, 2.0f, IntRectCompat(-576, -828, -506, -828)),
//            Item7(IntSizeCompat(575, 427), Inside, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, CenterEnd, 2.0f, IntRectCompat(-1080, -828, -1010, -828)),
//            Item7(IntSizeCompat(575, 427), Inside, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, BottomStart, 2.0f, IntRectCompat(-70, -1656, 0, -1656)),
//            Item7(IntSizeCompat(575, 427), Inside, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, BottomCenter, 2.0f, IntRectCompat(-576, -1656, -506, -1656)),
//            Item7(IntSizeCompat(575, 427), Inside, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Inside, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, -1010, -1656)),
//            Item7(IntSizeCompat(575, 427), Fit, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, TopStart, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, TopCenter, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, TopEnd, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, CenterStart, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(575, 427), Fit, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, Center, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(575, 427), Fit, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, CenterEnd, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(575, 427), Fit, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(575, 427), Fit, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(575, 427), Fit, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Fit, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(575, 427), FillWidth, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, TopStart, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, TopCenter, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, TopEnd, 2.0f, IntRectCompat(-1080, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, CenterStart, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(575, 427), FillWidth, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, Center, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(575, 427), FillWidth, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, CenterEnd, 2.0f, IntRectCompat(-1080, -828, 0, -828)),
//            Item7(IntSizeCompat(575, 427), FillWidth, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(575, 427), FillWidth, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(
//                IntSizeCompat(575, 427),
//                FillWidth,
//                BottomCenter,
//                2.0f,
//                IntRectCompat(-1080, -1656, 0, -1656)
//            ),
//            Item7(IntSizeCompat(575, 427), FillWidth, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillWidth, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, -1656)),
//            Item7(IntSizeCompat(575, 427), FillHeight, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillHeight, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), FillBounds, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(575, 427), Crop, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, TopStart, 2.0f, IntRectCompat(-22, -420, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, TopCenter, 2.0f, IntRectCompat(-552, -420, -530, 0)),
//            Item7(IntSizeCompat(551, 1038), None, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, TopEnd, 2.0f, IntRectCompat(-1080, -420, -1058, 0)),
//            Item7(IntSizeCompat(551, 1038), None, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, CenterStart, 2.0f, IntRectCompat(-22, -1038, 0, -618)),
//            Item7(IntSizeCompat(551, 1038), None, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, Center, 2.0f, IntRectCompat(-552, -1038, -530, -618)),
//            Item7(IntSizeCompat(551, 1038), None, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, CenterEnd, 2.0f, IntRectCompat(-1080, -1038, -1058, -618)),
//            Item7(IntSizeCompat(551, 1038), None, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, BottomStart, 2.0f, IntRectCompat(-22, -1656, 0, -1236)),
//            Item7(IntSizeCompat(551, 1038), None, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, BottomCenter, 2.0f, IntRectCompat(-552, -1656, -530, -1236)),
//            Item7(IntSizeCompat(551, 1038), None, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), None, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, -1058, -1236)),
//            Item7(IntSizeCompat(551, 1038), Inside, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, TopStart, 2.0f, IntRectCompat(-22, -420, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, TopCenter, 2.0f, IntRectCompat(-552, -420, -530, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, TopEnd, 2.0f, IntRectCompat(-1080, -420, -1058, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, CenterStart, 2.0f, IntRectCompat(-22, -1038, 0, -618)),
//            Item7(IntSizeCompat(551, 1038), Inside, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, Center, 2.0f, IntRectCompat(-552, -1038, -530, -618)),
//            Item7(IntSizeCompat(551, 1038), Inside, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, CenterEnd, 2.0f, IntRectCompat(-1080, -1038, -1058, -618)),
//            Item7(IntSizeCompat(551, 1038), Inside, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, BottomStart, 2.0f, IntRectCompat(-22, -1656, 0, -1236)),
//            Item7(IntSizeCompat(551, 1038), Inside, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(
//                IntSizeCompat(551, 1038),
//                Inside,
//                BottomCenter,
//                2.0f,
//                IntRectCompat(-552, -1656, -530, -1236)
//            ),
//            Item7(IntSizeCompat(551, 1038), Inside, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Inside, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, -1058, -1236)),
//            Item7(IntSizeCompat(551, 1038), Fit, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, TopStart, 2.0f, IntRectCompat(-678, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, TopCenter, 2.0f, IntRectCompat(-880, -1656, -202, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, TopEnd, 2.0f, IntRectCompat(-1080, -1656, -402, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, CenterStart, 2.0f, IntRectCompat(-678, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, Center, 2.0f, IntRectCompat(-880, -1656, -202, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, -402, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, BottomStart, 2.0f, IntRectCompat(-678, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, BottomCenter, 2.0f, IntRectCompat(-880, -1656, -202, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Fit, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, -402, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, BottomStart, 2.0f, IntRectCompat(-1080, -1655, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, BottomCenter, 2.0f, IntRectCompat(-1080, -1655, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillWidth, BottomEnd, 2.0f, IntRectCompat(-1080, -1655, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, TopStart, 2.0f, IntRectCompat(-678, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, TopCenter, 2.0f, IntRectCompat(-880, -1656, -202, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, TopEnd, 2.0f, IntRectCompat(-1080, -1656, -402, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, CenterStart, 2.0f, IntRectCompat(-678, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, Center, 2.0f, IntRectCompat(-880, -1656, -202, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, -402, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, BottomStart, 2.0f, IntRectCompat(-678, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(
//                IntSizeCompat(551, 1038),
//                FillHeight,
//                BottomCenter,
//                2.0f,
//                IntRectCompat(-880, -1656, -202, 0)
//            ),
//            Item7(IntSizeCompat(551, 1038), FillHeight, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillHeight, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, -402, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, BottomStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, BottomCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), FillBounds, BottomEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, TopStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, TopStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, TopCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, TopCenter, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, TopEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, TopEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, CenterStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, CenterStart, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, Center, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, Center, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, CenterEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, CenterEnd, 2.0f, IntRectCompat(-1080, -1656, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, BottomStart, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, BottomStart, 2.0f, IntRectCompat(-1080, -1655, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, BottomCenter, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, BottomCenter, 2.0f, IntRectCompat(-1080, -1655, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, BottomEnd, 1.0f, IntRectCompat(0, 0, 0, 0)),
//            Item7(IntSizeCompat(551, 1038), Crop, BottomEnd, 2.0f, IntRectCompat(-1080, -1655, 0, 0)),
//        ).forEach { item ->
//            val result = computeUserOffsetBounds(
//                containerSize = containerSize,
//                contentSize = item.contentSize,
//                contentScale = item.contentScale,
//                alignment = item.alignment,
//                userScale = item.scale,
//            ).roundToIntRect()
//            Assert.assertEquals(
//                /* message = */ item.getMessage(containerSize),
//                /* expected = */ item.expected,
//                /* actual = */ result,
//            )
//        }
//    }

    @Test
    fun testComputeContentVisibleRect() {
        // todo 补充测试用例
        Assert.assertEquals(
            IntRectCompat(0, 0, 345, 6088),
            computeContentVisibleRect(
                containerSize = IntSizeCompat(1080, 2068),
                contentSize = IntSizeCompat(345, 6088),
                contentScale = Fit,
                alignment = Center,
                rotation = 0,
                userScale = 1f,
                userOffset = Companion.Zero
            ).round()
        )
    }


//    @Test
//    fun testComputeContainerOriginByTouchPosition() {
//        var containerSize = IntSizeCompat(1080, 1920)
//
//        var scale = 1f
//        var offset = IntOffsetCompat(0, 0)
//        listOf(
//            IntOffsetCompat(216, 960) to Origin(0.2, 0.5),
//            IntOffsetCompat(540, 384) to Origin(0.5, 0.2),
//            IntOffsetCompat(864, 960) to Origin(0.8, 0.5),
//            IntOffsetCompat(540, 1536) to Origin(0.5, 0.8)
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
//        offset = IntOffsetCompat(540, 960)
//        listOf(
//            IntOffsetCompat(216, 960) to Origin(0, 0),
//            IntOffsetCompat(540, 384) to Origin(0, 0),
//            IntOffsetCompat(864, 960) to Origin(0.3, 0),
//            IntOffsetCompat(540, 1536) to Origin(0, 0.3)
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
//        offset = IntOffsetCompat(-540, -960)
//        listOf(
//            IntOffsetCompat(216, 960) to Origin(0.7, 1),
//            IntOffsetCompat(540, 384) to Origin(1, 0.7),
//            IntOffsetCompat(864, 960) to Origin(1, 1),
//            IntOffsetCompat(540, 1536) to Origin(1, 1)
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
//        offset = IntOffsetCompat(0, 0)
//        listOf(
//            IntOffsetCompat(216, 960) to Origin(0.1, 0.25),
//            IntOffsetCompat(540, 384) to Origin(0.25, 0.1),
//            IntOffsetCompat(864, 960) to Origin(0.4, 0.25),
//            IntOffsetCompat(540, 1536) to Origin(0.25, 0.4)
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
//        offset = IntOffsetCompat(540, 960)
//        listOf(
//            IntOffsetCompat(216, 960) to Origin(0, 0),
//            IntOffsetCompat(540, 384) to Origin(0, 0),
//            IntOffsetCompat(864, 960) to Origin(0.15, 0),
//            IntOffsetCompat(540, 1536) to Origin(0, 0.15)
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
//        offset = IntOffsetCompat(-540, -960)
//        listOf(
//            IntOffsetCompat(216, 960) to Origin(0.35, 0.5),
//            IntOffsetCompat(540, 384) to Origin(0.5, 0.35),
//            IntOffsetCompat(864, 960) to Origin(0.65, 0.5),
//            IntOffsetCompat(540, 1536) to Origin(0.5, 0.65)
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
//        containerSize = IntSizeCompat.Unspecified
//        scale = 1f
//        offset = IntOffsetCompat(0, 0)
//        listOf(
//            IntOffsetCompat(216, 960) to Origin(0, 0),
//            IntOffsetCompat(540, 384) to Origin(0, 0),
//            IntOffsetCompat(864, 960) to Origin(0, 0),
//            IntOffsetCompat(540, 1536) to Origin(0, 0)
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
//        var containerSize = IntSizeCompat(1000, 1000)
//        var contentSize = IntSizeCompat(800, 200)
//        var containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopStart, Origin(0.625, 1.0)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopEnd, Origin(0.375, 1.0)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterStart, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterEnd, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomStart, Origin(0.625, 0.0)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomEnd, Origin(0.375, 0.0)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopStart, Origin(0.625, 1.0)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, Origin(0.375, 1.0)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, Origin(0.625, 0.0)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, Origin(0.375, 0.0)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopStart, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, Origin(0.875, 0.5)),
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
//        containerSize = IntSizeCompat(1000, 1000)
//        contentSize = IntSizeCompat(200, 800)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopStart, Origin(1.0, 0.625)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopCenter, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopEnd, Origin(0.0, 0.625)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomStart, Origin(1.0, 0.375)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomCenter, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomEnd, Origin(0.0, 0.375)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopStart, Origin(1.0, 0.625)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, Origin(0.0, 0.625)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, Origin(1.0, 0.375)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, Origin(0.0, 0.375)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopStart, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, Origin(0.5, 0.875)),
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
//        containerSize = IntSizeCompat(1000, 1000)
//        contentSize = IntSizeCompat(1600, 1200)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopStart, Origin(0.3125, 0.41666666)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopCenter, Origin(0.5, 0.41666666)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopEnd, Origin(0.6875, 0.41666666)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterStart, Origin(0.3125, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterEnd, Origin(0.6875, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomStart, Origin(0.3125, 0.5833333)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomCenter, Origin(0.5, 0.5833333)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomEnd, Origin(0.6875, 0.5833333)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopStart, Origin(0.5, 0.6666667)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, Origin(0.5, 0.6666667)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, Origin(0.5, 0.6666667)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, Origin(0.5, 0.33333334)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, Origin(0.5, 0.33333334)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, Origin(0.5, 0.33333334)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopStart, Origin(0.5, 0.6666667)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, Origin(0.5, 0.6666667)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, Origin(0.5, 0.6666667)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, Origin(0.5, 0.33333334)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, Origin(0.5, 0.33333334)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, Origin(0.5, 0.33333334)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, Origin(0.5, 0.6666667)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, Origin(0.5, 0.6666667)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, Origin(0.5, 0.6666667)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, Origin(0.5, 0.33333334)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, Origin(0.5, 0.33333334)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, Origin(0.5, 0.33333334)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, Origin(0.49999997, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, Origin(0.62499994, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.Center, Origin(0.49999997, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, Origin(0.62499994, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, Origin(0.49999997, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, Origin(0.62499994, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, Origin(0.49999997, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, Origin(0.62499994, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.Center, Origin(0.49999997, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, Origin(0.62499994, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, Origin(0.49999997, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, Origin(0.62499994, 0.5)),
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
//        containerSize = IntSizeCompat(1000, 1000)
//        contentSize = IntSizeCompat(1200, 1600)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopStart, Origin(0.41666666, 0.3125)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopCenter, Origin(0.5, 0.3125)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopEnd, Origin(0.5833333, 0.3125)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterStart, Origin(0.41666666, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterEnd, Origin(0.5833333, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomStart, Origin(0.41666666, 0.6875)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomCenter, Origin(0.5, 0.6875)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomEnd, Origin(0.5833333, 0.6875)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, Origin(0.5, 0.49999997)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.Center, Origin(0.5, 0.49999997)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, Origin(0.5, 0.49999997)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, Origin(0.5, 0.62499994)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, Origin(0.5, 0.62499994)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, Origin(0.5, 0.62499994)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, Origin(0.6666667, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, Origin(0.33333334, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopStart, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, Origin(0.5, 0.49999997)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.Center, Origin(0.5, 0.49999997)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, Origin(0.5, 0.49999997)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, Origin(0.5, 0.62499994)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, Origin(0.5, 0.62499994)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, Origin(0.5, 0.62499994)),
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
//        var containerSize = IntSizeCompat(1000, 1000)
//        var contentSize = IntSizeCompat(800, 200)
//        var containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopStart, Origin(0.4, 0.1)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopCenter, Origin(0.5, 0.1)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopEnd, Origin(0.6, 0.1)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterStart, Origin(0.4, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterEnd, Origin(0.6, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomStart, Origin(0.4, 0.9)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomCenter, Origin(0.5, 0.9)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomEnd, Origin(0.6, 0.9)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopStart, Origin(0.4, 0.1)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, Origin(0.5, 0.1)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, Origin(0.6, 0.1)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, Origin(0.4, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, Origin(0.6, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, Origin(0.4, 0.9)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, Origin(0.5, 0.9)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, Origin(0.6, 0.9)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopStart, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, Origin(0.5, 0.125)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, Origin(0.5, 0.875)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, Origin(0.0, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, Origin(1.0, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, Origin(0.0, 0.5)),
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
//        containerSize = IntSizeCompat(1000, 1000)
//        contentSize = IntSizeCompat(200, 800)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopStart, Origin(0.1, 0.4)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopCenter, Origin(0.5, 0.4)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopEnd, Origin(0.9, 0.4)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterStart, Origin(0.1, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterEnd, Origin(0.9, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomStart, Origin(0.1, 0.6)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomCenter, Origin(0.5, 0.6)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomEnd, Origin(0.9, 0.6)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopStart, Origin(0.1, 0.4)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, Origin(0.5, 0.4)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, Origin(0.9, 0.4)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, Origin(0.1, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, Origin(0.9, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, Origin(0.1, 0.6)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, Origin(0.5, 0.6)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, Origin(0.9, 0.6)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, Origin(0.125, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, Origin(0.875, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopStart, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, Origin(0.5, 1.0)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, Origin(0.5, 0.0)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, Origin(0.5, 0.0)),
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
//        containerSize = IntSizeCompat(1000, 1000)
//        contentSize = IntSizeCompat(1600, 1200)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopStart, Origin(0.8, 0.6)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopCenter, Origin(0.5, 0.6)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopEnd, Origin(0.2, 0.6)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterStart, Origin(0.8, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterEnd, Origin(0.2, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomStart, Origin(0.8, 0.4)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomCenter, Origin(0.5, 0.4)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomEnd, Origin(0.2, 0.4)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopStart, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopStart, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, Origin(0.5, 0.375)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, Origin(0.5, 0.625)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, Origin(0.50000006, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, Origin(0.3333334, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.Center, Origin(0.50000006, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, Origin(0.3333334, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, Origin(0.50000006, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, Origin(0.3333334, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, Origin(0.50000006, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, Origin(0.3333334, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.Center, Origin(0.50000006, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, Origin(0.3333334, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, Origin(0.6666666, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, Origin(0.50000006, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, Origin(0.3333334, 0.5)),
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
//        containerSize = IntSizeCompat(1000, 1000)
//        contentSize = IntSizeCompat(1200, 1600)
//        containerOrigin = Origin(0.5, 0.5)
//        listOf(
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopStart, Origin(0.6, 0.8)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopCenter, Origin(0.5, 0.8)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.TopEnd, Origin(0.4, 0.8)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterStart, Origin(0.6, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.CenterEnd, Origin(0.4, 0.5)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomStart, Origin(0.6, 0.2)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomCenter, Origin(0.5, 0.2)),
//            Item2(ContentScaleCompat.None, AlignmentCompat.BottomEnd, Origin(0.4, 0.2)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.TopEnd, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.CenterEnd, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Inside, AlignmentCompat.BottomEnd, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.TopEnd, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.CenterEnd, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Fit, AlignmentCompat.BottomEnd, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopStart, Origin(0.5, 0.6666666)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopCenter, Origin(0.5, 0.6666666)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.TopEnd, Origin(0.5, 0.6666666)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterStart, Origin(0.5, 0.50000006)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.Center, Origin(0.5, 0.50000006)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.CenterEnd, Origin(0.5, 0.50000006)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomStart, Origin(0.5, 0.3333334)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomCenter, Origin(0.5, 0.3333334)),
//            Item2(ContentScaleCompat.FillWidth, AlignmentCompat.BottomEnd, Origin(0.5, 0.3333334)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.TopEnd, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.CenterEnd, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomStart, Origin(0.375, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillHeight, AlignmentCompat.BottomEnd, Origin(0.625, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.TopEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.Center, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.CenterEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomStart, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomCenter, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.FillBounds, AlignmentCompat.BottomEnd, Origin(0.5, 0.5)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopStart, Origin(0.5, 0.6666666)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopCenter, Origin(0.5, 0.6666666)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.TopEnd, Origin(0.5, 0.6666666)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterStart, Origin(0.5, 0.50000006)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.Center, Origin(0.5, 0.50000006)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.CenterEnd, Origin(0.5, 0.50000006)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomStart, Origin(0.5, 0.3333334)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomCenter, Origin(0.5, 0.3333334)),
//            Item2(ContentScaleCompat.Crop, AlignmentCompat.BottomEnd, Origin(0.5, 0.3333334)),
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
//        val contentSize = IntSizeCompat(1000, 1000)
//
//        listOf(
//            (IntRectCompat(0, 0, 1000, 1000) to true) to BOTH,
//            (IntRectCompat(0, 0, 1000, 1000) to false) to BOTH,
//
//            (IntRectCompat(0, 0, 500, 500) to true) to START,
//            (IntRectCompat(0, 0, 500, 500) to false) to START,
//            (IntRectCompat(200, 0, 800, 500) to true) to NONE,
//            (IntRectCompat(200, 0, 800, 500) to false) to START,
//            (IntRectCompat(500, 0, 1000, 1000) to true) to END,
//            (IntRectCompat(500, 0, 1000, 500) to false) to START,
//
//            (IntRectCompat(0, 200, 500, 800) to true) to START,
//            (IntRectCompat(0, 200, 500, 800) to false) to NONE,
//            (IntRectCompat(200, 200, 800, 800) to true) to NONE,
//            (IntRectCompat(200, 200, 800, 800) to false) to NONE,
//            (IntRectCompat(500, 200, 1000, 800) to true) to END,
//            (IntRectCompat(500, 200, 1000, 800) to false) to NONE,
//
//            (IntRectCompat(0, 500, 500, 1000) to true) to START,
//            (IntRectCompat(0, 500, 500, 1000) to false) to END,
//            (IntRectCompat(200, 500, 800, 1000) to true) to NONE,
//            (IntRectCompat(200, 500, 800, 1000) to false) to END,
//            (IntRectCompat(500, 500, 1000, 1000) to true) to END,
//            (IntRectCompat(500, 500, 1000, 1000) to false) to END,
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

    @Test
    fun testCalculateNextStepScale() {
        val stepScales = floatArrayOf(1f, 2f, 3f, 4f, 5f)
        Assert.assertEquals(1f, calculateNextStepScale(stepScales, 0.0f))
        Assert.assertEquals(1f, calculateNextStepScale(stepScales, 0.8f))
        Assert.assertEquals(2f, calculateNextStepScale(stepScales, 0.8f, rangeOfError = 0.2f))
        Assert.assertEquals(2f, calculateNextStepScale(stepScales, 0.9f))
        Assert.assertEquals(1f, calculateNextStepScale(stepScales, 0.9f, rangeOfError = 0f))
        Assert.assertEquals(2f, calculateNextStepScale(stepScales, 1.0f))
        Assert.assertEquals(2f, calculateNextStepScale(stepScales, 1.5f))
        Assert.assertEquals(3f, calculateNextStepScale(stepScales, 2.5f))
        Assert.assertEquals(4f, calculateNextStepScale(stepScales, 3.5f))
        Assert.assertEquals(5f, calculateNextStepScale(stepScales, 4.5f))
        Assert.assertEquals(1f, calculateNextStepScale(stepScales, 5.5f))
        Assert.assertEquals(1f, calculateNextStepScale(stepScales, 6.5f))

        Assert.assertEquals(0.0f, calculateNextStepScale(floatArrayOf(), 0.0f))
        Assert.assertEquals(0.8f, calculateNextStepScale(floatArrayOf(), 0.8f))
        Assert.assertEquals(0.9f, calculateNextStepScale(floatArrayOf(), 0.9f))
        Assert.assertEquals(1.0f, calculateNextStepScale(floatArrayOf(), 1.0f))
        Assert.assertEquals(2.5f, calculateNextStepScale(floatArrayOf(), 2.5f))
        Assert.assertEquals(3.5f, calculateNextStepScale(floatArrayOf(), 3.5f))
    }
}