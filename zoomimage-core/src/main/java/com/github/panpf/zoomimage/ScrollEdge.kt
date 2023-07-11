package com.github.panpf.zoomimage

data class ScrollEdge(val horizontal: Edge, val vertical: Edge)

fun ScrollEdge.toShortString(): String {
    return "(${horizontal.name}, ${vertical.name})"
}