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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BackgroundTask
import com.example.data.model.BackgroundTaskState
import com.example.data.model.Project
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.CodeBlockBackground
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightSurfaceElevated
import com.example.ui.theme.PureWhiteBackground
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.viewmodel.AltronViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackgroundTasksScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val tasks by viewModel.backgroundTasks.collectAsState()
  val projects by viewModel.projects.collectAsState()
  val activeProject by viewModel.activeProject.collectAsState()
  val concurrencyLimit by viewModel.taskConcurrencyLimit.collectAsState()

  var showNewTaskDialog by remember { mutableStateOf(false) }
  var viewingLogsTask by remember { mutableStateOf<BackgroundTask?>(null) }
  var viewingChangesTask by remember { mutableStateOf<BackgroundTask?>(null) }
  var filterState by remember { mutableStateOf<BackgroundTaskState?>(null) }

  val runningCount = tasks.count { it.state == BackgroundTaskState.RUNNING }
  val queuedCount = tasks.count { it.state == BackgroundTaskState.QUEUED }

  val filteredTasks = remember(tasks, filterState) {
    if (filterState == null) tasks else tasks.filter { it.state == filterState }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .widthIn(max = 960.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Header & Controls
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Background Tasks",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = BrandTextBlack
            )
          )
          if (runningCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(BrandPrimaryOrange)
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = "$runningCount ACTIVE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTextBlack
              )
            }
          }
        }
        Text(
          text = "Autonomous coding jobs that continue when app is closed",
          fontSize = 12.sp,
          color = BrandTextSecondary
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Clear finished button
        if (tasks.any { it.state == BackgroundTaskState.COMPLETED || it.state == BackgroundTaskState.FAILED }) {
          IconButton(
            onClick = { viewModel.clearFinishedBackgroundTasks() },
            modifier = Modifier
              .size(38.dp)
              .background(LightSurface, RoundedCornerShape(8.dp))
              .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Clear History",
              tint = BrandTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // New Task Button
        Button(
          onClick = { showNewTaskDialog = true },
          colors = ButtonDefaults.buttonColors(
            containerColor = BrandPrimaryOrange,
            contentColor = BrandTextBlack
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("start_bg_task_btn")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandTextBlack)
          Spacer(modifier = Modifier.width(4.dp))
          Text("New Task", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandTextBlack)
        }
      }
    }

    // Status Banner & Concurrency control
    AltronGlassCard(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (runningCount > 0) {
            CircularProgressIndicator(
              modifier = Modifier.size(14.dp),
              color = BrandPrimaryOrange,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Worker Engine: Running ($runningCount active, $queuedCount queued)",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BrandPrimaryOrange
            )
          } else {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(TerminalGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Worker Engine: Ready & Idle",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = BrandTextBlack
            )
          }
        }

        // Concurrency Selector
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "Concurrency: ", fontSize = 11.sp, color = BrandTextSecondary)
          listOf(1, 2, 3).forEach { limit ->
            val isSelected = concurrencyLimit == limit
            Box(
              modifier = Modifier
                .padding(horizontal = 2.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) BrandPrimaryOrange else LightSurfaceElevated)
                .border(0.8.dp, if (isSelected) BrandPrimaryOrange else LightBorder, RoundedCornerShape(6.dp))
                .clickable { viewModel.setTaskConcurrency(limit) }
                .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
              Text(
                text = "$limit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTextBlack
              )
            }
          }
        }
      }
    }

    // Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      FilterChip(
        label = "All (${tasks.size})",
        isSelected = filterState == null,
        onClick = { filterState = null }
      )
      FilterChip(
        label = "Running ($runningCount)",
        isSelected = filterState == BackgroundTaskState.RUNNING,
        onClick = { filterState = BackgroundTaskState.RUNNING }
      )
      FilterChip(
        label = "Queued ($queuedCount)",
        isSelected = filterState == BackgroundTaskState.QUEUED,
        onClick = { filterState = BackgroundTaskState.QUEUED }
      )
      FilterChip(
        label = "Completed (${tasks.count { it.state == BackgroundTaskState.COMPLETED }})",
        isSelected = filterState == BackgroundTaskState.COMPLETED,
        onClick = { filterState = BackgroundTaskState.COMPLETED }
      )
      FilterChip(
        label = "Failed (${tasks.count { it.state == BackgroundTaskState.FAILED }})",
        isSelected = filterState == BackgroundTaskState.FAILED,
        onClick = { filterState = BackgroundTaskState.FAILED }
      )
    }

    // Task List
    if (filteredTasks.isEmpty()) {
      AltronGlassCard(modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.HourglassEmpty,
              contentDescription = null,
              tint = BrandTextSecondary,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No background tasks found",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = BrandTextBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tap 'New Task' to queue a coding job that runs while app is closed.",
              fontSize = 12.sp,
              color = BrandTextSecondary
            )
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredTasks, key = { it.id }) { task ->
          BackgroundTaskCard(
            task = task,
            onCancel = { viewModel.cancelBackgroundTask(task.id) },
            onRetry = { viewModel.retryBackgroundTask(task.id) },
            onApprove = { viewModel.approveBackgroundTaskAction(task.id) },
            onDelete = { viewModel.deleteBackgroundTask(task.id) },
            onViewLogs = { viewingLogsTask = task },
            onViewChanges = { viewingChangesTask = task },
            onOpenProject = {
              val proj = projects.find { it.id == task.projectId }
              if (proj != null) {
                viewModel.selectProject(proj)
                viewModel.setScreen(Screen.PROJECTS)
              }
            }
          )
        }
      }
    }
  }

  // Modals & Dialogs
  if (showNewTaskDialog) {
    NewBackgroundTaskModal(
      projects = projects,
      activeProject = activeProject,
      onDismiss = { showNewTaskDialog = false },
      onSubmit = { title, desc, prompt, proj ->
        viewModel.submitBackgroundTask(title, desc, prompt, proj)
        showNewTaskDialog = false
      }
    )
  }

  if (viewingLogsTask != null) {
    TaskLogsDialog(
      task = viewingLogsTask!!,
      onDismiss = { viewingLogsTask = null }
    )
  }

  if (viewingChangesTask != null) {
    TaskChangesDialog(
      task = viewingChangesTask!!,
      onDismiss = { viewingChangesTask = null }
    )
  }
}

@Composable
fun FilterChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(if (isSelected) BrandPrimaryOrange else LightSurfaceElevated)
      .border(1.dp, if (isSelected) BrandPrimaryOrange else LightBorder, RoundedCornerShape(20.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = BrandTextBlack
    )
  }
}

@Composable
fun BackgroundTaskCard(
  task: BackgroundTask,
  onCancel: () -> Unit,
  onRetry: () -> Unit,
  onApprove: () -> Unit,
  onDelete: () -> Unit,
  onViewLogs: () -> Unit,
  onViewChanges: () -> Unit,
  onOpenProject: () -> Unit
) {
  val isRunning = task.state == BackgroundTaskState.RUNNING
  val isWaitingApproval = task.state == BackgroundTaskState.WAITING_APPROVAL

  val statusColor = when (task.state) {
    BackgroundTaskState.QUEUED -> TerminalCyan
    BackgroundTaskState.RUNNING -> BrandPrimaryOrange
    BackgroundTaskState.WAITING_APPROVAL -> AltronAmber
    BackgroundTaskState.COMPLETED -> TerminalGreen
    BackgroundTaskState.FAILED -> TerminalRed
    BackgroundTaskState.CANCELLED -> BrandTextSecondary
  }

  AltronGlassCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header: Title & Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = task.title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = BrandTextBlack
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = task.projectName,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = BrandPrimaryOrange
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "• ${task.modelId}",
              fontSize = 11.sp,
              color = BrandTextSecondary
            )
          }
        }

        StatusBadge(text = task.state.displayName, color = statusColor)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Progress bar if running or queued
      if (isRunning || task.state == BackgroundTaskState.QUEUED || isWaitingApproval) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = task.currentOperation,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = BrandTextBlack,
              maxLines = 1
            )
            Text(
              text = "${task.progressPercent}%",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BrandPrimaryOrange
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { task.progressPercent / 100f },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = BrandPrimaryOrange,
            trackColor = LightBorder
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      // Timing & Metadata
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val startedStr = if (task.startedAt != null) "Started: ${timeFormat.format(Date(task.startedAt))}" else "Created: ${timeFormat.format(Date(task.createdAt))}"
        val elapsedStr = if (task.startedAt != null) {
          val end = task.completedAt ?: System.currentTimeMillis()
          val sec = ((end - task.startedAt) / 1000).coerceAtLeast(0)
          "Elapsed: ${sec}s"
        } else ""

        Text(
          text = "$startedStr  ${if (elapsedStr.isNotEmpty()) "• $elapsedStr" else ""}",
          fontSize = 11.sp,
          color = BrandTextSecondary
        )

        if (task.changes.isNotEmpty()) {
          Text(
            text = "${task.changes.size} files modified",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TerminalGreen
          )
        }
      }

      // Waiting Approval Banner
      if (isWaitingApproval && task.pendingApprovalDescription != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AltronAmber.copy(alpha = 0.12f))
            .border(1.dp, AltronAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = "⚠️ Approval Required Before Modifying Files:",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = AltronAmber
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = task.pendingApprovalDescription,
              fontSize = 11.sp,
              color = BrandTextBlack
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
              onClick = onApprove,
              colors = ButtonDefaults.buttonColors(containerColor = AltronAmber, contentColor = BrandTextBlack),
              shape = RoundedCornerShape(6.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
              Text("Approve & Continue", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }

      // Error message if failed
      if (task.errorMessage != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Error: ${task.errorMessage}",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TerminalRed
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // View Logs
          TextButton(
            onClick = onViewLogs,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text("View Logs (${task.logs.size})", fontSize = 11.sp, color = BrandTextBlack, fontWeight = FontWeight.Bold)
          }

          // View Changes
          if (task.changes.isNotEmpty()) {
            TextButton(
              onClick = onViewChanges,
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("View Changes", fontSize = 11.sp, color = BrandPrimaryOrange, fontWeight = FontWeight.Bold)
            }
          }

          // Open Project
          IconButton(
            onClick = onOpenProject,
            modifier = Modifier.size(30.dp)
          ) {
            Icon(Icons.Default.Folder, contentDescription = "Open Project", tint = BrandTextSecondary, modifier = Modifier.size(16.dp))
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          if (isRunning || task.state == BackgroundTaskState.QUEUED) {
            Button(
              onClick = onCancel,
              colors = ButtonDefaults.buttonColors(containerColor = LightSurfaceElevated, contentColor = TerminalRed),
              shape = RoundedCornerShape(8.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp), tint = TerminalRed)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          if (task.state == BackgroundTaskState.FAILED || task.state == BackgroundTaskState.CANCELLED) {
            Button(
              onClick = onRetry,
              colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryOrange, contentColor = BrandTextBlack),
              shape = RoundedCornerShape(8.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = BrandTextBlack)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          if (task.state == BackgroundTaskState.COMPLETED || task.state == BackgroundTaskState.FAILED || task.state == BackgroundTaskState.CANCELLED) {
            IconButton(
              onClick = onDelete,
              modifier = Modifier.size(30.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BrandTextSecondary, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
fun NewBackgroundTaskModal(
  projects: List<Project>,
  activeProject: Project?,
  onDismiss: () -> Unit,
  onSubmit: (title: String, description: String, prompt: String, project: Project) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var prompt by remember { mutableStateOf("") }
  var selectedProject by remember { mutableStateOf(activeProject ?: projects.firstOrNull()) }

  Dialog(onDismissRequest = onDismiss) {
    AltronGlassCard(modifier = Modifier.fillMaxWidth(), hasGlowBorder = true) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Text(
          text = "Start Background Coding Task",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = BrandTextBlack
        )
        Text(
          text = "Will continue running even if you switch apps or lock phone",
          fontSize = 11.sp,
          color = BrandTextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Task Title (e.g. Refactor API Layer)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = prompt,
          onValueChange = { prompt = it },
          label = { Text("Detailed Instructions / Prompt") },
          modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
          placeholder = { Text("Describe the full changes, components, or fixes needed...", fontSize = 12.sp) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Target Project", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandTextBlack)
        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          projects.forEach { proj ->
            val isSelected = selectedProject?.id == proj.id
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) BrandPrimaryOrange else LightSurfaceElevated)
                .border(1.dp, if (isSelected) BrandPrimaryOrange else LightBorder, RoundedCornerShape(8.dp))
                .clickable { selectedProject = proj }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = proj.name,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = BrandTextBlack
              )
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
            text = "Start in Background",
            onClick = {
              if (selectedProject != null) {
                onSubmit(
                  title.ifBlank { "Autonomous Coding Task" },
                  prompt,
                  prompt,
                  selectedProject!!
                )
              }
            },
            enabled = selectedProject != null && (prompt.isNotBlank() || title.isNotBlank())
          )
        }
      }
    }
  }
}

@Composable
fun TaskLogsDialog(
  task: BackgroundTask,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = PureWhiteBackground,
      modifier = Modifier
        .fillMaxWidth()
        .height(500.dp)
        .border(1.dp, LightBorder, RoundedCornerShape(16.dp))
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Task Logs: ${task.title}",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = BrandTextBlack
            )
            Text(
              text = "${task.logs.size} log entries recorded",
              fontSize = 11.sp,
              color = BrandTextSecondary
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = BrandTextBlack)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(CodeBlockBackground)
            .padding(10.dp)
        ) {
          val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
          val scrollState = rememberScrollState()

          SelectionContainer {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
            ) {
              if (task.logs.isEmpty()) {
                Text(
                  text = "No logs recorded yet.",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              } else {
                task.logs.forEach { log ->
                  val color = when (log.level) {
                    "STEP" -> TerminalCyan
                    "SUCCESS" -> TerminalGreen
                    "WARN" -> AltronAmber
                    "ERROR" -> TerminalRed
                    else -> Color.LightGray
                  }
                  Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                      text = "[${timeFormat.format(Date(log.timestamp))}] ",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      color = Color.DarkGray
                    )
                    Text(
                      text = "[${log.level}] ",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = color
                    )
                    Text(
                      text = log.message,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      color = Color.White
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun TaskChangesDialog(
  task: BackgroundTask,
  onDismiss: () -> Unit
) {
  var selectedChangeIndex by remember { mutableStateOf(0) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = PureWhiteBackground,
      modifier = Modifier
        .fillMaxWidth()
        .height(540.dp)
        .border(1.dp, LightBorder, RoundedCornerShape(16.dp))
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "File Changes (${task.changes.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = BrandTextBlack
            )
            Text(
              text = "Generated files applied to '${task.projectName}'",
              fontSize = 11.sp,
              color = BrandTextSecondary
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = BrandTextBlack)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Files tab strip
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          task.changes.forEachIndexed { idx, ch ->
            val isSelected = selectedChangeIndex == idx
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) BrandPrimaryOrange else LightSurfaceElevated)
                .border(1.dp, if (isSelected) BrandPrimaryOrange else LightBorder, RoundedCornerShape(8.dp))
                .clickable { selectedChangeIndex = idx }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "${if (ch.isCreated) "+ " else "• "}${ch.relativePath}",
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = BrandTextBlack
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Code viewer
        val currentChange = task.changes.getOrNull(selectedChangeIndex)
        if (currentChange != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(CodeBlockBackground)
              .padding(10.dp)
          ) {
            SelectionContainer {
              Text(
                text = currentChange.newContent,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color.White,
                modifier = Modifier
                  .fillMaxSize()
                  .verticalScroll(rememberScrollState())
                  .horizontalScroll(rememberScrollState())
              )
            }
          }
        }
      }
    }
  }
}
