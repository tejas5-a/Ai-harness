package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentToolAction
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.components.AltronSecondaryButton
import com.example.ui.components.PulsingAgentStatus
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.AltronOrange
import com.example.ui.theme.AltronOrangeBright
import com.example.ui.theme.AltronOrangeRim
import com.example.ui.theme.Graphite700
import com.example.ui.theme.Graphite800
import com.example.ui.theme.Graphite850
import com.example.ui.theme.Graphite950
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.viewmodel.AltronViewModel

@Composable
fun AiAgentScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val messages by viewModel.chatMessages.collectAsState()
  val isThinking by viewModel.isAgentThinking.collectAsState()
  val activeProvider by viewModel.activeProvider.collectAsState()
  val activeProject by viewModel.activeProject.collectAsState()

  var inputPrompt by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    // Agent Top Status Header
    AltronGlassCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("ai_agent_header_card")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          PulsingAgentStatus(isThinking = isThinking)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${activeProvider.providerType.displayName} • ${activeProvider.selectedModel}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = { viewModel.setScreen(Screen.PROVIDERS) },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Configure API",
            tint = AltronOrange,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Prompt Preset Suggestions
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      val presets = listOf(
        "Explain project architecture",
        "Generate Compose UI card",
        "Write unit test",
        "Inspect project files",
        "Fix syntax errors"
      )
      presets.forEach { preset ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .clickable {
              inputPrompt = preset
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = preset,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Chat Messages List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(messages) { message ->
        ChatMessageBubble(
          message = message,
          onExecuteTool = { action ->
            viewModel.executeAgentTool(message, action)
          }
        )
      }

      if (isThinking) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              color = AltronOrange,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "ALTRON reasoning & generating response...",
              fontSize = 12.sp,
              color = AltronOrangeBright,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Chat Input Bar
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
        placeholder = { Text("Ask ALTRON to code, debug, or plan...", fontSize = 13.sp) },
        modifier = Modifier
          .weight(1f)
          .testTag("ai_agent_input_field"),
        shape = RoundedCornerShape(24.dp),
        maxLines = 4,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AltronOrange,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
      )

      IconButton(
        onClick = {
          if (inputPrompt.isNotBlank() && !isThinking) {
            val text = inputPrompt
            inputPrompt = ""
            viewModel.sendAgentMessage(text)
          }
        },
        enabled = inputPrompt.isNotBlank() && !isThinking,
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(if (inputPrompt.isNotBlank() && !isThinking) AltronOrange else Graphite800)
          .testTag("ai_agent_send_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Send,
          contentDescription = "Send",
          tint = if (inputPrompt.isNotBlank() && !isThinking) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessage,
  onExecuteTool: (AgentToolAction) -> Unit
) {
  val isUser = message.sender == MessageSender.USER

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    if (isUser) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
          .background(AltronOrange.copy(alpha = 0.2f))
          .border(1.dp, AltronOrange.copy(alpha = 0.4f), RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
          .padding(14.dp)
      ) {
        Text(
          text = message.content,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    } else {
      AltronGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AltronOrange),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(14.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "ALTRON AGENT",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = AltronOrangeBright
            )
          }

          Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )

          // Tool Action Card if AI proposed creating a file or running a command
          if (message.toolAction != null) {
            val action = message.toolAction
            Spacer(modifier = Modifier.height(10.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Graphite950, RoundedCornerShape(10.dp))
                .border(1.dp, AltronOrangeRim, RoundedCornerShape(10.dp))
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
                      tint = AltronOrange,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = if (action.toolName == "create_file") "FILE ACTION: ${action.targetPath}" else "COMMAND ACTION",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = AltronOrange
                    )
                  }
                  StatusBadge(text = if (action.requiresApproval) "APPROVAL REQUIRED" else "AUTONOMOUS", color = if (action.requiresApproval) AltronAmber else TerminalGreen)
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
}
