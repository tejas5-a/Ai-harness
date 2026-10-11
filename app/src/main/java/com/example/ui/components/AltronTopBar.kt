package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface

/**
 * AltronTopBar: Translucent glass styling with soft ambient shadow.
 */
@Composable
fun AltronTopBar(
  activeProject: Project?,
  onMenuClicked: () -> Unit,
  onTerminalClicked: (() -> Unit)? = null,
  onPreviewClicked: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 2.dp, spotColor = Color(0x10000000))
      .border(0.5.dp, GlassBorder),
    color = GlassSurface,
    tonalElevation = 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "ALTRON CODE AI",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = BrandPrimaryOrange,
            fontSize = 17.sp
          )
        )
        if (activeProject != null) {
          Text(
            text = " • ${activeProject.name}",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = BrandTextSecondary,
              fontSize = 13.sp
            )
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (onPreviewClicked != null) {
          IconButton(onClick = onPreviewClicked) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Live Preview",
              tint = BrandPrimaryOrange
            )
          }
        }
        if (onTerminalClicked != null) {
          IconButton(onClick = onTerminalClicked) {
            Icon(
              imageVector = Icons.Default.Terminal,
              contentDescription = "Terminal",
              tint = BrandTextBlack
            )
          }
        }
        IconButton(onClick = onMenuClicked) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = BrandTextBlack
          )
        }
      }
    }
  }
}
