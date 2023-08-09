package com.github.panpf.zoomimage.sample.ui.test.view

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.widget.ImageView.ScaleType
import androidx.appcompat.widget.Toolbar
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.zoomimage.sample.databinding.ImageMatrixFragmentBinding
import com.github.panpf.zoomimage.sample.ui.base.view.ToolbarBindingFragment
import com.github.panpf.zoomimage.sample.ui.util.view.computeZoomInitialConfig
import com.github.panpf.zoomimage.sample.ui.util.view.getRotation
import com.github.panpf.zoomimage.sample.ui.util.view.getScale
import com.github.panpf.zoomimage.sample.ui.util.view.getTranslation
import com.github.panpf.zoomimage.sample.util.BitmapScaleTransformation
import com.github.panpf.zoomimage.sample.util.format
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.toShortString
import kotlin.math.min

class ImageMatrixFragment : ToolbarBindingFragment<ImageMatrixFragmentBinding>() {

    //    private val matrix = Matrix()
    private val scaleStep = 0.2f
    private val offsetStep = 50
    private val rotateStep = 90

    private val baseMatrix = Matrix()
    private val userMatrix = Matrix()
    private val displayMatrix = Matrix()
    private val scaleType = ScaleType.FIT_CENTER
    private var viewSize = IntSizeCompat.Zero
    private var rotation = 0

    override fun onViewCreated(
        toolbar: Toolbar,
        binding: ImageMatrixFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(toolbar, binding, savedInstanceState)
        toolbar.title = "Image Matrix"

        binding.imageMatrixFragmentImageView.apply {
        }

        // todo 横图竖图合成一个按钮，在增加 ScaleType 选项
        binding.imageMatrixFragmentHorizontalButton.setOnClickListener {
            setImage(binding, true)
        }

        binding.imageMatrixFragmentVerticalButton.setOnClickListener {
            setImage(binding, false)
        }

        binding.imageMatrixFragmentResetButton.setOnClickListener {
            userMatrix.reset()
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentScalePlusButton.setOnClickListener {
            val currentScale = userMatrix.getScale().scaleX
            val targetScale = currentScale + scaleStep
            val deltaScale = targetScale / currentScale
            userMatrix.postScale(deltaScale, deltaScale)
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentScaleMinusButton.setOnClickListener {
            val currentScale = userMatrix.getScale().scaleX
            val targetScale = currentScale - scaleStep
            val deltaScale = targetScale / currentScale
            userMatrix.postScale(deltaScale, deltaScale)
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentOffsetUpButton.setOnClickListener {
            userMatrix.postTranslate(0f, -offsetStep.toFloat())
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentOffsetDownButton.setOnClickListener {
            userMatrix.postTranslate(0f, offsetStep.toFloat())
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentOffsetRightButton.setOnClickListener {
            userMatrix.postTranslate(offsetStep.toFloat(), 0f)
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentOffsetLeftButton.setOnClickListener {
            userMatrix.postTranslate(-offsetStep.toFloat(), 0f)
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentRotatePlusButton.setOnClickListener {
            rotation = (rotation + rotateStep) % 360
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentRotateMinusButton.setOnClickListener {
            rotation = (rotation - rotateStep) % 360
            updateMatrix(binding)
        }

        binding.imageMatrixFragmentImageView.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (viewSize.width != v.width || viewSize.height != v.height) {
                viewSize = IntSizeCompat(v.width, v.height)
                updateMatrix(binding)
            }
        }

        updateMatrix(binding)
        setImage(binding, true)
    }

    @SuppressLint("SetTextI18n")
    private fun updateMatrix(binding: ImageMatrixFragmentBinding) {
        val drawable = binding.imageMatrixFragmentImageView.drawable
        val drawableSize = drawable?.let { IntSizeCompat(it.intrinsicWidth, it.intrinsicHeight) }
            ?: IntSizeCompat.Zero

        val rotation = rotation
        val initialConfig = computeZoomInitialConfig(
            containerSize = viewSize,
            contentSize = drawableSize,
            contentOriginSize = IntSizeCompat.Zero,
            scaleType = scaleType,
            rotation = rotation,
            readMode = null,
            mediumScaleMinMultiple = 2f,
        )
        val baseMatrix = baseMatrix.apply {
            reset()
            val transform = initialConfig.baseTransform
            require(transform.scale.scaleX > 0f && transform.scale.scaleY > 0f) { "resetBaseMatrix transform scale=$transform is invalid" }
            postScale(transform.scale.scaleX, transform.scale.scaleY)
            postTranslate(transform.offset.x, transform.offset.y)
            postRotate(rotation.toFloat())
        }

        val userMatrix = userMatrix
        val displayMatrix = displayMatrix.apply {
            set(baseMatrix)
            postConcat(userMatrix)
        }

        binding.imageMatrixFragmentImageView.imageMatrix = displayMatrix
        updateValues(binding)
    }

    @SuppressLint("SetTextI18n")
    private fun updateValues(binding: ImageMatrixFragmentBinding) {
        val matrix = binding.imageMatrixFragmentImageView.imageMatrix
        val scaleString = matrix.getScale().scaleX.format(2)
        val translationString = matrix.getTranslation().toShortString()
        val rotationString = matrix.getRotation().toString()
        binding.imageMatrixFragmentTransformValueText.text =
            "缩放：${scaleString}；位移：${translationString}；旋转：$rotationString"

        val displayRect = RectF()
        val drawable = binding.imageMatrixFragmentImageView.drawable
        if (drawable != null) {
            displayRect.set(
                /* left = */ 0f,
                /* top = */ 0f,
                /* right = */ drawable.intrinsicWidth.toFloat(),
                /* bottom = */ drawable.intrinsicHeight.toFloat()
            )
        }
        matrix.mapRect(displayRect)
        binding.imageMatrixFragmentDisplayValueText.text = "display：${displayRect.toShortString()}"
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setImage(binding: ImageMatrixFragmentBinding, hor: Boolean) {
        val imageUri = if (hor) {
            newAssetUri("sample_elephant.jpg")
        } else {
            newAssetUri("sample_cat.jpg")
        }
        binding.imageMatrixFragmentImageView.displayImage(imageUri) {
            val resources = requireContext().resources
            val maxSize =
                min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels) / 4
            addTransformations(BitmapScaleTransformation(maxSize))
            listener(
                onSuccess = { _, _ ->
                    updateMatrix(binding)
                },
                onError = { _, _ ->
                    updateMatrix(binding)
                }
            )
        }
    }
}