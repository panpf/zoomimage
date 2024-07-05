package com.github.panpf.zoomimage.sample.ui.photoalbum.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.ui.AppSettingsDialog
import com.github.panpf.zoomimage.sample.ui.base.compose.BaseAppBarComposeFragment
import com.github.panpf.zoomimage.sample.ui.examples.compose.ZoomImageType

class PhotoPagerComposeFragment : BaseAppBarComposeFragment() {

    private val args by navArgs<PhotoPagerComposeFragmentArgs>()
    private val zoomImageType by lazy { ZoomImageType.valueOf(args.zoomImageType) }
    private val optionDialogShowViewModel by viewModels<OptionsDialogShowViewModel>()

    override fun getTitle(): String {
        return zoomImageType.title
    }

    override fun getSubtitle(): String? {
        return zoomImageType.subtitle
    }

    @Composable
    override fun RowScope.DrawActions() {
        val context = LocalContext.current
        val horizontalLayout by context.appSettings.horizontalPagerLayout
            .collectAsState(initial = true)
        IconButton(onClick = {
            context.appSettings.horizontalPagerLayout.value = !horizontalLayout
        }) {
            val meuIcon =
                if (horizontalLayout) R.drawable.ic_swap_ver else R.drawable.ic_swap_hor
            Icon(painter = painterResource(id = meuIcon), contentDescription = "Icon")
        }

        IconButton(onClick = { optionDialogShowViewModel.toggleOptionDialogShow() }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "options",
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun DrawContent() {
        val context = LocalContext.current
        val horizontalLayout by context.appSettings.horizontalPagerLayout
            .collectAsState(initial = true)
        val imageUrlList = remember { args.imageUris.split(",") }
        val pagerState = rememberPagerState(initialPage = args.position - args.startPosition) {
            imageUrlList.size
        }
        Box(modifier = Modifier.fillMaxSize()) {
            if (horizontalLayout) {
                HorizontalPager(
                    state = pagerState,
                    beyondBoundsPageCount = 0,
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    zoomImageType.drawContent(imageUrlList[index])
                }
            } else {
                VerticalPager(
                    state = pagerState,
                    beyondBoundsPageCount = 0,
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    zoomImageType.drawContent(imageUrlList[index])
                }
            }
//            PageNumber(
//                number = pagerState.currentPage + 1,
//                total = args.totalCount,
//                modifier = Modifier.align(Alignment.TopEnd)
//            )
        }

        val showingOptionsDialog by optionDialogShowViewModel.showStateFlow.collectAsState(initial = false)
        if (showingOptionsDialog) {
            AppSettingsDialog(my = zoomImageType.my) {
                optionDialogShowViewModel.toggleOptionDialogShow()
            }
        }
    }
}