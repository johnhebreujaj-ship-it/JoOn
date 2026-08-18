package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcBlue
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcTeal
import com.example.ui.theme.StatusOffline
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    isOnline: Boolean,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorAnimations")

    val outerRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isProcessing) 3000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRotation"
    )

    val innerRotation = infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isProcessing) 2000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRotation"
    )

    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = if (isSpeaking || isProcessing) 1.15f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 400 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isSpeaking || isProcessing) 0.95f else 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isProcessing) 500 else 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val primaryColor = if (isOnline) ArcCyan else StatusOffline
    val secondaryColor = if (isOnline) ArcTeal else ArcBlue

    Box(
        modifier = modifier
            .size(size)
            .testTag("arc_reactor_core")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val baseRadius = this.size.minDimension / 2 * 0.9f

            // 1. Central Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = glowAlpha.value * 0.45f),
                        primaryColor.copy(alpha = glowAlpha.value * 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * pulseScale.value
                ),
                radius = baseRadius * pulseScale.value,
                center = center
            )

            // 2. Outermost Tech Ring with Dash Marks
            rotate(outerRotation.value, pivot = center) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = baseRadius * 0.95f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Outer Segmented Arcs (12 sectors)
                val segments = 12
                for (i in 0 until segments) {
                    val startAngle = i * (360f / segments)
                    drawArc(
                        color = primaryColor.copy(alpha = if (i % 2 == 0) 0.8f else 0.3f),
                        startAngle = startAngle,
                        sweepAngle = 18f,
                        useCenter = false,
                        topLeft = Offset(center.x - baseRadius * 0.95f, center.y - baseRadius * 0.95f),
                        size = androidx.compose.ui.geometry.Size(baseRadius * 1.9f, baseRadius * 1.9f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Cyber satellite nodes
                for (i in 0 until 4) {
                    val angle = Math.toRadians((i * 90.0))
                    val nx = center.x + (baseRadius * 0.95f) * cos(angle).toFloat()
                    val ny = center.y + (baseRadius * 0.95f) * sin(angle).toFloat()
                    drawCircle(
                        color = primaryColor,
                        radius = 3.dp.toPx(),
                        center = Offset(nx, ny)
                    )
                }
            }

            // 3. Middle Gyroscopic Ring
            rotate(innerRotation.value, pivot = center) {
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.4f),
                    radius = baseRadius * 0.72f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Triangular / Hexagonal Core nodes
                val points = 6
                val nodePoints = mutableListOf<Offset>()
                for (i in 0 until points) {
                    val angle = Math.toRadians((i * (360.0 / points)))
                    val px = center.x + (baseRadius * 0.72f) * cos(angle).toFloat()
                    val py = center.y + (baseRadius * 0.72f) * sin(angle).toFloat()
                    nodePoints.add(Offset(px, py))
                }

                drawPoints(
                    points = nodePoints,
                    pointMode = PointMode.Points,
                    color = primaryColor,
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Inner Reactor Core
            val coreRadius = baseRadius * 0.45f * pulseScale.value
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        primaryColor.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // Inner Core Border Ring
            drawCircle(
                color = Color.White,
                radius = coreRadius * 0.7f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Dynamic Voice Waveform Spokes
            val spokes = if (isSpeaking || isProcessing) 16 else 8
            for (i in 0 until spokes) {
                val angle = Math.toRadians((i * (360.0 / spokes) + outerRotation.value * 0.5))
                val spokeLen = if (isSpeaking) (10..22).random().dp.toPx() else 8.dp.toPx()
                val startX = center.x + (coreRadius * 0.7f) * cos(angle).toFloat()
                val startY = center.y + (coreRadius * 0.7f) * sin(angle).toFloat()
                val endX = center.x + (coreRadius * 0.7f + spokeLen) * cos(angle).toFloat()
                val endY = center.y + (coreRadius * 0.7f + spokeLen) * sin(angle).toFloat()

                drawLine(
                    color = primaryColor.copy(alpha = 0.9f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
