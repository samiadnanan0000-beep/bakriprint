package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassTokens {
    val DefaultCorner = 22.dp
    val PillCorner = 100.dp
    val SmallCorner = 14.dp

    fun specularBrush(isDark: Boolean): Brush {
        return if (isDark) {
            Brush.linearGradient(
                listOf(
                    Color(0x8000F2FE), // Luminous Cyan
                    Color(0x409333EA), // Violet
                    Color(0x1000F2FE),
                    Color(0x05FFFFFF)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color(0x990284C7), // Blue specular
                    Color(0x407C3AED), // Violet
                    Color(0x200284C7),
                    Color(0x0D000000)
                )
            )
        }
    }

    fun liquidSurfaceColor(isDark: Boolean): Color {
        return if (isDark) Color(0xD9101726) else Color(0xEEFFFFFF)
    }

    fun glowBrush(): Brush {
        return Brush.horizontalGradient(
            listOf(
                Color(0xFF00F2FE),
                Color(0xFF4FACFE),
                Color(0xFF9333EA)
            )
        )
    }
}

@Composable
fun Modifier.liquidGlass(
    cornerRadius: Dp = GlassTokens.DefaultCorner,
    isDark: Boolean = isSystemInDarkTheme(),
    elevation: Dp = if (isDark) 8.dp else 4.dp
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val specularBrush = GlassTokens.specularBrush(isDark)
    val surfaceColor = GlassTokens.liquidSurfaceColor(isDark)

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color(0x6600F2FE) else Color(0x20000000),
            spotColor = if (isDark) Color(0x409333EA) else Color(0x1A000000)
        )
        .clip(shape)
        .background(surfaceColor, shape)
        .border(width = 1.2.dp, brush = specularBrush, shape = shape)
}
