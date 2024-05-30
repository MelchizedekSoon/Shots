package com.example.shots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.data.TypeOfMedia
import com.example.shots.data.User

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileMediaDisplay(media: String?, typeOfMedia: TypeOfMedia?, showSnackbar: () -> Unit) {
    Card(
        modifier = Modifier.height(600.dp),
        colors = CardColors(
            containerColor = Color.White,
            contentColor = Color.Black, disabledContentColor = Color.Red,
            disabledContainerColor = Color.Red
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (typeOfMedia == TypeOfMedia.VIDEO) {
                media?.toUri()
                    ?.let { videoPlayer(videoUri = it, maxDuration = 15_000L, showSnackbar) }
            } else {
                GlideImage(
                    model = media?.toUri(),
                    contentDescription = "$media",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileUserCardDisplay(user: User, showSnackbar: () -> Unit, navToUserProfile: () -> Unit) {
    // Display card content
    val contrast = 0.8f // 0f..10f (1 should be default)
    val brightness = 0f // -255f..255f (0 should be default)
    val colorMatrix = floatArrayOf(
        contrast, 0f, 0f, 0f, brightness,
        0f, contrast, 0f, 0f, brightness,
        0f, 0f, contrast, 0f, brightness,
        0f, 0f, 0f, 1f, 0f
    )
    if (user.typeOfMediaOne == TypeOfMedia.VIDEO) {
        val uri = user.mediaOne?.toUri()
        if (uri != null) {
            videoPlayerForUserCard(
                videoUri = uri,
                maxDuration = 15_000L,
                showSnackbar,
                navToUserProfile
            )
        }
    } else {
        GlideImage(
            user.mediaOne,
            "User's card for dating profile: ${user.displayName}\"",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix(colorMatrix))
        )
    }
}

@Composable
fun PlaceholderImage(modifier: Modifier = Modifier, user : User?) {
    Box(modifier = Modifier.fillMaxSize()) {
        Icon(
            painterResource(id = R.drawable.no_image_svgrepo_com),
            "No Video Icon",
            modifier = Modifier
                .height(280.dp)
                .width(280.dp)
                .align(Alignment.TopCenter)
                .padding(0.dp, 200.dp, 0.dp, 0.dp)
        )
        Text(
            user?.displayName + " has no profile video so is unverified. ",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(0.dp, 80.dp, 0.dp, 0.dp)
        )
    }
    // Placeholder image implementation
}