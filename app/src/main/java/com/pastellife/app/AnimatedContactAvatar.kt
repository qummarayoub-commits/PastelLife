package com.pastellife.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val avatarPalette = listOf(
    Color(0xFFE6D9FF), Color(0xFFFFDDE8), Color(0xFFD6F4E5),
    Color(0xFFFFE8B8), Color(0xFFD8E9FF), Color(0xFFFFDCCB),
    Color(0xFFE1E7FF), Color(0xFFD4F3F0)
)

@Composable
fun AnimatedContactAvatar(name: String, modifier: Modifier = Modifier) {
    val seed = name.trim().lowercase().hashCode().let { kotlin.math.abs(it % avatarPalette.size) }
    val background = avatarPalette[seed]
    val pulse by rememberInfiniteTransition(label = "contact-avatar").animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800 + seed * 90),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar-pulse"
    )
    val initials = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        .take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }

    Box(modifier = modifier.size(54.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxSize().scale(pulse),
            shape = CircleShape,
            color = background,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(initials, color = Color(0xFF373044), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFF8C6CCB),
            modifier = Modifier.align(Alignment.TopEnd).size(15.dp)
        )
    }
}
