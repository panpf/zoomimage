package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.subsampling.internal.Tile

data class SamplingTiles(val sampleSize: Int, val tiles: List<Tile>)