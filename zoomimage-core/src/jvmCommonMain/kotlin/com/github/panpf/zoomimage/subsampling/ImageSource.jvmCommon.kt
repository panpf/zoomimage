package com.github.panpf.zoomimage.subsampling

import okio.Path.Companion.toOkioPath
import java.io.File

fun FileImageSource(file: File): FileImageSource = FileImageSource(file.toOkioPath())

fun ImageSource.Companion.fromFile(file: File): ImageSource {
    return FileImageSource(file)
}