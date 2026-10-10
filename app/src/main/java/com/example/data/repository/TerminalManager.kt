package com.example.data.repository

import com.example.data.model.TerminalEntry
import com.example.data.model.TerminalEntryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class TerminalManager {

  private val _entries = MutableStateFlow<List<TerminalEntry>>(
    listOf(
      TerminalEntry(
        type = TerminalEntryType.SYSTEM_INFO,
        text = "ALTRON Shell Terminal Adapter initialized.\nOS: Android Linux (Toybox/POSIX environment)\nSandbox working directory active.\nType 'help' or tap quick actions below."
      )
    )
  )
  val entries: StateFlow<List<TerminalEntry>> = _entries.asStateFlow()

  private val _isRunning = MutableStateFlow(false)
  val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

  private val dangerousPattern = Pattern.compile(
    "(?i)\\brm\\s+-[a-zA-Z]*[rf]|(?i)\\brm\\s+--recursive|(?i)\\bmkfs|(?i)\\bdd\\s+if=|(?i)\\breboot|(?i)\\bshutdown|(?i)\\bchmod\\s+-[a-zA-Z]*777|(?i)>\\s*/dev/|:\\(\\)\\s*\\{"
  )

  fun isPotentiallyDangerous(command: String): Boolean {
    return dangerousPattern.matcher(command).find()
  }

  suspend fun executeCommand(
    rawCommand: String,
    workingDir: File,
    isApproved: Boolean = false
  ): Int = withContext(Dispatchers.IO) {
    val trimmed = rawCommand.trim()
    if (trimmed.isEmpty()) return@withContext 0

    // Check danger
    if (isPotentiallyDangerous(trimmed) && !isApproved) {
      appendEntry(TerminalEntryType.STDERR, "SECURITY BLOCKED: Command '$trimmed' contains dangerous operations requiring explicit approval.")
      return@withContext -1
    }

    appendEntry(TerminalEntryType.COMMAND_INPUT, "$ $trimmed")
    _isRunning.value = true

    // Special built-in commands
    if (trimmed == "clear") {
      _entries.value = emptyList()
      _isRunning.value = false
      return@withContext 0
    }

    if (trimmed == "help") {
      appendEntry(
        TerminalEntryType.SYSTEM_INFO,
        """ALTRON TERMINAL COMMANDS:
          |  pwd          - Show current project directory
          |  ls -la       - List directory contents in detail
          |  cat <file>   - Output contents of file
          |  df -h        - Display storage filesystem metrics
          |  uname -a     - Display Linux kernel version
          |  id / whoami  - Show current Android user/sandbox ID
          |  echo <text>  - Echo text string
          |  mkdir <dir>  - Create directory in project
          |  clear        - Clear terminal history
        """.trimMargin()
      )
      _isRunning.value = false
      return@withContext 0
    }

    try {
      // Ensure workingDir exists
      if (!workingDir.exists()) {
        workingDir.mkdirs()
      }

      // Execute on real Android Linux process
      val process = ProcessBuilder("/system/bin/sh", "-c", trimmed)
        .directory(workingDir)
        .redirectErrorStream(false)
        .apply {
          environment()["TERM"] = "xterm-256color"
          environment()["HOME"] = workingDir.absolutePath
          environment()["PWD"] = workingDir.absolutePath
        }
        .start()

      // Read standard output
      val outputReader = BufferedReader(InputStreamReader(process.inputStream))
      val errorReader = BufferedReader(InputStreamReader(process.errorStream))

      val stdoutLines = mutableListOf<String>()
      val stderrLines = mutableListOf<String>()

      // Read stdout
      var line: String?
      while (outputReader.readLine().also { line = it } != null) {
        line?.let { stdoutLines.add(it) }
      }

      // Read stderr
      while (errorReader.readLine().also { line = it } != null) {
        line?.let { stderrLines.add(it) }
      }

      val exited = process.waitFor(15, TimeUnit.SECONDS)
      val exitCode = if (exited) process.exitValue() else {
        process.destroy()
        appendEntry(TerminalEntryType.STDERR, "Execution timed out after 15 seconds")
        -9
      }

      if (stdoutLines.isNotEmpty()) {
        appendEntry(TerminalEntryType.STDOUT, stdoutLines.joinToString("\n"))
      }

      if (stderrLines.isNotEmpty()) {
        appendEntry(TerminalEntryType.STDERR, stderrLines.joinToString("\n"))
      }

      if (stdoutLines.isEmpty() && stderrLines.isEmpty()) {
        appendEntry(TerminalEntryType.SYSTEM_INFO, "(Command executed with code $exitCode and no output)")
      } else {
        appendEntry(TerminalEntryType.EXIT_CODE, "[Process completed with exit code: $exitCode]")
      }

      _isRunning.value = false
      return@withContext exitCode
    } catch (e: Exception) {
      appendEntry(TerminalEntryType.STDERR, "Execution failed: ${e.localizedMessage}")
      _isRunning.value = false
      return@withContext -1
    }
  }

  private fun appendEntry(type: TerminalEntryType, text: String) {
    val current = _entries.value.toMutableList()
    current.add(TerminalEntry(type = type, text = text))
    // Keep reasonable history size
    if (current.size > 200) {
      _entries.value = current.takeLast(150)
    } else {
      _entries.value = current
    }
  }

  fun clear() {
    _entries.value = emptyList()
  }
}
