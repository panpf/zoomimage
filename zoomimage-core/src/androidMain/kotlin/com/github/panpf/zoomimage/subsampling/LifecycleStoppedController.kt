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

package com.github.panpf.zoomimage.subsampling

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

open class LifecycleStoppedController constructor(val lifecycle: Lifecycle) : StoppedController {

    private var stoppedWrapper: StoppedController.StoppedWrapper? = null
    private val resetStoppedLifecycleObserver = ResetStoppedLifecycleObserver(this)

    init {
        registerLifecycleObserver()
    }

    override fun bindStoppedWrapper(stoppedWrapper: StoppedController.StoppedWrapper?) {
        this.stoppedWrapper = stoppedWrapper
        resetStopped()
    }

    fun resetStopped() {
        val stopped = !lifecycle.currentState.isAtLeast(STARTED)
        stoppedWrapper?.stopped = stopped
    }

    fun registerLifecycleObserver() {
        lifecycle.addObserver(resetStoppedLifecycleObserver)
    }

    fun unregisterLifecycleObserver() {
        lifecycle.removeObserver(resetStoppedLifecycleObserver)
    }

    private class ResetStoppedLifecycleObserver(
        private val controller: LifecycleStoppedController
    ) : LifecycleEventObserver {

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_START) {
                controller.resetStopped()
            } else if (event == Lifecycle.Event.ON_STOP) {
                controller.resetStopped()
            }
        }
    }

    override fun onDestroy() {
        unregisterLifecycleObserver()
    }
}
