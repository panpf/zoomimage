package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.zoom.DefaultPanToScaleTransformer
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.PanToScaleTransformer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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

    @Test
    fun testDefaultPanToScaleTransformer() {
        val panToScaleTransformer1 = DefaultPanToScaleTransformer()
        assertEquals(
            expected = 200,
            actual = panToScaleTransformer1.reference
        )
        assertEquals(
            expected = "DefaultPanToScaleTransformer(reference=200)",
            actual = panToScaleTransformer1.toString()
        )
        assertEquals(
            expected = 1.005f,
            actual = panToScaleTransformer1.transform(1f)
        )
        assertEquals(
            expected = 1.505f,
            actual = panToScaleTransformer1.transform(101f)
        )
        assertEquals(
            expected = 2.0f,
            actual = panToScaleTransformer1.transform(200f)
        )
        assertEquals(
            expected = 2.005f,
            actual = panToScaleTransformer1.transform(201f)
        )

        val panToScaleTransformer2 = DefaultPanToScaleTransformer(100)
        assertEquals(
            expected = 100,
            actual = panToScaleTransformer2.reference
        )
        assertEquals(
            expected = "DefaultPanToScaleTransformer(reference=100)",
            actual = panToScaleTransformer2.toString()
        )
        assertEquals(
            expected = 1.01f,
            actual = panToScaleTransformer2.transform(1f)
        )
        assertEquals(
            expected = 2.01f,
            actual = panToScaleTransformer2.transform(101f)
        )
        assertEquals(
            expected = 3.0f,
            actual = panToScaleTransformer2.transform(200f)
        )
        assertEquals(
            expected = 3.01f,
            actual = panToScaleTransformer2.transform(201f)
        )

        assertNotEquals(
            illegal = panToScaleTransformer1,
            actual = panToScaleTransformer2
        )
    }
}