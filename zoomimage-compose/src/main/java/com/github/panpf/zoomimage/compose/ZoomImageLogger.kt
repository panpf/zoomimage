/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.zoomimage.Logger


/**
 * Creates and remember a [Logger]
 *
 * @param tag The tag of the log
 * @param module The module of the log
 * @param showThreadName Whether to show the thread name in the log
 * @param level The level of the log
 */
@Composable
fun rememberZoomImageLogger(
    tag: String = "ZoomImage",
    module: String? = null,
    showThreadName: Boolean = false,
    level: Int = Logger.INFO,
): Logger {
    val logger = remember(tag, module, showThreadName) {
        Logger(tag = tag, module = module, showThreadName = showThreadName)
    }
    logger.level = level
    return logger
}