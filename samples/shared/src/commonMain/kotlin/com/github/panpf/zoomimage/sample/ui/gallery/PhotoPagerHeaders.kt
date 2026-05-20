package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.Res
import com.github.panpf.zoomimage.sample.getComposeImageLoaderIcon
import com.github.panpf.zoomimage.sample.ic_settings
import com.github.panpf.zoomimage.sample.ic_swap_hor
import com.github.panpf.zoomimage.sample.ic_swap_ver
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.LocalNavBackStack
import com.github.panpf.zoomimage.sample.ui.SwitchImageLoader
import com.github.panpf.zoomimage.sample.ui.components.MyDialog
import com.github.panpf.zoomimage.sample.ui.components.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.settings.AppSettingsList
import com.github.panpf.zoomimage.sample.ui.settings.AppSettingsPage
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

val photoPagerTopBarHeight = 80.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PhotoPagerHeaders(
    params: PhotoPagerScreenParams,
    pagerState: PagerState,
    horizontalLayout: Boolean,
    photoPaletteState: MutableState<PhotoPalette>
) {
    val appSettings: AppSettings = koinInject()
    val photoPalette by photoPaletteState
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.height(photoPagerTopBarHeight).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navBackStack = LocalNavBackStack.current
            IconButton(
                onClick = { navBackStack.removeLastOrNull() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(40.dp).padding(8.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            val switchImageLoaderDialogState = rememberMyDialogState()
            Box(
                modifier = Modifier.size(40.dp)
                    .clip(CircleShape)
                    .background(photoPalette.containerColor)
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

            Spacer(modifier = Modifier.size(10.dp))

            IconButton(
                onClick = { appSettings.horizontalPagerLayout.value = !horizontalLayout },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
                )
            ) {
                val icon = if (horizontalLayout) {
                    painterResource(Res.drawable.ic_swap_ver)
                } else {
                    painterResource(Res.drawable.ic_swap_hor)
                }
                Icon(
                    painter = icon,
                    contentDescription = "orientation",
                    modifier = Modifier.size(40.dp).padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            val zoomImageSettingsListDialogState = rememberMyDialogState()
            IconButton(
                onClick = { zoomImageSettingsListDialogState.show() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_settings),
                    contentDescription = "settings",
                    modifier = Modifier.size(40.dp).padding(8.dp)
                )
            }
            MyDialog(zoomImageSettingsListDialogState) {
                AppSettingsList(AppSettingsPage.VIEWER)
            }

            Spacer(modifier = Modifier.size(10.dp))

            Box(
                Modifier
                    .height(40.dp)
                    .background(
                        color = photoPalette.containerColor,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                val numberText by remember {
                    derivedStateOf {
                        val number = params.startPosition + pagerState.currentPage + 1
                        "${number}/${params.totalCount}"
                    }
                }
                Text(
                    text = numberText,
                    textAlign = TextAlign.Center,
                    color = photoPalette.contentColor,
                    style = TextStyle(lineHeight = 12.sp),
                )
            }
        }
    }
}