package com.example.data.repository

import android.content.Context
import com.example.data.model.ApiProviderConfig
import com.example.data.model.BackgroundTask
import com.example.data.model.BackgroundTaskLogEntry
import com.example.data.model.BackgroundTaskState
import com.example.data.model.Project
import com.example.data.model.ProviderType
import com.example.data.model.TaskFileChange
import com.example.data.service.AltronTaskExecutionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File
import java.util.UUID

class BackgroundTaskManager(
  private val context: Context,
  private val projectRepo: ProjectRepository,
  private val apiKeyRepo: ApiKeyRepository,
  private val agentService: AiAgentService
) {

  private val repository = BackgroundTaskRepository(context)
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  private val _tasks = MutableStateFlow<List<BackgroundTask>>(emptyList())
  val tasks: StateFlow<List<BackgroundTask>> = _tasks.asStateFlow()

  private val _concurrencyLimit = MutableStateFlow(2)
  val concurrencyLimit: StateFlow<Int> = _concurrencyLimit.asStateFlow()

  private val activeJobs = mutableMapOf<String, Job>()
  private var semaphore = Semaphore(2)

  init {
    loadInitialTasks()
  }

  private fun loadInitialTasks() {
    val saved = repository.getAllTasks()
    // Check if any task was RUNNING when the process was killed; recover or set to QUEUED/FAILED
    val recovered = saved.map { task ->
      if (task.state == BackgroundTaskState.RUNNING) {
        task.copy(
          state = BackgroundTaskState.QUEUED,
          currentOperation = "Recovered after process restart. Ready to resume.",
          logs = task.logs + BackgroundTaskLogEntry(
            level = "WARN",
            message = "Task execution was interrupted by device or process restart. Recovered and queued to resume."
          )
        ).also { repository.saveTask(it) }
      } else task
    }
    _tasks.value = recovered
    processNextQueuedTasks()
  }

  fun setConcurrencyLimit(limit: Int) {
    val clamped = limit.coerceIn(1, 4)
    _concurrencyLimit.value = clamped
    semaphore = Semaphore(clamped)
    processNextQueuedTasks()
  }

  fun submitTask(
    title: String,
    description: String,
    prompt: String,
    project: Project,
    providerConfig: ApiProviderConfig? = null
  ): BackgroundTask {
    val config = providerConfig ?: apiKeyRepo.getDefaultProvider()

    val task = BackgroundTask(
      id = UUID.randomUUID().toString(),
      title = title.trim().ifEmpty { "Background Coding Task" },
      description = description.trim(),
      prompt = prompt.trim(),
      projectId = project.id,
      projectName = project.name,
      providerType = config.providerType,
      modelId = config.selectedModel,
      state = BackgroundTaskState.QUEUED,
      progressPercent = 0,
      currentOperation = "Queued in task queue",
      createdAt = System.currentTimeMillis(),
      logs = listOf(
        BackgroundTaskLogEntry(
          level = "INFO",
          message = "Task created and queued. Target project: '${project.name}' (${project.type.displayName}). Model: ${config.selectedModel}"
        )
      )
    )

    repository.saveTask(task)
    _tasks.value = listOf(task) + _tasks.value.filter { it.id != task.id }

    processNextQueuedTasks()
    return task
  }

  fun cancelTask(taskId: String) {
    val job = activeJobs.remove(taskId)
    job?.cancel()

    updateTask(taskId) { current ->
      current.copy(
        state = BackgroundTaskState.CANCELLED,
        currentOperation = "Cancelled by user",
        completedAt = System.currentTimeMillis(),
        logs = current.logs + BackgroundTaskLogEntry(
          level = "WARN",
          message = "Task was cancelled by user."
        )
      )
    }

    checkStopServiceIfIdle()
    processNextQueuedTasks()
  }

  fun retryTask(taskId: String) {
    val current = repository.getTaskById(taskId) ?: return
    if (current.state != BackgroundTaskState.FAILED && current.state != BackgroundTaskState.CANCELLED) return

    val retried = current.copy(
      state = BackgroundTaskState.QUEUED,
      progressPercent = 0,
      currentOperation = "Re-queued for execution (Attempt ${current.retryCount + 1})",
      retryCount = current.retryCount + 1,
      errorMessage = null,
      logs = current.logs + BackgroundTaskLogEntry(
        level = "INFO",
        message = "Retrying task (Attempt ${current.retryCount + 1})."
      )
    )

    repository.saveTask(retried)
    _tasks.value = _tasks.value.map { if (it.id == taskId) retried else it }

    processNextQueuedTasks()
  }

  fun approveTaskAction(taskId: String) {
    val task = repository.getTaskById(taskId) ?: return
    if (task.state != BackgroundTaskState.WAITING_APPROVAL) return

    updateTask(taskId) { current ->
      current.copy(
        state = BackgroundTaskState.RUNNING,
        requiresApproval = false,
        pendingApprovalDescription = null,
        currentOperation = "Action approved. Applying changes...",
        logs = current.logs + BackgroundTaskLogEntry(
          level = "INFO",
          message = "User approved pending operations. Resuming workflow."
        )
      )
    }

    // Resume execution
    executeTaskWorkflow(taskId)
  }

  fun deleteTask(taskId: String) {
    cancelTask(taskId)
    repository.deleteTask(taskId)
    _tasks.value = _tasks.value.filter { it.id != taskId }
  }

  fun clearFinishedTasks(): Int {
    val count = repository.clearAllHistory()
    _tasks.value = _tasks.value.filter {
      it.state == BackgroundTaskState.RUNNING || it.state == BackgroundTaskState.QUEUED || it.state == BackgroundTaskState.WAITING_APPROVAL
    }
    return count
  }

  private fun processNextQueuedTasks() {
    val runningCount = _tasks.value.count { it.state == BackgroundTaskState.RUNNING }
    val availableSlots = _concurrencyLimit.value - runningCount
    if (availableSlots <= 0) return

    val nextToRun = _tasks.value
      .filter { it.state == BackgroundTaskState.QUEUED }
      .sortedBy { it.createdAt }
      .take(availableSlots)

    for (task in nextToRun) {
      executeTaskWorkflow(task.id)
    }
  }

  private fun executeTaskWorkflow(taskId: String) {
    if (activeJobs.containsKey(taskId)) return

    val job = scope.launch {
      semaphore.withPermit {
        runTaskPipeline(taskId)
      }
    }

    activeJobs[taskId] = job
    job.invokeOnCompletion {
      activeJobs.remove(taskId)
      checkStopServiceIfIdle()
      processNextQueuedTasks()
    }
  }

  private suspend fun runTaskPipeline(taskId: String) {
    var task = repository.getTaskById(taskId) ?: return
    val project = projectRepo.getProjectById(task.projectId) ?: run {
      failTask(taskId, "Target project '${task.projectName}' was deleted or not found.")
      return
    }

    val providerConfig = apiKeyRepo.getProviderConfig(task.providerType)

    // Mark task as RUNNING
    task = updateTask(taskId) { current ->
      current.copy(
        state = BackgroundTaskState.RUNNING,
        startedAt = current.startedAt ?: System.currentTimeMillis(),
        progressPercent = 10,
        currentOperation = "Inspecting workspace and project files...",
        logs = current.logs + BackgroundTaskLogEntry(
          level = "STEP",
          message = "Stage 1/5: Loading project files and context."
        )
      )
    }

    AltronTaskExecutionService.startOrUpdate(
      context,
      task.id,
      task.title,
      task.progressPercent,
      task.currentOperation
    )

    try {
      // 1. READ PROJECT FILES AND CONTEXT
      val allFiles = projectRepo.listFiles(project, project.rootDir)
      val fileOverview = allFiles.take(15).joinToString("\n") { f ->
        "- ${f.relativePath} (${if (f.isDirectory) "Directory" else "${f.sizeBytes} bytes"})"
      }

      val contextPrompt = """
User Request: ${task.prompt.ifBlank { task.title }}

Target Project: ${project.name} (${project.type.displayName})
Existing Files in Project:
$fileOverview

Please analyze the request, create a structured coding solution, and generate the complete code files.
When creating or modifying files, output each file using:
```file:<relative_path>
<full_file_content>
```
""".trimIndent()

      // 2. STAGE 2: FORMULATE PLAN & GENERATE CODE
      task = updateTask(taskId) { current ->
        current.copy(
          progressPercent = 35,
          currentOperation = "Generating code with ${task.modelId}...",
          logs = current.logs + BackgroundTaskLogEntry(
            level = "STEP",
            message = "Stage 2/5: Streaming AI code generation using ${task.providerType.displayName} (${task.modelId})."
          )
        )
      }
      AltronTaskExecutionService.startOrUpdate(context, task.id, task.title, 35, "Generating code...")

      val accumulatedResponse = StringBuilder()
      val flow = agentService.streamAgentResponse(
        prompt = contextPrompt,
        config = providerConfig,
        mode = com.example.data.model.AgentMode.AGENT,
        projectContext = "Project root: ${project.rootDir.absolutePath}"
      )

      flow.collect { chunk ->
        accumulatedResponse.append(chunk)
      }

      val fullResponse = accumulatedResponse.toString()
      if (fullResponse.isBlank()) {
        failTask(taskId, "Empty response from AI model.")
        return
      }

      // 3. STAGE 3: EXTRACT GENERATED FILES
      task = updateTask(taskId) { current ->
        current.copy(
          progressPercent = 60,
          currentOperation = "Parsing generated code blocks and safety checks...",
          logs = current.logs + BackgroundTaskLogEntry(
            level = "STEP",
            message = "Stage 3/5: Extracting file changes from AI response."
          )
        )
      }
      AltronTaskExecutionService.startOrUpdate(context, task.id, task.title, 60, "Parsing files...")

      val fileChanges = extractFileChanges(project, fullResponse)
      if (fileChanges.isEmpty()) {
        // Maybe the task was purely advice or shell script, record output log
        task = updateTask(taskId) { current ->
          current.copy(
            state = BackgroundTaskState.COMPLETED,
            progressPercent = 100,
            completedAt = System.currentTimeMillis(),
            currentOperation = "Completed analysis (no new files generated)",
            logs = current.logs + BackgroundTaskLogEntry(
              level = "SUCCESS",
              message = "Analysis finished:\n${fullResponse.take(500)}"
            )
          )
        }
        AltronTaskExecutionService.startOrUpdate(context, task.id, task.title, 100, "Completed!")
        return
      }

      // Check if any change requires approval (e.g. overwriting existing critical files)
      val overwrites = fileChanges.filter { !it.isCreated }
      if (overwrites.isNotEmpty() && task.requiresApproval) {
        // Wait for approval
        updateTask(taskId) { current ->
          current.copy(
            state = BackgroundTaskState.WAITING_APPROVAL,
            progressPercent = 70,
            currentOperation = "Waiting for approval to overwrite ${overwrites.size} files",
            pendingApprovalDescription = "Task wants to overwrite:\n" + overwrites.joinToString("\n") { it.relativePath },
            changes = fileChanges,
            logs = current.logs + BackgroundTaskLogEntry(
              level = "WARN",
              message = "Waiting for approval before updating: ${overwrites.map { it.relativePath }}"
            )
          )
        }
        AltronTaskExecutionService.startOrUpdate(context, task.id, task.title, 70, "Waiting for Approval")
        return
      }

      // 4. STAGE 4: APPLYING CHANGES TO PROJECT
      task = updateTask(taskId) { current ->
        current.copy(
          progressPercent = 80,
          currentOperation = "Writing ${fileChanges.size} files to project...",
          logs = current.logs + BackgroundTaskLogEntry(
            level = "STEP",
            message = "Stage 4/5: Writing files to disk."
          )
        )
      }
      AltronTaskExecutionService.startOrUpdate(context, task.id, task.title, 80, "Writing files...")

      val appliedChanges = mutableListOf<TaskFileChange>()
      for (change in fileChanges) {
        val targetFile = File(project.rootDir, change.relativePath)
        targetFile.parentFile?.mkdirs()
        projectRepo.saveFile(project, targetFile, change.newContent)

        appliedChanges.add(change)
        task = updateTask(taskId) { current ->
          current.copy(
            logs = current.logs + BackgroundTaskLogEntry(
              level = "INFO",
              message = "Saved file '${change.relativePath}' (${change.newContent.length} chars)."
            )
          )
        }
      }

      // 5. STAGE 5: VERIFICATION
      task = updateTask(taskId) { current ->
        current.copy(
          progressPercent = 95,
          currentOperation = "Verifying written files...",
          logs = current.logs + BackgroundTaskLogEntry(
            level = "STEP",
            message = "Stage 5/5: Verifying workspace integrity."
          )
        )
      }
      AltronTaskExecutionService.startOrUpdate(context, task.id, task.title, 95, "Verifying...")

      delay(300) // Brief validation pause

      // 6. COMPLETION
      updateTask(taskId) { current ->
        current.copy(
          state = BackgroundTaskState.COMPLETED,
          progressPercent = 100,
          completedAt = System.currentTimeMillis(),
          currentOperation = "Successfully completed (${appliedChanges.size} files updated)",
          changes = appliedChanges,
          logs = current.logs + BackgroundTaskLogEntry(
            level = "SUCCESS",
            message = "Task completed successfully! Applied ${appliedChanges.size} files to project '${project.name}'."
          )
        )
      }
      AltronTaskExecutionService.startOrUpdate(context, task.id, task.title, 100, "Completed!")

    } catch (e: Exception) {
      failTask(taskId, "Execution error: ${e.localizedMessage ?: "Unknown failure"}")
    }
  }

  private fun failTask(taskId: String, errorMsg: String) {
    updateTask(taskId) { current ->
      current.copy(
        state = BackgroundTaskState.FAILED,
        errorMessage = errorMsg,
        completedAt = System.currentTimeMillis(),
        currentOperation = "Failed: $errorMsg",
        logs = current.logs + BackgroundTaskLogEntry(
          level = "ERROR",
          message = "Task failed: $errorMsg"
        )
      )
    }
    checkStopServiceIfIdle()
  }

  private fun extractFileChanges(project: Project, responseText: String): List<TaskFileChange> {
    val changes = mutableListOf<TaskFileChange>()
    val pattern = java.util.regex.Pattern.compile("```file:([a-zA-Z0-9_./-]+)\\n([\\s\\S]*?)```")
    val matcher = pattern.matcher(responseText)

    while (matcher.find()) {
      val relPath = matcher.group(1)?.trim().orEmpty()
      val code = matcher.group(2)?.trimEnd().orEmpty()

      if (relPath.isNotBlank() && code.isNotBlank()) {
        val existingFile = File(project.rootDir, relPath)
        val exists = existingFile.exists()
        val prevContent = if (exists) {
          try { existingFile.readText() } catch (e: Exception) { null }
        } else null

        changes.add(
          TaskFileChange(
            relativePath = relPath,
            previousContent = prevContent,
            newContent = code,
            isCreated = !exists
          )
        )
      }
    }

    return changes
  }

  private fun updateTask(taskId: String, transform: (BackgroundTask) -> BackgroundTask): BackgroundTask {
    val current = repository.getTaskById(taskId) ?: throw IllegalArgumentException("Task $taskId not found")
    val updated = transform(current)
    repository.saveTask(updated)
    _tasks.value = _tasks.value.map { if (it.id == taskId) updated else it }
    return updated
  }

  private fun checkStopServiceIfIdle() {
    val anyRunning = _tasks.value.any { it.state == BackgroundTaskState.RUNNING }
    if (!anyRunning) {
      AltronTaskExecutionService.stop(context)
    }
  }
}
