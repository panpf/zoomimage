/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.zoom

/**
 * Edge state for the current offset
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ScrollEdgeTest
 */
data class ScrollEdge(val horizontal: Edge, val vertical: Edge) {

    companion object {
        val Default: ScrollEdge = ScrollEdge(Edge.BOTH, Edge.BOTH)
    }
}

/**
 * Return short string descriptions, for example: '(START,END)'
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ScrollEdgeTest.testToShortString
 */
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