package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import com.example.ui.navigation.Screen
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Project
import com.example.data.model.ProjectFile
import com.example.data.model.ProjectType
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.components.DeleteFileConfirmationDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.CodeBlockBackground
import com.example.ui.theme.CodeBlockText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightSurfaceElevated
import com.example.ui.theme.PureWhiteBackground
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
  val context = LocalContext.current
  val projects by viewModel.projects.collectAsState()
  val activeProject by viewModel.activeProject.collectAsState()
  val currentFiles by viewModel.currentFiles.collectAsState()
  val currentDir by viewModel.currentDirectory.collectAsState()
  val activeTab by viewModel.activeTab.collectAsState()
  val activeContent by viewModel.activeContent.collectAsState()
  val isContentModified by viewModel.isContentModified.collectAsState()

  var showNewProjectDialog by remember { mutableStateOf(false) }
  var showDeleteProjectDialog by remember { mutableStateOf(false) }
  var showNewFileDialog by remember { mutableStateOf(false) }
  var showNewFolderDialog by remember { mutableStateOf(false) }
  var renamingFile by remember { mutableStateOf<File?>(null) }
  var deletingFile by remember { mutableStateOf<File?>(null) }
  var viewingFileEditor by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Projects & Files",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = BrandTextBlack
          )
        )
        Text(
          text = "${projects.size} active workspaces on device",
          fontSize = 12.sp,
          color = BrandTextSecondary
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (activeProject != null) {
          Button(
            onClick = { showDeleteProjectDialog = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = TerminalRed.copy(alpha = 0.12f),
              contentColor = TerminalRed
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .defaultMinSize(minHeight = 40.dp)
              .testTag("delete_project_files_button")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Project and Files",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Delete",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        AltronPrimaryButton(
          text = "New Project",
          icon = Icons.Default.Add,
          onClick = { showNewProjectDialog = true }
        )
      }
    }

    // Horizontal Project Switcher
    if (projects.isNotEmpty()) {
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
              .background(if (isCurrent) BrandPrimaryOrange else LightSurfaceElevated)
              .border(
                1.dp,
                if (isCurrent) BrandPrimaryOrange else LightBorder,
                RoundedCornerShape(10.dp)
              )
              .clickable { viewModel.selectProject(proj) }
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = proj.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTextBlack
              )
              if (isCurrent) {
                Spacer(modifier = Modifier.width(6.dp))
                StatusBadge(text = proj.type.badge, color = BrandTextBlack)
              }
            }
          }
        }
      }
    }

    // PROMINENT ORANGE ACTION BUTTON: "Download Project ZIP"
    Button(
      onClick = { viewModel.downloadProjectZip(context) },
      modifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 48.dp)
        .testTag("download_project_zip_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = BrandPrimaryOrange,
        contentColor = BrandTextBlack
      )
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.FolderZip,
          contentDescription = null,
          tint = BrandTextBlack,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Download Project ZIP",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = BrandTextBlack,
            fontSize = 15.sp
          )
        )
      }
    }

    // Integrated File Viewer / Editor panel if opened
    if (activeTab != null && viewingFileEditor) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .border(1.dp, LightBorder, RoundedCornerShape(12.dp)),
        color = LightSurfaceElevated
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = BrandPrimaryOrange,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${activeTab!!.file.name} ${if (isContentModified) "•" else ""}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BrandTextBlack
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Button(
                onClick = { viewModel.saveActiveFile() },
                colors = ButtonDefaults.buttonColors(
                  containerColor = BrandPrimaryOrange,
                  contentColor = BrandTextBlack
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(14.dp), tint = BrandTextBlack)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandTextBlack)
              }

              IconButton(onClick = { viewModel.downloadIndividualFile(context, activeTab!!.file) }) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Download File", tint = BrandTextBlack)
              }

              IconButton(onClick = { viewingFileEditor = false }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = BrandTextBlack)
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = activeContent,
            onValueChange = { viewModel.updateActiveContent(it) },
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = FontFamily.Monospace,
              color = BrandTextBlack,
              fontSize = 12.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = PureWhiteBackground,
              unfocusedContainerColor = PureWhiteBackground,
              focusedBorderColor = BrandPrimaryOrange,
              unfocusedBorderColor = LightBorder
            )
          )
        }
      }
    }

    // File Explorer Toolbar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
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
              tint = BrandPrimaryOrange,
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
          fontFamily = FontFamily.Monospace,
          color = BrandTextBlack,
          maxLines = 1,
          fontWeight = FontWeight.Bold
        )
      }

      // Quick File Action Buttons
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        IconButton(
          onClick = { showNewFileDialog = true },
          modifier = Modifier
            .size(38.dp)
            .background(LightSurface, RoundedCornerShape(8.dp))
            .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
            .testTag("file_explorer_add_file_btn")
        ) {
          Icon(
            imageVector = Icons.Default.NoteAdd,
            contentDescription = "New File",
            tint = BrandPrimaryOrange,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(
          onClick = { showNewFolderDialog = true },
          modifier = Modifier
            .size(38.dp)
            .background(LightSurface, RoundedCornerShape(8.dp))
            .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
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
            .background(LightSurface, RoundedCornerShape(8.dp))
            .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = BrandTextBlack,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(
          onClick = { viewModel.setScreen(Screen.TASKS) },
          modifier = Modifier
            .size(38.dp)
            .background(LightSurface, RoundedCornerShape(8.dp))
            .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
            .testTag("file_explorer_tasks_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Autorenew,
            contentDescription = "Background Tasks",
            tint = BrandPrimaryOrange,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // Hierarchical File List
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
                color = BrandTextSecondary
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
              viewingFileEditor = true
            }
          },
          onDownload = {
            viewModel.downloadIndividualFile(context, projectFile.file)
          },
          onRename = {
            renamingFile = projectFile.file
          },
          onDelete = {
            deletingFile = projectFile.file
          }
        )
      }
    }
  }

  // Dialogs
  if (deletingFile != null) {
    val fileToDelete = deletingFile!!
    val hasUnsavedEdits = activeTab?.file?.absolutePath == fileToDelete.absolutePath && isContentModified
    DeleteFileConfirmationDialog(
      fileName = fileToDelete.name,
      hasUnsavedChanges = hasUnsavedEdits,
      onConfirm = {
        viewModel.deleteFile(fileToDelete)
        deletingFile = null
      },
      onDismiss = {
        deletingFile = null
      }
    )
  }

  if (showDeleteProjectDialog && activeProject != null) {
    val projectToDelete = activeProject!!
    Dialog(onDismissRequest = { showDeleteProjectDialog = false }) {
      AltronGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TerminalRed.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = TerminalRed,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Delete Project & Files?",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = BrandTextBlack
                )
              )
              Text(
                text = projectToDelete.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = BrandPrimaryOrange
              )
            }
          }

          Text(
            text = "Are you sure you want to delete this project and all its workspace files (${currentFiles.size} items) from your device?\n\nThis action cannot be undone.",
            fontSize = 13.sp,
            color = BrandTextSecondary,
            lineHeight = 18.sp
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextButton(
              onClick = { showDeleteProjectDialog = false }
            ) {
              Text("Cancel", color = BrandTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                viewModel.deleteProject(projectToDelete)
                showDeleteProjectDialog = false
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = TerminalRed,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Delete Project & Files", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  if (showNewProjectDialog) {
    NewProjectModal(
      onDismiss = { showNewProjectDialog = false },
      onCreate = { name, type, desc ->
        viewModel.createProject(name, type, desc)
        showNewProjectDialog = false
      }
    )
  }

  if (showNewFileDialog) {
    NewFileModal(
      onDismiss = { showNewFileDialog = false },
      onCreate = { name ->
        viewModel.createNewFile(name)
        showNewFileDialog = false
        viewingFileEditor = true
      }
    )
  }

  if (showNewFolderDialog) {
    NewFolderModal(
      onDismiss = { showNewFolderDialog = false },
      onCreate = { name ->
        viewModel.createNewFolder(name)
        showNewFolderDialog = false
      }
    )
  }

  if (renamingFile != null) {
    RenameFileModal(
      currentName = renamingFile!!.name,
      onDismiss = { renamingFile = null },
      onRename = { newName ->
        viewModel.renameFile(renamingFile!!, newName)
        renamingFile = null
      }
    )
  }
}

@Composable
fun FileItemRow(
  projectFile: ProjectFile,
  onOpen: () -> Unit,
  onDownload: () -> Unit,
  onRename: () -> Unit,
  onDelete: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  AltronGlassCard(
    modifier = Modifier.fillMaxWidth(),
    onClick = onOpen
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        val icon = when {
          projectFile.isDirectory -> Icons.Default.Folder
          projectFile.name.endsWith(".kt") || projectFile.name.endsWith(".java") -> Icons.Default.Description
          projectFile.name.endsWith(".html") || projectFile.name.endsWith(".htm") -> Icons.Default.Description
          projectFile.name.endsWith(".js") || projectFile.name.endsWith(".ts") || projectFile.name.endsWith(".tsx") -> Icons.Default.Description
          projectFile.name.endsWith(".py") -> Icons.Default.Description
          else -> Icons.Default.Description
        }

        val iconTint = when {
          projectFile.isDirectory -> AltronAmber
          projectFile.name.endsWith(".html") -> BrandPrimaryOrange
          projectFile.name.endsWith(".kt") -> TerminalCyan
          projectFile.name.endsWith(".py") -> TerminalGreen
          else -> BrandPrimaryOrange
        }

        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = projectFile.name,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              color = BrandTextBlack
            )
          )
          Text(
            text = if (projectFile.isDirectory) "Directory" else "${projectFile.file.length()} bytes",
            fontSize = 11.sp,
            color = BrandTextSecondary
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (!projectFile.isDirectory) {
          IconButton(
            onClick = onDownload,
            modifier = Modifier
              .size(36.dp)
              .testTag("download_file_${projectFile.name}")
          ) {
            Icon(
              imageVector = Icons.Default.CloudDownload,
              contentDescription = "Download ${projectFile.name}",
              tint = BrandPrimaryOrange,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Direct touch-friendly Trash / Delete button beside each file
        IconButton(
          onClick = onDelete,
          modifier = Modifier
            .size(36.dp)
            .testTag("delete_file_${projectFile.name}")
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete ${projectFile.name}",
            tint = TerminalRed,
            modifier = Modifier.size(18.dp)
          )
        }

        Box {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier
              .size(36.dp)
              .testTag("more_file_${projectFile.name}")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "File Actions",
              tint = BrandTextBlack,
              modifier = Modifier.size(16.dp)
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(PureWhiteBackground)
          ) {
            DropdownMenuItem(
              text = { Text("Rename", color = BrandTextBlack, fontWeight = FontWeight.Bold) },
              onClick = {
                showMenu = false
                onRename()
              }
            )
            DropdownMenuItem(
              text = { Text("Delete", color = TerminalRed, fontWeight = FontWeight.Bold) },
              onClick = {
                showMenu = false
                onDelete()
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun RenameFileModal(
  currentName: String,
  onDismiss: () -> Unit,
  onRename: (newName: String) -> Unit
) {
  var newName by remember { mutableStateOf(currentName) }

  Dialog(onDismissRequest = onDismiss) {
    AltronGlassCard(modifier = Modifier.fillMaxWidth(), hasGlowBorder = true) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Rename Item",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = BrandTextBlack
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
          value = newName,
          onValueChange = { newName = it },
          label = { Text("New Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = BrandTextSecondary)
          }
          Spacer(modifier = Modifier.width(8.dp))
          AltronPrimaryButton(
            text = "Rename",
            onClick = { if (newName.isNotBlank()) onRename(newName) },
            enabled = newName.isNotBlank() && newName != currentName
          )
        }
      }
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
  var selectedType by remember { mutableStateOf(ProjectType.CUSTOM) }

  Dialog(onDismissRequest = onDismiss) {
    AltronGlassCard(modifier = Modifier.fillMaxWidth(), hasGlowBorder = true) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Create New Project",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = BrandTextBlack
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Project Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text("Template", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandTextBlack)
        Spacer(modifier = Modifier.height(6.dp))
        ProjectType.entries.forEach { type ->
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
              colors = RadioButtonDefaults.colors(selectedColor = BrandPrimaryOrange)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(type.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandTextBlack)
              Text(type.badge, fontSize = 11.sp, color = BrandTextSecondary)
            }
          }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = BrandTextSecondary)
          }
          Spacer(modifier = Modifier.width(8.dp))
          AltronPrimaryButton(
            text = "Create Project",
            onClick = { if (name.isNotBlank()) onCreate(name, selectedType, description) },
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
          color = BrandTextBlack
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = fileName,
          onValueChange = { fileName = it },
          label = { Text("File Name (e.g. index.html, main.py)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = BrandTextSecondary)
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
          color = BrandTextBlack
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = folderName,
          onValueChange = { folderName = it },
          label = { Text("Folder Name (e.g. src, assets)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = BrandTextSecondary)
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
