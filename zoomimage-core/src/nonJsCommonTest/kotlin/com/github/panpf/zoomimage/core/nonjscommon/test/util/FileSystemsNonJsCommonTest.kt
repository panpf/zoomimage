package com.github.panpf.zoomimage.core.nonjscommon.test.util

import com.github.panpf.zoomimage.util.defaultFileSystem
import okio.FileSystem
import okio.SYSTEM
import kotlin.test.Test
import kotlin.test.assertEquals

class FileSystemsNonJsCommonTest {

    @Test
    fun testDefaultFileSystem() {
        assertEquals(expected = FileSystem.SYSTEM, actual = defaultFileSystem())
    }
}