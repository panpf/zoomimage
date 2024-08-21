package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.zoom.DefaultPanToScaleTransformer
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.PanToScaleTransformer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class OneFingerScaleSpecTest {

    @Test
    fun test() {
        assertSame(
            expected = PanToScaleTransformer.Default,
            actual = OneFingerScaleSpec().panToScaleTransformer
        )
        assertEquals(
            expected = DefaultPanToScaleTransformer(101),
            actual = OneFingerScaleSpec(DefaultPanToScaleTransformer(101)).panToScaleTransformer
        )
        assertEquals(
            expected = OneFingerScaleSpec(),
            actual = OneFingerScaleSpec.Default
        )
        assertSame(
            expected = OneFingerScaleSpec.Default,
            actual = OneFingerScaleSpec.Default
        )
    }

    @Test
    fun testPanToScaleTransformerDefault() {
        assertEquals(
            expected = DefaultPanToScaleTransformer(),
            actual = PanToScaleTransformer.Default
        )
        assertSame(
            expected = PanToScaleTransformer.Default,
            actual = PanToScaleTransformer.Default
        )
    }
}