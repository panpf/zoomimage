package com.github.panpf.zoomimage.sample.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.composeImageLoaders
import com.github.panpf.zoomimage.sample.getComposeImageLoaderIcon

@Composable
fun SwitchImageLoader(onDismissRequest: () -> Unit) {
    val appSettings = LocalPlatformContext.current.appSettings
    LazyColumn(Modifier.fillMaxWidth()) {
        items(composeImageLoaders) { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        appSettings.composeImageLoader.value = item.name
                        onDismissRequest()
                    }
                    .padding(vertical = 14.dp, horizontal = 20.dp)
            ) {
                Image(
                    painter = getComposeImageLoaderIcon(item.name),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.CenterVertically)
                        .clip(CircleShape)
                )

                Spacer(Modifier.size(14.dp))

                Column(Modifier.align(Alignment.CenterVertically)) {
                    Text(text = item.name, fontWeight = FontWeight.Bold)
                    Text(text = item.desc, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}
