package com.github.panpf.zoomimage.core.desktop.test.util

import com.github.panpf.zoomimage.util.createFile
import com.github.panpf.zoomimage.util.deleteContents
import okio.FileSystem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileSystemsTest {

    @Test
    fun testCreateFile() {
        val fileSystem = FileSystem.SYSTEM

        val dir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY
        val file1 = dir.resolve("zoomimage-test-create-file-1.txt")
        val file2 = dir.resolve("zoomimage-test-create-file-2.txt")
        fileSystem.delete(file1)
        fileSystem.delete(file2)
        assertFalse(fileSystem.exists(file1))
        assertFalse(fileSystem.exists(file2))

        try {
            fileSystem.createFile(file1)
            assertTrue(fileSystem.exists(file1))

            fileSystem.createFile(file2, mustCreate = true)
            assertTrue(fileSystem.exists(file2))
        } finally {
            fileSystem.delete(file1)
            fileSystem.delete(file2)
        }
    }

    @Test
    fun testDeleteContents() {
        val fileSystem = FileSystem.SYSTEM

        val dir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("zoomimage-test-delete-contents")
        val file1 = dir.resolve("zoomimage-test-delete-contents-file-1.txt")
        val dir1 = dir.resolve("zoomimage-test-delete-contents-dir-1")
        val file11 = dir1.resolve("zoomimage-test-delete-contents-file-1-1.txt")
        fileSystem.delete(file11)
        fileSystem.delete(dir1)
        fileSystem.delete(file1)
        fileSystem.delete(dir)
        assertFalse(fileSystem.exists(file11))
        assertFalse(fileSystem.exists(dir1))
        assertFalse(fileSystem.exists(file1))
        assertFalse(fileSystem.exists(dir))

        try {
            fileSystem.createDirectory(dir)
            fileSystem.createDirectory(file1)
            fileSystem.createDirectory(dir1)
            fileSystem.createDirectory(file11)
            assertTrue(fileSystem.exists(file11))
            assertTrue(fileSystem.exists(dir1))
            assertTrue(fileSystem.exists(file1))
            assertTrue(fileSystem.exists(dir))

            fileSystem.deleteContents(dir)
            assertFalse(fileSystem.exists(file11))
            assertFalse(fileSystem.exists(dir1))
            assertFalse(fileSystem.exists(file1))
            assertTrue(fileSystem.exists(dir))
        } finally {
            fileSystem.delete(file11)
            fileSystem.delete(dir1)
            fileSystem.delete(file1)
            fileSystem.delete(dir)
        }
    }
}