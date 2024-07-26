package com.github.panpf.zoomimage.core.jscommon.test.util

import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

class CoroutinesJsCommonTest {

    @Test
    fun testIoCoroutineDispatcher() {
        assertEquals(expected = Dispatchers.Default, actual = ioCoroutineDispatcher())
    }
}