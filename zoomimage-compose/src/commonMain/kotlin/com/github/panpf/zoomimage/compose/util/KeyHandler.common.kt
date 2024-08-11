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

package com.github.panpf.zoomimage.compose.util

import androidx.compose.runtime.Stable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type


expect fun platformAssistKey(): AssistKey

@Stable
interface KeyHandler {

    fun handle(event: KeyEvent): Boolean
}

fun MatcherKeyHandler(
    keyMatchers: List<KeyMatcher>,
    onCanceled: ((KeyEvent) -> Unit)? = null,
    onKey: (KeyEvent) -> Unit,
): KeyHandler {
    return object : MatcherKeyHandler(keyMatchers) {
        override fun onKey(event: KeyEvent) {
            onKey(event)
        }

        override fun onCanceled(event: KeyEvent) {
            onCanceled?.invoke(event)
        }
    }
}

@Stable
abstract class MatcherKeyHandler(
    open val keyMatchers: List<KeyMatcher>
) : KeyHandler {

    override fun handle(
        event: KeyEvent
    ): Boolean {
        var matched = false
        keyMatchers.forEach {
            if (!matched && it.match(event)) {
                onKey(event)
                it.keyed = true
                matched = true
            } else if (it.keyed) {
                it.keyed = false
                onCanceled(event)
            }
        }
        return matched
    }

    abstract fun onKey(event: KeyEvent)

    abstract fun onCanceled(event: KeyEvent)
}

@Stable
class KeyMatcher(
    val key: Key,
    val assistKeys: Array<AssistKey>? = null,
    val type: KeyEventType? = null,
) {

    constructor(
        key: Key,
        assistKey: AssistKey,
        type: KeyEventType? = null,
    ) : this(key, arrayOf(assistKey), type)

    var keyed: Boolean = false

    fun match(event: KeyEvent): Boolean {
        return event.key == key
                && (type == null || event.type == type)
                && (assistKeys == null || assistKeys.all { it.check(event) })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as KeyMatcher
        if (key != other.key) return false
        if (assistKeys != null) {
            if (other.assistKeys == null) return false
            if (!assistKeys.contentEquals(other.assistKeys)) return false
        } else if (other.assistKeys != null) return false
        if (type != other.type) return false
        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + (assistKeys?.contentHashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "KeyMatcher(key=$key, assistKeys=${assistKeys?.contentToString()}, type=$type)"
    }
}

@Stable
enum class AssistKey {
    Ctrl, Meta, Alt, Shift, None;

    fun check(event: KeyEvent): Boolean {
        return when (this) {
            Ctrl -> event.isCtrlPressed
            Meta -> event.isMetaPressed
            Alt -> event.isAltPressed
            Shift -> event.isShiftPressed
            None -> !event.isShiftPressed
                    && !event.isAltPressed
                    && !event.isMetaPressed
                    && !event.isCtrlPressed
        }
    }

    operator fun plus(assistKey: AssistKey): Array<AssistKey> {
        return arrayOf(this, assistKey)
    }
}