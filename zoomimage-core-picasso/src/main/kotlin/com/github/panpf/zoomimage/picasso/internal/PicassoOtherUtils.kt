package com.github.panpf.zoomimage.picasso.internal


internal fun Any.toHexString(): String = Integer.toHexString(this.hashCode())