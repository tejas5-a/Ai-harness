package com.example.data.repository

import android.content.Context
import com.example.data.model.AgentMode
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.MessageSender
import com.example.data.model.ResearchSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * ChatHistoryRepository: Fully persistent on-device storage for ALTRON conversations
 * and message histories. Stores chat sessions in JSON files under app-private storage
 * to guarantee conversations survive app restarts, model switching, and phone reboots.
 */
class ChatHistoryRepository(private val context: Context) {

  private val chatsDir: File by lazy {
    File(context.filesDir, "altron_chats").apply {
      if (!exists()) mkdirs()
    }
  }

  suspend fun getAllConversations(): List<Conversation> = withContext(Dispatchers.IO) {
    val metaFile = File(chatsDir, "conversations_index.json")
    if (!metaFile.exists()) return@withContext emptyList()

    try {
      val jsonStr = metaFile.readText()
      val arr = JSONArray(jsonStr)
      val list = mutableListOf<Conversation>()
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val modeStr = obj.optString("mode", AgentMode.AGENT.name)
        val mode = try { AgentMode.valueOf(modeStr) } catch (e: Exception) { AgentMode.AGENT }
        list.add(
          Conversation(
            id = obj.getString("id"),
            title = obj.getString("title"),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            lastModified = obj.optLong("lastModified", System.currentTimeMillis()),
            isPinned = obj.optBoolean("isPinned", false),
            projectId = obj.optString("projectId", "default"),
            providerUsed = obj.optString("providerUsed", "GEMINI"),
            modelUsed = obj.optString("modelUsed", "gemini-3.8-flash"),
            mode = mode
          )
        )
      }
      list.sortedByDescending { it.lastModified }
    } catch (e: Exception) {
      emptyList()
    }
  }

  suspend fun saveConversationIndex(conversations: List<Conversation>) = withContext(Dispatchers.IO) {
    val metaFile = File(chatsDir, "conversations_index.json")
    val arr = JSONArray()
    conversations.forEach { c ->
      arr.put(JSONObject().apply {
        put("id", c.id)
        put("title", c.title)
        put("createdAt", c.createdAt)
        put("lastModified", c.lastModified)
        put("isPinned", c.isPinned)
        put("projectId", c.projectId)
        put("providerUsed", c.providerUsed)
        put("modelUsed", c.modelUsed)
        put("mode", c.mode.name)
      })
    }
    metaFile.writeText(arr.toString(2))
  }

  suspend fun loadMessages(conversationId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
    val msgFile = File(chatsDir, "chat_${conversationId}.json")
    if (!msgFile.exists()) return@withContext emptyList()

    try {
      val jsonStr = msgFile.readText()
      val arr = JSONArray(jsonStr)
      val messages = mutableListOf<ChatMessage>()
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val sender = MessageSender.valueOf(obj.optString("sender", MessageSender.USER.name))
        val content = obj.optString("content", "")
        val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
        val providerUsed = obj.optString("providerUsed", null)

        // Sources
        val sourcesList = mutableListOf<ResearchSource>()
        val sourcesArr = obj.optJSONArray("researchSources")
        if (sourcesArr != null) {
          for (s in 0 until sourcesArr.length()) {
            val so = sourcesArr.getJSONObject(s)
            sourcesList.add(
              ResearchSource(
                title = so.optString("title", ""),
                url = so.optString("url", ""),
                snippet = so.optString("snippet", "")
              )
            )
          }
        }

        messages.add(
          ChatMessage(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            sender = sender,
            content = content,
            timestamp = timestamp,
            providerUsed = providerUsed,
            researchSources = sourcesList
          )
        )
      }
      messages
    } catch (e: Exception) {
      emptyList()
    }
  }

  suspend fun saveMessages(conversationId: String, messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
    val msgFile = File(chatsDir, "chat_${conversationId}.json")
    val arr = JSONArray()
    messages.forEach { msg ->
      arr.put(JSONObject().apply {
        put("id", msg.id)
        put("sender", msg.sender.name)
        put("content", msg.content)
        put("timestamp", msg.timestamp)
        put("providerUsed", msg.providerUsed)
        if (msg.researchSources.isNotEmpty()) {
          val sArr = JSONArray()
          msg.researchSources.forEach { s ->
            sArr.put(JSONObject().apply {
              put("title", s.title)
              put("url", s.url)
              put("snippet", s.snippet)
            })
          }
          put("researchSources", sArr)
        }
      })
    }
    msgFile.writeText(arr.toString(2))
  }

  suspend fun deleteConversation(conversationId: String) = withContext(Dispatchers.IO) {
    val msgFile = File(chatsDir, "chat_${conversationId}.json")
    if (msgFile.exists()) msgFile.delete()

    val current = getAllConversations().filter { it.id != conversationId }
    saveConversationIndex(current)
  }
}
