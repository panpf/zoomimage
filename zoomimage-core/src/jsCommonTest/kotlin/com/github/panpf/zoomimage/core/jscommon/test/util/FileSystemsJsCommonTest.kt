package com.github.panpf.zoomimage.core.jscommon.test.util

import com.github.panpf.zoomimage.util.defaultFileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertFailsWith

class FileSystemsJsCommonTest {

    @Test
    fun testDefaultFileSystem() {
        val fileSystem = defaultFileSystem()
        val path1 = "/sdcard/sample.jpeg".toPath()
        val path2 = "/sdcard/sample.png".toPath()

        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.atomicMove(path1, path2)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.canonicalize(path1)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.createDirectory(path1)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.createSymlink(path1, path2)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.delete(path1)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.list(path1)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.listOrNull(path1)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.metadataOrNull(path1)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.openReadOnly(path1)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.openReadWrite(path1)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.sink(path1)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.appendingSink(path1)
        }
        assertFailsWith(UnsupportedOperationException::class) {
            fileSystem.source(path1)
        }
    }
}