//package com.github.panpf.zoomimage.compose.common.test.util
//
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.snapshots.SnapshotMutableState
//import com.github.panpf.zoomimage.compose.util.ConvertorMutableState
//import com.github.panpf.zoomimage.compose.util.ConvertorSnapshotMutableState
//import com.github.panpf.zoomimage.compose.util.convert
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//class ConvertorMutableStateTest {
//
//    @Test
//    fun testConvert() {
//        val snapshotState1 = mutableStateOf(1)
//        assertEquals(
//            expected = true,
//            actual = snapshotState1 is SnapshotMutableState
//        )
//        assertEquals(
//            expected = false,
//            actual = snapshotState1 is ConvertorMutableState
//        )
//
//        val snapshotState2 = snapshotState1.convert { it + 1 }
//        assertEquals(
//            expected = true,
//            actual = snapshotState2 is SnapshotMutableState
//        )
//        assertEquals(
//            expected = true,
//            actual = snapshotState2 is ConvertorSnapshotMutableState
//        )
//
//        val state1 = MyMutableState(1)
//        @Suppress("USELESS_IS_CHECK")
//        assertEquals(
//            expected = true,
//            actual = state1 is MutableState<*>
//        )
//        @Suppress("USELESS_IS_CHECK")
//        assertEquals(
//            expected = false,
//            actual = state1 is ConvertorMutableState<*>
//        )
//
//        val state2 = state1.convert { it + 1 }
//        @Suppress("USELESS_IS_CHECK")
//        assertEquals(
//            expected = true,
//            actual = state2 is MutableState<*>
//        )
//        assertEquals(
//            expected = true,
//            actual = state2 is ConvertorMutableState
//        )
//    }
//
//    @Test
//    fun testSnapshot() {
//        val state = mutableStateOf(0)
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
//    }
//
//    @Test
//    fun test() {
//        val state = MyMutableState(0)
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
//    }
//
//    class MyMutableState<T>(value: T) : MutableState<T> {
//
//        private var _value = value
//
//        override var value: T
//            get() = _value
//            set(value) {
//                _value = value
//            }
//
//        override fun component1(): T = _value
//
//        override fun component2(): (T) -> Unit = { value = it }
//    }
//}