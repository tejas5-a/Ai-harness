package com.example.data.repository

import android.content.Context
import com.example.data.model.BackgroundTask
import com.example.data.model.BackgroundTaskLogEntry
import com.example.data.model.BackgroundTaskState
import com.example.data.model.ProviderType
import com.example.data.model.TaskFileChange
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class BackgroundTaskRepository(private val context: Context) {

  private val tasksDir: File = File(context.filesDir, "background_tasks").apply {
    if (!exists()) mkdirs()
  }

  @Synchronized
  fun getAllTasks(): List<BackgroundTask> {
    val list = mutableListOf<BackgroundTask>()
    val files = tasksDir.listFiles { _, name -> name.endsWith(".json") } ?: return emptyList()

    for (file in files) {
      try {
        val jsonStr = file.readText(StandardCharsets.UTF_8)
        val task = parseTaskFromJson(JSONObject(jsonStr))
        list.add(task)
      } catch (e: Exception) {
        // Skip corrupted file
      }
    }

    return list.sortedByDescending { it.createdAt }
  }

  @Synchronized
  fun getTaskById(taskId: String): BackgroundTask? {
    val file = File(tasksDir, "$taskId.json")
    if (!file.exists()) return null
    return try {
      val jsonStr = file.readText(StandardCharsets.UTF_8)
      parseTaskFromJson(JSONObject(jsonStr))
    } catch (e: Exception) {
      null
    }
  }

  @Synchronized
  fun saveTask(task: BackgroundTask) {
    val file = File(tasksDir, "${task.id}.json")
    val jsonObj = taskToJson(task)
    try {
      val tempFile = File(tasksDir, "${task.id}.tmp")
      FileOutputStream(tempFile).use { out ->
        out.write(jsonObj.toString(2).toByteArray(StandardCharsets.UTF_8))
      }
      if (tempFile.exists()) {
        if (file.exists()) file.delete()
        tempFile.renameTo(file)
      }
    } catch (e: Exception) {
      // Fallback write
      try {
        file.writeText(jsonObj.toString(2), StandardCharsets.UTF_8)
      } catch (ignored: Exception) {}
    }
  }

  @Synchronized
  fun deleteTask(taskId: String): Boolean {
    val file = File(tasksDir, "$taskId.json")
    return if (file.exists()) file.delete() else false
  }

  @Synchronized
  fun clearAllHistory(): Int {
    var count = 0
    val files = tasksDir.listFiles { _, name -> name.endsWith(".json") } ?: return 0
    for (file in files) {
      try {
        val jsonStr = file.readText(StandardCharsets.UTF_8)
        val task = parseTaskFromJson(JSONObject(jsonStr))
        if (task.state == BackgroundTaskState.COMPLETED ||
            task.state == BackgroundTaskState.FAILED ||
            task.state == BackgroundTaskState.CANCELLED) {
          if (file.delete()) count++
        }
      } catch (e: Exception) {
        file.delete()
      }
    }
    return count
  }

  private fun taskToJson(task: BackgroundTask): JSONObject {
    val obj = JSONObject()
    obj.put("id", task.id)
    obj.put("title", task.title)
    obj.put("description", task.description)
    obj.put("prompt", task.prompt)
    obj.put("projectId", task.projectId)
    obj.put("projectName", task.projectName)
    obj.put("providerType", task.providerType.name)
    obj.put("modelId", task.modelId)
    obj.put("state", task.state.name)
    obj.put("progressPercent", task.progressPercent)
    obj.put("currentOperation", task.currentOperation)
    obj.put("createdAt", task.createdAt)
    if (task.startedAt != null) obj.put("startedAt", task.startedAt)
    if (task.completedAt != null) obj.put("completedAt", task.completedAt)
    obj.put("requiresApproval", task.requiresApproval)
    if (task.pendingApprovalDescription != null) obj.put("pendingApprovalDescription", task.pendingApprovalDescription)
    if (task.errorMessage != null) obj.put("errorMessage", task.errorMessage)
    obj.put("retryCount", task.retryCount)
    obj.put("maxRetries", task.maxRetries)

    // Logs array
    val logsArr = JSONArray()
    task.logs.forEach { log ->
      logsArr.put(JSONObject().apply {
        put("timestamp", log.timestamp)
        put("level", log.level)
        put("message", log.message)
      })
    }
    obj.put("logs", logsArr)

    // Changes array
    val changesArr = JSONArray()
    task.changes.forEach { ch ->
      changesArr.put(JSONObject().apply {
        put("relativePath", ch.relativePath)
        if (ch.previousContent != null) put("previousContent", ch.previousContent)
        put("newContent", ch.newContent)
        put("isCreated", ch.isCreated)
        put("isDeleted", ch.isDeleted)
      })
    }
    obj.put("changes", changesArr)

    return obj
  }

  private fun parseTaskFromJson(obj: JSONObject): BackgroundTask {
    val logsList = mutableListOf<BackgroundTaskLogEntry>()
    val logsArr = obj.optJSONArray("logs")
    if (logsArr != null) {
      for (i in 0 until logsArr.length()) {
        val lObj = logsArr.getJSONObject(i)
        logsList.add(
          BackgroundTaskLogEntry(
            timestamp = lObj.optLong("timestamp", System.currentTimeMillis()),
            level = lObj.optString("level", "INFO"),
            message = lObj.optString("message", "")
          )
        )
      }
    }

    val changesList = mutableListOf<TaskFileChange>()
    val changesArr = obj.optJSONArray("changes")
    if (changesArr != null) {
      for (i in 0 until changesArr.length()) {
        val cObj = changesArr.getJSONObject(i)
        changesList.add(
          TaskFileChange(
            relativePath = cObj.optString("relativePath", ""),
            previousContent = if (cObj.has("previousContent")) cObj.optString("previousContent") else null,
            newContent = cObj.optString("newContent", ""),
            isCreated = cObj.optBoolean("isCreated", false),
            isDeleted = cObj.optBoolean("isDeleted", false)
          )
        )
      }
    }

    val providerName = obj.optString("providerType", ProviderType.GEMINI.name)
    val provider = try {
      ProviderType.valueOf(providerName)
    } catch (e: Exception) {
      ProviderType.GEMINI
    }

    val stateName = obj.optString("state", BackgroundTaskState.QUEUED.name)
    val state = try {
      BackgroundTaskState.valueOf(stateName)
    } catch (e: Exception) {
      BackgroundTaskState.QUEUED
    }

    return BackgroundTask(
      id = obj.getString("id"),
      title = obj.optString("title", "Background Task"),
      description = obj.optString("description", ""),
      prompt = obj.optString("prompt", ""),
      projectId = obj.optString("projectId", ""),
      projectName = obj.optString("projectName", "Project"),
      providerType = provider,
      modelId = obj.optString("modelId", provider.defaultModel),
      state = state,
      progressPercent = obj.optInt("progressPercent", 0),
      currentOperation = obj.optString("currentOperation", "Idle"),
      createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
      startedAt = if (obj.has("startedAt")) obj.optLong("startedAt") else null,
      completedAt = if (obj.has("completedAt")) obj.optLong("completedAt") else null,
      logs = logsList,
      changes = changesList,
      requiresApproval = obj.optBoolean("requiresApproval", false),
      pendingApprovalDescription = if (obj.has("pendingApprovalDescription")) obj.optString("pendingApprovalDescription") else null,
      errorMessage = if (obj.has("errorMessage")) obj.optString("errorMessage") else null,
      retryCount = obj.optInt("retryCount", 0),
      maxRetries = obj.optInt("maxRetries", 3)
    )
  }
}
