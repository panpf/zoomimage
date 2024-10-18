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

package com.github.panpf.zoomimage.sample.ui.examples

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.chrisbanes.photoview.PhotoView
import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.sketch.loadImage
import com.github.panpf.zoomimage.sample.databinding.FragmentPhotoViewBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.sample.util.toVeryShortString
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.toShortString
import kotlin.math.pow
import kotlin.math.sqrt

class PhotoViewFragment : BaseBindingFragment<FragmentPhotoViewBinding>() {

    private val args by navArgs<PhotoViewFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenMode = false
    }

    override fun onViewCreated(
        binding: FragmentPhotoViewBinding,
        savedInstanceState: Bundle?
    ) {
        binding.photoView.apply {
            setOnScaleChangeListener { _, _, _ ->
                updateInfo(binding)
            }
            setOnMatrixChangeListener {
                updateInfo(binding)
            }
        }

        updateInfo(binding)
        setImage(binding)
    }

    private fun setImage(binding: FragmentPhotoViewBinding) {
        binding.photoView.loadImage(args.imageUri) {
            crossfade()
            addListener(
                onStart = {
                    binding.stateView.loading()
                },
                onSuccess = { _, _ ->
                    binding.stateView.gone()
                },
                onError = { _, result ->
                    binding.stateView.error {
                        message(result.throwable)
                        retryAction {
                            setImage(binding)
                        }
                    }
                },
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo(binding: FragmentPhotoViewBinding) {
        binding.infoHeaderText.text = """
                scale: 
                offset: 
                visible: 
            """.trimIndent()
        binding.infoText.text = binding.photoView.run {
            val scales = floatArrayOf(minimumScale, mediumScale, maximumScale)
                .joinToString(prefix = "[", postfix = "]") { it.format(2).toString() }
            """
                ${displayScale.format(2)}(${baseScale.format(2)}x${suppScale.format(2)}) in $scales
                ${imageMatrix?.getTranslation()?.toShortString()}
                ${displayRect?.toVeryShortString()}
            """.trimIndent()
        }
    }

    class ItemFactory : FragmentItemFactory<String>(String::class) {

        override fun createFragment(
            bindingAdapterPosition: Int,
            absoluteAdapterPosition: Int,
            data: String
        ): Fragment = PhotoViewFragment().apply {
            arguments = PhotoViewFragmentArgs(data).toBundle()
        }
    }

    private fun Matrix.getTranslation(): OffsetCompat {
        val values = FloatArray(9).apply { getValues(this) }
        return OffsetCompat(
            x = values[Matrix.MTRANS_X],
            y = values[Matrix.MTRANS_Y],
        )
    }

    private fun Matrix.getScale(): Float {
        val values = FloatArray(9).apply { getValues(this) }
        val scaleX: Float = values[Matrix.MSCALE_X]
        val skewY: Float = values[Matrix.MSKEW_Y]
        return sqrt(scaleX.toDouble().pow(2.0) + skewY.toDouble().pow(2.0)).toFloat()
    }

    private val PhotoView.baseScale: Float
        get() = displayScale / suppScale

    private val PhotoView.suppScale: Float
        get() = scale

    private val PhotoView.displayScale: Float
        get() = Matrix().apply { getDisplayMatrix(this) }.getScale()
}