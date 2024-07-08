package com.github.panpf.zoomimage.sample.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <T> T.letIf(predicate: Boolean, block: (T) -> T): T {
    contract {
        callsInPlace(block, EXACTLY_ONCE)
    }
    return if (predicate) block(this) else this
}