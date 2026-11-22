package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.api.AiLogoStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AiModelLogo(
    style: AiLogoStyle,
    size: Dp = 38.dp,
    modifier: Modifier = Modifier
) {
    val bgBrush = when (style) {
        AiLogoStyle.OPENAI_GPT -> Brush.linearGradient(listOf(Color(0xFF10A37F), Color(0xFF0D7A5F)))
        AiLogoStyle.OPENAI_EXTRA -> Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF10A37F)))
        AiLogoStyle.RAUF_CROWN -> Brush.linearGradient(listOf(Color(0xFF3969EF), Color(0xFF6D28D9)))
        AiLogoStyle.RAUF_MINI -> Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4)))
        AiLogoStyle.CLAUDE_SPARK -> Brush.linearGradient(listOf(Color(0xFFD97757), Color(0xFFB45309)))
        AiLogoStyle.GEMINI_STAR -> Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))
        AiLogoStyle.LLAMA_LOOP -> Brush.linearGradient(listOf(Color(0xFF0668E1), Color(0xFF2563EB)))
        AiLogoStyle.CRESCENT -> Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF047857)))
        AiLogoStyle.LOTUS -> Brush.linearGradient(listOf(Color(0xFFEA580C), Color(0xFFF59E0B)))
        AiLogoStyle.SCRIPTURE -> Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
        AiLogoStyle.VISION_LENS -> Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF6366F1)))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val r = this.size.minDimension * 0.42f
            val strokeW = this.size.minDimension * 0.11f

            when (style) {
                AiLogoStyle.OPENAI_GPT, AiLogoStyle.OPENAI_EXTRA -> {
                    val accent = if (style == AiLogoStyle.OPENAI_EXTRA) Color(0xFFFDE047) else Color.White
                    // 6 interlocking hexagonal rosette loops inspired by ChatGPT
                    for (i in 0 until 6) {
                        val angle = (i * 60.0) * PI / 180.0
                        val px = cx + (r * 0.45f) * cos(angle).toFloat()
                        val py = cy + (r * 0.45f) * sin(angle).toFloat()
                        drawCircle(
                            color = accent,
                            radius = r * 0.46f,
                            center = Offset(px, py),
                            style = Stroke(width = strokeW * 0.85f)
                        )
                    }
                    if (style == AiLogoStyle.OPENAI_EXTRA) {
                        drawCircle(color = Color.White, radius = r * 0.18f, center = Offset(cx, cy))
                    }
                }

                AiLogoStyle.RAUF_CROWN -> {
                    val path = Path().apply {
                        moveTo(cx - r, cy + r * 0.55f)
                        lineTo(cx - r * 0.85f, cy - r * 0.35f)
                        lineTo(cx - r * 0.25f, cy + r * 0.1f)
                        lineTo(cx, cy - r * 0.72f)
                        lineTo(cx + r * 0.25f, cy + r * 0.1f)
                        lineTo(cx + r * 0.85f, cy - r * 0.35f)
                        lineTo(cx + r, cy + r * 0.55f)
                        close()
                    }
                    drawPath(path, color = Color.White, style = Stroke(width = strokeW, cap = StrokeCap.Round))
                }

                AiLogoStyle.RAUF_MINI -> {
                    val path = Path().apply {
                        moveTo(cx + r * 0.15f, cy - r)
                        lineTo(cx - r * 0.65f, cy + r * 0.1f)
                        lineTo(cx + r * 0.05f, cy + r * 0.1f)
                        lineTo(cx - r * 0.15f, cy + r)
                        lineTo(cx + r * 0.65f, cy - r * 0.1f)
                        lineTo(cx - r * 0.05f, cy - r * 0.1f)
                        close()
                    }
                    drawPath(path, color = Color.White)
                }

                AiLogoStyle.CLAUDE_SPARK -> {
                    // 8-ray sunburst emblem
                    for (i in 0 until 8) {
                        val a = (i * 45.0) * PI / 180.0
                        val len = if (i % 2 == 0) r else r * 0.68f
                        drawLine(
                            color = Color.White,
                            start = Offset(cx, cy),
                            end = Offset(cx + len * cos(a).toFloat(), cy + len * sin(a).toFloat()),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                    }
                }

                AiLogoStyle.GEMINI_STAR -> {
                    // Crisp 4-point star sparkle
                    val path = Path().apply {
                        moveTo(cx, cy - r)
                        quadraticTo(cx, cy, cx + r, cy)
                        quadraticTo(cx, cy, cx, cy + r)
                        quadraticTo(cx, cy, cx - r, cy)
                        quadraticTo(cx, cy, cx, cy - r)
                        close()
                    }
                    drawPath(path, color = Color.White)
                }

                AiLogoStyle.LLAMA_LOOP -> {
                    drawCircle(
                        color = Color.White,
                        radius = r * 0.48f,
                        center = Offset(cx - r * 0.45f, cy),
                        style = Stroke(width = strokeW)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = r * 0.48f,
                        center = Offset(cx + r * 0.45f, cy),
                        style = Stroke(width = strokeW)
                    )
                }

                AiLogoStyle.CRESCENT -> {
                    drawCircle(color = Color.White, radius = r * 0.8f, center = Offset(cx, cy))
                    drawCircle(color = Color(0xFF047857), radius = r * 0.65f, center = Offset(cx + r * 0.35f, cy - r * 0.15f))
                }

                AiLogoStyle.LOTUS -> {
                    for (i in -1..1) {
                        val angle = (-90 + i * 32) * PI / 180.0
                        drawLine(
                            color = Color.White,
                            start = Offset(cx, cy + r * 0.6f),
                            end = Offset(cx + r * 0.95f * cos(angle).toFloat(), cy + r * 0.95f * sin(angle).toFloat()),
                            strokeWidth = strokeW * 1.15f,
                            cap = StrokeCap.Round
                        )
                    }
                    drawLine(
                        color = Color.White,
                        start = Offset(cx - r * 0.7f, cy + r * 0.65f),
                        end = Offset(cx + r * 0.7f, cy + r * 0.65f),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }

                AiLogoStyle.SCRIPTURE -> {
                    drawLine(color = Color.White, start = Offset(cx, cy - r * 0.8f), end = Offset(cx, cy + r * 0.8f), strokeWidth = strokeW, cap = StrokeCap.Round)
                    drawLine(color = Color.White, start = Offset(cx - r * 0.7f, cy - r * 0.2f), end = Offset(cx + r * 0.7f, cy - r * 0.2f), strokeWidth = strokeW, cap = StrokeCap.Round)
                }

                AiLogoStyle.VISION_LENS -> {
                    drawCircle(color = Color.White, radius = r * 0.85f, center = Offset(cx, cy), style = Stroke(width = strokeW))
                    drawCircle(color = Color.White, radius = r * 0.32f, center = Offset(cx, cy))
                }
            }
        }
    }
}
