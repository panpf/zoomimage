package com.github.panpf.zoomimage.compose.subsampling

import com.github.panpf.zoomimage.compose.subsampling.internal.SkiaToComposeTileBitmapConvertor
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapConvertor


actual fun createTileBitmapConvertor(): TileBitmapConvertor? = SkiaToComposeTileBitmapConvertor()