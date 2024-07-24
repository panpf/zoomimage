package com.github.panpf.zoomimage.sample.util

import platform.Foundation.NSURL


val NSURL.authority: String?
    get() {
        val port = port
        return if (port != null) this.host + ":" + port() else this.host
    }