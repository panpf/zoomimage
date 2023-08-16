package com.github.panpf.zoomimage

enum class Edge {
    NONE, START, END, BOTH
}

data class ScrollEdge(val horizontal: Edge, val vertical: Edge)

// todo Unit tests
fun ScrollEdge.toShortString(): String = "(${horizontal.name},${vertical.name})"