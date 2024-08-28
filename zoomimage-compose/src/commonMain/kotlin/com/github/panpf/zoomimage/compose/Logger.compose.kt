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

package com.github.panpf.zoomimage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.Logger.Level
import com.github.panpf.zoomimage.util.Logger.Pipeline

/**
 * Creates and remember a [Logger]
 *
 * @param tag The tag of the log
 * @see com.github.panpf.zoomimage.compose.common.test.LoggerComposeTest.testRememberZoomImageLogger
 */
@Composable
fun rememberZoomImageLogger(
    tag: String = "ZoomImage",
    level: Level? = null,
    pipeline: Pipeline? = null,
): Logger {
    val logger = remember(tag) {
        Logger(tag = tag)
    }
    if (level != null && logger.level != level) {
        logger.level = level
    }
    if (pipeline != null && logger.pipeline != pipeline) {
        logger.pipeline = pipeline
    }
    return logger
}