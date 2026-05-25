package com.github.panpf.zoomimage.sample.image

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.github.panpf.sketch.fetch.isComposeResourceUri
import com.github.panpf.sketch.util.toUri
import java.io.InputStream

class ComposeResourceLoader : ModelLoader<String, InputStream> {

    override fun buildLoadData(
        model: String,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ObjectKey(model), ComposeResourceFetcher(model))
    }

    override fun handles(model: String): Boolean {
        return isComposeResourceUri(model.toUri())
    }

    class Factory : ModelLoaderFactory<String, InputStream> {

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
            return ComposeResourceLoader()
        }

        override fun teardown() {

        }
    }
}