package com.github.panpf.zoomimage.sample

import com.githb.panpf.zoomimage.images.ImageFile
import com.github.panpf.sketch.fetch.newComposeResourceUri
import com.github.panpf.sketch.util.Size
import com.github.panpf.zoomimage.sample.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

object ComposeResourceImages {

    @OptIn(ExperimentalResourceApi::class)
    val hugeChina: ImageFile =
        ImageFile(
            newComposeResourceUri(Res.getUri("files/huge_china.jpg")),
            "CHINA",
            Size(6799, 4882)
        )
}