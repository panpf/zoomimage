package com.github.panpf.zoomimage.compose.android.test.subsampling

import com.github.panpf.zoomimage.compose.subsampling.isCloseAntiAliasForDrawTile
import kotlin.test.Test
import kotlin.test.assertEquals

class SubsamplingAndroidTest {

    @Test
    fun testIsCloseAntiAliasForDrawTile(){
        assertEquals(expected = false, actual = isCloseAntiAliasForDrawTile())
    }
}