package com.example.data.repository

import com.example.data.model.AgentToolAction
import com.example.data.model.ApiProviderConfig
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class AiAgentService {

  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(25, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  // ==================== TEST CONNECTION ====================

  suspend fun testConnection(config: ApiProviderConfig): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    if (config.apiKey.isBlank() && config.providerType != ProviderType.CUSTOM_COMPATIBLE) {
      return@withContext false to "API Key is empty. Please enter a valid key."
    }

    try {
      when (config.providerType) {
        ProviderType.GEMINI -> {
          val url = "${config.baseUrl.trimEnd('/')}/v1beta/models/${config.selectedModel}:generateContent"
          val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", config.apiKey)
            .get()
            .build()
          client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
              true to "Connected successfully to ${config.selectedModel} (HTTP 200 OK)"
            } else {
              val errBody = response.body?.string().orEmpty()
              val msg = formatError(response.code, errBody, config.selectedModel)
              false to msg
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
            put("max_tokens", 1)
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
              true to "Connected successfully to ${config.selectedModel} (HTTP ${response.code} OK)"
            } else {
              val errBody = response.body?.string().orEmpty()
              val msg = formatError(response.code, errBody, config.selectedModel)
              false to msg
            }
          }
        }

        ProviderType.CLAUDE -> {
          val targetUrl = "${config.baseUrl.trimEnd('/')}/v1/messages"
          val bodyJson = JSONObject().apply {
            put("model", config.selectedModel)
            put("max_tokens", 1)
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
              true to "Connected successfully to ${config.selectedModel} (HTTP ${response.code} OK)"
            } else {
              val errBody = response.body?.string().orEmpty()
              val msg = formatError(response.code, errBody, config.selectedModel)
              false to msg
            }
          }
        }
      }
    } catch (e: IOException) {
      false to "Network Connection Error: ${e.localizedMessage ?: "Unable to reach server"}"
    } catch (e: Exception) {
      false to "Error: ${e.localizedMessage ?: "Unexpected error"}"
    }
  }

  // ==================== DYNAMIC MODEL FETCHING ====================

  suspend fun fetchAvailableModels(config: ApiProviderConfig): Pair<Boolean, List<String>> = withContext(Dispatchers.IO) {
    if (config.apiKey.isBlank() && config.providerType != ProviderType.CUSTOM_COMPATIBLE) {
      return@withContext false to emptyList()
    }

    try {
      when (config.providerType) {
        ProviderType.GEMINI -> {
          val url = "${config.baseUrl.trimEnd('/')}/v1beta/models?key=${config.apiKey}"
          val request = Request.Builder().url(url).get().build()
          client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext false to emptyList()
            val body = response.body?.string().orEmpty()
            val jsonObj = JSONObject(body)
            val modelsArr = jsonObj.optJSONArray("models") ?: return@withContext false to emptyList()
            val list = mutableListOf<String>()
            for (i in 0 until modelsArr.length()) {
              val m = modelsArr.getJSONObject(i)
              val name = m.optString("name", "").removePrefix("models/")
              val methods = m.optJSONArray("supportedGenerationMethods")
              var supportsContent = false
              if (methods != null) {
                for (j in 0 until methods.length()) {
                  if (methods.getString(j) == "generateContent") supportsContent = true
                }
              }
              if (supportsContent && name.startsWith("gemini")) {
                list.add(name)
              }
            }
            true to list.sortedDescending()
          }
        }

        ProviderType.OPENAI, ProviderType.DEEPSEEK, ProviderType.GROQ, ProviderType.OPENROUTER, ProviderType.CUSTOM_COMPATIBLE -> {
          var targetUrl = config.baseUrl.trimEnd('/')
          if (targetUrl.endsWith("/chat/completions")) {
            targetUrl = targetUrl.removeSuffix("/chat/completions")
          }
          val modelsUrl = if (targetUrl.endsWith("/v1")) "$targetUrl/models" else "$targetUrl/v1/models"

          val reqBuilder = Request.Builder().url(modelsUrl).get()
          if (config.apiKey.isNotBlank()) {
            reqBuilder.addHeader("Authorization", "Bearer ${config.apiKey}")
          }
          if (config.providerType == ProviderType.OPENROUTER) {
            reqBuilder.addHeader("HTTP-Referer", "https://altroncode.ai")
          }

          client.newCall(reqBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) return@withContext false to emptyList()
            val body = response.body?.string().orEmpty()
            val jsonObj = JSONObject(body)
            val dataArr = jsonObj.optJSONArray("data") ?: return@withContext false to emptyList()
            val list = mutableListOf<String>()
            for (i in 0 until dataArr.length()) {
              val m = dataArr.getJSONObject(i)
              val id = m.optString("id", "")
              if (id.isNotBlank()) list.add(id)
            }
            true to list
          }
        }

        ProviderType.CLAUDE -> {
          val url = "${config.baseUrl.trimEnd('/')}/v1/models"
          val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-api-key", config.apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .build()
          client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext false to emptyList()
            val body = response.body?.string().orEmpty()
            val jsonObj = JSONObject(body)
            val dataArr = jsonObj.optJSONArray("data") ?: return@withContext false to emptyList()
            val list = mutableListOf<String>()
            for (i in 0 until dataArr.length()) {
              val id = dataArr.getJSONObject(i).optString("id", "")
              if (id.isNotBlank()) list.add(id)
            }
            true to list
          }
        }
      }
    } catch (e: Exception) {
      false to emptyList()
    }
  }

  // ==================== REAL-TIME STREAMING RESPONSE ====================

  fun streamAgentResponse(
    prompt: String,
    config: ApiProviderConfig,
    projectContext: String = "",
    history: List<ChatMessage> = emptyList()
  ): Flow<String> = callbackFlow {
    if (config.apiKey.isBlank() && config.providerType != ProviderType.CUSTOM_COMPATIBLE) {
      trySend("⚠️ No API Key configured for ${config.providerType.displayName}. Please go to API Providers to configure your key.")
      close()
      return@callbackFlow
    }

    val systemInstruction = """You are ALTRON CODE AI, an expert mobile coding agent running on Android.
You help developers write code, inspect files, debug issues, and execute terminal commands safely.
When you suggest a shell command, format it as:
```bash
<command>
```
When you suggest creating or replacing a file, format it as:
```file:<relative_path>
<file_contents>
```
Project Context:
$projectContext
Keep responses clear and concise.
""".trimIndent()

    var activeCall: Call? = null

    try {
      when (config.providerType) {
        ProviderType.GEMINI -> {
          val url = "${config.baseUrl.trimEnd('/')}/v1beta/models/${config.selectedModel}:streamGenerateContent?key=${config.apiKey}&alt=sse"
          val contentsArray = JSONArray()
          history.takeLast(6).forEach { msg ->
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            contentsArray.put(JSONObject().apply {
              put("role", role)
              put("parts", JSONArray().put(JSONObject().apply { put("text", msg.content) }))
            })
          }
          contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().apply { put("text", prompt) }))
          })

          val bodyJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
              put("parts", JSONArray().put(JSONObject().apply { put("text", systemInstruction) }))
            })
            put("contents", contentsArray)
          }

          val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

          val call = client.newCall(request)
          activeCall = call
          call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
              trySend("❌ Network Error: ${e.localizedMessage}")
              close()
            }

            override fun onResponse(call: Call, response: Response) {
              response.use {
                if (!response.isSuccessful) {
                  val err = formatError(response.code, response.body?.string().orEmpty(), config.selectedModel)
                  trySend("❌ Error: $err")
                  close()
                  return
                }

                val reader = BufferedReader(InputStreamReader(response.body!!.byteStream()))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                  val current = line?.trim().orEmpty()
                  if (current.startsWith("data: ")) {
                    val jsonChunk = current.removePrefix("data: ").trim()
                    if (jsonChunk.isNotEmpty()) {
                      try {
                        val obj = JSONObject(jsonChunk)
                        val candidates = obj.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                          val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                          if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text", "")
                            if (text.isNotEmpty()) trySend(text)
                          }
                        }
                      } catch (e: Exception) {
                        // ignore chunk parsing error
                      }
                    }
                  }
                }
                close()
              }
            }
          })
        }

        ProviderType.OPENAI, ProviderType.DEEPSEEK, ProviderType.OPENROUTER, ProviderType.GROQ, ProviderType.CUSTOM_COMPATIBLE -> {
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
            put("stream", true)
          }

          val reqBuilder = Request.Builder()
            .url(targetUrl)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer ${config.apiKey}")

          if (config.providerType == ProviderType.OPENROUTER) {
            reqBuilder.addHeader("HTTP-Referer", "https://altroncode.ai")
            reqBuilder.addHeader("X-Title", "Altron Code AI")
          }

          val call = client.newCall(reqBuilder.build())
          activeCall = call
          call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
              trySend("❌ Network Error: ${e.localizedMessage}")
              close()
            }

            override fun onResponse(call: Call, response: Response) {
              response.use {
                if (!response.isSuccessful) {
                  val err = formatError(response.code, response.body?.string().orEmpty(), config.selectedModel)
                  trySend("❌ Error: $err")
                  close()
                  return
                }

                val reader = BufferedReader(InputStreamReader(response.body!!.byteStream()))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                  val current = line?.trim().orEmpty()
                  if (current.startsWith("data: ")) {
                    val jsonChunk = current.removePrefix("data: ").trim()
                    if (jsonChunk == "[DONE]") break
                    if (jsonChunk.isNotEmpty()) {
                      try {
                        val obj = JSONObject(jsonChunk)
                        val choices = obj.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                          val delta = choices.getJSONObject(0).optJSONObject("delta")
                          val content = delta?.optString("content", "") ?: ""
                          if (content.isNotEmpty()) trySend(content)
                        }
                      } catch (e: Exception) {
                        // ignore chunk error
                      }
                    }
                  }
                }
                close()
              }
            }
          })
        }

        ProviderType.CLAUDE -> {
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
            put("max_tokens", 4096)
            put("stream", true)
            put("messages", messagesArray)
          }

          val request = Request.Builder()
            .url(targetUrl)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .addHeader("x-api-key", config.apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .build()

          val call = client.newCall(request)
          activeCall = call
          call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
              trySend("❌ Network Error: ${e.localizedMessage}")
              close()
            }

            override fun onResponse(call: Call, response: Response) {
              response.use {
                if (!response.isSuccessful) {
                  val err = formatError(response.code, response.body?.string().orEmpty(), config.selectedModel)
                  trySend("❌ Error: $err")
                  close()
                  return
                }

                val reader = BufferedReader(InputStreamReader(response.body!!.byteStream()))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                  val current = line?.trim().orEmpty()
                  if (current.startsWith("data: ")) {
                    val jsonChunk = current.removePrefix("data: ").trim()
                    if (jsonChunk.isNotEmpty()) {
                      try {
                        val obj = JSONObject(jsonChunk)
                        val type = obj.optString("type")
                        if (type == "content_block_delta") {
                          val delta = obj.optJSONObject("delta")
                          val text = delta?.optString("text", "") ?: ""
                          if (text.isNotEmpty()) trySend(text)
                        }
                      } catch (e: Exception) {
                        // ignore chunk error
                      }
                    }
                  }
                }
                close()
              }
            }
          })
        }
      }
    } catch (e: Exception) {
      trySend("❌ Unexpected Error: ${e.localizedMessage}")
      close()
    }

    awaitClose {
      activeCall?.cancel()
    }
  }

  // ==================== SINGLE-SHOT AGENT RESPONSE ====================

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
        error = "MISSING_KEY",
        providerUsed = config.providerType.displayName
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
        toolAction = toolAction,
        providerUsed = "${config.providerType.displayName} (${config.selectedModel})"
      )
    } catch (e: Exception) {
      ChatMessage(
        sender = MessageSender.AGENT,
        content = "❌ ${e.localizedMessage ?: "Failed to reach ${config.providerType.displayName}"}",
        error = e.localizedMessage,
        providerUsed = config.providerType.displayName
      )
    }
  }

  private fun callGemini(
    config: ApiProviderConfig,
    prompt: String,
    systemInstruction: String,
    history: List<ChatMessage>
  ): String {
    val url = "${config.baseUrl.trimEnd('/')}/v1beta/models/${config.selectedModel}:generateContent"
    val contentsArray = JSONArray()

    history.takeLast(6).forEach { msg ->
      val role = if (msg.sender == MessageSender.USER) "user" else "model"
      contentsArray.put(JSONObject().apply {
        put("role", role)
        put("parts", JSONArray().put(JSONObject().apply { put("text", msg.content) }))
      })
    }

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
        put("maxOutputTokens", 4096)
      })
    }

    val request = Request.Builder()
      .url(url)
      .addHeader("x-goog-api-key", config.apiKey)
      .post(bodyJson.toString().toRequestBody(jsonMediaType))
      .build()

    client.newCall(request).execute().use { response ->
      val body = response.body?.string().orEmpty()
      if (!response.isSuccessful) {
        throw IOException(formatError(response.code, body, config.selectedModel))
      }

      val jsonObj = JSONObject(body)
      val candidates = jsonObj.optJSONArray("candidates")
      if (candidates != null && candidates.length() > 0) {
        val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
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
        throw IOException(formatError(response.code, body, config.selectedModel))
      }

      val jsonObj = JSONObject(body)
      val choices = jsonObj.optJSONArray("choices")
      if (choices != null && choices.length() > 0) {
        val message = choices.getJSONObject(0).optJSONObject("message")
        return message?.optString("content", "").orEmpty()
      }
      return "No content returned from ${config.providerType.displayName}."
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
      put("max_tokens", 4096)
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
        throw IOException(formatError(response.code, body, config.selectedModel))
      }

      val jsonObj = JSONObject(body)
      val contentArr = jsonObj.optJSONArray("content")
      if (contentArr != null && contentArr.length() > 0) {
        return contentArr.getJSONObject(0).optString("text", "")
      }
      return "No content returned from Claude."
    }
  }

  // ==================== ERROR FORMATTER & DIAGNOSTICS ====================

  private fun formatError(httpCode: Int, responseBody: String, model: String): String {
    val rawMsg = extractErrorMessage(responseBody)
    return when (httpCode) {
      401 -> "Unauthorized / Invalid API Key (HTTP 401). Please verify your key."
      402 -> "Payment Required / Insufficient Balance (HTTP 402). Check provider billing."
      403 -> "Permission Denied / Access Forbidden (HTTP 403). Check project and API permissions."
      404 -> "Model '$model' or endpoint not found (HTTP 404). Verify model ID."
      429 -> "Rate Limit Exceeded / Quota Exhausted (HTTP 429). Retry in a moment."
      500, 502, 503, 504 -> "Provider Service Error (HTTP $httpCode). The AI service is currently unavailable or overloaded."
      else -> rawMsg ?: "HTTP Error $httpCode"
    }
  }

  private fun extractErrorMessage(body: String): String? {
    return try {
      val json = JSONObject(body)
      if (json.has("error")) {
        val err = json.opt("error")
        if (err is JSONObject) {
          err.optString("message", null) ?: err.optString("type", null)
        } else {
          err?.toString()
        }
      } else if (json.has("message")) {
        json.optString("message")
      } else {
        null
      }
    } catch (e: Exception) {
      null
    }
  }

  fun extractToolAction(text: String): AgentToolAction? {
    // Check file block ```file:path
    val filePattern = Pattern.compile("```file:([^\n\r]+)[\n\r]([\\s\\S]*?)```", Pattern.MULTILINE)
    val fileMatcher = filePattern.matcher(text)
    if (fileMatcher.find()) {
      val path = fileMatcher.group(1).orEmpty().trim()
      val content = fileMatcher.group(2).orEmpty().trim()
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
      val command = bashMatcher.group(1).orEmpty().trim()
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
