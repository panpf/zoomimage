package com.githb.panpf.zoomimage.images

import com.github.panpf.sketch.util.Size

object HttpImages {
    private const val PATH = "http://img.panpengfei.com"

    val hugeLongComic: ImageFile = ImageFile(
        uri = "${PATH}/sample_long_comic.jpg",
        name = "COMIC",
        size = Size(690, 12176)
    )
}