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

package com.github.panpf.zoomimage.sample.ui.common.menu

import com.github.panpf.zoomimage.sample.util.SettingsStateFlow

interface SwitchMenu {
    val title: String
    var isChecked: Boolean
    val disabled: Boolean
    val onLongClick: (() -> Unit)?
}

class SwitchMenuFlow(
    override val title: String,
    private val data: SettingsStateFlow<Boolean>,
    override val disabled: Boolean = false,
    private val reverse: Boolean = false,
    override val onLongClick: (() -> Unit)? = null,
) : SwitchMenu {

    override var isChecked: Boolean
        get() = if (reverse) {
            !data.value
        } else {
            data.value
        }
        set(value) {
            val newValue = if (reverse) {
                !value
            } else {
                value
            }
            data.value = newValue
        }
}
