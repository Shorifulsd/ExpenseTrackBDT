package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Premium Glassmorphism Theme Brushes and Colors
object GlassTheme {
    val DarkGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF080B10), // Ultra dark obsidian (bg-[#080B10])
            Color(0xFF0E131F), // Very deep slate navy
            Color(0xFF080B10)  // Ultra dark obsidian (bg-[#080B10])
        )
    )

    val LightGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FAFC), // Slate 50
            Color(0xFFF1F5F9), // Slate 100
            Color(0xFFE2E8F0)  // Slate 200
        )
    )

    @Composable
    fun glassCardBackground(isDark: Boolean = isSystemInDarkTheme()): Brush {
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x1AFFFFFF), // White with 10% opacity (bg-white/10)
                    Color(0x0DFFFFFF)  // White with 5% opacity (bg-white/5)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xE6FFFFFF), // White with 90% opacity
                    Color(0x99FFFFFF)  // White with 60% opacity
                )
            )
        }
    }

    @Composable
    fun glassBorderColor(isDark: Boolean = isSystemInDarkTheme()): Brush {
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x33FFFFFF), // White 20% opacity (border-white/20)
                    Color(0x1AFFFFFF)  // White 10% opacity (border-white/10)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x66FFFFFF), // White 40%
                    Color(0x26000000)  // Black 15%
                )
            )
        }
    }

    @Composable
    fun glassTextColor(isDark: Boolean = isSystemInDarkTheme()): Color {
        return if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    }

    @Composable
    fun glassSubTextColor(isDark: Boolean = isSystemInDarkTheme()): Color {
        return if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    }
}

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) GlassTheme.DarkGradient else GlassTheme.LightGradient)
    ) {
        // Decorative floating color blobs for realistic refraction look
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    if (isDark) {
                        // Top-left blur: indigo-600/30 (Color(0x4D4F46E5))
                        drawCircle(
                            color = Color(0x4D4F46E5),
                            radius = size.width * 0.7f,
                            center = androidx.compose.ui.geometry.Offset(size.width * -0.1f, size.height * -0.1f)
                        )
                        // Bottom-right blur: purple-600/20 (Color(0x339333EA))
                        drawCircle(
                            color = Color(0x339333EA),
                            radius = size.width * 0.6f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 1.1f, size.height * 0.9f)
                        )
                    } else {
                        drawCircle(
                            color = Color(0x3338BDF8), // Light Blue
                            radius = size.width * 0.4f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.2f)
                        )
                        drawCircle(
                            color = Color(0x26F472B6), // Light Pink
                            radius = size.width * 0.5f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.8f)
                        )
                    }
                }
        )
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val backgroundBrush = GlassTheme.glassCardBackground(isDark)
    val borderBrush = GlassTheme.glassBorderColor(isDark)

    Column(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(borderWidth, borderBrush, shape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GlassCardClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val backgroundBrush = GlassTheme.glassCardBackground(isDark)
    val borderBrush = GlassTheme.glassBorderColor(isDark)

    Column(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(borderWidth, borderBrush, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0x22FFFFFF),
        contentColor = Color.White
    ),
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val borderBrush = GlassTheme.glassBorderColor(isDark)

    Button(
        onClick = onClick,
        modifier = modifier
            .border(1.dp, borderBrush, shape),
        enabled = enabled,
        shape = shape,
        colors = colors,
        content = content
    )
}

@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun AnimatedCounter(
    value: Double,
    modifier: Modifier = Modifier,
    prefix: String = "৳ ",
    suffix: String = "",
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = GlassTheme.glassTextColor()
) {
    // Standard animated number or transition representation
    val animValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "CounterAnimation"
    )

    // Formats amount nicely like ৳ 1,25,000.00
    val formattedText = formatBDT(animValue.toDouble())

    Text(
        text = "$prefix$formattedText$suffix",
        modifier = modifier,
        style = style,
        color = color
    )
}

fun formatBDT(amount: Double): String {
    // Custom Bangladeshi digit grouping formatter (e.g. 12,34,567.89 instead of 1,234,567.89)
    val parts = String.format("%.2f", amount).split(".")
    val integerPart = parts[0]
    val decimalPart = parts[1]

    if (integerPart.length <= 3) {
        return "$integerPart.$decimalPart"
    }

    val lastThree = integerPart.substring(integerPart.length - 3)
    val remaining = integerPart.substring(0, integerPart.length - 3)

    // Regex to group remaining by two digits
    val sb = StringBuilder()
    var i = remaining.length
    while (i > 0) {
        val start = if (i - 2 < 0) 0 else i - 2
        val part = remaining.substring(start, i)
        if (sb.isNotEmpty()) {
            sb.insert(0, ",")
        }
        sb.insert(0, part)
        i -= 2
    }

    return "${sb.toString()},$lastThree.$decimalPart"
}
