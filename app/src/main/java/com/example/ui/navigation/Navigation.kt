package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AltronOrange
import com.example.ui.theme.AltronOrangeGlow
import com.example.ui.theme.AltronOrangeRim

enum class Screen(
  val route: String,
  val label: String,
  val icon: ImageVector,
  val testTag: String
) {
  HOME("home", "Home", Icons.Default.Home, "nav_home"),
  PROJECTS("projects", "Projects", Icons.Default.Folder, "nav_projects"),
  EDITOR("editor", "Editor", Icons.Default.Code, "nav_editor"),
  AGENT("agent", "AI Agent", Icons.Default.Psychology, "nav_agent"),
  TERMINAL("terminal", "Terminal", Icons.Default.Terminal, "nav_terminal"),
  PROVIDERS("providers", "API Panel", Icons.Default.Settings, "nav_providers"),
  GITHUB_BUILD("build", "Build Center", Icons.Default.Build, "nav_build"),
  PREVIEW("preview", "Web Preview", Icons.Default.Language, "nav_preview")
}

@Composable
fun AltronBottomNavBar(
  currentScreen: Screen,
  onScreenSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
    tonalElevation = 8.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
          shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        )
        .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Screen.values().forEach { screen ->
          val isSelected = currentScreen == screen
          val itemBg = if (isSelected) AltronOrangeGlow else Color.Transparent
          val itemBorder = if (isSelected) AltronOrangeRim else Color.Transparent
          val contentColor = if (isSelected) AltronOrange else MaterialTheme.colorScheme.onSurfaceVariant

          Box(
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp, minWidth = 64.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(itemBg)
              .border(1.dp, itemBorder, RoundedCornerShape(12.dp))
              .testTag(screen.testTag),
            contentAlignment = Alignment.Center
          ) {
            IconButton(
              onClick = { onScreenSelected(screen) },
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = screen.icon,
                  contentDescription = screen.label,
                  tint = contentColor,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = screen.label,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = contentColor,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }
  }
}
