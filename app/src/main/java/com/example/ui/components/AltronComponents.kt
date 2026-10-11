package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.AltronOrange
import com.example.ui.theme.AltronOrangeBright
import com.example.ui.theme.AltronOrangeGlow
import com.example.ui.theme.AltronOrangeRim
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.BorderColor
import com.example.ui.theme.Graphite700
import com.example.ui.theme.Graphite800
import com.example.ui.theme.Graphite850
import com.example.ui.theme.Graphite950
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed

@Composable
fun AltronGlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(16.dp),
  hasGlowBorder: Boolean = false,
  backgroundColor: Color = MaterialTheme.colorScheme.surface,
  borderColor: Color = if (hasGlowBorder) PrimaryOrange.copy(alpha = 0.5f) else BorderColor,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val cardModifier = if (onClick != null) {
    modifier
      .clip(shape)
      .clickable(onClick = onClick)
  } else {
    modifier.clip(shape)
  }

  Surface(
    modifier = cardModifier
      .border(
        width = if (hasGlowBorder) 1.5.dp else 1.dp,
        color = borderColor,
        shape = shape
      ),
    shape = shape,
    color = backgroundColor,
    tonalElevation = 0.dp
  ) {
    content()
  }
}

@Composable
fun AltronPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  isLoading: Boolean = false
) {
  Button(
    onClick = onClick,
    modifier = modifier.defaultMinSize(minHeight = 48.dp),
    enabled = enabled && !isLoading,
    colors = ButtonDefaults.buttonColors(
      containerColor = PrimaryOrange,
      contentColor = Color.White
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Text(
        text = if (isLoading) "Processing..." else text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
    )
  }
}

@Composable
fun AltronSecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier.defaultMinSize(minHeight = 48.dp),
    enabled = enabled,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
      contentColor = MaterialTheme.colorScheme.onSurface
    ),
    shape = RoundedCornerShape(12.dp),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      )
    }
  }
}

@Composable
fun StatusBadge(
  text: String,
  color: Color = AltronOrange,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
      .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
      text = text,
      color = color,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )
  }
}

@Composable
fun PulsingAgentStatus(
  isThinking: Boolean,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
  )

  Row(
    modifier = modifier
      .background(
        if (isThinking) AltronOrange.copy(alpha = 0.2f) else TerminalGreen.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp)
      )
      .border(
        1.dp,
        if (isThinking) AltronOrange.copy(alpha = alpha) else TerminalGreen.copy(alpha = 0.5f),
        RoundedCornerShape(20.dp)
      )
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .background(
          if (isThinking) AltronOrange.copy(alpha = alpha) else TerminalGreen,
          shape = CircleShape
        )
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = if (isThinking) "Agent Reasoning..." else "Agent Ready",
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = if (isThinking) AltronOrange else TerminalGreen
    )
  }
}

@Composable
fun ApprovalDialog(
  title: String,
  message: String,
  commandOrFile: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    AltronGlassCard(
      modifier = Modifier.fillMaxWidth(),
      hasGlowBorder = true,
      borderColor = TerminalRed.copy(alpha = 0.8f)
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Warning",
            tint = TerminalRed,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TerminalRed
          )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = message,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Graphite950, RoundedCornerShape(8.dp))
            .border(1.dp, Graphite700, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Text(
            text = commandOrFile,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 12.sp,
            color = AltronAmber
          )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
          ) {
            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(containerColor = TerminalRed),
            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Approve & Run", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
