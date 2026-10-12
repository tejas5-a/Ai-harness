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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
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
import com.example.ui.theme.StitchBorder
import com.example.ui.theme.StitchCardBackground
import com.example.ui.theme.StitchMainBackground
import com.example.ui.theme.StitchPrimaryAccent
import com.example.ui.theme.StitchPrimaryText
import com.example.ui.theme.StitchSecondaryText

/**
 * 12 Screens for ALTRON CODE AI based on Google Stitch Designs:
 * 1. HOME: Home Dashboard
 * 2. RESEARCH: AI Research Workspace
 * 3. CODER: AI Coder Workspace
 * 4. FILE_EXPLORER: Project File Explorer
 * 5. CODE_EDITOR: Code Editor
 * 6. TERMINAL: Terminal
 * 7. APK_BUILDER: APK Builder (Compiler Studio)
 * 8. GITHUB: GitHub Connection
 * 9. GITHUB_WORKSPACE: GitHub Repository Workspace
 * 10. PROJECTS: Projects Dashboard
 * 11. CHATS: AI Chats
 * 12. SETTINGS: API Settings
 */
enum class Screen(
  val route: String,
  val label: String,
  val icon: ImageVector,
  val testTag: String
) {
  HOME("home", "Home", Icons.Default.Home, "nav_home"),
  PROJECTS("projects", "Projects", Icons.Default.Folder, "nav_projects"),
  CHATS("chats", "Chats", Icons.Default.Chat, "nav_chats"),
  SETTINGS("settings", "Settings", Icons.Default.Settings, "nav_settings"),

  // Dedicated Developer Workspaces
  RESEARCH("research", "Research", Icons.Default.Explore, "nav_research"),
  CODER("coder", "Coder", Icons.Default.Code, "nav_coder"),
  FILE_EXPLORER("file_explorer", "Files", Icons.Default.FolderOpen, "nav_files"),
  CODE_EDITOR("code_editor", "Editor", Icons.Default.EditNote, "nav_editor"),
  TERMINAL("terminal", "Terminal", Icons.Default.Terminal, "nav_terminal"),
  APK_BUILDER("apk_builder", "Builder", Icons.Default.Android, "nav_builder"),
  GITHUB("github", "GitHub", Icons.Default.Code, "nav_github"),
  GITHUB_WORKSPACE("github_workspace", "Repo", Icons.Default.AccountTree, "nav_repo"),

  // Compatibility aliases
  CHAT("chats", "Chats", Icons.Default.Chat, "nav_chats"),
  PREVIEW("preview", "Preview", Icons.Default.PlayArrow, "nav_preview"),
  TASKS("tasks", "Tasks", Icons.Default.Autorenew, "nav_tasks")
}

/**
 * AltronBottomNavBar: 4-tab Stitch navigation bar (Home, Projects, Chats, Settings).
 * Styled with warm ivory background #FAF9F6, terracotta accent #D97757, and subtle border #E7E3DC.
 */
@Composable
fun AltronBottomNavBar(
  currentScreen: Screen,
  onScreenSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier
) {
  val primaryTabs = listOf(Screen.HOME, Screen.PROJECTS, Screen.CHATS, Screen.SETTINGS)

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
      .shadow(elevation = 6.dp, spotColor = Color(0x0A000000)),
    color = StitchMainBackground,
    tonalElevation = 0.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = StitchBorder,
          shape = RoundedCornerShape(0.dp)
        )
        .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        primaryTabs.forEach { screen ->
          val isSelected = currentScreen == screen ||
            (screen == Screen.HOME && currentScreen == Screen.RESEARCH) ||
            (screen == Screen.PROJECTS && (currentScreen == Screen.FILE_EXPLORER || currentScreen == Screen.CODE_EDITOR || currentScreen == Screen.GITHUB_WORKSPACE)) ||
            (screen == Screen.CHATS && (currentScreen == Screen.CHAT || currentScreen == Screen.CODER)) ||
            (screen == Screen.SETTINGS && (currentScreen == Screen.GITHUB || currentScreen == Screen.APK_BUILDER || currentScreen == Screen.TERMINAL))

          val contentColor = if (isSelected) StitchPrimaryAccent else StitchSecondaryText

          Box(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 2.dp)
              .defaultMinSize(minHeight = 48.dp)
              .clip(RoundedCornerShape(12.dp))
              .clickable(
                onClick = { onScreenSelected(screen) },
                role = Role.Tab
              )
              .padding(vertical = 6.dp, horizontal = 2.dp)
              .testTag(screen.testTag),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = screen.icon,
                contentDescription = screen.label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = screen.label,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
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
