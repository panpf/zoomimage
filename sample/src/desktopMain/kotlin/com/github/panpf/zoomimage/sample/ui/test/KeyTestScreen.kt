package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold

class KeyTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("Key", ignoreNavigationBarInsets = true) {

            val focusRequester = remember { FocusRequester() }
//            val focusManager = LocalFocusManager.current
//            val keyboardController = LocalSoftwareKeyboardController.current

            var keyEventList by remember { mutableStateOf(emptyList<KeyEvent>()) }
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent {
                    println("keyZoom onKeyEvent: $it")
                    keyEventList += it
                    true
                }
            ) {
                items(keyEventList.size) {
                    Text(text = keyEventList[it].toString(), modifier = Modifier.padding(20.dp))
                }
            }

//            // Request focus when the composable is first displayed
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
//                keyboardController?.hide() // Hide the software keyboard if visible
            }
        }
    }
}