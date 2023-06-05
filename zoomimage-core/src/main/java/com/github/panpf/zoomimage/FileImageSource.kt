package com.github.panpf.zoomimage

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class FileImageSource(val file: File) : ImageSource {

    override val key: String = file.path

    override suspend fun openInputStream(): Result<InputStream> {
        return kotlin.runCatching {
            FileInputStream(file)
        }
    }
}