package com.example.data.repository

import com.example.data.model.DiffLine
import com.example.data.model.DiffLineType
import com.example.data.model.FileBackupSnapshot
import com.example.data.model.FileDiff
import com.example.data.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * FileDiffService: Inspects and calculates line-by-line diffs between original and proposed files,
 * creates automatic pre-edit backup snapshots under .altron_history, and enables safe file restoration.
 */
class FileDiffService {

  fun computeDiff(relativePath: String, originalContent: String, proposedContent: String): FileDiff {
    val oldLines = originalContent.lines()
    val newLines = proposedContent.lines()

    val diffLines = mutableListOf<DiffLine>()
    var additions = 0
    var deletions = 0

    var oldIndex = 0
    var newIndex = 0

    // Simple LCS-based or line comparison diff generator
    while (oldIndex < oldLines.size || newIndex < newLines.size) {
      if (oldIndex < oldLines.size && newIndex < newLines.size && oldLines[oldIndex] == newLines[newIndex]) {
        diffLines.add(
          DiffLine(
            type = DiffLineType.UNCHANGED,
            content = oldLines[oldIndex],
            oldLineNum = oldIndex + 1,
            newLineNum = newIndex + 1
          )
        )
        oldIndex++
        newIndex++
      } else {
        // Look ahead in newLines for match with oldLines[oldIndex]
        val matchInNew = if (oldIndex < oldLines.size) {
          (newIndex until minOf(newIndex + 6, newLines.size)).find { newLines[it] == oldLines[oldIndex] }
        } else null

        // Look ahead in oldLines for match with newLines[newIndex]
        val matchInOld = if (newIndex < newLines.size) {
          (oldIndex until minOf(oldIndex + 6, oldLines.size)).find { oldLines[it] == newLines[newIndex] }
        } else null

        when {
          matchInNew != null && (matchInOld == null || matchInNew - newIndex <= matchInOld - oldIndex) -> {
            // Lines in newLines were added
            while (newIndex < matchInNew) {
              diffLines.add(
                DiffLine(
                  type = DiffLineType.ADDED,
                  content = newLines[newIndex],
                  oldLineNum = null,
                  newLineNum = newIndex + 1
                )
              )
              additions++
              newIndex++
            }
          }
          matchInOld != null -> {
            // Lines in oldLines were removed
            while (oldIndex < matchInOld) {
              diffLines.add(
                DiffLine(
                  type = DiffLineType.REMOVED,
                  content = oldLines[oldIndex],
                  oldLineNum = oldIndex + 1,
                  newLineNum = null
                )
              )
              deletions++
              oldIndex++
            }
          }
          else -> {
            // Both differed
            if (oldIndex < oldLines.size) {
              diffLines.add(
                DiffLine(
                  type = DiffLineType.REMOVED,
                  content = oldLines[oldIndex],
                  oldLineNum = oldIndex + 1,
                  newLineNum = null
                )
              )
              deletions++
              oldIndex++
            }
            if (newIndex < newLines.size) {
              diffLines.add(
                DiffLine(
                  type = DiffLineType.ADDED,
                  content = newLines[newIndex],
                  oldLineNum = null,
                  newLineNum = newIndex + 1
                )
              )
              additions++
              newIndex++
            }
          }
        }
      }
    }

    return FileDiff(
      relativePath = relativePath,
      lines = diffLines,
      additionsCount = additions,
      deletionsCount = deletions
    )
  }

  suspend fun createSnapshot(project: Project, file: File, reason: String = "AI Edit Backup"): FileBackupSnapshot? =
    withContext(Dispatchers.IO) {
      if (!file.exists() || !file.isFile) return@withContext null
      try {
        val relPath = file.relativeTo(project.rootDir).path
        val historyDir = File(project.rootDir, ".altron_history").apply { if (!exists()) mkdirs() }
        val snapshotId = "${System.currentTimeMillis()}_${file.name}"
        val snapshotFile = File(historyDir, snapshotId)
        val content = file.readText()
        snapshotFile.writeText(content)

        FileBackupSnapshot(
          id = snapshotId,
          relativePath = relPath,
          content = content,
          timestamp = System.currentTimeMillis(),
          reason = reason
        )
      } catch (e: Exception) {
        null
      }
    }

  suspend fun restoreSnapshot(project: Project, snapshot: FileBackupSnapshot): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val targetFile = File(project.rootDir, snapshot.relativePath)
        targetFile.parentFile?.mkdirs()
        targetFile.writeText(snapshot.content)
        true
      } catch (e: Exception) {
        false
      }
    }

  suspend fun listSnapshots(project: Project): List<FileBackupSnapshot> = withContext(Dispatchers.IO) {
    val historyDir = File(project.rootDir, ".altron_history")
    if (!historyDir.exists()) return@withContext emptyList()
    val files = historyDir.listFiles() ?: return@withContext emptyList()
    files.mapNotNull { f ->
      try {
        val timestamp = f.name.substringBefore("_").toLongOrNull() ?: f.lastModified()
        val originalName = f.name.substringAfter("_", f.name)
        FileBackupSnapshot(
          id = f.name,
          relativePath = originalName,
          content = f.readText(),
          timestamp = timestamp,
          reason = "History version"
        )
      } catch (e: Exception) {
        null
      }
    }.sortedByDescending { it.timestamp }
  }
}
