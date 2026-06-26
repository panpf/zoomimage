package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

suspend fun saveImageToExternalFilesDir(
    imageFiles: List<ComposeResImageFile>,
    cacheDir: Path
): Unit = withContext(ioCoroutineDispatcher()) {
    val fileSystem = defaultFileSystem()
    if (!fileSystem.exists(cacheDir)) {
        fileSystem.createDirectories(cacheDir)
    }
    imageFiles.forEach {
        val file = cacheDir.resolve(it.name)
        if (!fileSystem.exists(file)) {
            try {
                it.toImageSource().openSource()
                    .buffer().use { input ->
                        fileSystem.sink(file).buffer().use { output ->
                            output.writeAll(input)
                        }
                    }
            } catch (e: Exception) {
                fileSystem.delete(file)
                throw Exception("Failed to copy ${it.name} to ${file}", e)
            }
        }
    }
}

/**
 * Get default file system.
 */
expect fun defaultFileSystem(): FileSystem