package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.AltronOrange
import com.example.ui.theme.AltronOrangeBright
import com.example.ui.theme.Graphite950
import com.example.ui.theme.TerminalGreen

@Composable
fun AltronTopBar(
  activeProject: Project?,
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  onSelectProjectClicked: () -> Unit,
  onRunClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.statusBars),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
    tonalElevation = 6.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
          shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        )
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Brand & Logo
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(
                brush = Brush.linearGradient(
                  colors = listOf(AltronOrange, AltronAmber)
                )
              )
              .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Code,
              contentDescription = "Altron Logo",
              tint = Color.Black,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "ALTRON",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp,
                  color = AltronOrange
                )
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "CODE",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              )
              Spacer(modifier = Modifier.width(4.dp))
              Box(
                modifier = Modifier
                  .background(AltronOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                  .border(0.8.dp, AltronOrange.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "AI",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black,
                  color = AltronOrange
                )
              }
            }
            Text(
              text = "Mobile Studio",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Active Project Pill & Quick Actions
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Project Selector Pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
              .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
              .clickable(onClick = onSelectProjectClicked)
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("top_bar_project_selector")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(7.dp)
                  .background(TerminalGreen, CircleShape)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = activeProject?.name?.take(14) ?: "Select Project",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
              )
            }
          }

          // Run / Preview Action
          IconButton(
            onClick = onRunClicked,
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(AltronOrange.copy(alpha = 0.15f))
              .border(1.dp, AltronOrange.copy(alpha = 0.5f), CircleShape)
              .testTag("top_bar_run_action")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Run",
              tint = AltronOrange,
              modifier = Modifier.size(18.dp)
            )
          }

          // Theme Toggle
          IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
              .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
              .testTag("theme_toggle_button")
          ) {
            Icon(
              imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = "Toggle Theme",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
