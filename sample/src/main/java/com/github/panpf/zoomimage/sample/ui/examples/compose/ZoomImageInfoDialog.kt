package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.rememberZoomImageLogger
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString

@Composable
fun rememberZoomImageInfoDialogState(showing: Boolean = false): ZoomImageInfoDialogState =
    remember { ZoomImageInfoDialogState(showing) }

class ZoomImageInfoDialogState(showing: Boolean = false) {
    var showing by mutableStateOf(showing)
}

@Composable
fun ZoomImageInfoDialog(
    state: ZoomImageInfoDialogState = rememberZoomImageInfoDialogState(),
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
    val otherInfo = remember(
        zoomableState.userOffsetBounds,
        zoomableState.containerVisibleRect,
        zoomableState.contentInContainerRect,
        zoomableState.contentInContainerVisibleRect,
    ) {
        """
            offsetBounds: ${zoomableState.userOffsetBounds.toShortString()}
            containerVisible: ${zoomableState.containerVisibleRect.toShortString()}
            contentInContainer: ${zoomableState.contentInContainerRect.toShortString()}
            contentInContainerVisible: ${zoomableState.contentInContainerVisibleRect.toShortString()}
        """.trimIndent()
    }
    if (state.showing) {
        Dialog(onDismissRequest = { state.showing = false }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                InfoItem("URI：", imageUri)

                Spacer(modifier = Modifier.size(12.dp))
                InfoItem("Size：", sizeInfo)

                Spacer(modifier = Modifier.size(12.dp))
                InfoItem("Other：", otherInfo)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ZoomImageInfoDialogPreview() {
    ZoomImageInfoDialog(
        state = rememberZoomImageInfoDialogState(showing = true),
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