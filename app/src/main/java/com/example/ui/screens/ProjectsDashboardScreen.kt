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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Project
import com.example.data.model.ProjectType
import com.example.ui.navigation.Screen
import com.example.ui.theme.StitchBorder
import com.example.ui.theme.StitchCardBackground
import com.example.ui.theme.StitchError
import com.example.ui.theme.StitchMainBackground
import com.example.ui.theme.StitchPrimaryAccent
import com.example.ui.theme.StitchPrimaryText
import com.example.ui.theme.StitchSecondaryBackground
import com.example.ui.theme.StitchSecondaryText
import com.example.ui.theme.StitchSoftAccent
import com.example.ui.theme.StitchSuccess
import com.example.ui.viewmodel.AltronViewModel

/**
 * SCREEN 10: PROJECTS DASHBOARD
 * Accurate Jetpack Compose translation of Google Stitch Projects design:
 * - Header ("Your Projects", "Create Project" button)
 * - Search & View Toggle Toolbar (Grid / List modes)
 * - Filter category chips (All, TypeScript, Python, Kotlin, GitHub Connected)
 * - Project Cards with language tags, branch badges, sync tags, 3-dots action menu
 * - Empty state fallback
 * - Modal dialog to initialize new projects
 */
@Composable
fun ProjectsDashboardScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val projects by viewModel.projects.collectAsState()
  val activeProject by viewModel.activeProject.collectAsState()
  val context = LocalContext.current

  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("All") }
  var isGridView by remember { mutableStateOf(true) }
  var showCreateDialog by remember { mutableStateOf(false) }

  // Project Deletion State
  var projectToDelete by remember { mutableStateOf<Project?>(null) }
  var showDeleteConfirmDialog by remember { mutableStateOf(false) }

  // New Project Form State
  var newProjName by remember { mutableStateOf("") }
  var newProjType by remember { mutableStateOf(ProjectType.ANDROID_KOTLIN) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(StitchMainBackground)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item { Spacer(modifier = Modifier.height(4.dp)) }

    // Projects Header & Action
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Your Projects",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = StitchPrimaryText,
            letterSpacing = (-0.5).sp
          )
          Text(
            text = "Manage local repositories & AI workspaces",
            fontSize = 12.sp,
            color = StitchSecondaryText
          )
        }

        Button(
          onClick = { showCreateDialog = true },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryAccent, contentColor = Color.White),
          modifier = Modifier.testTag("projects_create_btn")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Create Project", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Search & View Toggle Toolbar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search projects...", fontSize = 13.sp, color = StitchSecondaryText) },
          leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(18.dp)) },
          modifier = Modifier
            .weight(1f)
            .height(46.dp),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = StitchSecondaryBackground,
            unfocusedContainerColor = StitchSecondaryBackground,
            focusedBorderColor = StitchPrimaryAccent,
            unfocusedBorderColor = StitchBorder
          ),
          singleLine = true
        )

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = StitchSecondaryBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Row(modifier = Modifier.padding(2.dp)) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isGridView) StitchCardBackground else Color.Transparent)
                .clickable { isGridView = true },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Grid View",
                tint = if (isGridView) StitchPrimaryAccent else StitchSecondaryText,
                modifier = Modifier.size(18.dp)
              )
            }
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (!isGridView) StitchCardBackground else Color.Transparent)
                .clickable { isGridView = false },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ViewList,
                contentDescription = "List View",
                tint = if (!isGridView) StitchPrimaryAccent else StitchSecondaryText,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }

    // Filter Category Chips
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf("All (5)", "TypeScript", "Python", "Kotlin", "GitHub Connected").forEach { cat ->
          val isSel = (cat.startsWith("All") && selectedFilter == "All") || selectedFilter == cat
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(if (isSel) StitchPrimaryAccent else StitchSecondaryBackground)
              .border(1.dp, if (isSel) StitchPrimaryAccent else StitchBorder, RoundedCornerShape(20.dp))
              .clickable { selectedFilter = if (cat.startsWith("All")) "All" else cat }
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = cat,
              fontSize = 11.5.sp,
              fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
              color = if (isSel) Color.White else StitchSecondaryText
            )
          }
        }
      }
    }

    // Projects Grid / List
    val filteredProjects = projects.filter { proj ->
      val matchesSearch = searchQuery.isBlank() ||
          proj.name.contains(searchQuery, ignoreCase = true) ||
          proj.description.contains(searchQuery, ignoreCase = true)
      val matchesFilter = when (selectedFilter) {
        "All" -> true
        "Kotlin" -> proj.type == ProjectType.ANDROID || proj.type == ProjectType.ANDROID_KOTLIN || proj.type == ProjectType.KOTLIN
        "TypeScript" -> proj.type == ProjectType.WEB || proj.type == ProjectType.NODE || proj.type == ProjectType.NODEJS
        "Python" -> proj.type == ProjectType.PYTHON
        "Go" -> proj.type == ProjectType.CUSTOM
        else -> true
      }
      matchesSearch && matchesFilter
    }

    item {
      if (filteredProjects.isEmpty()) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          shape = RoundedCornerShape(16.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(StitchSoftAccent),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                tint = StitchPrimaryAccent,
                modifier = Modifier.size(26.dp)
              )
            }
            Text(
              text = if (projects.isEmpty()) "No projects yet" else "No matching projects",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = StitchPrimaryText
            )
            Text(
              text = if (projects.isEmpty())
                "Create a new Android Compose or Web project to begin coding with ALTRON AI."
              else
                "No projects match \"$searchQuery\" with filter \"$selectedFilter\".",
              fontSize = 12.5.sp,
              color = StitchSecondaryText,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (projects.isEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = StitchPrimaryAccent,
                  contentColor = Color.White
                )
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Initialize Starter Project", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          filteredProjects.forEach { proj ->
            val icon = when (proj.type) {
              ProjectType.ANDROID, ProjectType.ANDROID_KOTLIN -> Icons.Default.PhoneAndroid
              ProjectType.PYTHON -> Icons.Default.Terminal
              ProjectType.WEB -> Icons.Default.Code
              else -> Icons.Default.Laptop
            }
            StitchProjectCard(
              name = proj.name,
              lang = proj.type.displayName,
              desc = proj.description.ifBlank { "Workspace Project" },
              branch = "main",
              source = if (proj.gitRepoUrl != null) "GitHub" else "Local",
              time = "Active",
              icon = icon,
              onOpen = {
                viewModel.selectProject(proj)
                viewModel.setScreen(Screen.FILE_EXPLORER)
              },
              onDelete = {
                projectToDelete = proj
                showDeleteConfirmDialog = true
              }
            )
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }

  // Delete Confirmation Dialog
  if (showDeleteConfirmDialog && projectToDelete != null) {
    val proj = projectToDelete!!
    Dialog(onDismissRequest = {
      showDeleteConfirmDialog = false
      projectToDelete = null
    }) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("delete_project_dialog"),
        shape = RoundedCornerShape(16.dp),
        color = StitchCardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
        shadowElevation = 8.dp
      ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(StitchError.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = StitchError, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Delete Project", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          }

          Text(
            text = "Are you sure you want to permanently delete \"${proj.name}\"?\n\nThis will remove the project folder and all local files. Remote GitHub repositories will NOT be affected.",
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = StitchSecondaryText
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                showDeleteConfirmDialog = false
                projectToDelete = null
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StitchSecondaryBackground, contentColor = StitchPrimaryText),
              modifier = Modifier.testTag("cancel_delete_project_btn")
            ) {
              Text("Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
              onClick = {
                val ok = viewModel.deleteProject(proj)
                showDeleteConfirmDialog = false
                projectToDelete = null
                if (ok) {
                  android.widget.Toast.makeText(context, "Project \"${proj.name}\" deleted successfully.", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                  android.widget.Toast.makeText(context, "Failed to delete project \"${proj.name}\".", android.widget.Toast.LENGTH_SHORT).show()
                }
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StitchError, contentColor = Color.White),
              modifier = Modifier.testTag("confirm_delete_project_btn")
            ) {
              Text("Delete", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Create Project Modal Dialog
  if (showCreateDialog) {
    Dialog(onDismissRequest = { showCreateDialog = false }) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = StitchCardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
        shadowElevation = 8.dp
      ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(text = "Create New Project", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Project Name", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = StitchPrimaryText)
            OutlinedTextField(
              value = newProjName,
              onValueChange = { newProjName = it },
              placeholder = { Text("e.g. altron-api-gateway") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = StitchSecondaryBackground,
                unfocusedContainerColor = StitchSecondaryBackground,
                focusedBorderColor = StitchPrimaryAccent,
                unfocusedBorderColor = StitchBorder
              ),
              singleLine = true
            )
          }

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Primary Language / Template", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = StitchPrimaryText)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              listOf(
                Pair(ProjectType.ANDROID_KOTLIN, "Kotlin"),
                Pair(ProjectType.WEB, "TypeScript / HTML"),
                Pair(ProjectType.NODEJS, "Node.js / Express"),
                Pair(ProjectType.PYTHON, "Python API")
              ).forEach { (type, label) ->
                val isSel = newProjType == type
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) StitchPrimaryAccent else StitchSecondaryBackground)
                    .border(1.dp, if (isSel) StitchPrimaryAccent else StitchBorder, RoundedCornerShape(8.dp))
                    .clickable { newProjType = type }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (isSel) Color.White else StitchPrimaryText,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = { showCreateDialog = false },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StitchSecondaryBackground, contentColor = StitchSecondaryText)
            ) {
              Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (newProjName.isNotBlank()) {
                  viewModel.createProject(newProjName, newProjType, "Initialized via Stitch Projects")
                  showCreateDialog = false
                  android.widget.Toast.makeText(context, "Project \"$newProjName\" initialized!", android.widget.Toast.LENGTH_SHORT).show()
                }
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryAccent, contentColor = Color.White)
            ) {
              Text("Initialize Project", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StitchProjectCard(
  name: String,
  lang: String,
  desc: String,
  branch: String,
  source: String,
  time: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onOpen: () -> Unit,
  onDelete: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(elevation = 1.dp, shape = RoundedCornerShape(14.dp), spotColor = Color(0x0A000000))
      .clickable(onClick = onOpen),
    shape = RoundedCornerShape(14.dp),
    color = StitchCardBackground,
    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(StitchSoftAccent),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = icon, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(StitchSecondaryBackground)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(text = lang, fontSize = 10.sp, color = StitchSecondaryText)
              }
            }
            Text(text = desc, fontSize = 11.5.sp, color = StitchSecondaryText, maxLines = 1)
          }
        }

        Box {
          IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
          }
          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Open Project")
                }
              },
              onClick = {
                showMenu = false
                onOpen()
              }
            )
            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Sync with GitHub")
                }
              },
              onClick = { showMenu = false }
            )
            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = StitchError, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Delete", color = StitchError)
                }
              },
              onClick = {
                showMenu = false
                onDelete()
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .border(
            width = 1.dp,
            color = StitchBorder,
            shape = RoundedCornerShape(8.dp)
          )
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(StitchSecondaryBackground)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(text = branch, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (source == "GitHub") StitchSuccess.copy(alpha = 0.15f) else StitchSecondaryBackground)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (source == "GitHub") Icons.Default.CloudDone else Icons.Default.Laptop,
                contentDescription = null,
                tint = if (source == "GitHub") StitchSuccess else StitchSecondaryText,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = source,
                fontSize = 10.5.sp,
                fontWeight = if (source == "GitHub") FontWeight.Bold else FontWeight.Normal,
                color = if (source == "GitHub") StitchSuccess else StitchSecondaryText
              )
            }
          }
        }

        Text(text = time, fontSize = 11.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
      }
    }
  }
}
