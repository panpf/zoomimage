package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.Source

class TestImageSource(override val key: String = "TestImageSource") : ImageSource {

    override fun openSource(): Source {
        throw UnsupportedOperationException("Not supported")
    }
}