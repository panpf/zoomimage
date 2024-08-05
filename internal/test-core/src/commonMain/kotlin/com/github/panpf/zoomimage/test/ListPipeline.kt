package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.util.Logger

class ListPipeline : Logger.Pipeline {

    var logs = mutableListOf<String>()

    override fun log(level: Logger.Level, tag: String, msg: String, tr: Throwable?) {
        val finalMsg = if (tr != null) {
            "${level}-$tag-$msg-$tr"
        } else {
            "${level}-$tag-$msg"
        }
        logs.add(finalMsg)
    }

    override fun flush() {
        logs.add("flush")
    }

    override fun toString(): String {
        return "ListPipeline"
    }
}