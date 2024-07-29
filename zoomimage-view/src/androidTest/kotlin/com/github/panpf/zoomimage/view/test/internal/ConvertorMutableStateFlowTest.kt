package com.github.panpf.zoomimage.view.test.internal

import com.github.panpf.zoomimage.view.internal.convert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ConvertorMutableStateFlowTest {

    @Test
    fun testConvert() {
        MutableStateFlow(100).convert { it + 1 }
    }

    @Test
    fun test() {
        val state = MutableStateFlow(0)
            .convert { it * if (it % 2 == 0) 2 else 3 }
        assertEquals(expected = 0, actual = state.value)

        state.value = 1
        assertEquals(expected = 3, actual = state.value)
        state.value = 2
        assertEquals(expected = 4, actual = state.value)
        state.value = 3
        assertEquals(expected = 9, actual = state.value)
        state.value = 4
        assertEquals(expected = 8, actual = state.value)
        state.value = 5
        assertEquals(expected = 15, actual = state.value)

        val list = mutableListOf<Int>()
        GlobalScope.launch {
            state.collect {
                list.add(it)
            }
        }
        repeat(6) {
            state.value = it
            Thread.sleep(100)
        }
        assertEquals(
            expected = listOf(0, 3, 4, 9, 8, 15).joinToString(),
            actual = list.toList().joinToString()
        )
    }
}