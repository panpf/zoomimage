package com.github.panpf.zoomimage.test


public inline fun <T> Iterable<T>.allFold(operation: (t1: T, t2: T) -> Boolean): Boolean {
    var result = true
    var last: T? = null
    this.forEach {
        val last1 = last
        if (last1 != null) {
            result = result and operation(last1, it)
        } else {
            last = it
        }
    }
    return result
}