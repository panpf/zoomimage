/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.sample.ui.gallery

import android.app.Application
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PhotoPaletteViewModel(application: Application) : AndroidViewModel(application) {

    private val _photoPaletteState = MutableStateFlow(
        PhotoPalette(
            palette = null,
            primaryColor = getPrimaryColor(),
            tertiaryColor = getTertiaryColor()
        )
    )
    val photoPaletteState: StateFlow<PhotoPalette> = _photoPaletteState

    fun setPhotoPalette(photoPalette: PhotoPalette) {
        _photoPaletteState.value = photoPalette
    }

    private fun getPrimaryColor(): Int {
        val resources = (getApplication() as Application).resources
        return ResourcesCompat.getColor(resources, R.color.md_theme_primary, null)
    }

    private fun getTertiaryColor(): Int {
        val resources = (getApplication() as Application).resources
        return ResourcesCompat.getColor(resources, R.color.md_theme_tertiary, null)
    }
}