package com.cornerkick.planner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cornerkick.planner.data.model.CornerArrow
import com.cornerkick.planner.data.model.CornerMarker
import com.cornerkick.planner.data.model.MarkerType
import com.cornerkick.planner.data.model.TeamColorRole
import com.cornerkick.planner.ui.theme.AttackAccent
import com.cornerkick.planner.ui.theme.DefenseAccent
import com.cornerkick.planner.ui.theme.PitchGreen
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * A simple, stable top-down corner-area tactical board rendered with Canvas.
 * Goal is at the top. Coordinates are relative (0f..1f). When [onTap] is
 * provided the board reports the tapped relative position (used for tap-to-place).
 */
@Composable
fun CornerBoard(
    markers: List<CornerMarker>,
    arrows: List<CornerArrow>,
    modifier: Modifier = Modifier,
    onTap: ((Float, Float) -> Unit)? = null,
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.82f)
                .then(
                    if (onTap != null) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val rx = (offset.x / size.width).coerceIn(0f, 1f)
                                val ry = (offset.y / size.height).coerceIn(0f, 1f)
                                onTap(rx, ry)
                            }
                        }
                    } else Modifier
                )
        ) {
            drawPitch()
            arrows.forEach { drawArrow(it) }
            markers.forEach { drawMarker(it, textMeasurer) }
        }
    }
}

private fun DrawScope.px(x: Float, y: Float): Offset =
    Offset(x * size.width, y * size.height)

private fun DrawScope.drawPitch() {
    val line = Color.White
    val lineW = size.minDimension * 0.006f

    // Grass background.
    drawRect(color = PitchGreen, size = size)

    // Subtle mow stripes.
    val stripe = Color(0x14FFFFFF)
    val stripeCount = 6
    val stripeH = size.height / stripeCount
    for (i in 0 until stripeCount step 2) {
        drawRect(
            color = stripe,
            topLeft = Offset(0f, i * stripeH),
            size = Size(size.width, stripeH),
        )
    }

    // Goal line (top) and outer boundary.
    drawRect(
        color = line,
        topLeft = Offset(lineW, lineW),
        size = Size(size.width - lineW * 2, size.height - lineW * 2),
        style = Stroke(width = lineW),
    )

    // Goal frame (top center).
    val goalW = size.width * 0.20f
    val goalH = size.height * 0.022f
    drawRect(
        color = line,
        topLeft = Offset(size.width / 2 - goalW / 2, 0f),
        size = Size(goalW, goalH),
        style = Stroke(width = lineW * 1.2f),
    )

    // Six-yard box.
    val sixW = size.width * 0.34f
    val sixH = size.height * 0.11f
    drawRect(
        color = line,
        topLeft = Offset(size.width / 2 - sixW / 2, 0f),
        size = Size(sixW, sixH),
        style = Stroke(width = lineW),
    )

    // Penalty box.
    val penW = size.width * 0.66f
    val penH = size.height * 0.42f
    drawRect(
        color = line,
        topLeft = Offset(size.width / 2 - penW / 2, 0f),
        size = Size(penW, penH),
        style = Stroke(width = lineW),
    )

    // Penalty spot.
    drawCircle(
        color = line,
        radius = lineW * 1.4f,
        center = px(0.5f, 0.30f),
    )

    // Penalty arc ("D") at the bottom edge of the box.
    val arcRadius = size.width * 0.18f
    val arcCenter = px(0.5f, 0.30f)
    drawArc(
        color = line,
        startAngle = 25f,
        sweepAngle = 130f,
        useCenter = false,
        topLeft = Offset(arcCenter.x - arcRadius, arcCenter.y - arcRadius),
        size = Size(arcRadius * 2, arcRadius * 2),
        style = Stroke(width = lineW),
    )

    // Corner arcs (top-left and top-right).
    val cornerR = size.width * 0.06f
    drawArc(
        color = line,
        startAngle = 0f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(-cornerR, -cornerR),
        size = Size(cornerR * 2, cornerR * 2),
        style = Stroke(width = lineW),
    )
    drawArc(
        color = line,
        startAngle = 90f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(size.width - cornerR, -cornerR),
        size = Size(cornerR * 2, cornerR * 2),
        style = Stroke(width = lineW),
    )
}

private fun markerColor(marker: CornerMarker): Color = when (marker.type) {
    MarkerType.Attacker -> AttackAccent
    MarkerType.Defender -> DefenseAccent
    MarkerType.Goalkeeper -> Color(0xFF16A34A)
    MarkerType.Ball -> Color.White
    MarkerType.Target -> Color(0xFFFACC15)
}

private fun DrawScope.drawMarker(marker: CornerMarker, textMeasurer: TextMeasurer) {
    val center = px(marker.x, marker.y)
    val radius = size.minDimension * 0.045f
    val fill = markerColor(marker)

    when (marker.type) {
        MarkerType.Ball -> {
            drawCircle(color = Color.White, radius = radius * 0.7f, center = center)
            drawCircle(color = Color.Black, radius = radius * 0.7f, center = center, style = Stroke(width = radius * 0.18f))
        }
        MarkerType.Target -> {
            // Target = ringed marker.
            drawCircle(color = fill, radius = radius, center = center, style = Stroke(width = radius * 0.3f))
            drawCircle(color = fill, radius = radius * 0.35f, center = center)
        }
        else -> {
            drawCircle(color = Color.Black, radius = radius * 1.12f, center = center)
            drawCircle(color = fill, radius = radius, center = center)
        }
    }

    val label = marker.label.trim()
    if (label.isNotEmpty()) {
        val textColor = if (marker.type == MarkerType.Defender) Color.White else Color.Black
        val measured = textMeasurer.measure(
            text = label.take(8),
            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor),
        )
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                center.x - measured.size.width / 2f,
                center.y - measured.size.height / 2f,
            ),
        )
    }
}

private fun DrawScope.drawArrow(arrow: CornerArrow) {
    val start = px(arrow.startX, arrow.startY)
    val end = px(arrow.endX, arrow.endY)
    val strokeW = size.minDimension * 0.012f
    val color = Color(0xFFFFFFFF)

    val dist = hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble()).toFloat()
    if (dist < 1f) {
        drawCircle(color = color, radius = strokeW, center = start)
        return
    }

    when (arrow.type) {
        com.cornerkick.planner.data.model.ArrowType.Pass,
        com.cornerkick.planner.data.model.ArrowType.Cross -> {
            // Dashed style for ball movement.
            drawDashedLine(start, end, color, strokeW)
        }
        com.cornerkick.planner.data.model.ArrowType.DummyMove -> {
            drawDashedLine(start, end, Color(0xFFFACC15), strokeW)
        }
        com.cornerkick.planner.data.model.ArrowType.PressingArrow -> {
            drawLine(Color(0xFFEF4444), start, end, strokeWidth = strokeW)
        }
        else -> {
            drawLine(color, start, end, strokeWidth = strokeW)
        }
    }

    // Arrow head.
    val headColor = when (arrow.type) {
        com.cornerkick.planner.data.model.ArrowType.DummyMove -> Color(0xFFFACC15)
        com.cornerkick.planner.data.model.ArrowType.PressingArrow -> Color(0xFFEF4444)
        else -> color
    }
    val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
    val headLen = size.minDimension * 0.045f
    val a1 = angle - Math.PI / 7
    val a2 = angle + Math.PI / 7
    val p1 = Offset(end.x - (headLen * cos(a1)).toFloat(), end.y - (headLen * sin(a1)).toFloat())
    val p2 = Offset(end.x - (headLen * cos(a2)).toFloat(), end.y - (headLen * sin(a2)).toFloat())
    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }
    drawPath(path, headColor)
}

private fun DrawScope.drawDashedLine(start: Offset, end: Offset, color: Color, strokeW: Float) {
    val total = hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble()).toFloat()
    val dash = size.minDimension * 0.03f
    val gap = dash * 0.7f
    val step = dash + gap
    val dirX = (end.x - start.x) / total
    val dirY = (end.y - start.y) / total
    var traveled = 0f
    while (traveled < total) {
        val segEnd = (traveled + dash).coerceAtMost(total)
        val s = Offset(start.x + dirX * traveled, start.y + dirY * traveled)
        val e = Offset(start.x + dirX * segEnd, start.y + dirY * segEnd)
        drawLine(color, s, e, strokeWidth = strokeW)
        traveled += step
    }
}

/** Marker legend colors used by screens for chips. */
object MarkerColors {
    fun forType(type: MarkerType): Color = when (type) {
        MarkerType.Attacker -> AttackAccent
        MarkerType.Defender -> DefenseAccent
        MarkerType.Goalkeeper -> Color(0xFF16A34A)
        MarkerType.Ball -> Color(0xFF9CA3AF)
        MarkerType.Target -> Color(0xFFFACC15)
    }

    fun forRole(role: TeamColorRole): Color = when (role) {
        TeamColorRole.Attack -> AttackAccent
        TeamColorRole.Defense -> DefenseAccent
        TeamColorRole.Neutral -> Color(0xFF9CA3AF)
    }
}

// Kept for potential reuse; avoids an unused-import warning on Rect.
@Suppress("unused")
private fun DrawScope.boundsRect(): Rect = Rect(Offset.Zero, size)
