package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat

class TestRegionDecoder(
    override val imageInfo: ImageInfo,
) : RegionDecoder {

    override fun close() {

    }

    override fun prepare() {

    }

    override fun decodeRegion(region: IntRectCompat, sampleSize: Int): TileBitmap {
        throw UnsupportedOperationException()
    }

    override fun copy(): RegionDecoder {
        return TestRegionDecoder(imageInfo)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as TestRegionDecoder
        if (imageInfo != other.imageInfo) return false
        return true
    }

    override fun hashCode(): Int {
        return imageInfo.hashCode()
    }

    override fun toString(): String {
        return "TestRegionDecoder(imageInfo=$imageInfo)"
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
                true
            } else if (unsupportedMimeTypes?.contains(mimeType) == true) {
                false
            } else {
                null
            }
        }

        override suspend fun create(
            subsamplingImage: SubsamplingImage,
            imageSource: ImageSource
        ): RegionDecoder {
            actions?.add("create")
            return TestRegionDecoder(imageInfo)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String {
            return "TestRegionDecoder"
        }
    }
}