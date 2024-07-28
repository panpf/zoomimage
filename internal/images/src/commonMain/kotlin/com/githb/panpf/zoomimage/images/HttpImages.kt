package com.githb.panpf.zoomimage.images

import com.github.panpf.zoomimage.util.IntSizeCompat

object HttpImages {
    private const val PATH = "http://img.panpengfei.com"

    val hugeLongComic: ImageFile = ImageFile(
        uri = "${PATH}/sample_long_comic.jpg",
        name = "COMIC",
        size = IntSizeCompat(690, 12176)
    )
}