package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.zoom.Edge
import com.github.panpf.zoomimage.zoom.ScrollEdge
import com.github.panpf.zoomimage.zoom.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals

class ScrollEdgeTest {

    @Test
    fun test() {
        ScrollEdge.Default.apply {
            assertEquals(Edge.BOTH, horizontal)
            assertEquals(Edge.BOTH, vertical)
        }
    }

    @Test
    fun testToShortString() {
        ScrollEdge.Default.apply {
            assertEquals("(BOTH,BOTH)", toShortString())
        }
        ScrollEdge(Edge.START, Edge.END).apply {
            assertEquals("(START,END)", toShortString())
        }
    }
}