package com.github.panpf.zoomimage.sample.data

import com.githb.panpf.zoomimage.images.ImageFile
import com.github.panpf.sketch.fetch.newComposeResourceUri
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.jetbrains.compose.resources.ExperimentalResourceApi

object ComposeResourceImages {

    @OptIn(ExperimentalResourceApi::class)
    val hugeChina: ImageFile = ImageFile(
        uri = newComposeResourceUri(resourcePath = Res.getUri("files/huge_china.jpg")),
        name = "CHINA",
        size = IntSizeCompat(6799, 4882)
    )
}