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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Project
import com.example.data.model.ProjectFile
import com.example.data.model.ProjectType
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.components.AltronSecondaryButton
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.AltronOrange
import com.example.ui.theme.AltronOrangeBright
import com.example.ui.theme.AltronOrangeRim
import com.example.ui.theme.Graphite700
import com.example.ui.theme.Graphite800
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.viewmodel.AltronViewModel
import java.io.File

@Composable
fun ProjectsScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val projects by viewModel.projects.collectAsState()
  val activeProject by viewModel.activeProject.collectAsState()
  val currentFiles by viewModel.currentFiles.collectAsState()
  val currentDir by viewModel.currentDirectory.collectAsState()

  var showNewProjectDialog by remember { mutableStateOf(false) }
  var showNewFileDialog by remember { mutableStateOf(false) }
  var showNewFolderDialog by remember { mutableStateOf(false) }
  var showProjectMenu by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header & Workspace Picker
    AltronGlassCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("projects_header_card"),
      hasGlowBorder = true
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "PROJECT WORKSPACE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = AltronOrange
            )
            Text(
              text = activeProject?.name ?: "No Project",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AltronPrimaryButton(
              text = "New",
              icon = Icons.Default.Add,
              onClick = { showNewProjectDialog = true },
              modifier = Modifier.testTag("create_new_project_btn")
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Project Switcher Pills
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          projects.forEach { proj ->
            val isCurrent = proj.id == activeProject?.id
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isCurrent) AltronOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                .border(
                  1.dp,
                  if (isCurrent) AltronOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                  RoundedCornerShape(10.dp)
                )
                .clickable { viewModel.selectProject(proj) }
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = proj.name,
                  fontSize = 12.sp,
                  fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                  color = if (isCurrent) AltronOrangeBright else MaterialTheme.colorScheme.onSurface
                )
                if (isCurrent) {
                  Spacer(modifier = Modifier.width(6.dp))
                  StatusBadge(text = proj.type.badge, color = TerminalCyan)
                }
              }
            }
          }
        }
      }
    }

    // Security Boundary Chip
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        .border(1.dp, TerminalGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = null,
        tint = TerminalGreen,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "SANDBOX BOUNDARY ENFORCED: Real files scoped to project directory",
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = TerminalGreen
      )
    }

    // File Explorer Toolbar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Directory Breadcrumbs
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        val root = activeProject?.rootDir
        val isSubDir = currentDir != null && root != null && currentDir != root
        if (isSubDir) {
          IconButton(
            onClick = { viewModel.navigateUpDirectory() },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Up",
              tint = AltronOrange,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        val displayPath = if (currentDir != null && root != null) {
          if (currentDir == root) "/" else "/" + currentDir!!.relativeTo(root).path
        } else "/"

        Text(
          text = displayPath,
          style = MaterialTheme.typography.bodyMedium,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          fontWeight = FontWeight.Bold
        )
      }

      // Quick File Action Buttons
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
          onClick = { showNewFileDialog = true },
          modifier = Modifier
            .size(38.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .testTag("file_explorer_add_file_btn")
        ) {
          Icon(
            imageVector = Icons.Default.NoteAdd,
            contentDescription = "New File",
            tint = AltronOrange,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(
          onClick = { showNewFolderDialog = true },
          modifier = Modifier
            .size(38.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        ) {
          Icon(
            imageVector = Icons.Default.CreateNewFolder,
            contentDescription = "New Folder",
            tint = AltronAmber,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(
          onClick = { viewModel.refreshFiles() },
          modifier = Modifier
            .size(38.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // File List
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (currentFiles.isEmpty()) {
        item {
          AltronGlassCard(modifier = Modifier.fillMaxWidth()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Folder is empty. Create a new file or directory.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      items(currentFiles) { projectFile ->
        FileItemRow(
          projectFile = projectFile,
          onOpen = {
            if (projectFile.isDirectory) {
              viewModel.navigateToDirectory(projectFile.file)
            } else {
              viewModel.openFile(projectFile.file)
              // viewModel.setScreen(Screen.EDITOR)
            }
          },
          onDelete = {
            viewModel.deleteFile(projectFile.file)
          }
        )
      }
    }
  }

  // New Project Dialog
  if (showNewProjectDialog) {
    NewProjectModal(
      onDismiss = { showNewProjectDialog = false },
      onCreate = { name, type, desc ->
        viewModel.createProject(name, type, desc)
        showNewProjectDialog = false
      }
    )
  }

  // New File Dialog
  if (showNewFileDialog) {
    NewFileModal(
      onDismiss = { showNewFileDialog = false },
      onCreate = { name ->
        viewModel.createNewFile(name)
        showNewFileDialog = false
      }
    )
  }

  // New Folder Dialog
  if (showNewFolderDialog) {
    NewFolderModal(
      onDismiss = { showNewFolderDialog = false },
      onCreate = { name ->
        viewModel.createNewFolder(name)
        showNewFolderDialog = false
      }
    )
  }
}

@Composable
fun FileItemRow(
  projectFile: ProjectFile,
  onOpen: () -> Unit,
  onDelete: () -> Unit
) {
  AltronGlassCard(
    modifier = Modifier.fillMaxWidth(),
    onClick = onOpen
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        val (icon, tint) = getFileIcon(projectFile)
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = projectFile.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (projectFile.isDirectory) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (!projectFile.isDirectory) {
            Text(
              text = "${projectFile.sizeBytes} bytes",
              fontSize = 10.sp,
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete",
          tint = TerminalRed.copy(alpha = 0.7f),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

fun getFileIcon(file: ProjectFile): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
  return if (file.isDirectory) {
    Icons.Default.Folder to AltronAmber
  } else {
    when (file.extension.lowercase()) {
      "kt", "java" -> Icons.Default.Code to AltronOrange
      "html", "htm" -> Icons.Default.Description to TerminalCyan
      "js", "ts", "json" -> Icons.Default.Code to AltronAmber
      "py" -> Icons.Default.Code to TerminalGreen
      else -> Icons.Default.Description to Color(0xFF94A3B8)
    }
  }
}

@Composable
fun NewProjectModal(
  onDismiss: () -> Unit,
  onCreate: (name: String, type: ProjectType, description: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf(ProjectType.ANDROID) }

  Dialog(onDismissRequest = onDismiss) {
    AltronGlassCard(
      modifier = Modifier.fillMaxWidth(),
      hasGlowBorder = true
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Create New Project",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Project Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AltronOrange,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AltronOrange,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Project Template:",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = AltronOrange
        )

        Spacer(modifier = Modifier.height(6.dp))

        ProjectType.values().forEach { type ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedType = type }
              .padding(vertical = 4.dp)
          ) {
            RadioButton(
              selected = selectedType == type,
              onClick = { selectedType = type },
              colors = RadioButtonDefaults.colors(selectedColor = AltronOrange)
            )
            Text(
              text = type.displayName,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.width(8.dp))
          AltronPrimaryButton(
            text = "Create",
            onClick = {
              if (name.isNotBlank()) {
                onCreate(name, selectedType, description)
              }
            },
            enabled = name.isNotBlank()
          )
        }
      }
    }
  }
}

@Composable
fun NewFileModal(
  onDismiss: () -> Unit,
  onCreate: (name: String) -> Unit
) {
  var fileName by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    AltronGlassCard(modifier = Modifier.fillMaxWidth(), hasGlowBorder = true) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Create New File",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = fileName,
          onValueChange = { fileName = it },
          label = { Text("File Name (e.g. Script.kt, index.html)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.width(8.dp))
          AltronPrimaryButton(
            text = "Create File",
            onClick = { if (fileName.isNotBlank()) onCreate(fileName) },
            enabled = fileName.isNotBlank()
          )
        }
      }
    }
  }
}

@Composable
fun NewFolderModal(
  onDismiss: () -> Unit,
  onCreate: (name: String) -> Unit
) {
  var folderName by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    AltronGlassCard(modifier = Modifier.fillMaxWidth(), hasGlowBorder = true) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Create New Folder",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = folderName,
          onValueChange = { folderName = it },
          label = { Text("Folder Name (e.g. src, components)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.width(8.dp))
          AltronPrimaryButton(
            text = "Create Folder",
            onClick = { if (folderName.isNotBlank()) onCreate(folderName) },
            enabled = folderName.isNotBlank()
          )
        }
      }
    }
  }
}
