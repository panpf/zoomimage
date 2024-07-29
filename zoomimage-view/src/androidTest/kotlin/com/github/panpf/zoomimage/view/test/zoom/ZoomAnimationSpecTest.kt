package com.github.panpf.zoomimage.view.test.zoom

import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ZoomAnimationSpecTest {

    @Test
    fun test() {
        ZoomAnimationSpec().apply {
            assertEquals(
                expected = ZoomAnimationSpec.DEFAULT_DURATION_MILLIS,
                actual = durationMillis
            )
            assertEquals(
                expected = ZoomAnimationSpec.DEFAULT_INTERPOLATOR,
                actual = interpolator
            )
        }

        assertEquals(
            expected = 300,
            actual = ZoomAnimationSpec.DEFAULT_DURATION_MILLIS
        )

        assertEquals(
            expected = ZoomAnimationSpec(),
            actual = ZoomAnimationSpec.Default
        )
        assertSame(
            expected = ZoomAnimationSpec.Default,
            actual = ZoomAnimationSpec.Default
        )

        assertEquals(
            expected = ZoomAnimationSpec(durationMillis = 0),
            actual = ZoomAnimationSpec.None
        )
        assertSame(
            expected = ZoomAnimationSpec.None,
            actual = ZoomAnimationSpec.None
        )
    }
}