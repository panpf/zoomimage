package com.github.panpf.zoomimage.compose.subsampling

import com.github.panpf.zoomimage.compose.subsampling.internal.AndroidToComposeTileBitmapConvertor
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapConvertor


actual fun createTileBitmapConvertor(): TileBitmapConvertor? = AndroidToComposeTileBitmapConvertor()