package com.github.panpf.zoomimage.view.subsampling.internal

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.graphics.Rect
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withSave
import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.view.internal.applyTransform
import kotlin.math.ceil
import kotlin.math.floor

class TileDrawHelper(private val engine: SubsamplingEngine) {

    private val cacheRect1 = Rect()
    private val cacheRect2 = Rect()
    private val cacheDisplayMatrix: Matrix = Matrix()
    private var cacheTileBoundsPaint: Paint? = null

    fun drawTiles(
        canvas: Canvas,
        transform: TransformCompat,
        containerSize: IntSizeCompat,
        showTileBounds: Boolean,
    ) {
        val imageInfo = engine.imageInfo ?: return
        val contentSize = engine.contentSize.takeIf { !it.isEmpty() } ?: return
        val tileSnapshotList = engine.tileSnapshotList.takeIf { it.isNotEmpty() } ?: return
        val imageLoadRect = engine.imageLoadRect.takeIf { !it.isEmpty } ?: return

        val widthScale = imageInfo.width / contentSize.width.toFloat()
        val heightScale = imageInfo.height / contentSize.height.toFloat()
        var insideLoadCount = 0
        var outsideLoadCount = 0
        var realDrawCount = 0
        canvas.withSave {
            canvas.concat(cacheDisplayMatrix.applyTransform(transform, containerSize))

            tileSnapshotList.forEach { tileSnapshot ->
                if (tileSnapshot.srcRect.overlaps(imageLoadRect)) {
                    insideLoadCount++
                    val tileBitmap = tileSnapshot.bitmap
                    val tileDrawDstRect = cacheRect1.apply {
                        set(
                            /* left = */ floor(tileSnapshot.srcRect.left / widthScale).toInt(),
                            /* top = */ floor(tileSnapshot.srcRect.top / heightScale).toInt(),
                            /* right = */ floor(tileSnapshot.srcRect.right / widthScale).toInt(),
                            /* bottom = */ floor(tileSnapshot.srcRect.bottom / heightScale).toInt()
                        )
                    }
                    if (tileBitmap != null) {
                        realDrawCount++
                        val tileDrawSrcRect = cacheRect2.apply {
                            set(0, 0, tileBitmap.width, tileBitmap.height)
                        }
                        canvas.drawBitmap(
                            /* bitmap = */ tileBitmap,
                            /* src = */ tileDrawSrcRect,
                            /* dst = */ tileDrawDstRect,
                            /* paint = */ null
                        )
                    }

                    if (showTileBounds) {
                        val boundsColor = when (tileSnapshot.state) {
                            Tile.STATE_LOADED -> Color.GREEN
                            Tile.STATE_LOADING -> Color.YELLOW
                            else -> Color.RED
                        }
                        val tileBoundsPaint = getTileBoundsPaint()
                        tileBoundsPaint.color = ColorUtils.setAlphaComponent(boundsColor, 100)
                        val boundsStrokeHalfWidth by lazy { (tileBoundsPaint.strokeWidth) / 2 }
                        val tileBoundsRect = cacheRect2.apply {
                            set(
                                /* left = */
                                floor(tileDrawDstRect.left + boundsStrokeHalfWidth).toInt(),
                                /* top = */
                                floor(tileDrawDstRect.top + boundsStrokeHalfWidth).toInt(),
                                /* right = */
                                ceil(tileDrawDstRect.right - boundsStrokeHalfWidth).toInt(),
                                /* bottom = */
                                ceil(tileDrawDstRect.bottom - boundsStrokeHalfWidth).toInt()
                            )
                        }
                        canvas.drawRect(/* r = */ tileBoundsRect, /* paint = */ tileBoundsPaint)
                    }
                } else {
                    outsideLoadCount++
                }
            }
        }
        engine.logger.d {
            "drawTiles. tiles=${tileSnapshotList.size}, " +
                    "insideLoadCount=${insideLoadCount}, " +
                    "outsideLoadCount=${outsideLoadCount}, " +
                    "realDrawCount=${realDrawCount}. " +
                    "'${engine.imageKey}'"
        }
    }

    private fun getTileBoundsPaint(): Paint {
        return cacheTileBoundsPaint ?: Paint()
            .apply {
                style = STROKE
                strokeWidth = 1f * Resources.getSystem().displayMetrics.density
            }
            .apply {
                this@TileDrawHelper.cacheTileBoundsPaint = this
            }
    }
}