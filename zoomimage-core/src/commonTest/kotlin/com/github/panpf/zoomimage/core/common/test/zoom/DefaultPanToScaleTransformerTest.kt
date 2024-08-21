package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.zoom.DefaultPanToScaleTransformer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DefaultPanToScaleTransformerTest {

    @Test
    fun testConstructor() {
        val panToScaleTransformer1 = DefaultPanToScaleTransformer()
        assertEquals(
            expected = 200,
            actual = panToScaleTransformer1.reference
        )

        val panToScaleTransformer2 = DefaultPanToScaleTransformer(100)
        assertEquals(
            expected = 100,
            actual = panToScaleTransformer2.reference
        )
    }

    @Test
    fun testTransform() {
        val panToScaleTransformer1 = DefaultPanToScaleTransformer()
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
    }

    @Test
    fun testEqualsAndHashCode() {
        val panToScaleTransformer1 = DefaultPanToScaleTransformer()
        val panToScaleTransformer12 = DefaultPanToScaleTransformer()
        val panToScaleTransformer2 = DefaultPanToScaleTransformer(100)

        assertEquals(expected = panToScaleTransformer1, actual = panToScaleTransformer1)
        assertEquals(expected = panToScaleTransformer1, actual = panToScaleTransformer12)
        assertNotEquals(illegal = panToScaleTransformer1, actual = null as Any?)
        assertNotEquals(illegal = panToScaleTransformer1, actual = Any())
        assertNotEquals(illegal = panToScaleTransformer1, actual = panToScaleTransformer2)

        assertEquals(
            expected = panToScaleTransformer1.hashCode(),
            actual = panToScaleTransformer1.hashCode()
        )
        assertEquals(
            expected = panToScaleTransformer1.hashCode(),
            actual = panToScaleTransformer12.hashCode()
        )
        assertNotEquals(
            illegal = panToScaleTransformer1.hashCode(),
            actual = panToScaleTransformer2.hashCode()
        )
    }

    @Test
    fun testToString() {
        val panToScaleTransformer1 = DefaultPanToScaleTransformer()
        val panToScaleTransformer2 = DefaultPanToScaleTransformer(100)
        assertEquals(
            expected = "DefaultPanToScaleTransformer(reference=200)",
            actual = panToScaleTransformer1.toString()
        )
        assertEquals(
            expected = "DefaultPanToScaleTransformer(reference=100)",
            actual = panToScaleTransformer2.toString()
        )
    }
}