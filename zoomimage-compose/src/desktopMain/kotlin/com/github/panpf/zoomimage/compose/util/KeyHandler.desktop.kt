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

package com.github.panpf.zoomimage.compose.util

import java.util.Locale

/**
 * Get the desktop platform assist key. For example, macOS is usually meta, and other platforms are usually ctrl.
 *
 * @see com.github.panpf.zoomimage.compose.desktop.test.util.KeyHandlerDesktopTest.testPlatformAssistKey
 */
actual fun platformAssistKey(): AssistKey {
    val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
    return if (osName.contains("mac")) AssistKey.Meta else AssistKey.Ctrl
}