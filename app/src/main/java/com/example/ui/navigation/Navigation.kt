package com.example.ui.navigation

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextInactive
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface

enum class Screen(
  val route: String,
  val label: String,
  val icon: ImageVector,
  val testTag: String
) {
  CHAT("chat", "Chat", Icons.Default.Psychology, "nav_chat"),
  PROJECTS("projects", "Projects", Icons.Default.Folder, "nav_projects"),
  TERMINAL("terminal", "Terminal", Icons.Default.Terminal, "nav_terminal"),
  SETTINGS("settings", "API Panel", Icons.Default.VpnKey, "nav_api_panel"),
  PREVIEW("preview", "Live Preview", Icons.Default.PlayArrow, "nav_preview"),
  TASKS("tasks", "Tasks", Icons.Default.Autorenew, "nav_tasks")
}

/**
 * AltronBottomNavBar: White glass-style surface with subtle shadow and orange active indicator.
 * Displays all 4 primary navigation tabs:
 * 1. Chat (left)
 * 2. Projects
 * 3. Terminal
 * 4. API Panel (right)
 */
@Composable
fun AltronBottomNavBar(
  currentScreen: Screen,
  onScreenSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier
) {
  val mainScreens = listOf(Screen.CHAT, Screen.PROJECTS, Screen.TERMINAL, Screen.SETTINGS)

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
      .shadow(elevation = 8.dp, spotColor = Color(0x14000000)),
    color = GlassSurface,
    tonalElevation = 0.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = GlassBorder,
          shape = RoundedCornerShape(0.dp)
        )
        .padding(vertical = 6.dp, horizontal = 6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        mainScreens.forEach { screen ->
          val isSelected = currentScreen == screen
          val itemBg = if (isSelected) BrandPrimaryOrange else Color.Transparent
          val contentColor = if (isSelected) BrandTextBlack else BrandTextInactive

          Box(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 2.dp)
              .defaultMinSize(minHeight = 48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(itemBg)
              .clickable(
                onClick = { onScreenSelected(screen) },
                role = Role.Tab
              )
              .padding(vertical = 5.dp, horizontal = 2.dp)
              .testTag(screen.testTag),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = screen.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = screen.label,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }
  }
}

/**
 * AltronNavigationRail: Side navigation for tablets, laptops, desktops, and wide viewports.
 * Displays brand badge, primary items (Chat, Projects, Terminal) and secondary shortcuts.
 */
@Composable
fun AltronNavigationRail(
  currentScreen: Screen,
  onScreenSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier
) {
  val primaryScreens = listOf(Screen.CHAT, Screen.PROJECTS, Screen.TERMINAL)
  val secondaryScreens = listOf(Screen.PREVIEW, Screen.SETTINGS)

  Surface(
    modifier = modifier
      .width(80.dp)
      .shadow(elevation = 6.dp, spotColor = Color(0x14000000)),
    color = GlassSurface
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .border(width = 1.dp, color = GlassBorder)
        .padding(vertical = 16.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Brand Mark
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BrandPrimaryOrange),
          contentAlignment = Alignment.Center
        ) {
          Text("AC", fontWeight = FontWeight.Black, fontSize = 16.sp, color = BrandTextBlack)
        }

        Spacer(modifier = Modifier.height(8.dp))

        primaryScreens.forEach { screen ->
          val isSelected = currentScreen == screen
          val itemBg = if (isSelected) BrandPrimaryOrange else Color.Transparent
          val contentColor = if (isSelected) BrandTextBlack else BrandTextInactive

          Box(
            modifier = Modifier
              .defaultMinSize(minHeight = 48.dp, minWidth = 64.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(itemBg)
              .clickable(
                onClick = { onScreenSelected(screen) },
                role = Role.Tab
              )
              .padding(horizontal = 6.dp, vertical = 6.dp)
              .testTag("rail_${screen.testTag}"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = screen.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = screen.label,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        secondaryScreens.forEach { screen ->
          val isSelected = currentScreen == screen
          val itemBg = if (isSelected) BrandPrimaryOrange else Color.Transparent
          val contentColor = if (isSelected) BrandTextBlack else BrandTextInactive

          Box(
            modifier = Modifier
              .defaultMinSize(minHeight = 44.dp, minWidth = 60.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(itemBg)
              .clickable(
                onClick = { onScreenSelected(screen) },
                role = Role.Tab
              )
              .padding(horizontal = 6.dp, vertical = 5.dp)
              .testTag("rail_${screen.testTag}"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = screen.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = screen.label,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }
  }
}
