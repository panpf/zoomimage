package com.github.panpf.zoomimage.view.test

import android.graphics.Color
import android.graphics.Matrix
import android.view.LayoutInflater
import android.widget.ImageView.ScaleType
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.internal.TileManager.Companion.DefaultPausedContinuousTransformTypes
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import com.github.panpf.zoomimage.zoom.ReadMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class ZoomImageViewTest {

    @Test
    fun testLogger() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val zoomImageView = ZoomImageView(context)
            assertEquals(expected = "ZoomImageView", actual = zoomImageView.logger.tag)
        }
    }

    @Test
    fun testMatrix() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val zoomImageView = ZoomImageView(context)
            assertEquals(
                expected = Matrix(Matrix.IDENTITY_MATRIX),
                actual = zoomImageView.imageMatrix
            )

            zoomImageView.imageMatrix = Matrix().apply {
                setScale(2f, 2f)
            }
            assertEquals(
                expected = Matrix(Matrix.IDENTITY_MATRIX),
                actual = zoomImageView.imageMatrix
            )
        }
    }

    @Test
    fun testScaleType() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val zoomImageView = ZoomImageView(context)
            assertEquals(
                expected = ScaleType.FIT_CENTER,
                actual = zoomImageView.scaleType
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomImageView.zoomable.contentScaleState.value
            )
            assertEquals(
                expected = AlignmentCompat.Center,
                actual = zoomImageView.zoomable.alignmentState.value
            )

            zoomImageView.scaleType = ScaleType.MATRIX
            assertEquals(
                expected = ScaleType.MATRIX,
                actual = zoomImageView.scaleType
            )
            assertEquals(
                expected = ContentScaleCompat.None,
                actual = zoomImageView.zoomable.contentScaleState.value
            )
            assertEquals(
                expected = AlignmentCompat.TopStart,
                actual = zoomImageView.zoomable.alignmentState.value
            )

            zoomImageView.scaleType = ScaleType.CENTER_CROP
            assertEquals(
                expected = ScaleType.CENTER_CROP,
                actual = zoomImageView.scaleType
            )
            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = zoomImageView.zoomable.contentScaleState.value
            )
            assertEquals(
                expected = AlignmentCompat.Center,
                actual = zoomImageView.zoomable.alignmentState.value
            )
        }
    }

    @Test
    fun testScrollBar() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val zoomImageView = ZoomImageView(context)
            assertSame(
                expected = ScrollBarSpec.Default,
                actual = zoomImageView.scrollBar
            )

            zoomImageView.scrollBar = ScrollBarSpec.Default.copy(
                color = Color.BLUE
            )
            assertNotEquals(
                illegal = ScrollBarSpec.Default,
                actual = zoomImageView.scrollBar
            )
            assertEquals(
                expected = ScrollBarSpec.Default.copy(
                    color = Color.BLUE
                ),
                actual = zoomImageView.scrollBar
            )
        }
    }

    @Test
    fun testXmlAttrsDefault() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context
            val zoomImageView =
                LayoutInflater.from(context).inflate(R.layout.zoom_image, null) as ZoomImageView

            assertEquals(ContentScaleCompat.Fit, zoomImageView.zoomable.contentScaleState.value)
            assertEquals(AlignmentCompat.Center, zoomImageView.zoomable.alignmentState.value)
            assertEquals(ZoomAnimationSpec.Default, zoomImageView.zoomable.animationSpecState.value)
            assertEquals(true, zoomImageView.zoomable.rubberBandScaleState.value)
            assertEquals(false, zoomImageView.zoomable.threeStepScaleState.value)
            assertEquals(false, zoomImageView.zoomable.limitOffsetWithinBaseVisibleRectState.value)
            assertEquals(null, zoomImageView.zoomable.readModeState.value)

            assertEquals(false, zoomImageView.subsampling.showTileBoundsState.value)
            assertEquals(
                DefaultPausedContinuousTransformTypes,
                zoomImageView.subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(false, zoomImageView.subsampling.disabledBackgroundTilesState.value)
            assertEquals(
                TileAnimationSpec.Default,
                zoomImageView.subsampling.tileAnimationSpecState.value
            )

            assertEquals(ScrollBarSpec.Default, zoomImageView.scrollBar)
        }
    }

    @Test
    fun testXmlAttrsContentScale() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_crop, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.FillBounds,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_fill_bounds, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.FillHeight,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_fill_height, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.FillWidth,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_fill_width, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_fit, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Inside,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_inside, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
            assertEquals(
                expected = ContentScaleCompat.None,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_content_scale_none, null)
                    .let { it as ZoomImageView }
                    .zoomable.contentScaleState.value
            )
        }
    }

    @Test
    fun testXmlAttrsAlignment() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = AlignmentCompat.BottomCenter,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_bottomcenter, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.BottomEnd,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_bottomend, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.BottomStart,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_bottomstart, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.Center,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_center, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.CenterEnd,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_centerend, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.CenterStart,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_centerstart, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.TopCenter,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_topcenter, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.TopEnd,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_topend, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
            assertEquals(
                expected = AlignmentCompat.TopStart,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_alignment_topstart, null)
                    .let { it as ZoomImageView }
                    .zoomable.alignmentState.value
            )
        }
    }

    @Test
    fun testXmlAttrsAnimateScale() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = ZoomAnimationSpec.Default,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_animate_scale_true, null)
                    .let { it as ZoomImageView }
                    .zoomable.animationSpecState.value
            )
            assertEquals(
                expected = ZoomAnimationSpec.None,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_animate_scale_false, null)
                    .let { it as ZoomImageView }
                    .zoomable.animationSpecState.value
            )
        }
    }

    @Test
    fun testXmlAttrsRubberBandScale() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = true,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_rubber_band_scale_true, null)
                    .let { it as ZoomImageView }
                    .zoomable.rubberBandScaleState.value
            )
            assertEquals(
                expected = false,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_rubber_band_scale_false, null)
                    .let { it as ZoomImageView }
                    .zoomable.rubberBandScaleState.value
            )
        }
    }

    @Test
    fun testXmlAttrsThreeStepScale() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = true,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_three_step_scale_true, null)
                    .let { it as ZoomImageView }
                    .zoomable.threeStepScaleState.value
            )
            assertEquals(
                expected = false,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_three_step_scale_false, null)
                    .let { it as ZoomImageView }
                    .zoomable.threeStepScaleState.value
            )
        }
    }

    @Test
    fun testXmlAttrsLimitOffsetWithinBaseVisibleRect() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = true,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_limit_offset_within_base_visible_rect_true, null)
                    .let { it as ZoomImageView }
                    .zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = false,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_limit_offset_within_base_visible_rect_false, null)
                    .let { it as ZoomImageView }
                    .zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
        }
    }

    @Test
    fun testXmlAttrsReadMode() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = ReadMode.Default,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_read_mode_both, null)
                    .let { it as ZoomImageView }
                    .zoomable.readModeState.value
            )
            assertEquals(
                expected = null,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_read_mode_none, null)
                    .let { it as ZoomImageView }
                    .zoomable.readModeState.value
            )

            assertEquals(
                expected = ReadMode.Default.copy(sizeType = ReadMode.SIZE_TYPE_HORIZONTAL),
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_read_mode_horizontal, null)
                    .let { it as ZoomImageView }
                    .zoomable.readModeState.value
            )
            assertEquals(
                expected = ReadMode.Default.copy(sizeType = ReadMode.SIZE_TYPE_VERTICAL),
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_read_mode_vertical, null)
                    .let { it as ZoomImageView }
                    .zoomable.readModeState.value
            )
        }
    }

    @Test
    fun testXmlAttrsShowTileBounds() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = true,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_show_tile_bounds_true, null)
                    .let { it as ZoomImageView }
                    .subsampling.showTileBoundsState.value
            )
            assertEquals(
                expected = false,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_show_tile_bounds_false, null)
                    .let { it as ZoomImageView }
                    .subsampling.showTileBoundsState.value
            )
        }
    }

    @Test
    fun testXmlAttrsDisabledBackgroundTiles() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = true,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_background_tiles_true, null)
                    .let { it as ZoomImageView }
                    .subsampling.disabledBackgroundTilesState.value
            )
            assertEquals(
                expected = false,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_background_tiles_false, null)
                    .let { it as ZoomImageView }
                    .subsampling.disabledBackgroundTilesState.value
            )
        }
    }

    @Test
    fun testXmlAttrsTileAnimation() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = TileAnimationSpec.Default,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_tile_animation_true, null)
                    .let { it as ZoomImageView }
                    .subsampling.tileAnimationSpecState.value
            )
            assertEquals(
                expected = TileAnimationSpec.None,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_tile_animation_false, null)
                    .let { it as ZoomImageView }
                    .subsampling.tileAnimationSpecState.value
            )
        }
    }

    @Test
    fun testXmlAttrsPausedContinuousTransformTypes() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = 0,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_none, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.SCALE,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_scale, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.OFFSET,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_offset, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.LOCATE,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_locate, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.GESTURE,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_gesture, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.FLING,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_fling, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
            assertEquals(
                expected = ContinuousTransformType.values.fold(0) { last, curr -> last or curr },
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_paused_continuous_transform_types_all, null)
                    .let { it as ZoomImageView }
                    .subsampling.pausedContinuousTransformTypesState.value
            )
        }
    }

    @Test
    fun testXmlAttrsDisabledScrollBar() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = null,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_scroll_bar_true, null)
                    .let { it as ZoomImageView }
                    .scrollBar
            )
            assertEquals(
                expected = ScrollBarSpec.Default,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_scroll_bar_false, null)
                    .let { it as ZoomImageView }
                    .scrollBar
            )
        }
    }

    @Test
    fun testXmlAttrsDisabledScrollBarStyle() = runTest {
        withContext(Dispatchers.Main) {
            val context = InstrumentationRegistry.getInstrumentation().context

            assertEquals(
                expected = ScrollBarSpec(
                    color = Color.parseColor("#FF0000"),
                    size = 55f,
                    margin = 17f,
                ),
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_scroll_bar_style, null)
                    .let { it as ZoomImageView }
                    .scrollBar
            )
            assertEquals(
                expected = null,
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_scroll_bar_true_style, null)
                    .let { it as ZoomImageView }
                    .scrollBar
            )
            assertEquals(
                expected = ScrollBarSpec(
                    color = Color.parseColor("#FF0000"),
                    size = 55f,
                    margin = 17f,
                ),
                actual = LayoutInflater.from(context)
                    .inflate(R.layout.zoom_image_disabled_scroll_bar_false_style, null)
                    .let { it as ZoomImageView }
                    .scrollBar
            )
        }
    }
}