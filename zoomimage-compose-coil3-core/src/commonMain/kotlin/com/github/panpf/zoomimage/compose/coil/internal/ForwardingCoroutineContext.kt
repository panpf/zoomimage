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

import kotlin.coroutines.CoroutineContext

/**
 * A special [CoroutineContext] implementation that observes changes to its elements.
 */
internal abstract class ForwardingCoroutineContext(
    private val delegate: CoroutineContext,
) : CoroutineContext by delegate {

    abstract fun newContext(
        old: CoroutineContext,
        new: CoroutineContext,
    ): ForwardingCoroutineContext

    override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext {
        val new = delegate.minusKey(key)
        return newContext(this, new)
    }

    override operator fun plus(context: CoroutineContext): CoroutineContext {
        val new = delegate + context
        return newContext(this, new)
    }

    override fun equals(other: Any?): Boolean {
        return delegate == other
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }

    override fun toString(): String {
        return "ForwardingCoroutineContext(delegate=$delegate)"
    }
}
