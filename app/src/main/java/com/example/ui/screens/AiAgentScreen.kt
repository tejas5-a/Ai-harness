package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentMode
import com.example.data.model.AgentToolAction
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.components.Floating3DElements
import com.example.ui.components.StatusBadge
import com.example.ui.components.triggerTactileFeedback
import com.example.ui.navigation.Screen
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.CodeBlockBackground
import com.example.ui.theme.CodeBlockHeader
import com.example.ui.theme.CodeBlockText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightSurfaceElevated
import com.example.ui.theme.PureWhiteBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.viewmodel.AltronViewModel
import java.util.regex.Pattern

data class CodeBlockData(
  val language: String,
  val fileName: String,
  val code: String
)

sealed class ContentSegment {
  data class Text(val content: String) : ContentSegment()
  data class Code(val data: CodeBlockData) : ContentSegment()
}

fun parseMessageSegments(content: String): List<ContentSegment> {
  val segments = mutableListOf<ContentSegment>()
  val pattern = Pattern.compile("```([a-zA-Z0-9_#+.-]*)(?::([a-zA-Z0-9_./-]+))?\\r?\\n([\\s\\S]*?)(?:```|$)")
  val matcher = pattern.matcher(content)
  var lastIndex = 0

  while (matcher.find()) {
    val start = matcher.start()
    val end = matcher.end()

    if (start > lastIndex) {
      val textBefore = content.substring(lastIndex, start).trim()
      if (textBefore.isNotEmpty()) {
        segments.add(ContentSegment.Text(textBefore))
      }
    }

    val rawLang = matcher.group(1)?.trim().orEmpty()
    val rawFileName = matcher.group(2)?.trim().orEmpty()
    val rawCode = matcher.group(3)?.trimEnd().orEmpty()

    val lang = if (rawLang.isNotBlank()) rawLang.uppercase() else detectLanguageFromCode(rawCode)
    val fileName = if (rawFileName.isNotBlank()) {
      rawFileName
    } else {
      guessFileNameFromLanguage(lang, rawCode)
    }

    segments.add(
      ContentSegment.Code(
        CodeBlockData(
          language = lang,
          fileName = fileName,
          code = rawCode
        )
      )
    )

    lastIndex = end
  }

  if (lastIndex < content.length) {
    val remainingText = content.substring(lastIndex).trim()
    if (remainingText.isNotEmpty()) {
      segments.add(ContentSegment.Text(remainingText))
    }
  }

  if (segments.isEmpty() && content.isNotEmpty()) {
    segments.add(ContentSegment.Text(content))
  }

  return segments
}

fun detectLanguageFromCode(code: String): String {
  val trimmed = code.trimStart()
  return when {
    trimmed.startsWith("<!DOCTYPE") || trimmed.startsWith("<html") -> "HTML"
    trimmed.startsWith("import React") || trimmed.contains("export default") -> "JAVASCRIPT"
    trimmed.startsWith("package ") || trimmed.contains("fun ") -> "KOTLIN"
    trimmed.startsWith("def ") || trimmed.startsWith("import ") -> "PYTHON"
    trimmed.startsWith("{") && trimmed.endsWith("}") -> "JSON"
    trimmed.contains("SELECT ") || trimmed.contains("CREATE TABLE") -> "SQL"
    trimmed.startsWith("#!/") || trimmed.startsWith("echo ") -> "BASH"
    else -> "CODE"
  }
}

fun guessFileNameFromLanguage(language: String, code: String): String {
  val trimmed = code.trimStart()
  if (trimmed.startsWith("<!DOCTYPE html") || language == "HTML") return "index.html"
  return when (language) {
    "KOTLIN", "KT" -> "Main.kt"
    "PYTHON" -> "main.py"
    "JAVASCRIPT", "JS" -> "script.js"
    "TYPESCRIPT", "TS" -> "main.ts"
    "JSON" -> "config.json"
    "CSS" -> "styles.css"
    "BASH", "SHELL" -> "run.sh"
    else -> "snippet.txt"
  }
}

fun parseMarkdownSpans(text: String, baseColor: Color): AnnotatedString {
  return buildAnnotatedString {
    var currentIndex = 0
    val boldPattern = Pattern.compile("(\\*\\*|__)(.*?)\\1")
    val matcher = boldPattern.matcher(text)

    while (matcher.find()) {
      val start = matcher.start()
      val end = matcher.end()
      val boldContent = matcher.group(2)

      if (start > currentIndex) {
        append(text.substring(currentIndex, start))
      }

      pushStyle(
        SpanStyle(
          fontWeight = FontWeight.Bold,
          color = baseColor
        )
      )
      append(boldContent)
      pop()

      currentIndex = end
    }

    if (currentIndex < text.length) {
      append(text.substring(currentIndex))
    }
  }
}

@Composable
fun AiAgentScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val messages by viewModel.chatMessages.collectAsState()
  val isThinking by viewModel.isAgentThinking.collectAsState()
  val activeProvider by viewModel.activeProvider.collectAsState()
  val currentMode by viewModel.agentMode.collectAsState()
  val conversations by viewModel.conversations.collectAsState()

  val haptic = LocalHapticFeedback.current
  val context = LocalContext.current
  var inputPrompt by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  var isAutoScrollingEnabled by remember { mutableStateOf(true) }
  var isProgrammaticScroll by remember { mutableStateOf(false) }

  var showModelMenu by remember { mutableStateOf(false) }
  var showHistoryMenu by remember { mutableStateOf(false) }

  // Detect manual user scrolling to pause auto-scroll
  LaunchedEffect(listState) {
    snapshotFlow { listState.isScrollInProgress }
      .collect { isScrolling ->
        if (isScrolling && !isProgrammaticScroll) {
          val layoutInfo = listState.layoutInfo
          val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
          val isAtBottom = lastVisibleItem != null && lastVisibleItem.index == layoutInfo.totalItemsCount - 1
          if (!isAtBottom) {
            isAutoScrollingEnabled = false
          }
        }
      }
  }

  // Auto resume when at bottom
  val isAtBottom by remember {
    derivedStateOf {
      val layoutInfo = listState.layoutInfo
      val totalItems = layoutInfo.totalItemsCount
      if (totalItems == 0) true
      else {
        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
        lastVisibleItem != null && lastVisibleItem.index == totalItems - 1
      }
    }
  }

  LaunchedEffect(isAtBottom) {
    if (isAtBottom) {
      isAutoScrollingEnabled = true
    }
  }

  // Smooth scroll follow during streaming
  LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
    if (isAutoScrollingEnabled && messages.isNotEmpty()) {
      isProgrammaticScroll = true
      listState.scrollToItem(messages.size - 1)
      isProgrammaticScroll = false
    }
  }

  val hasUserMessages = messages.any { it.sender == MessageSender.USER }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    // Top Controls Bar (Model Selector & Action Chips)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Model Selector Chip
      Box {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PureWhiteBackground)
            .border(1.dp, LightBorder, RoundedCornerShape(20.dp))
            .clickable { showModelMenu = true }
            .padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(BrandPrimaryOrange)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = activeProvider.selectedModel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BrandTextBlack,
            maxLines = 1
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Select Model",
            tint = BrandTextSecondary,
            modifier = Modifier.size(16.dp)
          )
        }

        DropdownMenu(
          expanded = showModelMenu,
          onDismissRequest = { showModelMenu = false },
          modifier = Modifier.background(PureWhiteBackground)
        ) {
          activeProvider.providerType.verifiedModels.forEach { modelName ->
            DropdownMenuItem(
              text = {
                Text(
                  text = modelName,
                  fontWeight = if (modelName == activeProvider.selectedModel) FontWeight.Bold else FontWeight.Normal,
                  color = if (modelName == activeProvider.selectedModel) BrandPrimaryOrange else BrandTextBlack
                )
              },
              onClick = {
                viewModel.saveProvider(activeProvider.copy(selectedModel = modelName))
                showModelMenu = false
              }
            )
          }
          DropdownMenuItem(
            text = { Text("Configure Providers...", color = BrandPrimaryOrange, fontWeight = FontWeight.Bold) },
            onClick = {
              showModelMenu = false
              viewModel.setScreen(Screen.SETTINGS)
            }
          )
        }
      }

      // Actions: New Chat & History
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        IconButton(
          onClick = {
            triggerTactileFeedback(context, haptic)
            viewModel.startNewChat()
          },
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(PureWhiteBackground)
            .border(1.dp, LightBorder, CircleShape)
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New Chat",
            tint = BrandPrimaryOrange,
            modifier = Modifier.size(18.dp)
          )
        }

        Box {
          IconButton(
            onClick = {
              triggerTactileFeedback(context, haptic)
              showHistoryMenu = true
            },
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(PureWhiteBackground)
              .border(1.dp, LightBorder, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = "History",
              tint = BrandTextBlack,
              modifier = Modifier.size(18.dp)
            )
          }

          DropdownMenu(
            expanded = showHistoryMenu,
            onDismissRequest = { showHistoryMenu = false },
            modifier = Modifier.background(PureWhiteBackground)
          ) {
            if (conversations.isEmpty()) {
              DropdownMenuItem(
                text = { Text("No saved conversations yet", color = BrandTextSecondary, fontSize = 12.sp) },
                onClick = { showHistoryMenu = false }
              )
            } else {
              conversations.forEach { conv ->
                DropdownMenuItem(
                  text = {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = conv.title,
                        maxLines = 1,
                        fontSize = 13.sp,
                        color = BrandTextBlack,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                      )
                      IconButton(
                        onClick = { viewModel.deleteConversation(conv.id) },
                        modifier = Modifier.size(24.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.DeleteOutline,
                          contentDescription = "Delete",
                          tint = TerminalRed,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }
                  },
                  onClick = {
                    viewModel.switchConversation(conv.id)
                    showHistoryMenu = false
                  }
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Mode Selector (ASK, CODE, AGENT)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      AgentMode.values().forEach { mode ->
        val isSelected = currentMode == mode
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) BrandPrimaryOrange else PureWhiteBackground)
            .border(
              1.dp,
              if (isSelected) BrandPrimaryOrange else LightBorder,
              RoundedCornerShape(8.dp)
            )
            .clickable {
              triggerTactileFeedback(context, haptic)
              viewModel.setAgentMode(mode)
            }
            .padding(vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = mode.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) BrandTextBlack else BrandTextSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Chat Area or Suggestion Screen
    if (!hasUserMessages) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        // Decorative floating 3D spheres along edges
        Floating3DElements()

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 16.dp)
        ) {
          Text(
            text = "ALTRON CODE AI",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = BrandPrimaryOrange
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Your Phone. Your AI. Your Coding Studio.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = BrandTextSecondary
            )
          )

          Spacer(modifier = Modifier.height(24.dp))

          // 3 Suggestion Buttons: Orange Background with Black Text
          listOf(
            "Build an HTML5 interactive web page with CSS & JS",
            "Write a Kotlin Compose screen for user profile",
            "Explain and debug my project files and scripts"
          ).forEach { prompt ->
            Button(
              onClick = {
                triggerTactileFeedback(context, haptic)
                inputPrompt = prompt
                viewModel.sendAgentMessage(prompt)
              },
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .defaultMinSize(minHeight = 48.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BrandPrimaryOrange,
                contentColor = BrandTextBlack
              )
            ) {
              Text(
                text = prompt,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BrandTextBlack,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }
    } else {
      // Chat Messages List with stable scrolling
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(messages) { message ->
          ChatMessageBubble(
            message = message,
            onExecuteTool = { action ->
              viewModel.executeAgentTool(message, action)
            },
            onSaveCode = { fileName, code ->
              viewModel.saveCodeBlockToFile(fileName, code)
            }
          )
        }

        if (isThinking) {
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  color = BrandPrimaryOrange,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Streaming from ${activeProvider.selectedModel}...",
                  fontSize = 12.sp,
                  color = BrandPrimaryOrange,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold
                )
              }

              // Stop generation button
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(TerminalRed.copy(alpha = 0.15f))
                  .border(0.8.dp, TerminalRed, RoundedCornerShape(6.dp))
                  .clickable {
                    triggerTactileFeedback(context, haptic)
                    viewModel.cancelAgent()
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = TerminalRed,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Stop", fontSize = 10.sp, color = TerminalRed, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Chat Input Composer Bar: White background with subtle gray border
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = inputPrompt,
        onValueChange = { inputPrompt = it },
        placeholder = { Text("Ask ALTRON to code, debug, or plan...", fontSize = 13.sp, color = BrandTextSecondary) },
        modifier = Modifier
          .weight(1f)
          .testTag("ai_agent_input_field"),
        shape = RoundedCornerShape(24.dp),
        maxLines = 4,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = PureWhiteBackground,
          unfocusedContainerColor = PureWhiteBackground,
          focusedBorderColor = BrandPrimaryOrange,
          unfocusedBorderColor = LightBorder,
          focusedTextColor = BrandTextBlack,
          unfocusedTextColor = BrandTextBlack
        )
      )

      if (isThinking) {
        IconButton(
          onClick = {
            triggerTactileFeedback(context, haptic)
            viewModel.cancelAgent()
          },
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(TerminalRed)
        ) {
          Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = "Stop",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      } else {
        IconButton(
          onClick = {
            if (inputPrompt.isNotBlank()) {
              triggerTactileFeedback(context, haptic)
              val text = inputPrompt
              inputPrompt = ""
              viewModel.sendAgentMessage(text)
            }
          },
          enabled = inputPrompt.isNotBlank(),
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (inputPrompt.isNotBlank()) BrandPrimaryOrange else LightSurfaceElevated)
            .testTag("ai_agent_send_btn")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint = if (inputPrompt.isNotBlank()) BrandTextBlack else BrandTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessage,
  onExecuteTool: (AgentToolAction) -> Unit,
  onSaveCode: (String, String) -> String
) {
  val isUser = message.sender == MessageSender.USER

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    if (isUser) {
      // User Question: Black text on soft light surface with subtle gray border
      Box(
        modifier = Modifier
          .align(Alignment.End)
          .clip(RoundedCornerShape(16.dp))
          .background(LightSurfaceElevated)
          .border(1.dp, LightBorder, RoundedCornerShape(16.dp))
          .padding(12.dp)
      ) {
        Text(
          text = message.content,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = BrandTextBlack,
            fontWeight = FontWeight.Medium
          )
        )
      }
    } else {
      // AI Response: Black text on white background
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(BrandPrimaryOrange)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "ALTRON CODE AI",
            fontSize = 12.sp,
            color = BrandPrimaryOrange,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Parse Message into Text and Code Blocks
        val segments = remember(message.content) { parseMessageSegments(message.content) }

        segments.forEach { segment ->
          when (segment) {
            is ContentSegment.Text -> {
              val annotated = remember(segment.content) {
                parseMarkdownSpans(segment.content, BrandTextBlack)
              }
              Text(
                text = annotated,
                style = MaterialTheme.typography.bodyMedium,
                color = BrandTextBlack,
                lineHeight = 20.sp,
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
            is ContentSegment.Code -> {
              Spacer(modifier = Modifier.height(6.dp))
              CodeBlockCard(
                data = segment.data,
                onSaveToProject = onSaveCode
              )
              Spacer(modifier = Modifier.height(6.dp))
            }
          }
        }

        // Tool Action Card if AI proposed creating a file or running a command
        if (message.toolAction != null) {
          val action = message.toolAction
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(PureWhiteBackground, RoundedCornerShape(10.dp))
              .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
              .padding(12.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (action.toolName == "create_file") Icons.Default.Code else Icons.Default.Terminal,
                    contentDescription = null,
                    tint = BrandPrimaryOrange,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (action.toolName == "create_file") "FILE ACTION: ${action.targetPath}" else "COMMAND ACTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandTextBlack
                  )
                }
                StatusBadge(
                  text = if (action.requiresApproval) "APPROVAL REQUIRED" else "AUTONOMOUS",
                  color = if (action.requiresApproval) AltronAmber else TerminalGreen
                )
              }

              if (action.command != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "$ ${action.command}",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = TerminalGreen
                )
              }

              Spacer(modifier = Modifier.height(10.dp))
              AltronPrimaryButton(
                text = if (action.toolName == "create_file") "Apply File to Project" else "Execute Command",
                onClick = { onExecuteTool(action) },
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }
    }
  }
}

// Code Block: Dark charcoal background with readable code highlighting
@Composable
fun CodeBlockCard(
  data: CodeBlockData,
  onSaveToProject: (String, String) -> String,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  val haptic = LocalHapticFeedback.current
  val context = LocalContext.current
  var isCopied by remember { mutableStateOf(false) }
  var isExpanded by remember { mutableStateOf(false) }

  val lines = remember(data.code) { data.code.lines() }
  val isLongCode = lines.size > 18
  val displayCode = if (isLongCode && !isExpanded) {
    lines.take(18).joinToString("\n")
  } else {
    data.code
  }

  LaunchedEffect(isCopied) {
    if (isCopied) {
      kotlinx.coroutines.delay(2000)
      isCopied = false
    }
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .border(1.dp, LightBorder, RoundedCornerShape(12.dp)),
    color = CodeBlockBackground
  ) {
    Column {
      // Header Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(CodeBlockHeader)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          StatusBadge(text = data.language, color = BrandPrimaryOrange)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = data.fileName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          // Download / Save button
          IconButton(
            onClick = {
              triggerTactileFeedback(context, haptic)
              val resultMsg = onSaveToProject(data.fileName, data.code)
              android.widget.Toast.makeText(context, resultMsg, android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CloudDownload,
              contentDescription = "Save to project",
              tint = BrandPrimaryOrange,
              modifier = Modifier.size(16.dp)
            )
          }

          // Copy Code button
          IconButton(
            onClick = {
              triggerTactileFeedback(context, haptic)
              try {
                clipboardManager.setText(AnnotatedString(data.code))
                isCopied = true
                android.widget.Toast.makeText(context, "Code copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
              } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to copy: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
              contentDescription = if (isCopied) "Copied!" else "Copy Code",
              tint = if (isCopied) TerminalGreen else Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Code content area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
      ) {
        Text(
          text = displayCode,
          fontFamily = FontFamily.Monospace,
          fontSize = 12.5.sp,
          color = CodeBlockText,
          lineHeight = 18.sp,
          modifier = Modifier.horizontalScroll(rememberScrollState())
        )
      }

      // Expand / Collapse toggle if long
      if (isLongCode) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              triggerTactileFeedback(context, haptic)
              isExpanded = !isExpanded
            }
            .background(CodeBlockHeader)
            .padding(vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isExpanded) "▲ Collapse code (${lines.size} lines)" else "▼ Expand code (${lines.size} lines)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPrimaryOrange
          )
        }
      }
    }
  }
}
