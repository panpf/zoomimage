package com.github.panpf.zoomimage.sample.ui.test.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.viewModels
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.compose.ui.ZoomImageOptionsDialog
import com.github.panpf.zoomimage.sample.ui.base.compose.AppBarFragment
import com.github.panpf.zoomimage.sample.ui.common.compose.HorizontalTabPager
import com.github.panpf.zoomimage.sample.ui.common.compose.PagerItem
import com.github.panpf.zoomimage.sample.ui.examples.compose.ZoomImageSample
import com.github.panpf.zoomimage.sample.ui.examples.compose.ZoomImageType
import com.github.panpf.zoomimage.sample.ui.examples.compose.rememberZoomImageOptionsState
import com.github.panpf.zoomimage.sample.ui.photoalbum.compose.OptionsDialogShowViewModel
import com.github.panpf.zoomimage.sample.ui.test.view.ExifOrientationTestContentViewModel

class ComposeExifOrientationTestFragment : AppBarFragment() {

    override fun getTitle(): String = "ExifOrientation Test"

    override fun getSubtitle(): String = "Compose"

    private val exifOrientationTestContentViewModel by viewModels<ExifOrientationTestContentViewModel>()
    private val optionDialogShowViewModel by viewModels<OptionsDialogShowViewModel>()

    @Composable
    override fun RowScope.DrawActions() {
        IconButton(onClick = { optionDialogShowViewModel.toggleOptionDialogShow() }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Options",
            )
        }
    }

    @Composable
    override fun DrawContent() {
        val showContent by exifOrientationTestContentViewModel.showContentState.collectAsState()
        val items = remember(showContent) {
            showContent.map {
                PagerItem(
                    data = it,
                    titleFactory = { data ->
                        data.first
                    },
                    contentFactory = { data, _ ->
                        ZoomImageSample(sketchImageUri = data.second.toString())
                    }
                )
            }.toTypedArray()
        }
        HorizontalTabPager(pagerItems = items)

        val showingOptionsDialog by optionDialogShowViewModel.showStateFlow.collectAsState(initial = false)
        if (showingOptionsDialog) {
            ZoomImageOptionsDialog(
                my = ZoomImageType.MyZoomImage.my,
                supportIgnoreExifOrientation = ZoomImageType.MyZoomImage.supportIgnoreExifOrientation,
                state = rememberZoomImageOptionsState()
            ) {
                optionDialogShowViewModel.toggleOptionDialogShow()
            }
        }
    }
}