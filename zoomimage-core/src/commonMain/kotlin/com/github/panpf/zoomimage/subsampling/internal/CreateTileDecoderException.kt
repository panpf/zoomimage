package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.subsampling.ImageInfo

class CreateTileDecoderException(
    val code: Int, val skipped: Boolean, message: String, val imageInfo: ImageInfo?
) : Exception(message)