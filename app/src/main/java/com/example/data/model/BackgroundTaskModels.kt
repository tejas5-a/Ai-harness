package com.example.data.model

import java.util.UUID

enum class BackgroundTaskState(val displayName: String, val badgeColorName: String) {
  QUEUED("Queued", "BLUE"),
  RUNNING("Running", "ORANGE"),
  WAITING_APPROVAL("Waiting Approval", "AMBER"),
  COMPLETED("Completed", "GREEN"),
  FAILED("Failed", "RED"),
  CANCELLED("Cancelled", "GRAY")
}

data class TaskFileChange(
  val relativePath: String,
  val previousContent: String? = null,
  val newContent: String,
  val isCreated: Boolean = false,
  val isDeleted: Boolean = false
)

data class BackgroundTaskLogEntry(
  val timestamp: Long = System.currentTimeMillis(),
  val level: String = "INFO", // "INFO", "WARN", "ERROR", "STEP", "SUCCESS"
  val message: String
)

data class BackgroundTask(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val description: String,
  val prompt: String,
  val projectId: String,
  val projectName: String,
  val providerType: ProviderType,
  val modelId: String,
  val state: BackgroundTaskState = BackgroundTaskState.QUEUED,
  val progressPercent: Int = 0,
  val currentOperation: String = "Queued for execution",
  val createdAt: Long = System.currentTimeMillis(),
  val startedAt: Long? = null,
  val completedAt: Long? = null,
  val logs: List<BackgroundTaskLogEntry> = emptyList(),
  val changes: List<TaskFileChange> = emptyList(),
  val requiresApproval: Boolean = false,
  val pendingApprovalDescription: String? = null,
  val errorMessage: String? = null,
  val retryCount: Int = 0,
  val maxRetries: Int = 3
)
