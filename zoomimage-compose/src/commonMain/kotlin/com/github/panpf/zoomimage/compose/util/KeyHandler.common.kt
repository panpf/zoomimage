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

/**
 * Get the platform-specific assist key. For example, macOS is usually meta, and other platforms are usually ctrl.
 *
 * @see com.github.panpf.zoomimage.compose.android.test.util.KeyHandlerAndroidTest.testPlatformAssistKey
 * @see com.github.panpf.zoomimage.compose.desktop.test.util.KeyHandlerDesktopTest.testPlatformAssistKey
 * @see com.github.panpf.zoomimage.compose.jscommon.test.util.KeyHandlerJsCommonTest.testPlatformAssistKey
 * @see com.github.panpf.zoomimage.compose.ios.test.util.KeyHandlerIosTest.testPlatformAssistKey
 */
expect fun platformAssistKey(): AssistKey

@Stable
interface KeyHandler {

    fun handle(event: KeyEvent): Boolean
}

/**
 * Create MatcherKeyHandler
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.MatcherKeyHandlerTest
 */
fun matcherKeyHandler(
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

/**
 * Match key events according to KeyMatcher, execute onKey when the match is successful,
 * execute onCanceled when the current match fails and the previous match was successful.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.MatcherKeyHandlerTest
 */
@Stable
abstract class MatcherKeyHandler(
    open val keyMatchers: List<KeyMatcher>
) : KeyHandler {

    private var lastMatched: Boolean = false

    override fun handle(event: KeyEvent): Boolean {
        val matched = keyMatchers.any { it.match(event) }
        if (matched) {
            lastMatched = true
            onKey(event)
        } else if (lastMatched) {
            lastMatched = false
            onCanceled(event)
        }
        return matched
    }

    abstract fun onKey(event: KeyEvent)

    abstract fun onCanceled(event: KeyEvent)
}

/**
 * Match key events based on conditions such as keys, auxiliary keys, key types, etc.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.KeyMatcherTest
 */
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

    fun match(event: KeyEvent): Boolean {
        val assistKeys = assistKeys ?: emptyArray()
        val yesAssistKeys: Sequence<AssistKey> = assistKeys.asSequence()
        val noAssistKeys: Sequence<AssistKey> = AssistKey.entries.asSequence()
            .filter { !assistKeys.contains(it) }
        return event.key == key
                && (type == null || event.type == type)
                && (yesAssistKeys.all { it.check(event) })
                && (noAssistKeys.all { !it.check(event) })
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

/**
 * Ctrl, Alt, Shift, Meta and other assist keys
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.AssistKeyTest
 */
@Stable
enum class AssistKey {
    Ctrl, Meta, Alt, Shift;

    fun check(event: KeyEvent): Boolean {
        return when (this) {
            Ctrl -> event.isCtrlPressed
            Meta -> event.isMetaPressed
            Alt -> event.isAltPressed
            Shift -> event.isShiftPressed
        }
    }

    operator fun plus(assistKey: AssistKey): Array<AssistKey> {
        return arrayOf(this, assistKey)
    }
}