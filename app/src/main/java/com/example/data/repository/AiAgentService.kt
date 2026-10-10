package com.example.data.repository

import com.example.data.model.AgentToolAction
import com.example.data.model.ApiProviderConfig
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class AiAgentService {

  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  suspend fun testConnection(config: ApiProviderConfig): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    if (config.apiKey.isBlank() && config.providerType != ProviderType.CUSTOM_COMPATIBLE) {
      return@withContext false to "API Key is empty. Please enter your key."
    }

    try {
      when (config.providerType) {
        ProviderType.GEMINI -> {
          val url = "${config.baseUrl.trimEnd('/')}/v1beta/models/${config.selectedModel}:generateContent?key=${config.apiKey}"
          val bodyJson = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
              put("parts", JSONArray().put(JSONObject().apply {
                put("text", "ping")
              }))
            }))
            put("generationConfig", JSONObject().apply {
              put("maxOutputTokens", 5)
            })
          }
          val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

          client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
              true to "Connected successfully (HTTP ${response.code} OK)"
            } else {
              val errBody = response.body?.string().orEmpty()
              val msg = extractErrorMessage(errBody) ?: "HTTP ${response.code} ${response.message}"
              false to "Failed: $msg"
            }
          }
        }

        ProviderType.OPENAI, ProviderType.DEEPSEEK, ProviderType.OPENROUTER, ProviderType.GROQ, ProviderType.CUSTOM_COMPATIBLE -> {
          var targetUrl = config.baseUrl.trimEnd('/')
          if (!targetUrl.endsWith("/chat/completions")) {
            targetUrl = if (targetUrl.endsWith("/v1")) "$targetUrl/chat/completions" else "$targetUrl/v1/chat/completions"
          }

          val bodyJson = JSONObject().apply {
            put("model", config.selectedModel)
            put("messages", JSONArray().put(JSONObject().apply {
              put("role", "user")
              put("content", "ping")
            }))
            put("max_tokens", 5)
          }

          val reqBuilder = Request.Builder()
            .url(targetUrl)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer ${config.apiKey}")

          if (config.providerType == ProviderType.OPENROUTER) {
            reqBuilder.addHeader("HTTP-Referer", "https://altroncode.ai")
            reqBuilder.addHeader("X-Title", "Altron Code AI")
          }

          client.newCall(reqBuilder.build()).execute().use { response ->
            if (response.isSuccessful) {
              true to "Connected successfully (HTTP ${response.code} OK)"
            } else {
              val errBody = response.body?.string().orEmpty()
              val msg = extractErrorMessage(errBody) ?: "HTTP ${response.code} ${response.message}"
              false to "Failed: $msg"
            }
          }
        }

        ProviderType.CLAUDE -> {
          val targetUrl = "${config.baseUrl.trimEnd('/')}/v1/messages"
          val bodyJson = JSONObject().apply {
            put("model", config.selectedModel)
            put("max_tokens", 10)
            put("messages", JSONArray().put(JSONObject().apply {
              put("role", "user")
              put("content", "ping")
            }))
          }
          val request = Request.Builder()
            .url(targetUrl)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .addHeader("x-api-key", config.apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .build()

          client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
              true to "Connected successfully (HTTP ${response.code} OK)"
            } else {
              val errBody = response.body?.string().orEmpty()
              val msg = extractErrorMessage(errBody) ?: "HTTP ${response.code} ${response.message}"
              false to "Failed: $msg"
            }
          }
        }
      }
    } catch (e: IOException) {
      false to "Network error: ${e.localizedMessage ?: "Unable to connect"}"
    } catch (e: Exception) {
      false to "Error: ${e.localizedMessage ?: "Invalid configuration"}"
    }
  }

  suspend fun generateAgentResponse(
    prompt: String,
    config: ApiProviderConfig,
    projectContext: String = "",
    history: List<ChatMessage> = emptyList()
  ): ChatMessage = withContext(Dispatchers.IO) {
    if (config.apiKey.isBlank() && config.providerType != ProviderType.CUSTOM_COMPATIBLE) {
      return@withContext ChatMessage(
        sender = MessageSender.AGENT,
        content = "⚠️ No API Key configured for ${config.providerType.displayName}.\n\nPlease go to **API Providers** in the bottom menu to configure your API key.",
        error = "MISSING_KEY"
      )
    }

    val systemInstructions = """You are ALTRON CODE AI, an expert mobile coding agent running on Android.
You help developers write code, inspect files, debug issues, and execute terminal commands safely.
When you suggest a shell command, format it as:
```bash
<command>
```
When you suggest creating or replacing a file, format it as:
```file:<relative_path>
<file_contents>
```
Current Project Context:
$projectContext
Keep responses concise, clear, and optimized for mobile screens.
""".trimIndent()

    try {
      val rawAnswer = when (config.providerType) {
        ProviderType.GEMINI -> callGemini(config, prompt, systemInstructions, history)
        ProviderType.OPENAI, ProviderType.DEEPSEEK, ProviderType.OPENROUTER, ProviderType.GROQ, ProviderType.CUSTOM_COMPATIBLE ->
          callOpenAiCompatible(config, prompt, systemInstructions, history)
        ProviderType.CLAUDE -> callClaude(config, prompt, systemInstructions, history)
      }

      val toolAction = extractToolAction(rawAnswer)

      ChatMessage(
        sender = MessageSender.AGENT,
        content = rawAnswer,
        toolAction = toolAction
      )
    } catch (e: Exception) {
      ChatMessage(
        sender = MessageSender.AGENT,
        content = "❌ Connection Error: ${e.localizedMessage ?: "Failed to reach ${config.providerType.displayName}"}",
        error = e.localizedMessage
      )
    }
  }

  private fun callGemini(
    config: ApiProviderConfig,
    prompt: String,
    systemInstruction: String,
    history: List<ChatMessage>
  ): String {
    val url = "${config.baseUrl.trimEnd('/')}/v1beta/models/${config.selectedModel}:generateContent?key=${config.apiKey}"

    val contentsArray = JSONArray()

    // Add recent context from history (up to last 6 messages)
    history.takeLast(6).forEach { msg ->
      val role = if (msg.sender == MessageSender.USER) "user" else "model"
      contentsArray.put(JSONObject().apply {
        put("role", role)
        put("parts", JSONArray().put(JSONObject().apply { put("text", msg.content) }))
      })
    }

    // Add current user prompt
    contentsArray.put(JSONObject().apply {
      put("role", "user")
      put("parts", JSONArray().put(JSONObject().apply { put("text", prompt) }))
    })

    val bodyJson = JSONObject().apply {
      put("systemInstruction", JSONObject().apply {
        put("parts", JSONArray().put(JSONObject().apply { put("text", systemInstruction) }))
      })
      put("contents", contentsArray)
      put("generationConfig", JSONObject().apply {
        put("temperature", 0.7)
        put("maxOutputTokens", 2048)
      })
    }

    val request = Request.Builder()
      .url(url)
      .post(bodyJson.toString().toRequestBody(jsonMediaType))
      .build()

    client.newCall(request).execute().use { response ->
      val body = response.body?.string().orEmpty()
      if (!response.isSuccessful) {
        val err = extractErrorMessage(body) ?: "HTTP ${response.code}"
        throw IOException("Gemini API Error: $err")
      }

      val jsonObj = JSONObject(body)
      val candidates = jsonObj.optJSONArray("candidates")
      if (candidates != null && candidates.length() > 0) {
        val content = candidates.getJSONObject(0).optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        if (parts != null && parts.length() > 0) {
          return parts.getJSONObject(0).optString("text", "")
        }
      }
      return "No content returned from Gemini."
    }
  }

  private fun callOpenAiCompatible(
    config: ApiProviderConfig,
    prompt: String,
    systemInstruction: String,
    history: List<ChatMessage>
  ): String {
    var targetUrl = config.baseUrl.trimEnd('/')
    if (!targetUrl.endsWith("/chat/completions")) {
      targetUrl = if (targetUrl.endsWith("/v1")) "$targetUrl/chat/completions" else "$targetUrl/v1/chat/completions"
    }

    val messagesArray = JSONArray().apply {
      put(JSONObject().apply {
        put("role", "system")
        put("content", systemInstruction)
      })
      history.takeLast(6).forEach { msg ->
        put(JSONObject().apply {
          put("role", if (msg.sender == MessageSender.USER) "user" else "assistant")
          put("content", msg.content)
        })
      }
      put(JSONObject().apply {
        put("role", "user")
        put("content", prompt)
      })
    }

    val bodyJson = JSONObject().apply {
      put("model", config.selectedModel)
      put("messages", messagesArray)
      put("temperature", 0.7)
    }

    val reqBuilder = Request.Builder()
      .url(targetUrl)
      .post(bodyJson.toString().toRequestBody(jsonMediaType))
      .addHeader("Authorization", "Bearer ${config.apiKey}")

    if (config.providerType == ProviderType.OPENROUTER) {
      reqBuilder.addHeader("HTTP-Referer", "https://altroncode.ai")
      reqBuilder.addHeader("X-Title", "Altron Code AI")
    }

    client.newCall(reqBuilder.build()).execute().use { response ->
      val body = response.body?.string().orEmpty()
      if (!response.isSuccessful) {
        val err = extractErrorMessage(body) ?: "HTTP ${response.code}"
        throw IOException("${config.providerType.displayName} Error: $err")
      }

      val jsonObj = JSONObject(body)
      val choices = jsonObj.optJSONArray("choices")
      if (choices != null && choices.length() > 0) {
        val message = choices.getJSONObject(0).optJSONObject("message")
        return message?.optString("content", "").orEmpty()
      }
      return "No content returned."
    }
  }

  private fun callClaude(
    config: ApiProviderConfig,
    prompt: String,
    systemInstruction: String,
    history: List<ChatMessage>
  ): String {
    val targetUrl = "${config.baseUrl.trimEnd('/')}/v1/messages"

    val messagesArray = JSONArray().apply {
      history.takeLast(6).forEach { msg ->
        put(JSONObject().apply {
          put("role", if (msg.sender == MessageSender.USER) "user" else "assistant")
          put("content", msg.content)
        })
      }
      put(JSONObject().apply {
        put("role", "user")
        put("content", prompt)
      })
    }

    val bodyJson = JSONObject().apply {
      put("model", config.selectedModel)
      put("system", systemInstruction)
      put("max_tokens", 2048)
      put("messages", messagesArray)
    }

    val request = Request.Builder()
      .url(targetUrl)
      .post(bodyJson.toString().toRequestBody(jsonMediaType))
      .addHeader("x-api-key", config.apiKey)
      .addHeader("anthropic-version", "2023-06-01")
      .build()

    client.newCall(request).execute().use { response ->
      val body = response.body?.string().orEmpty()
      if (!response.isSuccessful) {
        val err = extractErrorMessage(body) ?: "HTTP ${response.code}"
        throw IOException("Claude Error: $err")
      }

      val jsonObj = JSONObject(body)
      val contentArr = jsonObj.optJSONArray("content")
      if (contentArr != null && contentArr.length() > 0) {
        return contentArr.getJSONObject(0).optString("text", "")
      }
      return "No content returned from Claude."
    }
  }

  private fun extractErrorMessage(body: String): String? {
    return try {
      val json = JSONObject(body)
      if (json.has("error")) {
        val errObj = json.optJSONObject("error")
        errObj?.optString("message") ?: json.optString("error")
      } else {
        json.optString("message", null)
      }
    } catch (e: Exception) {
      null
    }
  }

  private fun extractToolAction(text: String): AgentToolAction? {
    // Check file block ```file:path
    val filePattern = Pattern.compile("```file:([^\n\r]+)[\n\r]([\\s\\S]*?)```", Pattern.MULTILINE)
    val fileMatcher = filePattern.matcher(text)
    if (fileMatcher.find()) {
      val path = fileMatcher.group(1).trim()
      val content = fileMatcher.group(2).trim()
      return AgentToolAction(
        toolName = "create_file",
        targetPath = path,
        content = content,
        requiresApproval = true
      )
    }

    // Check bash command block ```bash
    val bashPattern = Pattern.compile("```(?:bash|sh)[\n\r]([\\s\\S]*?)```", Pattern.MULTILINE)
    val bashMatcher = bashPattern.matcher(text)
    if (bashMatcher.find()) {
      val command = bashMatcher.group(1).trim()
      val isDangerous = command.contains("rm ") || command.contains("delete") || command.contains("reboot")
      return AgentToolAction(
        toolName = "run_terminal",
        command = command,
        requiresApproval = isDangerous
      )
    }

    return null
  }
}
