package com.githb.panpf.zoomimage.images

import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.util.Size

object ResourceImages {
    val hugeCard: ImageFile = ImageFile(
        uri = newResourceUri(drawableResId = com.github.panpf.zoomimage.images.R.raw.huge_card),
        name = "CARD",
        size = Size(7557, 5669)
    )
}