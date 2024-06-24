package com.github.panpf.zoomimage.compose.subsampling.internal

import androidx.compose.ui.graphics.ImageBitmap
import com.github.panpf.zoomimage.compose.internal.toHexString


internal fun ImageBitmap.toHexShortString(): String =
    "(${width}x${height},config='${config}',colorModel='${colorSpace}',@${toHexString()}"