package com.example.shots

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

object ProgressIndicatorUtils {
    @Composable
    fun Loader() {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.large_circle_loading_animation))
        val progress by animateLottieCompositionAsState(composition)
        Box(modifier = Modifier.fillMaxSize()) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
//            progress = { progress },
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.2f))
                    .fillMaxSize()
            )
        }
    }

    @Composable
    fun WaitScreen() {
        Scaffold() {
            Modifier.padding(it)
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.large_circle_loading_animation))
            val progress by animateLottieCompositionAsState(composition)
            Box(modifier = Modifier.fillMaxSize()) {

                Image(
                    painterResource(R.drawable.shots_3_cropped),
                    "Shots Logo",
                    modifier = Modifier
                        .height(360.dp)
                        .aspectRatio(1f)
                        .align(Alignment.TopCenter)
                )
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
//            progress = { progress },
                    modifier = Modifier
//                        .background(Color.Black.copy(alpha = 0.2f))
                        .fillMaxWidth()
                        .padding(0.dp, 0.dp, 0.dp, 40.dp)
                        .align(Alignment.Center)
                )
                Text(
                    text = "Please wait while we create!", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 320.dp, 0.dp, 0.dp)
                        .align(Alignment.Center)
                    //padding originally 120
                )

            }
        }
    }

    @Composable
    fun IndeterminateCircularIndicator(isActive : Boolean) {
        Box(modifier = Modifier.fillMaxSize()) {
            var loading by remember { mutableStateOf(isActive) }

            if (!loading) return

            CircularProgressIndicator(
                modifier = Modifier.width(64.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}