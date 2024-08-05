package com.github.panpf.zoomimage.compose.nonandroid.test.subsampling

import com.github.panpf.zoomimage.compose.subsampling.isCloseAntiAliasForDrawTile
import kotlin.test.Test
import kotlin.test.assertEquals

class SubsamplingNonAndroidTest {

    @Test
    fun testIsCloseAntiAliasForDrawTile() {
        assertEquals(expected = true, actual = isCloseAntiAliasForDrawTile())
    }
}