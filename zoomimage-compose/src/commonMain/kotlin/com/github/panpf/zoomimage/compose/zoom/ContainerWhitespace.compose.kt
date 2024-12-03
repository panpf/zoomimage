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

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.LayoutDirection
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.rtlFlipped

/**
 * If the layout direction is RTL, returns the RTL flipped ContainerWhitespace, otherwise returns the original ContainerWhitespace
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.ContainerWhitespaceComposeTest.testRtlFlipped
 */
@Stable
internal fun ContainerWhitespace.rtlFlipped(layoutDirection: LayoutDirection?): ContainerWhitespace {
    return if (layoutDirection == null || layoutDirection == LayoutDirection.Rtl) {
        this.rtlFlipped()
    } else {
        this
    }
}