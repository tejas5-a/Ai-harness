package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightSurfaceElevated
import com.example.ui.theme.PureWhiteBackground
import com.example.ui.theme.SoftDecorativePeach
import com.example.ui.theme.TerminalRed

fun triggerTactileFeedback(context: android.content.Context, haptic: HapticFeedback? = null) {
  try {
    haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
  } catch (_: Exception) {}

  try {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
      vibratorManager?.defaultVibrator?.vibrate(
        android.os.VibrationEffect.createOneShot(35, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
      )
    } else {
      @Suppress("DEPRECATION")
      val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vibrator?.vibrate(
          android.os.VibrationEffect.createOneShot(35, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
        )
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(35)
      }
    }
  } catch (_: Exception) {}
}

/**
 * AltronGlassCard: Restrained glass styling with translucent white surface, subtle border,
 * and soft depth shadow for project cards, settings, and floating panels.
 */
@Composable
fun AltronGlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(16.dp),
  hasGlowBorder: Boolean = false,
  backgroundColor: Color = GlassSurface,
  borderColor: Color = if (hasGlowBorder) BrandPrimaryOrange else GlassBorder,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val cardModifier = if (onClick != null) {
    modifier
      .shadow(elevation = 2.dp, shape = shape, spotColor = Color(0x1A000000))
      .clip(shape)
      .clickable(onClick = onClick)
  } else {
    modifier
      .shadow(elevation = 2.dp, shape = shape, spotColor = Color(0x1A000000))
      .clip(shape)
  }

  Surface(
    modifier = cardModifier
      .border(
        width = 1.dp,
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

/**
 * Primary action button: #FF8A00 orange background, #000000 black text and black icons.
 */
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
      containerColor = BrandPrimaryOrange,
      contentColor = BrandTextBlack,
      disabledContainerColor = BrandPrimaryOrange.copy(alpha = 0.45f),
      disabledContentColor = BrandTextBlack.copy(alpha = 0.45f)
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = BrandTextBlack
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = if (isLoading) "Processing..." else text,
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Bold,
          color = BrandTextBlack
        )
      )
    }
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
    border = BorderStroke(1.dp, LightBorder),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = PureWhiteBackground,
      contentColor = BrandTextBlack
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
          tint = BrandTextBlack
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          color = BrandTextBlack
        )
      )
    }
  }
}

@Composable
fun StatusBadge(
  text: String,
  color: Color = BrandPrimaryOrange,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(color.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp))
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
fun ApprovalDialog(
  title: String,
  message: String,
  commandOrFile: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhiteBackground),
      border = BorderStroke(1.dp, LightBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = BrandTextBlack
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = message,
          fontSize = 14.sp,
          color = BrandTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LightSurfaceElevated)
            .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
        ) {
          Text(
            text = commandOrFile,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 12.sp,
            color = BrandTextBlack
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = BrandTextSecondary)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(
              containerColor = TerminalRed,
              contentColor = Color.White
            ),
            modifier = Modifier.defaultMinSize(minHeight = 44.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Approve & Run", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
