package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.ui.theme.StitchBorder
import com.example.ui.theme.StitchMainBackground
import com.example.ui.theme.StitchPrimaryAccent
import com.example.ui.theme.StitchPrimaryText
import com.example.ui.theme.StitchSecondaryText
import com.example.ui.theme.StitchSuccess

/**
 * AltronTopBar: Google Stitch Header.
 * Features:
 * - ALTRON CODE branding + pulsating "Claude 3.5 Connected" status
 * - Quick workspace action icons (GitHub, Builder, Terminal, File Explorer, Editor)
 * - Settings (tune) and user avatar
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
  onGitHubClicked: (() -> Unit)? = null,
  onFilesClicked: (() -> Unit)? = null,
  onEditorClicked: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(900),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 2.dp, spotColor = Color(0x08000000))
      .border(0.5.dp, StitchBorder),
    color = StitchMainBackground,
    tonalElevation = 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Logo & App Title & Connection status
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f, fill = false)
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StitchPrimaryAccent),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Code,
            contentDescription = "Logo",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "ALTRON CODE",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = StitchPrimaryText,
                fontSize = 15.sp,
                letterSpacing = (-0.2).sp
              )
            )
            if (activeProject != null) {
              Text(
                text = " • ${activeProject.name}",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = StitchSecondaryText,
                  fontSize = 12.sp
                ),
                maxLines = 1
              )
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(StitchSuccess)
                .alpha(pulseAlpha)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "Claude 3.5 Connected",
              style = MaterialTheme.typography.labelSmall.copy(
                color = StitchSuccess,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }
      }

      // Right: Workspace Quick Shortcuts & Profile
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        if (onGitHubClicked != null) {
          IconButton(
            onClick = onGitHubClicked,
            modifier = Modifier.size(34.dp).testTag("topbar_github_btn")
          ) {
            Icon(
              imageVector = Icons.Default.AccountTree,
              contentDescription = "GitHub",
              tint = StitchSecondaryText,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        if (onTasksClicked != null) {
          IconButton(
            onClick = onTasksClicked,
            modifier = Modifier.size(34.dp).testTag("topbar_builder_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Android,
              contentDescription = "APK Builder",
              tint = StitchSecondaryText,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        if (onTerminalClicked != null) {
          IconButton(
            onClick = onTerminalClicked,
            modifier = Modifier.size(34.dp).testTag("topbar_terminal_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Terminal,
              contentDescription = "Terminal",
              tint = StitchSecondaryText,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        if (onFilesClicked != null) {
          IconButton(
            onClick = onFilesClicked,
            modifier = Modifier.size(34.dp).testTag("topbar_files_btn")
          ) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = "File Explorer",
              tint = StitchSecondaryText,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        if (onEditorClicked != null) {
          IconButton(
            onClick = onEditorClicked,
            modifier = Modifier.size(34.dp).testTag("topbar_editor_btn")
          ) {
            Icon(
              imageVector = Icons.Default.EditNote,
              contentDescription = "Code Editor",
              tint = StitchSecondaryText,
              modifier = Modifier.size(19.dp)
            )
          }
        }

        IconButton(
          onClick = onApiClicked,
          modifier = Modifier.size(34.dp).testTag("topbar_tune_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Settings",
            tint = StitchSecondaryText,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Avatar
        Box(
          modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(StitchPrimaryAccent)
            .clickable { onApiClicked() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Account",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
