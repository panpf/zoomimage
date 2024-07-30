package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.animation.core.FastOutSlowInEasing
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ZoomAnimationSpecTest {

    @Test
    fun test() {
        ZoomAnimationSpec().apply {
            assertEquals(
                expected = 300,
                actual = durationMillis
            )
            assertEquals(
                expected = FastOutSlowInEasing,
                actual = easing
            )
        }

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