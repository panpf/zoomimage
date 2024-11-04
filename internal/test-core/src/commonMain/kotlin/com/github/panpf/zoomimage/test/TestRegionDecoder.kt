package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.internal.RegionDecoder
import com.github.panpf.zoomimage.util.IntRectCompat

class TestRegionDecoder(
    override val subsamplingImage: SubsamplingImage,
    override val imageInfo: ImageInfo,
) : RegionDecoder {

    override fun close() {

    }

    override fun ready() {

    }

    override fun decodeRegion(key: String, region: IntRectCompat, sampleSize: Int): TileImage {
        throw UnsupportedOperationException()
    }

    override fun copy(): RegionDecoder {
        return TestRegionDecoder(subsamplingImage, imageInfo)
    }

    class Factory(
        val imageInfo: ImageInfo,
        val supportMimeTypes: List<String>? = null,
        val unsupportedMimeTypes: List<String>? = null,
        val actions: MutableList<String>? = null
    ) : RegionDecoder.Factory {

        override suspend fun accept(subsamplingImage: SubsamplingImage): Boolean {
            actions?.add("accept")
            return true
        }

        override fun checkSupport(mimeType: String): Boolean? {
            actions?.add("checkSupport")
            return if (supportMimeTypes?.contains(mimeType) == true) {
                return true
            } else if (unsupportedMimeTypes?.contains(mimeType) == true) {
                return false
            } else {
                null
            }
        }

        override fun create(
            subsamplingImage: SubsamplingImage,
            imageSource: ImageSource
        ): RegionDecoder {
            actions?.add("create")
            return TestRegionDecoder(subsamplingImage, imageInfo)
        }
    }
}