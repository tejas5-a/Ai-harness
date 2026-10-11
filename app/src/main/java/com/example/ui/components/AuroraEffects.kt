package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.PureWhiteBackground
import com.example.ui.theme.SoftDecorativePeach

/**
 * AuroraBackground: Ambient lightweight background combining soft orange glow near
 * upper-right and subtle peach glow near lower-left, blending smoothly into pure white.
 *
 * Exact CSS specification:
 * background-color: #FFFFFF;
 * background-image:
 *   radial-gradient(ellipse at 95% 5%, rgba(255, 138, 0, 0.16) 0%, transparent 38%),
 *   radial-gradient(ellipse at 5% 95%, rgba(255, 190, 120, 0.13) 0%, transparent 38%);
 */
@Composable
fun AuroraBackground(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(PureWhiteBackground)
  ) {
    // Ambient Aurora Glow Canvas rendering exact radial gradients
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val diag = kotlin.math.hypot(w, h)

      // 1. Soft orange glow near upper-right (at 95% 5%, rgba(255, 138, 0, 0.16) 0%, transparent 38%)
      val r1 = diag * 0.45f
      drawCircle(
        brush = Brush.radialGradient(
          0.0f to Color(0x29FF8A00),   // rgba(255, 138, 0, 0.16)
          0.38f to Color(0x00FF8A00),  // transparent at 38%
          1.0f to Color.Transparent,
          center = Offset(w * 0.95f, h * 0.05f),
          radius = r1
        ),
        radius = r1,
        center = Offset(w * 0.95f, h * 0.05f)
      )

      // 2. Subtle peach glow near lower-left (at 5% 95%, rgba(255, 190, 120, 0.13) 0%, transparent 38%)
      val r2 = diag * 0.45f
      drawCircle(
        brush = Brush.radialGradient(
          0.0f to Color(0x21FFBE78),   // rgba(255, 190, 120, 0.13)
          0.38f to Color(0x00FFBE78),  // transparent at 38%
          1.0f to Color.Transparent,
          center = Offset(w * 0.05f, h * 0.95f),
          radius = r2
        ),
        radius = r2,
        center = Offset(w * 0.05f, h * 0.95f)
      )
    }

    content()
  }
}

/**
 * Floating3DElements: Lightweight decorative 3D-inspired glass spheres and floating
 * geometric shapes placed along non-interactive edges with slow, smooth float animation.
 */
@Composable
fun Floating3DElements(
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "floating3d")

  val floatOffset1 by infiniteTransition.animateFloat(
    initialValue = -10f,
    targetValue = 10f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 6500, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "sphere1"
  )

  val floatOffset2 by infiniteTransition.animateFloat(
    initialValue = 8f,
    targetValue = -8f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 8000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "sphere2"
  )

  Canvas(modifier = modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height

    // Top-Right Glass Sphere with 3D Specular Highlight
    val sphere1Center = Offset(w * 0.88f, h * 0.08f + floatOffset1)
    val sphere1Radius = 38f

    // Sphere Body gradient (translucent glass)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color.White.copy(alpha = 0.90f),
          SoftDecorativePeach.copy(alpha = 0.60f),
          BrandPrimaryOrange.copy(alpha = 0.25f)
        ),
        center = Offset(sphere1Center.x - 10f, sphere1Center.y - 10f),
        radius = sphere1Radius
      ),
      radius = sphere1Radius,
      center = sphere1Center
    )

    // Specular Highlight for 3D depth
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color.White.copy(alpha = 0.98f), Color.Transparent),
        center = Offset(sphere1Center.x - 12f, sphere1Center.y - 12f),
        radius = 14f
      ),
      radius = 14f,
      center = Offset(sphere1Center.x - 12f, sphere1Center.y - 12f)
    )

    // Bottom-Left Floating Glass Sphere
    val sphere2Center = Offset(w * 0.10f, h * 0.84f + floatOffset2)
    val sphere2Radius = 46f

    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color.White.copy(alpha = 0.88f),
          SoftDecorativePeach.copy(alpha = 0.50f),
          BrandPrimaryOrange.copy(alpha = 0.20f)
        ),
        center = Offset(sphere2Center.x - 12f, sphere2Center.y - 12f),
        radius = sphere2Radius
      ),
      radius = sphere2Radius,
      center = sphere2Center
    )

    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color.White.copy(alpha = 0.95f), Color.Transparent),
        center = Offset(sphere2Center.x - 14f, sphere2Center.y - 14f),
        radius = 16f
      ),
      radius = 16f,
      center = Offset(sphere2Center.x - 14f, sphere2Center.y - 14f)
    )

    // Small subtle geometric diamond shape at right edge
    val diamondCenter = Offset(w * 0.94f, h * 0.45f + floatOffset1 * 0.6f)
    val dRadius = 14f
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(BrandPrimaryOrange.copy(alpha = 0.28f), Color.Transparent),
        center = diamondCenter,
        radius = dRadius
      ),
      radius = dRadius,
      center = diamondCenter
    )
  }
}
