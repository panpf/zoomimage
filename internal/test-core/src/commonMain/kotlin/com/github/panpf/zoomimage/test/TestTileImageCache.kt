package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.TileImageCache

class TestTileImageCache : TileImageCache {

    private val map = mutableMapOf<String, TileImage>()

    override fun get(key: String): TileImage? {
        return map[key]
    }

    override fun put(
        key: String,
        tileImage: TileImage,
        imageUrl: String,
        imageInfo: ImageInfo
    ): TileImage? {
        map[key] = tileImage
        return null
    }
}