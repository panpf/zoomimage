package com.github.panpf.zoomimage

/**
 * Edge state for the current offset
 */
data class ScrollEdge(val horizontal: Edge, val vertical: Edge) {
    companion object {
        val Default: ScrollEdge = ScrollEdge(Edge.BOTH, Edge.BOTH)
    }
}

fun ScrollEdge.toShortString(): String = "(${horizontal.name},${vertical.name})"

/**
 * Edge state
 */
enum class Edge {
    /**
     * Located in the middle position
     */
    NONE,

    /**
     * Located at the start position
     */
    START,

    /**
     * Located at the end position
     */
    END,

    /**
     * Located in the start and end positions
     */
    BOTH
}