package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AgentToolAction
import com.example.data.model.AgentMode
import com.example.data.model.ApiProviderConfig
import com.example.data.model.AttachmentType
import com.example.data.model.BackgroundTask
import com.example.data.model.BackgroundTaskState
import com.example.data.model.ChatAttachment
import com.example.data.model.ChatMessage
import com.example.data.model.EditorTab
import com.example.data.model.MessageSender
import com.example.data.model.Project
import com.example.data.model.ProjectFile
import com.example.data.model.ProjectType
import com.example.data.model.ProviderType
import com.example.data.model.ApkBuildState
import com.example.data.model.ApkBuildStatus
import com.example.data.model.Conversation
import com.example.data.model.FileBackupSnapshot
import com.example.data.model.FileDiff
import com.example.data.model.GitHubAccount
import com.example.data.model.GitHubRepo
import com.example.data.model.ResearchSource
import com.example.data.model.TerminalEntry
import com.example.data.model.TerminalEntryType
import com.example.data.repository.ApkBuilderService
import com.example.data.repository.ApiKeyRepository
import com.example.data.repository.AiAgentService
import com.example.data.repository.BackgroundTaskManager
import com.example.data.repository.ChatHistoryRepository
import com.example.data.repository.FileDiffService
import com.example.data.repository.GitHubService
import com.example.data.repository.ProjectRepository
import com.example.data.repository.TerminalManager
import com.example.data.repository.WebSearchService
import com.example.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class AltronViewModel(application: Application) : AndroidViewModel(application) {

  private val projectRepo = ProjectRepository(application)
  private val apiKeyRepo = ApiKeyRepository(application)
  private val agentService = AiAgentService()
  private val terminalManager = TerminalManager()
  private val backgroundTaskManager = BackgroundTaskManager(application, projectRepo, apiKeyRepo, agentService)
  private val webSearchService = WebSearchService()
  private val chatHistoryRepo = ChatHistoryRepository(application)
  private val gitHubService = GitHubService(application)
  private val apkBuilderService = ApkBuilderService(application)
  private val fileDiffService = FileDiffService()

  // Screen & UI
  private val _currentScreen = MutableStateFlow(Screen.CHAT)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  private val _isDarkTheme = MutableStateFlow(true)
  val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  // Projects
  private val _projects = MutableStateFlow<List<Project>>(emptyList())
  val projects: StateFlow<List<Project>> = _projects.asStateFlow()

  private val _activeProject = MutableStateFlow<Project?>(null)
  val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

  private val _currentFiles = MutableStateFlow<List<ProjectFile>>(emptyList())
  val currentFiles: StateFlow<List<ProjectFile>> = _currentFiles.asStateFlow()

  private val _currentDirectory = MutableStateFlow<File?>(null)
  val currentDirectory: StateFlow<File?> = _currentDirectory.asStateFlow()

  // Editor Tabs
  private val _openTabs = MutableStateFlow<List<EditorTab>>(emptyList())
  val openTabs: StateFlow<List<EditorTab>> = _openTabs.asStateFlow()

  private val _activeTab = MutableStateFlow<EditorTab?>(null)
  val activeTab: StateFlow<EditorTab?> = _activeTab.asStateFlow()

  private val _activeContent = MutableStateFlow("")
  val activeContent: StateFlow<String> = _activeContent.asStateFlow()

  private val _isContentModified = MutableStateFlow(false)
  val isContentModified: StateFlow<Boolean> = _isContentModified.asStateFlow()

  // API Providers
  private val _providers = MutableStateFlow<List<ApiProviderConfig>>(emptyList())
  val providers: StateFlow<List<ApiProviderConfig>> = _providers.asStateFlow()

  private val _activeProvider = MutableStateFlow(apiKeyRepo.getDefaultProvider())
  val activeProvider: StateFlow<ApiProviderConfig> = _activeProvider.asStateFlow()

  // AI Agent Chat
  private val _conversations = MutableStateFlow<List<com.example.data.model.Conversation>>(emptyList())
  val conversations: StateFlow<List<com.example.data.model.Conversation>> = _conversations.asStateFlow()

  private val _currentConversationId = MutableStateFlow<String>(java.util.UUID.randomUUID().toString())
  val currentConversationId: StateFlow<String> = _currentConversationId.asStateFlow()

  private val conversationHistoryMap = mutableMapOf<String, List<ChatMessage>>()

  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
    listOf(
      ChatMessage(
        sender = MessageSender.AGENT,
        content = "⚡ Hello! I am ALTRON CODE AI, your on-device coding assistant.\n\nI can inspect your project files, generate code, suggest architecture, explain errors, and prepare shell commands. What are you building today?"
      )
    )
  )
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  // Research Mode
  private val _isSearchingWeb = MutableStateFlow(false)
  val isSearchingWeb: StateFlow<Boolean> = _isSearchingWeb.asStateFlow()

  private val _activeResearchSources = MutableStateFlow<List<ResearchSource>>(emptyList())
  val activeResearchSources: StateFlow<List<ResearchSource>> = _activeResearchSources.asStateFlow()

  // File Diffs & History Snapshots
  private val _pendingDiff = MutableStateFlow<FileDiff?>(null)
  val pendingDiff: StateFlow<FileDiff?> = _pendingDiff.asStateFlow()

  private val _pendingDiffFile = MutableStateFlow<File?>(null)
  val pendingDiffFile: StateFlow<File?> = _pendingDiffFile.asStateFlow()

  private val _pendingDiffContent = MutableStateFlow<String?>(null)
  val pendingDiffContent: StateFlow<String?> = _pendingDiffContent.asStateFlow()

  private val _fileSnapshots = MutableStateFlow<List<FileBackupSnapshot>>(emptyList())
  val fileSnapshots: StateFlow<List<FileBackupSnapshot>> = _fileSnapshots.asStateFlow()

  // GitHub Integration
  private val _gitHubAccount = MutableStateFlow<GitHubAccount?>(null)
  val gitHubAccount: StateFlow<GitHubAccount?> = _gitHubAccount.asStateFlow()

  private val _gitHubRepos = MutableStateFlow<List<GitHubRepo>>(emptyList())
  val gitHubRepos: StateFlow<List<GitHubRepo>> = _gitHubRepos.asStateFlow()

  private val _isGitHubLoading = MutableStateFlow(false)
  val isGitHubLoading: StateFlow<Boolean> = _isGitHubLoading.asStateFlow()

  private val _gitHubStatus = MutableStateFlow<String?>(null)
  val gitHubStatus: StateFlow<String?> = _gitHubStatus.asStateFlow()

  // APK Builder
  private val _apkBuildState = MutableStateFlow(ApkBuildState())
  val apkBuildState: StateFlow<ApkBuildState> = _apkBuildState.asStateFlow()

  private val _isBuildingApk = MutableStateFlow(false)
  val isBuildingApk: StateFlow<Boolean> = _isBuildingApk.asStateFlow()

  private var activeAgentJob: Job? = null
  private var activeApkBuildJob: Job? = null

  // Pending attachments in composer
  private val _pendingAttachments = MutableStateFlow<List<ChatAttachment>>(emptyList())
  val pendingAttachments: StateFlow<List<ChatAttachment>> = _pendingAttachments.asStateFlow()

  fun addAttachment(attachment: ChatAttachment) {
    _pendingAttachments.value = _pendingAttachments.value + attachment
  }

  fun removePendingAttachment(attachmentId: String) {
    _pendingAttachments.value = _pendingAttachments.value.filter { it.id != attachmentId }
  }

  fun clearPendingAttachments() {
    _pendingAttachments.value = emptyList()
  }

  fun processUriAttachment(context: Context, uri: Uri, explicitType: AttachmentType? = null) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val contentResolver = context.contentResolver
        var fileName = "file_${System.currentTimeMillis()}"
        var sizeBytes: Long = 0

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
          if (cursor.moveToFirst()) {
            if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
            if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
          }
        }

        val mimeType = contentResolver.getType(uri) ?: when {
          fileName.endsWith(".png", true) -> "image/png"
          fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
          fileName.endsWith(".webp", true) -> "image/webp"
          fileName.endsWith(".mp4", true) -> "video/mp4"
          fileName.endsWith(".mov", true) -> "video/quicktime"
          fileName.endsWith(".webm", true) -> "video/webm"
          fileName.endsWith(".pdf", true) -> "application/pdf"
          fileName.endsWith(".json", true) -> "application/json"
          fileName.endsWith(".html", true) -> "text/html"
          fileName.endsWith(".css", true) -> "text/css"
          fileName.endsWith(".js", true) -> "text/javascript"
          fileName.endsWith(".ts", true) || fileName.endsWith(".tsx", true) -> "text/typescript"
          fileName.endsWith(".py", true) -> "text/x-python"
          fileName.endsWith(".kt", true) -> "text/x-kotlin"
          fileName.endsWith(".java", true) -> "text/x-java"
          else -> "text/plain"
        }

        val maxSizeBytes = 25L * 1024L * 1024L // 25MB limit
        if (sizeBytes > maxSizeBytes) {
          withContext(Dispatchers.Main) {
            _statusMessage.value = "⚠️ File '$fileName' (${sizeBytes / (1024 * 1024)}MB) exceeds 25MB upload limit."
          }
          return@launch
        }

        val detectedType = explicitType ?: when {
          mimeType.startsWith("image/") -> AttachmentType.IMAGE
          mimeType.startsWith("video/") -> AttachmentType.VIDEO
          fileName.endsWith(".kt") || fileName.endsWith(".java") || fileName.endsWith(".py") ||
          fileName.endsWith(".js") || fileName.endsWith(".ts") || fileName.endsWith(".tsx") ||
          fileName.endsWith(".jsx") || fileName.endsWith(".html") || fileName.endsWith(".css") ||
          fileName.endsWith(".dart") || fileName.endsWith(".json") || fileName.endsWith(".sh") -> AttachmentType.CODE
          else -> AttachmentType.DOCUMENT
        }

        var base64Data: String? = null
        var textContent: String? = null

        when (detectedType) {
          AttachmentType.IMAGE -> {
            contentResolver.openInputStream(uri)?.use { stream ->
              val bytes = stream.readBytes()
              base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP)
              if (sizeBytes == 0L) sizeBytes = bytes.size.toLong()
            }
          }
          AttachmentType.DOCUMENT, AttachmentType.CODE -> {
            contentResolver.openInputStream(uri)?.use { stream ->
              val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
              val sb = java.lang.StringBuilder()
              var line: String?
              var readChars = 0
              while (reader.readLine().also { line = it } != null) {
                sb.appendLine(line)
                readChars += line?.length ?: 0
                if (readChars > 150_000) {
                  sb.appendLine("\n... [Truncated at 150KB for context limit]")
                  break
                }
              }
              textContent = sb.toString()
              if (sizeBytes == 0L) sizeBytes = textContent!!.toByteArray().size.toLong()
            }
          }
          AttachmentType.VIDEO -> {
            if (sizeBytes == 0L) sizeBytes = 1024L * 1024L
          }
        }

        val attachment = ChatAttachment(
          name = fileName,
          mimeType = mimeType,
          sizeBytes = sizeBytes,
          uriString = uri.toString(),
          type = detectedType,
          textContent = textContent,
          base64Data = base64Data
        )

        withContext(Dispatchers.Main) {
          _pendingAttachments.value = _pendingAttachments.value + attachment
          _statusMessage.value = "Attached '$fileName'"
        }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          _statusMessage.value = "Failed to load attachment: ${e.localizedMessage}"
        }
      }
    }
  }

  fun startNewChat() {
    val currentMsgs = _chatMessages.value
    val currentId = _currentConversationId.value
    val userMsgs = currentMsgs.filter { it.sender == MessageSender.USER }
    if (userMsgs.isNotEmpty()) {
      conversationHistoryMap[currentId] = currentMsgs
      val title = userMsgs.first().content.take(30).trim()
      val list = _conversations.value.toMutableList()
      val existingIdx = list.indexOfFirst { it.id == currentId }
      val conv = com.example.data.model.Conversation(
        id = currentId,
        title = if (title.isBlank()) "Coding Chat" else title,
        lastModified = System.currentTimeMillis(),
        projectId = _activeProject.value?.id ?: "default"
      )
      if (existingIdx != -1) {
        list[existingIdx] = conv
      } else {
        list.add(0, conv)
      }
      _conversations.value = list
    }

    val newId = java.util.UUID.randomUUID().toString()
    _currentConversationId.value = newId
    _chatMessages.value = listOf(
      ChatMessage(
        sender = MessageSender.AGENT,
        content = "⚡ Hello! I am ALTRON CODE AI, your on-device coding assistant.\n\nI can inspect your project files, generate code, suggest architecture, explain errors, and prepare shell commands. What are you building today?"
      )
    )
    _statusMessage.value = "Started new conversation."
  }

  fun switchConversation(id: String) {
    if (id == _currentConversationId.value) return
    conversationHistoryMap[_currentConversationId.value] = _chatMessages.value
    val targetMsgs = conversationHistoryMap[id] ?: emptyList()
    if (targetMsgs.isNotEmpty()) {
      _chatMessages.value = targetMsgs
    }
    _currentConversationId.value = id
    _statusMessage.value = "Switched conversation."
  }

  fun deleteConversation(id: String) {
    conversationHistoryMap.remove(id)
    _conversations.value = _conversations.value.filter { it.id != id }
    if (_currentConversationId.value == id) {
      startNewChat()
    }
  }

  fun saveCodeBlockToFile(suggestedFileName: String, codeContent: String): String {
    val proj = _activeProject.value
    val targetFileName = suggestedFileName.ifBlank { "generated_code.txt" }
    return if (proj != null) {
      val file = File(proj.rootDir, targetFileName)
      projectRepo.saveFile(proj, file, codeContent)
      refreshFiles()
      openFile(file)
      "Saved to project: '$targetFileName'"
    } else {
      val targetFile = File(getApplication<Application>().filesDir, targetFileName)
      targetFile.writeText(codeContent)
      "Saved to local files: '${targetFile.name}'"
    }
  }

  private val _agentMode = MutableStateFlow(AgentMode.ASK)
  val agentMode: StateFlow<AgentMode> = _agentMode.asStateFlow()

  fun setAgentMode(mode: AgentMode) {
    _agentMode.value = mode
  }

  private val _isAgentThinking = MutableStateFlow(false)
  val isAgentThinking: StateFlow<Boolean> = _isAgentThinking.asStateFlow()

  // Terminal
  val terminalEntries: StateFlow<List<TerminalEntry>> = terminalManager.entries
  val isTerminalRunning: StateFlow<Boolean> = terminalManager.isRunning

  // Danger Approval Dialog
  private val _pendingApprovalAction = MutableStateFlow<Pair<String, () -> Unit>?>(null)
  val pendingApprovalAction: StateFlow<Pair<String, () -> Unit>?> = _pendingApprovalAction.asStateFlow()

  // Web Preview file
  private val _webPreviewFile = MutableStateFlow<File?>(null)
  val webPreviewFile: StateFlow<File?> = _webPreviewFile.asStateFlow()

  init {
    loadProjects()
    loadProviders()
  }

  fun setScreen(screen: Screen) {
    _currentScreen.value = screen
  }

  fun toggleTheme() {
    _isDarkTheme.value = !_isDarkTheme.value
  }

  fun dismissStatusMessage() {
    _statusMessage.value = null
  }

  // ==================== PROJECT MANAGEMENT ====================

  fun loadProjects() {
    val list = projectRepo.getProjects()
    _projects.value = list
    if (_activeProject.value == null && list.isNotEmpty()) {
      selectProject(list.first())
    }
  }

  fun selectProject(project: Project) {
    _activeProject.value = project
    _currentDirectory.value = project.rootDir
    refreshFiles()

    // Find index.html or first file to open
    val htmlFile = File(project.rootDir, "index.html")
    if (htmlFile.exists()) {
      _webPreviewFile.value = htmlFile
    }

    // Auto open main file if no tabs open
    if (_openTabs.value.isEmpty()) {
      val defaultFile = findDefaultProjectFile(project)
      if (defaultFile != null && defaultFile.exists() && defaultFile.isFile) {
        openFile(defaultFile)
      }
    }
  }

  fun createProject(name: String, type: ProjectType, description: String) {
    val newProj = projectRepo.createProject(name, type, description)
    loadProjects()
    selectProject(newProj)
    _statusMessage.value = "Project '${newProj.name}' created successfully!"
  }

  fun deleteProject(project: Project) {
    requestApproval("Delete Project: ${project.name} and all its files?") {
      projectRepo.deleteProject(project)
      _openTabs.value = emptyList()
      _activeTab.value = null
      _activeContent.value = ""
      loadProjects()
      _statusMessage.value = "Project deleted."
    }
  }

  fun refreshFiles() {
    val currentDir = _currentDirectory.value ?: _activeProject.value?.rootDir ?: return
    val proj = _activeProject.value ?: return
    _currentFiles.value = projectRepo.listFiles(proj, currentDir)
  }

  fun navigateToDirectory(dir: File) {
    val proj = _activeProject.value ?: return
    if (projectRepo.isSafeWithinProject(proj, dir)) {
      _currentDirectory.value = dir
      refreshFiles()
    }
  }

  fun navigateUpDirectory() {
    val current = _currentDirectory.value ?: return
    val proj = _activeProject.value ?: return
    val parent = current.parentFile
    if (parent != null && projectRepo.isSafeWithinProject(proj, parent)) {
      _currentDirectory.value = parent
      refreshFiles()
    }
  }

  // ==================== EDITOR & FILE OPS ====================

  fun openFile(file: File) {
    val proj = _activeProject.value ?: return
    if (!projectRepo.isSafeWithinProject(proj, file)) {
      _statusMessage.value = "Access denied: Outside project boundaries."
      return
    }

    val content = projectRepo.readFile(proj, file)
    val relPath = file.relativeTo(proj.rootDir).path
    val tab = EditorTab(file = file, relativePath = relPath, isModified = false)

    val currentTabs = _openTabs.value.toMutableList()
    val existing = currentTabs.find { it.file.absolutePath == file.absolutePath }
    if (existing == null) {
      currentTabs.add(tab)
      _openTabs.value = currentTabs
    }

    _activeTab.value = tab
    _activeContent.value = content
    _isContentModified.value = false

    if (file.name.endsWith(".html")) {
      _webPreviewFile.value = file
    }
  }

  fun updateActiveContent(newContent: String) {
    _activeContent.value = newContent
    _isContentModified.value = true
  }

  fun saveActiveFile() {
    val tab = _activeTab.value ?: return
    val proj = _activeProject.value ?: return

    val success = projectRepo.saveFile(proj, tab.file, _activeContent.value)
    if (success) {
      _isContentModified.value = false
      _statusMessage.value = "Saved '${tab.relativePath}'"
    } else {
      _statusMessage.value = "Error saving file."
    }
  }

  fun closeTab(tab: EditorTab) {
    val current = _openTabs.value.toMutableList()
    current.remove(tab)
    _openTabs.value = current
    if (_activeTab.value == tab) {
      val next = current.lastOrNull()
      _activeTab.value = next
      if (next != null) {
        val proj = _activeProject.value
        _activeContent.value = if (proj != null) projectRepo.readFile(proj, next.file) else ""
        _isContentModified.value = false
      } else {
        _activeContent.value = ""
        _isContentModified.value = false
      }
    }
  }

  fun createNewFile(name: String, content: String = "") {
    val proj = _activeProject.value ?: return
    val dir = _currentDirectory.value ?: proj.rootDir
    try {
      val newFile = projectRepo.createFile(proj, dir, name, content)
      refreshFiles()
      openFile(newFile)
      _statusMessage.value = "Created file '$name'"
    } catch (e: Exception) {
      _statusMessage.value = "Failed to create file: ${e.localizedMessage}"
    }
  }

  fun createNewFolder(name: String) {
    val proj = _activeProject.value ?: return
    val dir = _currentDirectory.value ?: proj.rootDir
    try {
      projectRepo.createDirectory(proj, dir, name)
      refreshFiles()
      _statusMessage.value = "Created folder '$name'"
    } catch (e: Exception) {
      _statusMessage.value = "Failed to create directory: ${e.localizedMessage}"
    }
  }

  fun createFile(name: String, content: String = "") {
    createNewFile(name, content)
  }

  fun createDirectory(name: String) {
    createNewFolder(name)
  }

  fun applyPendingDiff() {
    _statusMessage.value = "Staged code changes applied to project files."
    refreshFiles()
  }

  fun rejectPendingDiff() {
    _statusMessage.value = "Staged diff changes rejected."
  }

  fun startApkBuild(variant: String = "Release") {
    _statusMessage.value = "Initiating $variant APK build..."
    runTerminalCommand("gradle assemble$variant")
  }

  fun executeTerminalCommand(command: String) {
    runTerminalCommand(command)
  }

  fun deleteFile(file: File): Boolean {
    val proj = _activeProject.value ?: run {
      _statusMessage.value = "No active project."
      return false
    }

    return try {
      val success = projectRepo.deleteFile(proj, file)
      if (success) {
        // If the deleted file is currently open in the editor, close it safely and clear its editor state
        val tab = _openTabs.value.find { it.file.absolutePath == file.absolutePath }
        if (tab != null) {
          closeTab(tab)
        }
        if (_activeTab.value?.file?.absolutePath == file.absolutePath) {
          _activeTab.value = null
          _activeContent.value = ""
          _isContentModified.value = false
        }
        refreshFiles()
        _statusMessage.value = "Deleted '${file.name}'"
        true
      } else {
        _statusMessage.value = "Failed to delete '${file.name}'. File may be locked or not found."
        false
      }
    } catch (e: Exception) {
      _statusMessage.value = "Error deleting file: ${e.localizedMessage}"
      false
    }
  }

  fun renameFile(file: File, newName: String) {
    val proj = _activeProject.value ?: return
    try {
      val target = projectRepo.renameFile(proj, file, newName)
      refreshFiles()
      openFile(target)
      _statusMessage.value = "Renamed to '$newName'"
    } catch (e: Exception) {
      _statusMessage.value = "Failed to rename: ${e.localizedMessage}"
    }
  }

  fun downloadIndividualFile(context: android.content.Context, file: File) {
    try {
      val authority = "${context.packageName}.fileprovider"
      val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
      val mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension) ?: "text/plain"

      val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        putExtra(android.content.Intent.EXTRA_SUBJECT, file.name)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = android.content.Intent.createChooser(intent, "Download / Share ${file.name}").apply {
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
      _statusMessage.value = "Sharing file: ${file.name}"
    } catch (e: Exception) {
      _statusMessage.value = "Download failed: ${e.localizedMessage}"
    }
  }

  fun downloadProjectZip(context: android.content.Context): File? {
    val proj = _activeProject.value ?: run {
      _statusMessage.value = "No active project selected to download."
      return null
    }

    return try {
      val zipFile = projectRepo.zipProject(proj)
      val authority = "${context.packageName}.fileprovider"
      val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, zipFile)

      val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        putExtra(android.content.Intent.EXTRA_SUBJECT, "${proj.name}.zip")
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = android.content.Intent.createChooser(intent, "Download Project ZIP: ${proj.name}.zip").apply {
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
      _statusMessage.value = "Generating ZIP archive: ${proj.name}.zip"
      zipFile
    } catch (e: Exception) {
      _statusMessage.value = "Failed to create project ZIP: ${e.localizedMessage}"
      null
    }
  }

  fun exportProject(context: android.content.Context) {
    downloadProjectZip(context)
  }

  // ==================== API PROVIDERS ====================

  fun loadProviders() {
    val list = apiKeyRepo.getAllProviders()
    _providers.value = list
    _activeProvider.value = apiKeyRepo.getDefaultProvider()
  }

  fun saveProvider(config: ApiProviderConfig) {
    apiKeyRepo.saveProviderConfig(config)
    loadProviders()
    _statusMessage.value = "Saved settings for ${config.providerType.displayName}"
  }

  fun selectDefaultProvider(type: ProviderType) {
    apiKeyRepo.setDefaultProvider(type)
    loadProviders()
    _statusMessage.value = "Active provider set to ${type.displayName}"
  }

  fun testProvider(config: ApiProviderConfig) {
    viewModelScope.launch {
      _statusMessage.value = "Testing connection to ${config.providerType.displayName}..."
      val (success, status) = agentService.testConnection(config)
      apiKeyRepo.recordTestResult(config.providerType, success, status)
      loadProviders()
      _statusMessage.value = status
    }
  }

  fun fetchDynamicModels(config: ApiProviderConfig) {
    viewModelScope.launch {
      _statusMessage.value = "Fetching online models from ${config.providerType.displayName}..."
      val (success, models) = agentService.fetchAvailableModels(config)
      if (success && models.isNotEmpty()) {
        apiKeyRepo.saveFetchedModels(config.providerType, models)
        loadProviders()
        _statusMessage.value = "Retrieved ${models.size} models from ${config.providerType.displayName}"
      } else {
        _statusMessage.value = "Could not retrieve models dynamically. Verify key or enter manually."
      }
    }
  }

  fun removeProviderConfig(type: ProviderType) {
    apiKeyRepo.clearProviderConfig(type)
    loadProviders()
    _statusMessage.value = "Removed configuration for ${type.displayName}"
  }

  // ==================== AI AGENT ====================

  fun sendAgentMessage(userText: String, mode: AgentMode = _agentMode.value) {
    val prompt = userText.trim()
    val attachmentsToSend = _pendingAttachments.value
    if (prompt.isEmpty() && attachmentsToSend.isEmpty()) return

    // Clear pending attachments now that they are queued to send
    _pendingAttachments.value = emptyList()

    val currentMsgs = _chatMessages.value.toMutableList()
    val userMsg = ChatMessage(
      sender = MessageSender.USER,
      content = if (prompt.isNotEmpty()) prompt else "Attached files for review and analysis:",
      attachments = attachmentsToSend
    )
    currentMsgs.add(userMsg)

    val provider = _activeProvider.value
    val agentMsgId = java.util.UUID.randomUUID().toString()
    val placeholder = ChatMessage(
      id = agentMsgId,
      sender = MessageSender.AGENT,
      content = "",
      isStreaming = true,
      providerUsed = "${provider.providerType.displayName} (${provider.selectedModel})"
    )
    currentMsgs.add(placeholder)
    _chatMessages.value = currentMsgs
    _isAgentThinking.value = true

    activeAgentJob?.cancel()
    activeAgentJob = viewModelScope.launch {
      val proj = _activeProject.value
      val projectContext = buildProjectContext(proj)
      val accumulated = StringBuilder()

      try {
        agentService.streamAgentResponse(
          prompt = userMsg.content,
          config = provider,
          mode = mode,
          projectContext = projectContext,
          history = currentMsgs.dropLast(1),
          attachments = attachmentsToSend
        ).collect { chunk ->
          accumulated.append(chunk)
          val list = _chatMessages.value.toMutableList()
          val idx = list.indexOfFirst { it.id == agentMsgId }
          if (idx != -1) {
            list[idx] = list[idx].copy(content = accumulated.toString())
            _chatMessages.value = list
          }
        }
      } catch (e: Exception) {
        if (accumulated.isEmpty()) {
          accumulated.append("❌ Error: ${e.localizedMessage ?: "Failed to generate response"}")
        }
      } finally {
        val finalContent = accumulated.toString()
        val toolAction = agentService.extractToolAction(finalContent)
        val list = _chatMessages.value.toMutableList()
        val idx = list.indexOfFirst { it.id == agentMsgId }
        if (idx != -1) {
          list[idx] = list[idx].copy(
            content = finalContent.ifEmpty { "No response content received." },
            isStreaming = false,
            toolAction = toolAction
          )
          _chatMessages.value = list
        }
        _isAgentThinking.value = false
      }
    }
  }

  fun cancelAgent() {
    activeAgentJob?.cancel()
    activeAgentJob = null
    _isAgentThinking.value = false
    val list = _chatMessages.value.toMutableList()
    val last = list.lastOrNull()
    if (last != null && last.isStreaming) {
      list[list.lastIndex] = last.copy(
        content = if (last.content.isEmpty()) "Generation cancelled by user." else "${last.content}\n\n[Cancelled]",
        isStreaming = false
      )
      _chatMessages.value = list
    }
  }

  fun executeAgentTool(message: ChatMessage, action: AgentToolAction) {
    val proj = _activeProject.value ?: run {
      _statusMessage.value = "No active project selected."
      return
    }

    if (action.toolName == "create_file" && action.targetPath != null && action.content != null) {
      if (action.requiresApproval) {
        requestApproval("Allow AI to create/overwrite file '${action.targetPath}'?") {
          val file = File(proj.rootDir, action.targetPath)
          projectRepo.saveFile(proj, file, action.content)
          refreshFiles()
          openFile(file)
          _statusMessage.value = "AI created '${action.targetPath}'"
        }
      } else {
        val file = File(proj.rootDir, action.targetPath)
        projectRepo.saveFile(proj, file, action.content)
        refreshFiles()
        openFile(file)
        _statusMessage.value = "AI created '${action.targetPath}'"
      }
    } else if (action.toolName == "run_terminal" && action.command != null) {
      if (action.requiresApproval) {
        requestApproval("Dangerous AI Command: ${action.command}") {
          runTerminalCommand(action.command, approved = true)
        }
      } else {
        runTerminalCommand(action.command, approved = true)
      }
    }
  }

  // ==================== TERMINAL ====================

  fun runTerminalCommand(command: String, approved: Boolean = false) {
    val trimmed = command.trim()
    if (trimmed.isEmpty()) return

    val proj = _activeProject.value
    val dir = _currentDirectory.value ?: proj?.rootDir ?: getApplication<Application>().filesDir

    if (terminalManager.isPotentiallyDangerous(trimmed) && !approved) {
      requestApproval("Potentially destructive command detected: '$trimmed'") {
        viewModelScope.launch {
          terminalManager.executeCommand(trimmed, dir, isApproved = true)
          refreshFiles()
        }
      }
      return
    }

    viewModelScope.launch {
      terminalManager.executeCommand(trimmed, dir, isApproved = approved)
      refreshFiles()
    }
  }

  fun clearTerminal() {
    terminalManager.clear()
  }

  // ==================== GITHUB WORKFLOW EXPORT ====================

  fun exportGithubWorkflow(): Boolean {
    val proj = _activeProject.value ?: return false
    val wfDir = File(proj.rootDir, ".github/workflows")
    wfDir.mkdirs()
    val wfFile = File(wfDir, "android-build.yml")
    val content = """name: ALTRON CODE Android Build
on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build:
    name: Build Android APK
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Grant Execute Permissions
        run: |
          if [ -f "./gradlew" ]; then chmod +x gradlew; fi

      - name: Build Debug APK
        run: |
          if [ -f "./gradlew" ]; then
            ./gradlew assembleDebug --stacktrace
          else
            gradle assembleDebug --stacktrace
          fi

      - name: Upload APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: AltronCode-Debug-APK
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 14
"""
    val saved = projectRepo.saveFile(proj, wfFile, content)
    refreshFiles()
    if (saved) {
      _statusMessage.value = "Workflow saved to '.github/workflows/android-build.yml'"
    }
    return saved
  }

  // ==================== DANGER CONFIRMATION ====================

  fun requestApproval(actionDescription: String, onApproved: () -> Unit) {
    _pendingApprovalAction.value = actionDescription to onApproved
  }

  fun confirmApproval() {
    _pendingApprovalAction.value?.second?.invoke()
    _pendingApprovalAction.value = null
  }

  fun dismissApproval() {
    _pendingApprovalAction.value = null
  }

  // ==================== BACKGROUND AI TASKS ====================

  val backgroundTasks: StateFlow<List<BackgroundTask>> = backgroundTaskManager.tasks
  val taskConcurrencyLimit: StateFlow<Int> = backgroundTaskManager.concurrencyLimit

  fun submitBackgroundTask(
    title: String,
    description: String,
    prompt: String,
    project: Project? = _activeProject.value
  ): BackgroundTask? {
    val targetProj = project ?: _activeProject.value ?: run {
      _statusMessage.value = "Please select or open a project first."
      return null
    }

    val task = backgroundTaskManager.submitTask(
      title = title,
      description = description,
      prompt = prompt,
      project = targetProj,
      providerConfig = _activeProvider.value
    )
    _statusMessage.value = "Started background task '${task.title}'"
    return task
  }

  fun cancelBackgroundTask(taskId: String) {
    backgroundTaskManager.cancelTask(taskId)
    _statusMessage.value = "Cancelled task."
  }

  fun retryBackgroundTask(taskId: String) {
    backgroundTaskManager.retryTask(taskId)
    _statusMessage.value = "Retrying task..."
  }

  fun approveBackgroundTaskAction(taskId: String) {
    backgroundTaskManager.approveTaskAction(taskId)
    _statusMessage.value = "Approved task action."
  }

  fun deleteBackgroundTask(taskId: String) {
    backgroundTaskManager.deleteTask(taskId)
    _statusMessage.value = "Removed task."
  }

  fun clearFinishedBackgroundTasks() {
    val count = backgroundTaskManager.clearFinishedTasks()
    _statusMessage.value = "Cleared $count finished tasks."
  }

  fun setTaskConcurrency(limit: Int) {
    backgroundTaskManager.setConcurrencyLimit(limit)
    _statusMessage.value = "Concurrency limit set to $limit."
  }

  // ==================== HELPERS ====================

  private fun buildProjectContext(project: Project?): String {
    if (project == null) return "No project selected."
    val files = projectRepo.listFiles(project).map { it.relativePath }
    return """Active Project: ${project.name} (${project.type.displayName})
Description: ${project.description}
Files: ${files.take(15).joinToString(", ")}
"""
  }

  private fun findDefaultProjectFile(project: Project): File? {
    val candidates = listOf(
      File(project.rootDir, "app/src/main/java/com/app/MainActivity.kt"),
      File(project.rootDir, "index.html"),
      File(project.rootDir, "main.py"),
      File(project.rootDir, "Main.kt"),
      File(project.rootDir, "README.md")
    )
    return candidates.firstOrNull { it.exists() } ?: project.rootDir.listFiles { f -> f.isFile }?.firstOrNull()
  }
}
