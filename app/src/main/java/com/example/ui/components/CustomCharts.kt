package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlassPieChart(
    data: Map<String, Double>,
    colors: Map<String, Color>,
    modifier: Modifier = Modifier,
    centerLabel: String = "Expenses"
) {
    val total = data.values.sum()
    if (total == 0.0) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No Data Available",
                color = GlassTheme.glassSubTextColor(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    // Animation progress
    val animateProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "PieChartProgress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            val width = size.width
            val height = size.height
            val minSize = kotlin.math.min(width, height)
            val strokeWidth = minSize * 0.15f
            val outerRadius = minSize / 2f
            val innerRadius = outerRadius - strokeWidth
            val rectSize = minSize - strokeWidth
            val offset = (width - rectSize) / 2f
            val rect = androidx.compose.ui.geometry.Rect(offset, (height - rectSize) / 2f, offset + rectSize, (height - rectSize) / 2f + rectSize)

            var startAngle = -90f
            data.forEach { (category, value) ->
                val sweepAngle = ((value / total) * 360f).toFloat() * animateProgress
                val color = colors[category] ?: Color.Gray

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }

        // Inner centered glass element with values
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                color = GlassTheme.glassSubTextColor(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "৳" + formatBDT(total),
                color = GlassTheme.glassTextColor(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun GlassLineChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF818CF8) // Indigo primary
) {
    if (values.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No Trend Data",
                color = GlassTheme.glassSubTextColor(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val maxVal = values.maxOrNull() ?: 1.0
    val minVal = values.minOrNull() ?: 0.0
    val range = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal

    val animateProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "LineChartProgress"
    )

    Canvas(modifier = modifier.padding(16.dp)) {
        val width = size.width
        val height = size.height
        val paddingLeft = 30.dp.toPx()
        val paddingBottom = 20.dp.toPx()
        val paddingTop = 10.dp.toPx()
        val chartWidth = width - paddingLeft
        val chartHeight = height - paddingBottom - paddingTop

        val stepX = if (values.size > 1) chartWidth / (values.size - 1) else chartWidth

        // Draw horizontal grid lines
        val gridCount = 4
        for (g in 0..gridCount) {
            val y = paddingTop + chartHeight - (g * chartHeight / gridCount)
            drawLine(
                color = Color(0x1AFFFFFF),
                start = Offset(paddingLeft, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Generate points
        val points = values.mapIndexed { idx, v ->
            val ratio = ((v - minVal) / range).toFloat()
            val x = paddingLeft + (idx * stepX)
            val y = paddingTop + chartHeight - (ratio * chartHeight * animateProgress)
            Offset(x, y)
        }

        if (points.isNotEmpty()) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    // Smooth cubic curves instead of rigid straight lines
                    val prev = points[i - 1]
                    val curr = points[i]
                    cubicTo(
                        (prev.x + curr.x) / 2, prev.y,
                        (prev.x + curr.x) / 2, curr.y,
                        curr.x, curr.y
                    )
                }
            }

            // Draw line gradient background
            val fillPath = Path().apply {
                addPath(path)
                lineTo(points.last().x, paddingTop + chartHeight)
                lineTo(points.first().x, paddingTop + chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                )
            )

            // Draw line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw glow dots
            points.forEach { pt ->
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = lineColor,
                    radius = 6.dp.toPx(),
                    center = pt,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun GlassBarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF10B981) // Emerald Green
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No Data Available",
                color = GlassTheme.glassSubTextColor(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val maxVal = data.maxOf { it.second }
    val range = if (maxVal == 0.0) 1.0 else maxVal

    val animateProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "BarChartProgress"
    )

    val textMeasurer = rememberTextMeasurer()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Canvas(modifier = modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
        val width = size.width
        val height = size.height
        val paddingBottom = 24.dp.toPx()
        val paddingTop = 8.dp.toPx()
        val chartHeight = height - paddingBottom - paddingTop
        val barCount = data.size
        val gap = 16.dp.toPx()
        val totalGaps = gap * (barCount - 1)
        val barWidth = (width - totalGaps) / barCount

        data.forEachIndexed { idx, pair ->
            val value = pair.second
            val label = pair.first
            val barHeight = ((value / range) * chartHeight).toFloat() * animateProgress

            val x = idx * (barWidth + gap)
            val y = paddingTop + chartHeight - barHeight

            // Gradient for bar
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    barColor,
                    barColor.copy(alpha = 0.4f)
                )
            )

            // Draw rounded bar
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Measure & Draw small text labels at the bottom
            val textLayoutResult = textMeasurer.measure(
                text = label,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 10.sp,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            )

            val textX = x + (barWidth - textLayoutResult.size.width) / 2
            val textY = paddingTop + chartHeight + 4.dp.toPx()

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(textX, textY)
            )
        }
    }
}
