/*
 * Copyright 2023 Coil Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.compose.coil.internal

import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter.State
import coil3.compose.CrossfadePainter
import coil3.decode.DataSource
import coil3.request.SuccessResult
import coil3.request.crossfadeMillis
import kotlin.time.Duration.Companion.milliseconds

internal actual fun maybeNewCrossfadePainter(
    previous: State,
    current: State,
    contentScale: ContentScale,
): CrossfadePainter? {
    // We can only invoke the transition factory if the state is success or error.
    val result = when (current) {
        is State.Success -> current.result
        is State.Error -> current.result
        else -> return null
    }

    // Only animate successful requests.
    if (result !is SuccessResult) {
        return null
    }

    // Don't animate if the request was fulfilled by the memory cache.
    if (result.dataSource == DataSource.MEMORY_CACHE) {
        return null
    }

    // Wrap the painter in a CrossfadePainter if it has crossfadeMillis set.
    val crossfadeMillis = result.request.crossfadeMillis
    if (crossfadeMillis > 0) {
        return CrossfadePainter(
            start = previous.painter.takeIf { previous is State.Loading },
            end = current.painter,
            contentScale = contentScale,
            duration = crossfadeMillis.milliseconds,
            fadeStart = !result.isPlaceholderCached,
            preferExactIntrinsicSize = false,
        )
    } else {
        return null
    }
}
