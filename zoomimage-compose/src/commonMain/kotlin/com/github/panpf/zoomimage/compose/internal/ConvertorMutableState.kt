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

package com.github.panpf.zoomimage.compose.internal

import androidx.compose.runtime.MutableState

fun <T> MutableState<T>.convert(convertor: (T) -> T): MutableState<T> {
    return ConvertorMutableState(this, convertor)
}

class ConvertorMutableState<T>(
    private val state: MutableState<T>,
    private val convertor: (T) -> T
) : MutableState<T> {

    override var value: T
        get() = convertor(state.value)
        set(value) {
            state.value = value
        }

    override fun component1(): T {
        return state.component1()
    }

    override fun component2(): (T) -> Unit {
        return state.component2()
    }
}