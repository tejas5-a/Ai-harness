package com.example.data.model

import java.io.File
import java.util.UUID

enum class ProjectType(val displayName: String, val badge: String) {
  ANDROID("Android Compose", "KOTLIN"),
  WEB("Web App (HTML/JS/CSS)", "WEB"),
  PYTHON("Python Script", "PY"),
  KOTLIN("Kotlin Script", "KT"),
  NODE("Node.js / Express", "NODE"),
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
  val gitRepoUrl: String? = null
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
  val defaultModels: List<String>,
  val docsUrl: String
) {
  GEMINI(
    displayName = "Google Gemini",
    defaultBaseUrl = "https://generativelanguage.googleapis.com",
    defaultModel = "gemini-2.5-flash",
    defaultModels = listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-1.5-flash", "gemini-1.5-pro"),
    docsUrl = "https://aistudio.google.com/app/apikey"
  ),
  OPENAI(
    displayName = "OpenAI",
    defaultBaseUrl = "https://api.openai.com/v1",
    defaultModel = "gpt-4o-mini",
    defaultModels = listOf("gpt-4o-mini", "gpt-4o", "gpt-4-turbo"),
    docsUrl = "https://platform.openai.com/api-keys"
  ),
  DEEPSEEK(
    displayName = "DeepSeek AI",
    defaultBaseUrl = "https://api.deepseek.com",
    defaultModel = "deepseek-chat",
    defaultModels = listOf("deepseek-chat", "deepseek-coder", "deepseek-reasoner"),
    docsUrl = "https://platform.deepseek.com/api_keys"
  ),
  OPENROUTER(
    displayName = "OpenRouter",
    defaultBaseUrl = "https://openrouter.ai/api/v1",
    defaultModel = "google/gemini-2.5-flash",
    defaultModels = listOf("google/gemini-2.5-flash", "anthropic/claude-3.5-sonnet", "meta-llama/llama-3.3-70b-instruct"),
    docsUrl = "https://openrouter.ai/keys"
  ),
  CLAUDE(
    displayName = "Anthropic Claude",
    defaultBaseUrl = "https://api.anthropic.com/v1",
    defaultModel = "claude-3-5-sonnet-20241022",
    defaultModels = listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022"),
    docsUrl = "https://console.anthropic.com/settings/keys"
  ),
  GROQ(
    displayName = "Groq LPU",
    defaultBaseUrl = "https://api.groq.com/openai/v1",
    defaultModel = "llama-3.3-70b-versatile",
    defaultModels = listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant"),
    docsUrl = "https://console.groq.com/keys"
  ),
  CUSTOM_COMPATIBLE(
    displayName = "Custom Compatible API",
    defaultBaseUrl = "http://10.0.2.2:11434/v1",
    defaultModel = "default",
    defaultModels = listOf("default", "custom-model"),
    docsUrl = "https://ollama.com"
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
  val lastTestedSuccess: Boolean? = null
)

enum class MessageSender {
  USER,
  AGENT,
  SYSTEM
}

data class AgentToolAction(
  val id: String = UUID.randomUUID().toString(),
  val toolName: String, // "create_file", "edit_file", "run_terminal", "inspect_project"
  val targetPath: String? = null,
  val content: String? = null,
  val command: String? = null,
  val requiresApproval: Boolean = false,
  val isApproved: Boolean = false,
  val status: String = "PENDING" // PENDING, EXECUTED, REJECTED, FAILED
)

data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val sender: MessageSender,
  val content: String,
  val toolAction: AgentToolAction? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val isStreaming: Boolean = false,
  val error: String? = null
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

data class BuildWorkflowInfo(
  val repoName: String = "my-altron-app",
  val branch: String = "main",
  val workflowFilePath: String = ".github/workflows/android-build.yml",
  val lastArtifactName: String = "AltronCode-Debug-APK.apk",
  val status: String = "Ready to Export"
)
