package com.github.panpf.zoomimage.sample.ui.test

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ImageSwitchViewModel : ViewModel() {
    val imageUris = imageSwitchTestResources.map { it.uri }

    private val _currentImageUri = MutableStateFlow(imageUris.first())
    val currentImageUri: StateFlow<String> = _currentImageUri

    fun setImageUri(uri: String) {
        _currentImageUri.value = uri
    }
}