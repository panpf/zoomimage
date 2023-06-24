/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.zoomimage.sample.ui.base.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.github.panpf.tools4a.dimen.ktx.px2dp
import com.github.panpf.tools4a.display.ktx.getStatusBarHeight
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.base.compose.theme.MyTheme

abstract class AppBarFragment : Fragment() {

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context).apply {
            setContent {
                MyTopAppBarScaffold3(getTitle(), getSubtitle()) {
                    DrawContent()
                }
            }
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    open fun getTitle(): String? {
        return requireContext().resources.getString(R.string.app_name)
    }

    open fun getSubtitle(): String? = null

    @Composable
    abstract fun DrawContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyTopAppBarScaffold3(
    title: String? = null,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val statusBarHeight = remember {
        context.getStatusBarHeight().px2dp.dp
    }
    MyTheme {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {
                val colorScheme = MaterialTheme.colorScheme
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(statusBarHeight)
                        .background(colorScheme.primary)
                )
                if (title != null) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(text = title)
                                if (subtitle != null) {
                                    Text(text = subtitle, fontSize = 16.sp)
                                }
                            }
                        },
                        windowInsets = WindowInsets(0.dp),
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colorScheme.primary,
                            scrolledContainerColor = colorScheme.primary,
                            navigationIconContentColor = colorScheme.onPrimary,
                            titleContentColor = colorScheme.onPrimary,
                            actionIconContentColor = colorScheme.onPrimary,
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    content()
                }
            }
        }
    }
}

@Preview
@Composable
private fun MyTopAppBarScaffold3Preview() {
    MyTopAppBarScaffold3("Sample") {

    }
}