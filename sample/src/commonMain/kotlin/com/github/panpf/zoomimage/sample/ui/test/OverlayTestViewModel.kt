package com.github.panpf.zoomimage.sample.ui.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OverlayTestViewModel : ViewModel() {

    private val _marks = MutableStateFlow<List<Mark>>(emptyList())
    val marks: StateFlow<List<Mark>> = _marks

    init {
        viewModelScope.launch(ioCoroutineDispatcher()) {
            val jsonString = Res.readBytes("files/mark_datas.json").decodeToString()
            val markList: List<Mark> = Json.decodeFromString(jsonString)
            _marks.value = markList
        }
    }
}

@Serializable
data class Mark(
    val cxPx: Float,
    val cyPx: Float,
    val radiusPx: Float
)