package com.github.panpf.zoomimage.subsampling

interface StoppedController {

    fun bindStoppedWrapper(stoppedWrapper: StoppedWrapper?)

    fun onDestroy()

    interface StoppedWrapper {
        var stopped: Boolean
    }
}