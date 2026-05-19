package com.github.panpf.zoomimage.sample.data

import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.ui.model.Photo

expect class PhotoService(sketch: Sketch) {

    suspend fun loadFromGallery(pageStart: Int, pageSize: Int): List<Photo>

    suspend fun saveToGallery(imageUri: String): Result<String?>

    suspend fun share(imageUri: String): Result<String?>
}