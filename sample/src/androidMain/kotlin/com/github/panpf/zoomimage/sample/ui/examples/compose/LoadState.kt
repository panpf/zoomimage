package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.R

@Composable
fun LoadState(loadState: MyLoadState) {
    Box(Modifier.fillMaxSize()) {
        if (loadState is MyLoadState.Loading) {
            CircularProgressIndicator(
                Modifier
                    .align(Alignment.Center)
                    .size(50.dp)
            )
        } else if (loadState is MyLoadState.Error) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(200.dp)
                    .background(Color(0xEE2E2E2E), RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_error_baseline),
                    contentDescription = "icon",
                    tint = Color.White
                )

                Spacer(modifier = Modifier.size(6.dp))
                Text(text = "Display failure", color = Color.White)

                if (loadState.retry != null) {
                    Spacer(modifier = Modifier.size(24.dp))
                    Button(
                        onClick = { loadState.retry.invoke() },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(text = "Retry")
                    }
                }
            }
        }
    }
}

sealed interface MyLoadState {
    data object None : MyLoadState
    data object Loading : MyLoadState
    data class Error(val retry: (() -> Unit)? = null) : MyLoadState
}

@Preview
@Composable
fun LoadStatePreview1() {
    LoadState(MyLoadState.Loading)
}

@Preview
@Composable
fun LoadStatePreview2() {
    LoadState(MyLoadState.Error {})
}