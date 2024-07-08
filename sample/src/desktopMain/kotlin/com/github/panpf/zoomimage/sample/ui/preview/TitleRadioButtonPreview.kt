package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.ui.components.TitleRadioButton

@Preview
@Composable
fun TitleRadioButtonPreview() {
    Column {
        Row {
            TitleRadioButton(true, title = "横图", onClick = {})
            Spacer(modifier = Modifier.size(20.dp))
            TitleRadioButton(false, title = "竖图", onClick = {})
        }
        Row {
            TitleRadioButton(true, title = "小图", onClick = {})
            Spacer(modifier = Modifier.size(20.dp))
            TitleRadioButton(false, title = "大图", onClick = {})
        }
    }
}