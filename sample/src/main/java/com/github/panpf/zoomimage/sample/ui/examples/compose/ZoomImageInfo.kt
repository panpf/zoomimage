package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.rememberZoomImageLogger
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString

@Composable
fun ZoomImageInfo(
    imageUri: String,
    zoomableState: ZoomableState,
) {
    val sizeInfo = remember(
        zoomableState.containerSize,
        zoomableState.contentSize,
        zoomableState.contentOriginSize
    ) {
        """
            containerSize: ${zoomableState.containerSize.let { "${it.width}x${it.height}" }}
            contentSize: ${zoomableState.contentSize.let { "${it.width}x${it.height}" }}
            contentOriginSize: ${zoomableState.contentOriginSize.let { "${it.width}x${it.height}" }}
        """.trimIndent()
    }
    val offsetInfo = remember(
        zoomableState.transform,
    ) {
        """
            baseOffset: ${zoomableState.baseTransform.offset.toShortString()}
            userOffset: ${zoomableState.userTransform.offset.toShortString()}
            userOffsetBounds: ${zoomableState.userOffsetBounds.toShortString()}
        """.trimIndent()
    }
    val otherInfo = remember(
        zoomableState.userOffsetBounds,
        zoomableState.containerVisibleRect,
        zoomableState.contentInContainerRect,
        zoomableState.contentInContainerVisibleRect,
    ) {
        """
            containerVisible: ${zoomableState.containerVisibleRect.toShortString()}
            contentInContainer: ${zoomableState.contentInContainerRect.toShortString()}
            contentInContainerVisible: ${zoomableState.contentInContainerVisibleRect.toShortString()}
        """.trimIndent()
    }

    Column(Modifier.fillMaxWidth()) {
        InfoItem("URI：", imageUri)

        Spacer(modifier = Modifier.size(12.dp))
        InfoItem("Size：", sizeInfo)

        Spacer(modifier = Modifier.size(12.dp))
        InfoItem("Offset：", offsetInfo)

        Spacer(modifier = Modifier.size(12.dp))
        InfoItem("Other：", otherInfo)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ZoomImageInfoPreview() {
    ZoomImageInfo(
        imageUri = "https://www.github.com/panpf/zoomimage",
        zoomableState = rememberZoomableState(rememberZoomImageLogger())
    )
}

@Composable
fun ColumnScope.InfoItem(title: String, content: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = content,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}