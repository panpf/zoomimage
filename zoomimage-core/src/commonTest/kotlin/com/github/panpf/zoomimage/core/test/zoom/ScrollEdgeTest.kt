package com.github.panpf.zoomimage.core.test.zoom

import com.github.panpf.zoomimage.zoom.Edge
import com.github.panpf.zoomimage.zoom.ScrollEdge
import com.github.panpf.zoomimage.zoom.toShortString
import org.junit.Assert
import org.junit.Test

class ScrollEdgeTest {

    @Test
    fun test() {
        ScrollEdge.Default.apply {
            Assert.assertEquals(Edge.BOTH, horizontal)
            Assert.assertEquals(Edge.BOTH, vertical)
        }
    }

    @Test
    fun testToShortString() {
        ScrollEdge.Default.apply {
            Assert.assertEquals("(BOTH,BOTH)", toShortString())
        }
        ScrollEdge(Edge.START, Edge.END).apply {
            Assert.assertEquals("(START,END)", toShortString())
        }
    }
}