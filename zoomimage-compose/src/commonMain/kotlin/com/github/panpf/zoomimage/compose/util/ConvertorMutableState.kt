///*
// * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.github.panpf.zoomimage.compose.util
//
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.SnapshotMutationPolicy
//import androidx.compose.runtime.snapshots.SnapshotMutableState
//
///**
// * Convert the value of the original [MutableState] through the specified [convertor].
// *
// * @see com.github.panpf.zoomimage.compose.common.test.util.ConvertorMutableStateTest.testConvert
// */
//internal fun <T> MutableState<T>.convert(convertor: (T) -> T): MutableState<T> {
//    return if (this is SnapshotMutableState) {
//        ConvertorSnapshotMutableState(this, convertor)
//    } else {
//        ConvertorMutableState(this, convertor)
//    }
//}
//
///**
// * A [MutableState] that converts the value of the original [MutableState] through the specified [convertor].
// *
// * @see com.github.panpf.zoomimage.compose.common.test.util.ConvertorMutableStateTest
// */
//internal class ConvertorMutableState<T>(
//    private val state: MutableState<T>,
//    private val convertor: (T) -> T
//) : MutableState<T> {
//
//    override var value: T
//        get() = convertor(state.value)
//        set(value) {
//            state.value = value
//        }
//
//    override fun component1(): T {
//        return state.component1()
//    }
//
//    override fun component2(): (T) -> Unit {
//        return state.component2()
//    }
//}
//
///**
// * A [SnapshotMutableState] that converts the value of the original [SnapshotMutableState] through the specified [convertor].
// *
// * @see com.github.panpf.zoomimage.compose.common.test.util.ConvertorMutableStateTest
// */
//internal class ConvertorSnapshotMutableState<T>(
//    private val state: SnapshotMutableState<T>,
//    private val convertor: (T) -> T
//) : SnapshotMutableState<T> {
//
//    override val policy: SnapshotMutationPolicy<T>
//        get() = state.policy
//
//    override var value: T
//        get() = convertor(state.value)
//        set(value) {
//            state.value = value
//        }
//
//    override fun component1(): T {
//        return state.component1()
//    }
//
//    override fun component2(): (T) -> Unit {
//        return state.component2()
//    }
//}