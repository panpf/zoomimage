package com.github.panpf.zoomimage.core.test.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.calculateContentVisibleRect
import com.github.panpf.zoomimage.zoom.calculateInitialZoom
import com.github.panpf.zoomimage.zoom.calculateRestoreCenterUserTransform
import org.junit.Assert
import org.junit.Test

class CoreZoomUtilsTest5 {

    @Test
    fun test() {
        val containerSize = IntSizeCompat(800, 572)
        val contentSize = IntSizeCompat(6799, 4882)
        val contentOriginSize = IntSizeCompat.Zero
        val contentScale = ContentScaleCompat.Fit
        val alignment = AlignmentCompat.Center
        val rotation = 0
        val readMode: ReadMode? = null
        val scalesCalculator = ScalesCalculator.Dynamic
        val lastBaseTransform = TransformCompat(
            scale = ScaleFactorCompat(0.117165096f, 0.117165096f),
            offset = OffsetCompat(2.0f, 0.0f),
            rotationOrigin = TransformOriginCompat(4.249375f, 4.2674823f)
        )
        val lastUserTransform = TransformCompat(
            scale = ScaleFactorCompat(8.5349655f, 8.5349655f),
            offset = OffsetCompat(-3948.0999f, -2968.974f),
            rotationOrigin = TransformOriginCompat(4.249375f, 4.2674823f)
        )
        val lastTransform = (lastBaseTransform + lastUserTransform).apply {
            Assert.assertEquals(
                "TransformCompat(scale=1.0x1.0, " +
                        "offset=-3931.03x-2968.97, " +
                        "rotation=0.0, " +
                        "scaleOrigin=0.0x0.0, " +
                        "rotationOrigin=4.25x4.27)",
                this.toString()
            )
        }
        val contentVisibleCenterPoint = calculateContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            userScale = lastUserTransform.scaleX,
            userOffset = lastUserTransform.offset,
        ).center.apply {
            Assert.assertEquals("OffsetCompat(4331.0, 3255.0)", this.toString())
        }

        val newContainerSize = IntSizeCompat(900, 1072)

        val newInitialZoom = calculateInitialZoom(
            containerSize = newContainerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            readMode = readMode,
            scalesCalculator = scalesCalculator
        )
        val newBaseTransform = newInitialZoom.baseTransform
        val newUserTransform = calculateRestoreCenterUserTransform(
            containerSize = newContainerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            newBaseTransform = newBaseTransform,
            contentVisibleCenterPoint = contentVisibleCenterPoint.round(),
            lastScale = lastTransform.scale,
        )

        val newContentVisibleCenterPoint = calculateContentVisibleRect(
            containerSize = newContainerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = alignment,
            rotation = rotation,
            userScale = newUserTransform.scaleX,
            userOffset = newUserTransform.offset,
        ).center
        val newTransform = newBaseTransform + newUserTransform

        Assert.assertEquals(
            contentVisibleCenterPoint.round(),
            newContentVisibleCenterPoint.round()
        )
    }
}