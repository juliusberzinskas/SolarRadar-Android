package com.example.solarradarapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.solarradarapp.ui.theme.LocalAppColors

/**
 * Circular avatar that shows the technician's profile photo if available,
 * falling back to their initials on a branded background.
 *
 * Used at 34dp in the top bar (JobList) and 72dp on the Profile screen.
 */
@Composable
fun TopBarAvatar(
    displayName: String,
    photoUrl: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    fontSize: TextUnit = 13.sp,
) {
    val colors = LocalAppColors.current
    val initials = displayName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "?" }

    // Track whether the remote image failed to load so we can fall back to initials.
    // reset if photoUrl changes
    var photoError by remember(photoUrl) { mutableStateOf(false) }

    if (!photoUrl.isNullOrEmpty() && !photoError) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onError = { photoError = true },
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Surface(
            modifier = modifier.size(size),
            shape = CircleShape,
            color = colors.primaryBlue
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
