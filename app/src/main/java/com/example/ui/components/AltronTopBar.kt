package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
 * AltronTopBar: Translucent glass styling with soft ambient shadow, safe-area status bar insets,
 * active task indicator, and direct access to API Panel & tools.
 */
@Composable
fun AltronTopBar(
  activeProject: Project?,
  onApiClicked: () -> Unit,
  onMenuClicked: (() -> Unit)? = null,
  activeTasksCount: Int = 0,
  onTasksClicked: (() -> Unit)? = null,
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
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp),
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
            ),
            maxLines = 1
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (onTasksClicked != null) {
          Box {
            IconButton(
              onClick = onTasksClicked,
              modifier = Modifier.size(36.dp).testTag("topbar_tasks_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Autorenew,
                contentDescription = "Background Tasks",
                tint = if (activeTasksCount > 0) BrandPrimaryOrange else BrandTextBlack,
                modifier = Modifier.size(20.dp)
              )
            }
            if (activeTasksCount > 0) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(top = 2.dp, end = 2.dp)
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(BrandPrimaryOrange),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "$activeTasksCount",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = BrandTextBlack
                )
              }
            }
          }
        }
        if (onPreviewClicked != null) {
          IconButton(
            onClick = onPreviewClicked,
            modifier = Modifier.size(36.dp).testTag("topbar_preview_btn")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Live Preview",
              tint = BrandPrimaryOrange,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        if (onTerminalClicked != null) {
          IconButton(
            onClick = onTerminalClicked,
            modifier = Modifier.size(36.dp).testTag("topbar_terminal_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Terminal,
              contentDescription = "Terminal",
              tint = BrandTextBlack,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        // Dedicated API Panel button
        IconButton(
          onClick = onApiClicked,
          modifier = Modifier.size(36.dp).testTag("topbar_api_btn")
        ) {
          Icon(
            imageVector = Icons.Default.VpnKey,
            contentDescription = "API Providers & Models",
            tint = BrandPrimaryOrange,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
