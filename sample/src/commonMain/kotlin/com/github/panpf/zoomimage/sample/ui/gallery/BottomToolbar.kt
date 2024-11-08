package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.getComposeImageLoaderIcon
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_layout_grid
import com.github.panpf.zoomimage.sample.resources.ic_layout_grid_staggered
import com.github.panpf.zoomimage.sample.ui.SwitchImageLoader
import com.github.panpf.zoomimage.sample.ui.components.MyDialog
import com.github.panpf.zoomimage.sample.ui.components.rememberMyDialogState
import org.jetbrains.compose.resources.painterResource

@Composable
fun BottomToolbar(modifier: Modifier) {
    val context = LocalPlatformContext.current
    val appSettings = context.appSettings
    Row(
        modifier
            .background(colorScheme.tertiaryContainer, RoundedCornerShape(50))
            .padding(horizontal = 10.dp)
    ) {
        val staggeredGridMode by appSettings.staggeredGridMode.collectAsState()
        val staggeredGridModeIcon = if (!staggeredGridMode) {
            painterResource(Res.drawable.ic_layout_grid_staggered)
        } else {
            painterResource(Res.drawable.ic_layout_grid)
        }
        IconButton(
            onClick = { appSettings.staggeredGridMode.value = !staggeredGridMode },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                painter = staggeredGridModeIcon,
                contentDescription = null,
                tint = colorScheme.onTertiaryContainer
            )
        }

        val switchImageLoaderDialogState = rememberMyDialogState()
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { switchImageLoaderDialogState.show() },
        ) {
            val imageLoaderName by appSettings.composeImageLoader.collectAsState()
            val imageLoaderIcon = getComposeImageLoaderIcon(imageLoaderName)
            Image(
                painter = imageLoaderIcon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(24.dp).clip(CircleShape).align(Alignment.Center),
            )
        }
        MyDialog(switchImageLoaderDialogState) {
            SwitchImageLoader {
                switchImageLoaderDialogState.dismiss()
            }
        }
    }
}