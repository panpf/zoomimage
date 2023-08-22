package com.github.panpf.zoomimage

enum class Edge {
    NONE, START, END, BOTH
}

data class ScrollEdge(val horizontal: Edge, val vertical: Edge) {
    companion object {
        val Default: ScrollEdge = ScrollEdge(Edge.BOTH, Edge.BOTH)
    }
}

fun ScrollEdge.toShortString(): String = "(${horizontal.name},${vertical.name})"