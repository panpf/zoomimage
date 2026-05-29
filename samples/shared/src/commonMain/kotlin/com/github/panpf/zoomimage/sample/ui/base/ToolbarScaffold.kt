package com.github.panpf.zoomimage.sample.ui.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.ui.LocalNavBackStack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarScaffold(
    title: String,
    addNavigationBarWindowInsets: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    BaseScreen {
        val navBackStack = LocalNavBackStack.current
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { navBackStack.removeLastOrNull() }
                            .padding(14.dp),
                    )
                }
            )
            Box(
                Modifier.fillMaxWidth()
                    .weight(1f)
                    .let {
                        if (addNavigationBarWindowInsets)
                            it.windowInsetsPadding(WindowInsets.navigationBars) else it
                    }
            ) {
                content()
            }
        }
    }
}