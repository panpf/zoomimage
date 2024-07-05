package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun rememberMyDialogState(showing: Boolean = false): MyDialogState =
    remember { MyDialogState(showing) }

class MyDialogState(showing: Boolean = false) {
    var showing by mutableStateOf(showing)

    fun show() {
        showing = true
    }
}

@Composable
fun MyDialog(
    state: MyDialogState,
    content: @Composable() (ColumnScope.() -> Unit)
) {
    if (state.showing) {
        Dialog(onDismissRequest = { state.showing = false }) {
            Surface(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    content()
                }
            }
        }
    }
}