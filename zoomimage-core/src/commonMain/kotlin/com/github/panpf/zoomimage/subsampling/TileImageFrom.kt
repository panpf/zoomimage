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

package com.github.panpf.zoomimage.subsampling

import androidx.annotation.IntDef

/**
 * TileImage where from
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.TileImageFromTest
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    TileImageFrom.UNKNOWN,
    TileImageFrom.LOCAL,
    TileImageFrom.MEMORY_CACHE,
)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class TileImageFrom {

    companion object {

        const val UNKNOWN = 0
        const val MEMORY_CACHE = 1
        const val LOCAL = 2

        fun name(from: Int): String = when (from) {
            MEMORY_CACHE -> "MEMORY_CACHE"
            LOCAL -> "LOCAL"
            else -> "UNKNOWN"
        }
    }
}