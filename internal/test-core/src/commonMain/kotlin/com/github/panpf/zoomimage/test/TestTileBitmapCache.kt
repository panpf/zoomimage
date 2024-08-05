package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapCache

class TestTileBitmapCache : TileBitmapCache {

    private val map = mutableMapOf<String, TileBitmap>()

    override fun get(key: String): TileBitmap? {
        return map[key]
    }

    override fun put(
        key: String,
        tileBitmap: TileBitmap,
        imageUrl: String,
        imageInfo: ImageInfo
    ): TileBitmap? {
        map[key] = tileBitmap
        return null
    }
}