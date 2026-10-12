package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.StitchBorder
import com.example.ui.theme.StitchCardBackground
import com.example.ui.theme.StitchMainBackground
import com.example.ui.theme.StitchPrimaryAccent
import com.example.ui.theme.StitchPrimaryText
import com.example.ui.theme.StitchSecondaryBackground
import com.example.ui.theme.StitchSecondaryText
import com.example.ui.theme.StitchSoftAccent
import com.example.ui.theme.StitchSuccess
import com.example.ui.viewmodel.AltronViewModel
import java.io.File

/**
 * SCREEN 4: PROJECT FILE EXPLORER
 * Implements the Google Stitch Project File Explorer design:
 * - Project header with branch, version, and action icons
 * - Breadcrumbs trail
 * - Live filter search & extension chips (.kt, .xml, .gradle, .json, .md)
 * - Quick Action Tool Strip (File, Folder, Diff, Sync)
 * - Interactive hierarchical file tree
 * - Selected File Context Preview Card ("Open in Editor", "Ask AI", "Rename")
 * - Storage & Gradle status footer
 */
@Composable
fun ProjectFileExplorerScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val activeProject by viewModel.activeProject.collectAsState()
  val context = LocalContext.current

  var searchQuery by remember { mutableStateOf("") }
  var selectedExtension by remember { mutableStateOf("All") }
  var selectedFileName by remember { mutableStateOf("NavigationHost.kt") }

  // Directory expansion toggles
  var isAppExpanded by remember { mutableStateOf(true) }
  var isSrcExpanded by remember { mutableStateOf(true) }
  var isMainExpanded by remember { mutableStateOf(true) }
  var isJavaExpanded by remember { mutableStateOf(true) }
  var isNavExpanded by remember { mutableStateOf(true) }
  var isAgentExpanded by remember { mutableStateOf(true) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(StitchMainBackground)
  ) {
    // Top Context Sub-Header
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = StitchSecondaryBackground,
      shadowElevation = 1.dp,
      border = androidx.compose.foundation.BorderStroke(0.5.dp, StitchBorder)
    ) {
      Column {
        // Row 1: Project Identity & Operations
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
          ) {
            IconButton(
              onClick = { viewModel.setScreen(Screen.HOME) },
              modifier = Modifier.size(34.dp)
            ) {
              Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = activeProject?.name ?: "AltronCore-Android",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = StitchPrimaryText
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(StitchCardBackground)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(text = "v2.4", fontSize = 10.sp, color = StitchSecondaryText)
                }
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(StitchSoftAccent)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ForkRight, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "feat/agentic-loop", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryAccent)
                  }
                }
              }
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
              Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
            }
            IconButton(
              onClick = {
                viewModel.createFile("NewScreen.kt")
                android.widget.Toast.makeText(context, "New file created!", android.widget.Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(imageVector = Icons.Default.NoteAdd, contentDescription = "New File", tint = StitchPrimaryAccent, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
              Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
            }
          }
        }

        // Row 2: Breadcrumbs & Sync Status
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(StitchCardBackground)
            .padding(horizontal = 14.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .horizontalScroll(rememberScrollState())
              .weight(1f, fill = false)
          ) {
            Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("app", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
            Text(" / ", fontSize = 11.sp, color = StitchSecondaryText.copy(alpha = 0.5f))
            Text("src", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
            Text(" / ", fontSize = 11.sp, color = StitchSecondaryText.copy(alpha = 0.5f))
            Text("main", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
            Text(" / ", fontSize = 11.sp, color = StitchSecondaryText.copy(alpha = 0.5f))
            Text("java", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
            Text(" / ", fontSize = 11.sp, color = StitchSecondaryText.copy(alpha = 0.5f))
            Text("navigation", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryAccent, fontWeight = FontWeight.Bold)
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "24 files", fontSize = 11.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
          }
        }

        // Row 3: Filter Input & Extension Chips
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter files or symbols... (e.g. *.kt)", fontSize = 12.sp, color = StitchSecondaryText) },
            leadingIcon = { Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                  Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = StitchSecondaryText, modifier = Modifier.size(14.dp))
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("file_explorer_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = StitchCardBackground,
              unfocusedContainerColor = StitchCardBackground,
              focusedBorderColor = StitchPrimaryAccent,
              unfocusedBorderColor = StitchBorder
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("All", ".kt", ".xml", ".gradle", ".json", ".md").forEach { ext ->
              val isSel = selectedExtension == ext
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSel) StitchPrimaryAccent else StitchCardBackground)
                  .border(1.dp, if (isSel) StitchPrimaryAccent else StitchBorder, RoundedCornerShape(12.dp))
                  .clickable { selectedExtension = ext }
                  .padding(horizontal = 10.dp, vertical = 3.dp)
              ) {
                Text(
                  text = ext,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  color = if (isSel) Color.White else StitchPrimaryText,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }
        }
      }
    }

    // Quick Floating Action Strip
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 6.dp),
      shape = RoundedCornerShape(10.dp),
      color = StitchSecondaryBackground,
      border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        QuickToolButton(icon = Icons.Default.AddBox, label = "File", tint = StitchPrimaryAccent) {
          viewModel.createFile("NewFile.kt")
          android.widget.Toast.makeText(context, "File created", android.widget.Toast.LENGTH_SHORT).show()
        }
        QuickToolButton(icon = Icons.Default.CreateNewFolder, label = "Folder", tint = StitchSuccess) {
          viewModel.createDirectory("new_package")
          android.widget.Toast.makeText(context, "Folder created", android.widget.Toast.LENGTH_SHORT).show()
        }
        QuickToolButton(icon = Icons.Default.Difference, label = "Diff", tint = StitchPrimaryAccent) {
          viewModel.setScreen(Screen.CODER)
        }
        QuickToolButton(icon = Icons.Default.Sync, label = "Sync", tint = StitchSecondaryText) {
          viewModel.refreshFiles()
          android.widget.Toast.makeText(context, "Project tree synced", android.widget.Toast.LENGTH_SHORT).show()
        }
      }
    }

    // Hierarchical File Explorer Tree (Scrollable)
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 14.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      item {
        FolderTreeRow(name = "app", level = 0, isExpanded = isAppExpanded, tag = "module") { isAppExpanded = !isAppExpanded }
      }
      if (isAppExpanded) {
        item {
          FolderTreeRow(name = "src", level = 1, isExpanded = isSrcExpanded) { isSrcExpanded = !isSrcExpanded }
        }
        if (isSrcExpanded) {
          item {
            FolderTreeRow(name = "main", level = 2, isExpanded = isMainExpanded) { isMainExpanded = !isMainExpanded }
          }
          if (isMainExpanded) {
            item {
              FolderTreeRow(name = "java/com/altron/core", level = 3, isExpanded = isJavaExpanded) { isJavaExpanded = !isJavaExpanded }
            }
            if (isJavaExpanded) {
              item {
                FolderTreeRow(name = "navigation", level = 4, isExpanded = isNavExpanded, count = 3) { isNavExpanded = !isNavExpanded }
              }
              if (isNavExpanded) {
                item {
                  FileTreeRow(
                    name = "NavigationHost.kt",
                    level = 5,
                    badge = "+24 -8",
                    isSelected = selectedFileName == "NavigationHost.kt",
                    onClick = { selectedFileName = "NavigationHost.kt" }
                  )
                }
                item {
                  FileTreeRow(
                    name = "StatefulScreen.kt",
                    level = 5,
                    badge = "+14",
                    isSelected = selectedFileName == "StatefulScreen.kt",
                    onClick = { selectedFileName = "StatefulScreen.kt" }
                  )
                }
                item {
                  FileTreeRow(
                    name = "ScreenDestination.kt",
                    level = 5,
                    badge = "clean",
                    isSelected = selectedFileName == "ScreenDestination.kt",
                    onClick = { selectedFileName = "ScreenDestination.kt" }
                  )
                }
              }

              item {
                FolderTreeRow(name = "agent", level = 4, isExpanded = isAgentExpanded, count = 2) { isAgentExpanded = !isAgentExpanded }
              }
              if (isAgentExpanded) {
                item {
                  FileTreeRow(name = "AgentEngine.kt", level = 5, badge = "clean", isSelected = selectedFileName == "AgentEngine.kt", onClick = { selectedFileName = "AgentEngine.kt" })
                }
                item {
                  FileTreeRow(name = "ToolRegistry.kt", level = 5, badge = "clean", isSelected = selectedFileName == "ToolRegistry.kt", onClick = { selectedFileName = "ToolRegistry.kt" })
                }
              }
            }
          }
        }
      }

      // Root files
      item {
        FileTreeRow(name = "AndroidManifest.xml", level = 2, isXml = true, isSelected = selectedFileName == "AndroidManifest.xml", onClick = { selectedFileName = "AndroidManifest.xml" })
      }
      item {
        FileTreeRow(name = "build.gradle.kts", level = 1, isModified = true, isSelected = selectedFileName == "build.gradle.kts", onClick = { selectedFileName = "build.gradle.kts" })
      }
      item {
        FileTreeRow(name = "settings.gradle.kts", level = 0, isSelected = selectedFileName == "settings.gradle.kts", onClick = { selectedFileName = "settings.gradle.kts" })
      }
      item {
        FileTreeRow(name = "README.md", level = 0, isSelected = selectedFileName == "README.md", onClick = { selectedFileName = "README.md" })
      }

      item { Spacer(modifier = Modifier.height(130.dp)) }
    }

    // Selected File Context Preview Card (Sticky at bottom)
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), spotColor = Color(0x18000000)),
      shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
      color = StitchCardBackground,
      border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
    ) {
      Column {
        // Status strip
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(StitchSecondaryBackground)
            .padding(horizontal = 14.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(StitchPrimaryAccent),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.EditNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = selectedFileName, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchPrimaryAccent))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Modified 14:02", fontSize = 11.sp, color = StitchPrimaryAccent, fontWeight = FontWeight.Medium)
          }
        }

        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = ".../main/java/com/altron/core/navigation/",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = StitchSecondaryText
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Kotlin Source • 184 lines • 4.6 KB • UTF-8",
            fontSize = 11.sp,
            color = StitchSecondaryText
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Actions
          Button(
            onClick = {
              viewModel.openFile(File(selectedFileName))
              viewModel.setScreen(Screen.CODE_EDITOR)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("file_explorer_open_editor_btn"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = StitchPrimaryAccent,
              contentColor = Color.White
            )
          ) {
            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Open in Editor", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                viewModel.sendAgentMessage("Explain the purpose and architecture of $selectedFileName")
                viewModel.setScreen(Screen.CODER)
              },
              modifier = Modifier.weight(1f).height(38.dp),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StitchSecondaryBackground, contentColor = StitchPrimaryText),
              border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
            ) {
              Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Ask AI", fontSize = 12.sp)
            }

            Button(
              onClick = {
                android.widget.Toast.makeText(context, "Rename dialog opened", android.widget.Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.weight(1f).height(38.dp),
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StitchSecondaryBackground, contentColor = StitchPrimaryText),
              border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
            ) {
              Icon(imageVector = Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Rename", fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun QuickToolButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  tint: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(StitchCardBackground)
      .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text(text = label, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = StitchPrimaryText)
    }
  }
}

@Composable
private fun FolderTreeRow(
  name: String,
  level: Int,
  isExpanded: Boolean,
  tag: String? = null,
  count: Int? = null,
  onClick: () -> Unit
) {
  val paddingStart = (level * 16 + 4).dp
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = paddingStart)
      .clip(RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
      contentDescription = null,
      tint = StitchPrimaryText,
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Icon(
      imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
      contentDescription = null,
      tint = StitchPrimaryAccent,
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = name,
      fontSize = 12.5.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Medium,
      color = StitchPrimaryText,
      modifier = Modifier.weight(1f)
    )
    if (tag != null) {
      Text(text = tag, fontSize = 10.sp, color = StitchSecondaryText)
    }
    if (count != null) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(StitchSecondaryBackground)
          .padding(horizontal = 6.dp, vertical = 1.dp)
      ) {
        Text(text = "$count", fontSize = 10.sp, color = StitchSecondaryText)
      }
    }
  }
}

@Composable
private fun FileTreeRow(
  name: String,
  level: Int,
  badge: String? = null,
  isModified: Boolean = false,
  isXml: Boolean = false,
  isSelected: Boolean = false,
  onClick: () -> Unit
) {
  val paddingStart = (level * 16 + 18).dp
  val bg = if (isSelected) StitchSoftAccent else Color.Transparent

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = paddingStart)
      .clip(RoundedCornerShape(6.dp))
      .background(bg)
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (isXml) Icons.Default.IntegrationInstructions else Icons.Default.Code,
      contentDescription = null,
      tint = if (isSelected) StitchPrimaryAccent else StitchSecondaryText,
      modifier = Modifier.size(15.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = name,
      fontSize = 12.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) StitchPrimaryAccent else StitchPrimaryText,
      modifier = Modifier.weight(1f)
    )
    if (badge != null) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(if (badge.contains("-")) StitchSoftAccent else StitchSecondaryBackground)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = badge,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = if (badge.contains("-")) StitchPrimaryAccent else StitchSecondaryText,
          fontFamily = FontFamily.Monospace
        )
      }
    }
    if (isModified) {
      Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchPrimaryAccent))
    }
  }
}
