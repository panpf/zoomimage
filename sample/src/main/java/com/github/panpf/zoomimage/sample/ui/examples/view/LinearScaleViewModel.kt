package com.github.panpf.zoomimage.sample.ui.examples.view

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow

class LinearScaleViewModel : ViewModel() {
    val changeFlow = MutableSharedFlow<Float>()
}