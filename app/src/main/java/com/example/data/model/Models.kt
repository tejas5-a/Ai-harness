package com.example.data.model

import java.io.File
import java.util.UUID

enum class ProjectType(val displayName: String, val badge: String) {
  ANDROID("Android Compose", "KOTLIN"),
  ANDROID_KOTLIN("Android Compose", "KOTLIN"),
  WEB("Web App (HTML/JS/CSS)", "WEB"),
  PYTHON("Python Script", "PY"),
  KOTLIN("Kotlin Script", "KT"),
  NODE("Node.js / Express", "NODE"),
  NODEJS("Node.js / Express", "NODE"),
  CUSTOM("Custom Project", "SRC")
}

data class Project(
  val id: String,
  val name: String,
  val description: String,
  val type: ProjectType,
  val rootDir: File,
  val createdAt: Long = System.currentTimeMillis(),
  val lastModified: Long = System.currentTimeMillis(),
  val gitRepoUrl: String? = null,
  val aiInstructions: String = "",
  val preferredModel: String? = null
)

data class ProjectFile(
  val file: File,
  val name: String,
  val relativePath: String,
  val isDirectory: Boolean,
  val sizeBytes: Long,
  val extension: String
)

enum class ProviderType(
  val displayName: String,
  val defaultBaseUrl: String,
  val defaultModel: String,
  val verifiedModels: List<String>,
  val docsUrl: String,
  val defaultCapabilities: String
) {
  GEMINI(
    displayName = "Google Gemini",
    defaultBaseUrl = "https://generativelanguage.googleapis.com",
    defaultModel = "gemini-3.8-flash",
    verifiedModels = listOf(
      "gemini-3.8-flash",
      "gemini-2.0-flash",
      "gemini-1.5-flash",
      "gemini-1.5-pro"
    ),
    docsUrl = "https://aistudio.google.com/app/apikey",
    defaultCapabilities = "Multimodal, Fast Reasoning, Large Context (1M+)"
  ),
  OPENAI(
    displayName = "OpenAI",
    defaultBaseUrl = "https://api.openai.com/v1",
    defaultModel = "gpt-4o-mini",
    verifiedModels = listOf(
      "gpt-4o",
      "gpt-4o-mini",
      "o3-mini",
      "o1",
      "gpt-4-turbo"
    ),
    docsUrl = "https://platform.openai.com/api-keys",
    defaultCapabilities = "Function Calling, Complex Reasoning, Code Generation"
  ),
  DEEPSEEK(
    displayName = "DeepSeek AI",
    defaultBaseUrl = "https://api.deepseek.com",
    defaultModel = "deepseek-chat",
    verifiedModels = listOf(
      "deepseek-chat",
      "deepseek-reasoner"
    ),
    docsUrl = "https://platform.deepseek.com/api_keys",
    defaultCapabilities = "V3 671B MoE, R1 Chain-of-Thought Reasoning, Low Cost"
  ),
  OPENROUTER(
    displayName = "OpenRouter",
    defaultBaseUrl = "https://openrouter.ai/api/v1",
    defaultModel = "google/gemini-3.8-flash",
    verifiedModels = listOf(
      "google/gemini-3.8-flash",
      "anthropic/claude-3.7-sonnet",
      "anthropic/claude-3.5-sonnet",
      "deepseek/deepseek-r1",
      "deepseek/deepseek-chat",
      "openai/gpt-4o-mini",
      "meta-llama/llama-3.3-70b-instruct"
    ),
    docsUrl = "https://openrouter.ai/keys",
    defaultCapabilities = "Unified Multi-Provider Gateway, Auto-Fallback"
  ),
  CLAUDE(
    displayName = "Anthropic Claude",
    defaultBaseUrl = "https://api.anthropic.com/v1",
    defaultModel = "claude-3-5-sonnet-20241022",
    verifiedModels = listOf(
      "claude-3-7-sonnet-20250219",
      "claude-3-5-sonnet-20241022",
      "claude-3-5-haiku-20241022",
      "claude-3-opus-20240229"
    ),
    docsUrl = "https://console.anthropic.com/settings/keys",
    defaultCapabilities = "Hybrid Reasoning, Superior Coding, Artifacts"
  ),
  GROQ(
    displayName = "Groq LPU",
    defaultBaseUrl = "https://api.groq.com/openai/v1",
    defaultModel = "llama-3.3-70b-versatile",
    verifiedModels = listOf(
      "llama-3.3-70b-versatile",
      "llama-3.1-8b-instant",
      "qwen-2.5-coder-32b",
      "deepseek-r1-distill-llama-70b"
    ),
    docsUrl = "https://console.groq.com/keys",
    defaultCapabilities = "Ultra-Fast Inference (500+ tok/s), Open Models"
  ),
  CUSTOM_COMPATIBLE(
    displayName = "Custom Compatible API",
    defaultBaseUrl = "http://10.0.2.2:11434/v1",
    defaultModel = "default",
    verifiedModels = listOf(
      "default",
      "qwen2.5-coder",
      "llama3.2",
      "deepseek-r1:7b"
    ),
    docsUrl = "https://ollama.com",
    defaultCapabilities = "Self-Hosted, Ollama, vLLM, LM Studio, Private"
  )
}

data class ApiProviderConfig(
  val providerType: ProviderType,
  val apiKey: String = "",
  val baseUrl: String = providerType.defaultBaseUrl,
  val selectedModel: String = providerType.defaultModel,
  val isEnabled: Boolean = false,
  val isDefault: Boolean = false,
  val lastTestedStatus: String? = null,
  val lastTestedSuccess: Boolean? = null,
  val dynamicModels: List<String> = emptyList(),
  val capabilities: String = providerType.defaultCapabilities,
  val researchModel: String = providerType.defaultModel,
  val codingModel: String = providerType.defaultModel
)

enum class MessageSender {
  USER,
  AGENT,
  SYSTEM
}

enum class AgentMode(val displayName: String, val badge: String, val tagLine: String) {
  RESEARCH("Research", "WEB", "Live internet search & verified source synthesis"),
  CODE("Coder", "CODE", "Inspect, propose diffs, edit & generate project code"),
  AGENT("Agent", "AUTO", "Autonomous tasks, tool executions & terminal commands"),
  ASK("Ask", "CHAT", "Direct conceptual explanations & fast queries")
}

data class ResearchSource(
  val title: String,
  val url: String,
  val snippet: String
)

data class ResearchResult(
  val query: String,
  val sources: List<ResearchSource>,
  val synthesis: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class AgentToolAction(
  val id: String = UUID.randomUUID().toString(),
  val toolName: String, // "create_file", "edit_file", "run_terminal", "inspect_project", "web_search"
  val targetPath: String? = null,
  val content: String? = null,
  val originalContent: String? = null,
  val command: String? = null,
  val requiresApproval: Boolean = false,
  val isApproved: Boolean = false,
  val status: String = "PENDING",
  val diff: FileDiff? = null
)

enum class AttachmentType {
  IMAGE,
  VIDEO,
  DOCUMENT,
  CODE
}

data class ChatAttachment(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val mimeType: String,
  val sizeBytes: Long,
  val uriString: String? = null,
  val type: AttachmentType,
  val textContent: String? = null,
  val base64Data: String? = null
)

data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val sender: MessageSender,
  val content: String,
  val toolAction: AgentToolAction? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val isStreaming: Boolean = false,
  val error: String? = null,
  val providerUsed: String? = null,
  val attachments: List<ChatAttachment> = emptyList(),
  val researchSources: List<ResearchSource> = emptyList()
)

enum class TerminalEntryType {
  COMMAND_INPUT,
  STDOUT,
  STDERR,
  SYSTEM_INFO,
  EXIT_CODE
}

data class TerminalEntry(
  val type: TerminalEntryType,
  val text: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class EditorTab(
  val file: File,
  val relativePath: String,
  val isModified: Boolean = false
)

data class Conversation(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val createdAt: Long = System.currentTimeMillis(),
  val lastModified: Long = System.currentTimeMillis(),
  val isPinned: Boolean = false,
  val projectId: String,
  val providerUsed: String = "GEMINI",
  val modelUsed: String = "gemini-3.8-flash",
  val mode: AgentMode = AgentMode.AGENT
)

// GitHub Integration Models
data class GitHubAccount(
  val username: String,
  val name: String,
  val avatarUrl: String,
  val htmlUrl: String,
  val publicRepos: Int,
  val totalPrivateRepos: Int,
  val token: String
)

data class GitHubRepo(
  val id: Long,
  val name: String,
  val fullName: String,
  val description: String?,
  val isPrivate: Boolean,
  val htmlUrl: String,
  val defaultBranch: String,
  val language: String?,
  val stargazersCount: Int,
  val updatedAt: String
)

data class GitHubBranch(
  val name: String,
  val commitSha: String
)

data class GitHubCommitResult(
  val success: Boolean,
  val commitSha: String? = null,
  val htmlUrl: String? = null,
  val message: String
)

data class GitHubFileItem(
  val name: String,
  val path: String,
  val type: String,
  val size: Long = 0L,
  val sha: String = "",
  val downloadUrl: String? = null
) {
  val isDirectory: Boolean get() = type == "dir"
}

data class GitHubCommitItem(
  val sha: String,
  val shortSha: String,
  val message: String,
  val authorName: String,
  val date: String
)

// Android APK Builder Models
enum class ApkBuildStatus {
  IDLE,
  VALIDATING,
  COMPILING,
  PACKAGING,
  SUCCESS,
  FAILED
}

data class ApkBuildState(
  val status: ApkBuildStatus = ApkBuildStatus.IDLE,
  val currentStep: String = "Ready to build",
  val progressPercent: Float = 0f,
  val logs: List<String> = emptyList(),
  val outputApkFile: File? = null,
  val apkSizeBytes: Long = 0L,
  val errorMessage: String? = null,
  val errorAnalysis: String? = null,
  val durationMs: Long = 0L
)

// File Diff & Version History Models
enum class DiffLineType { UNCHANGED, ADDED, REMOVED }

data class DiffLine(
  val type: DiffLineType,
  val content: String,
  val oldLineNum: Int? = null,
  val newLineNum: Int? = null
)

data class FileDiff(
  val relativePath: String,
  val lines: List<DiffLine>,
  val additionsCount: Int,
  val deletionsCount: Int
)

data class FileBackupSnapshot(
  val id: String = UUID.randomUUID().toString(),
  val relativePath: String,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val reason: String = "Pre-edit backup"
)

data class BuildWorkflowInfo(
  val repoName: String = "my-altron-app",
  val branch: String = "main",
  val workflowFilePath: String = ".github/workflows/build-apk.yml",
  val lastArtifactName: String = "app-debug.apk",
  val status: String = "Ready to Export"
)
