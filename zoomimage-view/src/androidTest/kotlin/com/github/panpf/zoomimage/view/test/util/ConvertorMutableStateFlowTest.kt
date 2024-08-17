//package com.github.panpf.zoomimage.view.test.util
//
//import com.github.panpf.zoomimage.view.util.convert
//import kotlinx.coroutines.DelicateCoroutinesApi
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.launch
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//class ConvertorMutableStateFlowTest {
//
//    @Test
//    fun testConvert() {
//        MutableStateFlow(100).convert { it + 1 }
//    }
//
//    @Test
//    @OptIn(DelicateCoroutinesApi::class)
//    fun test() {
//        val state = MutableStateFlow(0)
//        assertEquals(expected = 0, actual = state.value)
//
//        val convertState = state.convert { it * if (it % 2 == 0) 2 else 3 }
//        assertEquals(expected = 0, actual = convertState.value)
//
//        convertState.value = 1
//        assertEquals(expected = 1, actual = state.value)
//        assertEquals(expected = 3, actual = convertState.value)
//
//        convertState.value = 2
//        assertEquals(expected = 2, actual = state.value)
//        assertEquals(expected = 4, actual = convertState.value)
//
//        convertState.value = 3
//        assertEquals(expected = 3, actual = state.value)
//        assertEquals(expected = 9, actual = convertState.value)
//
//        convertState.value = 4
//        assertEquals(expected = 4, actual = state.value)
//        assertEquals(expected = 8, actual = convertState.value)
//
//        convertState.value = 5
//        assertEquals(expected = 5, actual = state.value)
//        assertEquals(expected = 15, actual = convertState.value)
//
//        Thread.sleep(100)
//        val list1 = mutableListOf<Int>()
//        GlobalScope.launch {
//            state.collect {
//                list1.add(it)
//            }
//        }
//        val list2 = mutableListOf<Int>()
//        GlobalScope.launch {
//            convertState.collect {
//                list2.add(it)
//            }
//        }
//        repeat(6) {
//            convertState.value = it
//            Thread.sleep(100)
//        }
//        assertEquals(
//            expected = listOf(0, 1, 2, 3, 4, 5).joinToString(),
//            actual = list1.toList().joinToString()
//        )
//        assertEquals(
//            expected = listOf(0, 3, 4, 9, 8, 15).joinToString(),
//            actual = list2.toList().joinToString()
//        )
//    }
//}