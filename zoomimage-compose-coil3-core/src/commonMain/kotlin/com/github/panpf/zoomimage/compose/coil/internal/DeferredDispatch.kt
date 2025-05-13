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

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Launch [block] and defer dispatching until the context's [CoroutineDispatcher] changes.
 */
internal fun CoroutineScope.launchWithDeferredDispatch(
    block: suspend CoroutineScope.() -> Unit,
): Job {
    val originalDispatcher = coroutineContext.dispatcher
    if (originalDispatcher == null || originalDispatcher == Dispatchers.Unconfined) {
        return launch(
            context = Dispatchers.Unconfined,
            start = CoroutineStart.UNDISPATCHED,
            block = block,
        )
    } else {
        return CoroutineScope(DeferredDispatchCoroutineContext(coroutineContext)).launch(
            context = DeferredDispatchCoroutineDispatcher(originalDispatcher),
            start = CoroutineStart.UNDISPATCHED,
            block = block,
        )
    }
}

/**
 * A special [CoroutineContext] implementation that automatically enables
 * [DeferredDispatchCoroutineDispatcher] dispatching if the context's [CoroutineDispatcher] changes.
 */
private class DeferredDispatchCoroutineContext(
    context: CoroutineContext,
) : ForwardingCoroutineContext(context) {

    override fun newContext(
        old: CoroutineContext,
        new: CoroutineContext,
    ): ForwardingCoroutineContext {
        val oldDispatcher = old.dispatcher
        val newDispatcher = new.dispatcher
        if (oldDispatcher is DeferredDispatchCoroutineDispatcher && oldDispatcher != newDispatcher) {
            oldDispatcher.unconfined = false
        }

        return DeferredDispatchCoroutineContext(new)
    }
}

/**
 * A [CoroutineDispatcher] that delegates to [Dispatchers.Unconfined] while [unconfined] is true
 * and [delegate] when [unconfined] is false.
 */
private class DeferredDispatchCoroutineDispatcher(
    private val delegate: CoroutineDispatcher,
) : CoroutineDispatcher() {
    private val _unconfined = atomic(true)
    var unconfined by _unconfined

    private val currentDispatcher: CoroutineDispatcher
        get() = if (_unconfined.value) Dispatchers.Unconfined else delegate

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return currentDispatcher.isDispatchNeeded(context)
    }

    override fun limitedParallelism(parallelism: Int, name: String?): CoroutineDispatcher {
        return currentDispatcher.limitedParallelism(parallelism, name)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        currentDispatcher.dispatch(context, block)
    }

    @InternalCoroutinesApi
    override fun dispatchYield(context: CoroutineContext, block: Runnable) {
        currentDispatcher.dispatchYield(context, block)
    }

    override fun toString(): String {
        return "DeferredDispatchCoroutineDispatcher(delegate=$delegate)"
    }
}
