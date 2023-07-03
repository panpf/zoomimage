package com.github.panpf.zoomimage.core.imagesource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class FileImageSource(val file: File) : ImageSource {

    override val key: String = file.path

    override suspend fun openInputStream(): Result<InputStream> {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                FileInputStream(file)
            }
        }
    }
}