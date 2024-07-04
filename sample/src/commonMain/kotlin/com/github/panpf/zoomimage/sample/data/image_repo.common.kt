package com.github.panpf.zoomimage.sample.data

import com.githb.panpf.zoomimage.images.ImageFile
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.ImageInfo


expect suspend fun builtinImages(context: PlatformContext): List<ImageFile>

expect suspend fun localImages(
    context: PlatformContext,
    startPosition: Int,
    pageSize: Int
): List<String>

expect suspend fun readImageInfoOrNull(
    context: PlatformContext,
    sketch: Sketch,
    uri: String,
): ImageInfo?
