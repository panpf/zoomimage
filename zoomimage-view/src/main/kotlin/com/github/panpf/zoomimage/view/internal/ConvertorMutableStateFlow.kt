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
package com.github.panpf.zoomimage.view.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Convert the value of the original [MutableStateFlow] through the specified [convertor]
 *
 * @see com.github.panpf.zoomimage.view.test.internal.ConvertorMutableStateFlowTest.testConvert
 */
internal fun <T> MutableStateFlow<T>.convert(convertor: (T) -> T): MutableStateFlow<T> {
    return ConvertorMutableStateFlow(this, convertor)
}

/**
 * A [MutableStateFlow] that can convert the value of the original [MutableStateFlow] through the specified [convertor]
 *
 * @see com.github.panpf.zoomimage.view.test.internal.ConvertorMutableStateFlowTest
 */
class ConvertorMutableStateFlow<T>(
    private val state: MutableStateFlow<T>,
    private val convertor: (T) -> T
) : MutableStateFlow<T> {

    override val replayCache: List<T>
        get() = state.replayCache

    override val subscriptionCount: StateFlow<Int>
        get() = state.subscriptionCount

    override var value: T
        get() = convertor(state.value)
        set(value) {
            state.value = value
        }

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        // TODO convert is not used
        return state.collect(collector)
    }

    override fun compareAndSet(expect: T, update: T): Boolean {
        return state.compareAndSet(expect, update)
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        state.resetReplayCache()
    }

    override fun tryEmit(value: T): Boolean {
        return state.tryEmit(value)
    }

    override suspend fun emit(value: T) {
        state.emit(value)
    }
}