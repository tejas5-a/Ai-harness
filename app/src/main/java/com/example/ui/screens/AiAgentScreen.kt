package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AgentMode
import com.example.data.model.AgentToolAction
import com.example.data.model.AttachmentType
import com.example.data.model.ChatAttachment
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.components.Floating3DElements
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.CodeBlockBackground
import com.example.ui.theme.CodeBlockHeader
import com.example.ui.theme.CodeBlockText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurfaceElevated
import com.example.ui.theme.PureWhiteBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.viewmodel.AltronViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

// Represents parsed parts of assistant message
sealed class ContentSegment {
  data class Text(val content: String) : ContentSegment()
  data class Code(val data: CodeBlockData) : ContentSegment()
}

data class CodeBlockData(
  val language: String,
  val fileName: String,
  val code: String
)

fun parseMessageSegments(content: String): List<ContentSegment> {
  val segments = mutableListOf<ContentSegment>()
  val codePattern = Pattern.compile("```([a-zA-Z0-9_:-]*)\\n([\\s\\S]*?)```")
  val matcher = codePattern.matcher(content)

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

    val header = matcher.group(1).orEmpty().trim()
    val codeBody = matcher.group(2).orEmpty().trimEnd()

    val (lang, rawName) = when {
      header.startsWith("file:") -> "FILE" to header.removePrefix("file:").trim()
      header.isNotEmpty() -> header.uppercase() to inferFileName(header)
      else -> "CODE" to "snippet.txt"
    }

    val fileName = if (rawName.isNotBlank()) rawName else inferFileName(lang)

    segments.add(
      ContentSegment.Code(
        CodeBlockData(
          language = lang,
          fileName = fileName,
          code = codeBody
        )
      )
    )

    lastIndex = end
  }

  if (lastIndex < content.length) {
    val trailing = content.substring(lastIndex).trim()
    if (trailing.isNotEmpty()) {
      segments.add(ContentSegment.Text(trailing))
    }
  }

  return segments.ifEmpty { listOf(ContentSegment.Text(content)) }
}

fun inferFileName(lang: String): String {
  return when (lang.uppercase()) {
    "KOTLIN", "KT" -> "Main.kt"
    "JAVA" -> "Main.java"
    "PYTHON", "PY" -> "main.py"
    "JAVASCRIPT", "JS" -> "script.js"
    "TYPESCRIPT", "TS" -> "index.ts"
    "TSX" -> "App.tsx"
    "JSX" -> "App.jsx"
    "HTML" -> "index.html"
    "CSS" -> "styles.css"
    "JSON" -> "data.json"
    "BASH", "SHELL", "SH" -> "run.sh"
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

@OptIn(ExperimentalMaterial3Api::class)
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
  val pendingAttachments by viewModel.pendingAttachments.collectAsState()

  val haptic = LocalHapticFeedback.current
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var inputPrompt by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  var showModelMenu by remember { mutableStateOf(false) }
  var showHistoryMenu by remember { mutableStateOf(false) }
  var showAttachmentMenu by remember { mutableStateOf(false) }

  // Attachment Launchers
  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
  ) { uris ->
    uris.forEach { uri ->
      viewModel.processUriAttachment(context, uri, AttachmentType.IMAGE)
    }
  }

  val documentPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris ->
    uris.forEach { uri ->
      viewModel.processUriAttachment(context, uri)
    }
  }

  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris ->
    uris.forEach { uri ->
      viewModel.processUriAttachment(context, uri, AttachmentType.VIDEO)
    }
  }

  val takePhotoLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) {
      val baos = ByteArrayOutputStream()
      bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, baos)
      val bytes = baos.toByteArray()
      val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
      val attachment = ChatAttachment(
        name = "photo_${System.currentTimeMillis()}.jpg",
        mimeType = "image/jpeg",
        sizeBytes = bytes.size.toLong(),
        type = AttachmentType.IMAGE,
        base64Data = base64
      )
      viewModel.addAttachment(attachment)
    }
  }

  // ROBUST CHAT SCROLLING FIX:
  // 1. Check whether user is near the bottom of conversation.
  // CRITICAL FIX: If visibleItemsInfo is empty, do NOT default to true if totalItems > 1,
  // as empty list during layout passes caused sudden jump back to top/bottom!
  val isNearBottom by remember {
    derivedStateOf {
      val layoutInfo = listState.layoutInfo
      val totalItems = layoutInfo.totalItemsCount
      if (totalItems <= 1) return@derivedStateOf true
      val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
      lastVisible.index >= totalItems - 2
    }
  }

  // 2. Only follow streaming updates when user is near bottom and not actively dragging
  LaunchedEffect(messages.lastOrNull()?.content?.length) {
    if (isNearBottom && !listState.isScrollInProgress && messages.isNotEmpty()) {
      listState.scrollToItem(messages.size - 1)
    }
  }

  // 3. When a new message is posted (user submits prompt or response starts), smoothly scroll to bottom
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val hasUserMessages = messages.any { it.sender == MessageSender.USER }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .widthIn(max = 900.dp)
      .imePadding()
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
        }
      }

      // Action Header Icons: New Chat & History
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(PureWhiteBackground)
            .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
            .clickable {
              triggerTactileFeedback(context, haptic)
              viewModel.startNewChat()
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "New Chat",
              tint = BrandPrimaryOrange,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "New",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = BrandTextBlack
            )
          }
        }

        Box {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(PureWhiteBackground)
              .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
              .clickable { showHistoryMenu = true }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = "History",
                tint = BrandTextSecondary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${conversations.size}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTextBlack
              )
            }
          }

          DropdownMenu(
            expanded = showHistoryMenu,
            onDismissRequest = { showHistoryMenu = false },
            modifier = Modifier.background(PureWhiteBackground)
          ) {
            if (conversations.isEmpty()) {
              DropdownMenuItem(
                text = { Text("No saved chats yet", color = BrandTextSecondary, fontSize = 12.sp) },
                onClick = { showHistoryMenu = false }
              )
            } else {
              conversations.forEach { conv ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(
                        text = conv.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BrandTextBlack,
                        maxLines = 1
                      )
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

    // Mode Selector Segmented Tabs
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      AgentMode.entries.forEach { mode ->
        val isSelected = mode == currentMode
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) BrandPrimaryOrange.copy(alpha = 0.15f) else PureWhiteBackground)
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

    Spacer(modifier = Modifier.height(4.dp))

    // Main Chat Message Area (Persistent LazyColumn container to prevent scroll resets)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // If conversation has no user messages yet, show hero suggestions inside the LazyColumn
        if (!hasUserMessages) {
          item(key = "hero_suggestions_header") {
            HeroSuggestionsCard(
              onSelectSuggestion = { prompt ->
                triggerTactileFeedback(context, haptic)
                inputPrompt = prompt
                viewModel.sendAgentMessage(prompt)
              }
            )
          }
        }

        // Message items with STABLE unique key from message.id
        items(messages, key = { it.id }) { message ->
          ChatMessageBubble(
            message = message,
            onExecuteTool = { action -> viewModel.executeAgentTool(message, action) },
            onSaveCode = { fileName, code -> viewModel.saveCodeBlockToFile(fileName, code) }
          )
        }

        if (isThinking) {
          item(key = "agent_streaming_indicator") {
            StreamingIndicator(
              activeModel = activeProvider.selectedModel,
              onStop = { viewModel.cancelAgent() }
            )
          }
        }
      }

      // "Jump to latest" floating button when user has scrolled upward
      if (!isNearBottom && messages.size > 2) {
        Surface(
          onClick = {
            coroutineScope.launch {
              listState.animateScrollToItem(messages.size - 1)
            }
          },
          shape = RoundedCornerShape(20.dp),
          color = BrandPrimaryOrange,
          shadowElevation = 6.dp,
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 12.dp, end = 8.dp)
            .testTag("jump_to_latest_btn")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ArrowDownward,
              contentDescription = "Jump to latest",
              tint = BrandTextBlack,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Jump to latest",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = BrandTextBlack
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Attachment Previews row immediately above composer
    if (pendingAttachments.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 6.dp)
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        pendingAttachments.forEach { att ->
          AttachmentPreviewCard(
            attachment = att,
            onRemove = { viewModel.removePendingAttachment(att.id) }
          )
        }
      }
    }

    // Chat Input Composer Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Attachment Button (Plus / Paperclip)
      IconButton(
        onClick = {
          triggerTactileFeedback(context, haptic)
          showAttachmentMenu = true
        },
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(LightSurfaceElevated)
          .border(1.dp, LightBorder, CircleShape)
          .testTag("ai_agent_attach_btn")
      ) {
        Icon(
          imageVector = Icons.Default.AttachFile,
          contentDescription = "Add Attachments",
          tint = BrandPrimaryOrange,
          modifier = Modifier.size(20.dp)
        )
      }

      OutlinedTextField(
        value = inputPrompt,
        onValueChange = { inputPrompt = it },
        placeholder = { Text("Ask ALTRON to code, debug, or inspect...", fontSize = 13.sp, color = BrandTextSecondary) },
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

      val canSend = inputPrompt.isNotBlank() || pendingAttachments.isNotEmpty()

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
            if (canSend) {
              triggerTactileFeedback(context, haptic)
              val text = inputPrompt
              inputPrompt = ""
              viewModel.sendAgentMessage(text)
            }
          },
          enabled = canSend,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (canSend) BrandPrimaryOrange else LightSurfaceElevated)
            .testTag("ai_agent_send_btn")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint = if (canSend) BrandTextBlack else BrandTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }

  // Attachment Modal Bottom Sheet
  if (showAttachmentMenu) {
    ModalBottomSheet(
      onDismissRequest = { showAttachmentMenu = false },
      containerColor = PureWhiteBackground
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        Text(
          text = "Add Attachments",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = BrandTextBlack
        )
        Text(
          text = "Images, documents, code files, and videos for AI reasoning",
          fontSize = 12.sp,
          color = BrandTextSecondary
        )
        Spacer(modifier = Modifier.height(14.dp))

        AttachmentMenuOption(
          icon = Icons.Default.PhotoCamera,
          title = "Take Photo",
          subtitle = "Capture photo via camera",
          onClick = {
            showAttachmentMenu = false
            takePhotoLauncher.launch(null)
          }
        )

        AttachmentMenuOption(
          icon = Icons.Default.Image,
          title = "Upload Images",
          subtitle = "PNG, JPG, JPEG, WebP (up to 5)",
          onClick = {
            showAttachmentMenu = false
            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          }
        )

        AttachmentMenuOption(
          icon = Icons.Default.Videocam,
          title = "Upload Video",
          subtitle = "MP4, MOV, WebM",
          onClick = {
            showAttachmentMenu = false
            videoPickerLauncher.launch("video/*")
          }
        )

        AttachmentMenuOption(
          icon = Icons.Default.Description,
          title = "Upload Document",
          subtitle = "PDF, TXT, MD, CSV, DOCX, JSON",
          onClick = {
            showAttachmentMenu = false
            documentPickerLauncher.launch("*/*")
          }
        )

        AttachmentMenuOption(
          icon = Icons.Default.Code,
          title = "Upload Code File",
          subtitle = "JS, TS, HTML, CSS, Python, Java, Kotlin, Dart",
          onClick = {
            showAttachmentMenu = false
            documentPickerLauncher.launch("*/*")
          }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = { showAttachmentMenu = false },
          modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = LightSurfaceElevated,
            contentColor = BrandTextBlack
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}

@Composable
fun AttachmentMenuOption(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(BrandPrimaryOrange.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = BrandPrimaryOrange, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandTextBlack)
      Text(text = subtitle, fontSize = 11.sp, color = BrandTextSecondary)
    }
  }
}

@Composable
fun AttachmentPreviewCard(
  attachment: ChatAttachment,
  onRemove: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(LightSurfaceElevated)
      .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Icon or miniature image
      if (attachment.type == AttachmentType.IMAGE && attachment.base64Data != null) {
        val bitmap = remember(attachment.base64Data) {
          try {
            val bytes = android.util.Base64.decode(attachment.base64Data, android.util.Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
          } catch (e: Exception) {
            null
          }
        }
        if (bitmap != null) {
          androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
          )
        } else {
          Icon(Icons.Default.Image, contentDescription = null, tint = BrandPrimaryOrange, modifier = Modifier.size(18.dp))
        }
      } else {
        val icon = when (attachment.type) {
          AttachmentType.IMAGE -> Icons.Default.Image
          AttachmentType.VIDEO -> Icons.Default.Videocam
          AttachmentType.DOCUMENT -> Icons.Default.Description
          AttachmentType.CODE -> Icons.Default.Code
        }
        Icon(icon, contentDescription = null, tint = BrandPrimaryOrange, modifier = Modifier.size(18.dp))
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column(modifier = Modifier.widthIn(max = 130.dp)) {
        Text(
          text = attachment.name,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = BrandTextBlack,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = if (attachment.sizeBytes > 0) "${attachment.sizeBytes / 1024} KB" else attachment.type.name,
          fontSize = 9.sp,
          color = BrandTextSecondary
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      IconButton(
        onClick = onRemove,
        modifier = Modifier
          .size(24.dp)
          .testTag("remove_attachment_${attachment.id}")
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Remove attachment",
          tint = BrandTextSecondary,
          modifier = Modifier.size(14.dp)
        )
      }
    }
  }
}

@Composable
fun AttachedFileBadge(attachment: ChatAttachment) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(PureWhiteBackground)
      .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      val icon = when (attachment.type) {
        AttachmentType.IMAGE -> Icons.Default.Image
        AttachmentType.VIDEO -> Icons.Default.Videocam
        AttachmentType.DOCUMENT -> Icons.Default.Description
        AttachmentType.CODE -> Icons.Default.Code
      }
      Icon(icon, contentDescription = null, tint = BrandPrimaryOrange, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = attachment.name,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = BrandTextBlack,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.widthIn(max = 140.dp)
      )
    }
  }
}

@Composable
fun HeroSuggestionsCard(
  onSelectSuggestion: (String) -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp),
    contentAlignment = Alignment.Center
  ) {
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

      Spacer(modifier = Modifier.height(20.dp))

      listOf(
        "Build an HTML5 interactive web page with CSS & JS",
        "Write a Kotlin Compose screen for user profile",
        "Explain and debug my project files and scripts"
      ).forEach { prompt ->
        Button(
          onClick = { onSelectSuggestion(prompt) },
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
}

@Composable
fun StreamingIndicator(
  activeModel: String,
  onStop: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(PureWhiteBackground)
      .border(1.dp, LightBorder, RoundedCornerShape(8.dp))
      .padding(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
          modifier = Modifier.size(16.dp),
          color = BrandPrimaryOrange,
          strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Streaming from $activeModel...",
          fontSize = 12.sp,
          color = BrandPrimaryOrange,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(TerminalRed.copy(alpha = 0.15f))
          .border(0.8.dp, TerminalRed, RoundedCornerShape(6.dp))
          .clickable { onStop() }
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

@Composable
fun ChatMessageBubble(
  message: ChatMessage,
  onExecuteTool: (AgentToolAction) -> Unit,
  onSaveCode: (String, String) -> String
) {
  val isUser = message.sender == MessageSender.USER
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    if (isUser) {
      // User Question: Black text on soft light surface with subtle gray border
      Column(horizontalAlignment = Alignment.End) {
        // Show user attachments if any
        if (message.attachments.isNotEmpty()) {
          Row(
            modifier = Modifier
              .padding(bottom = 6.dp)
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            message.attachments.forEach { att ->
              AttachedFileBadge(attachment = att)
            }
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LightSurfaceElevated)
            .border(1.dp, LightBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
        ) {
          SelectionContainer {
            Text(
              text = message.content,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = BrandTextBlack,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }
      }
    } else {
      // AI Response: Black text on white background
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
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

          // Dedicated "Copy Response" action button for AI responses
          IconButton(
            onClick = {
              clipboardManager.setText(AnnotatedString(message.content))
              android.widget.Toast.makeText(context, "AI response copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
              .size(32.dp)
              .testTag("copy_response_${message.id}")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Response",
              tint = BrandTextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
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
              // SelectionContainer allows word, sentence, and long-press selection on Android
              SelectionContainer {
                Text(
                  text = annotated,
                  style = MaterialTheme.typography.bodyMedium,
                  color = BrandTextBlack,
                  lineHeight = 20.sp,
                  modifier = Modifier.padding(vertical = 4.dp)
                )
              }
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
                SelectionContainer {
                  Text(
                    text = "$ ${action.command}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalGreen
                  )
                }
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

// Code Block: Dark charcoal background with readable code highlighting and selectable text
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

      // Code content area with SelectionContainer for copy and word selection
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
      ) {
        SelectionContainer {
          Text(
            text = displayCode,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.5.sp,
            color = CodeBlockText,
            lineHeight = 18.sp,
            modifier = Modifier.horizontalScroll(rememberScrollState())
          )
        }
      }

      // Expand / Collapse toggle if long
      if (isLongCode) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isExpanded) "Collapse Code" else "Show Full Code (${lines.size} lines)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPrimaryOrange
          )
        }
      }
    }
  }
}

fun triggerTactileFeedback(
  context: android.content.Context,
  haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
  try {
    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
  } catch (e: Exception) {
    // Ignore if unsupported
  }
}
