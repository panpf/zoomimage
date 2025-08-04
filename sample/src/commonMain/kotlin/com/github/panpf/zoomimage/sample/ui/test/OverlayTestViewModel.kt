package com.github.panpf.zoomimage.sample.ui.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OverlayTestViewModel : ViewModel() {

    private val _marks: MutableStateFlow<ImmutableList<Mark>> =
        MutableStateFlow(emptyList<Mark>().toImmutableList())
    val marks: StateFlow<ImmutableList<Mark>> = _marks

    private val _partitionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val partitionMode: StateFlow<Boolean> = _partitionMode

    private val _rectMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val rectMode: StateFlow<Boolean> = _rectMode

    init {
        viewModelScope.launch(ioCoroutineDispatcher()) {
            val jsonString = Res.readBytes("files/mark_datas.json").decodeToString()
            val markList: List<Mark> = Json.decodeFromString(jsonString)
            _marks.value = markList.toImmutableList()
        }
    }

    fun setPartitionMode(value: Boolean) {
        _partitionMode.value = value
    }

    fun setRectMode(value: Boolean) {
        _rectMode.value = value
    }
}

@Serializable
data class Mark(
    val cxPx: Float,
    val cyPx: Float,
    val radiusPx: Float
)