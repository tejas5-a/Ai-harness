package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TerminalEntryType
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.CodeBlockBackground
import com.example.ui.theme.CodeBlockText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.PureWhiteBackground
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalYellow
import com.example.ui.viewmodel.AltronViewModel

@Composable
fun TerminalScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val entries by viewModel.terminalEntries.collectAsState()
  val isRunning by viewModel.isTerminalRunning.collectAsState()
  val activeProject by viewModel.activeProject.collectAsState()
  var commandInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(entries.size) {
    if (entries.isNotEmpty()) {
      listState.animateScrollToItem(entries.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Terminal,
          contentDescription = null,
          tint = BrandPrimaryOrange,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "Terminal Console",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = BrandTextBlack
            )
          )
          Text(
            text = activeProject?.name?.let { "Workspace: $it" } ?: "Sandbox Shell",
            fontSize = 12.sp,
            color = BrandTextSecondary
          )
        }
      }

      IconButton(onClick = { viewModel.clearTerminal() }) {
        Icon(
          imageVector = Icons.Default.Clear,
          contentDescription = "Clear",
          tint = BrandTextBlack
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Quick Command Pills: Orange background with black text
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf("ls -la", "pwd", "date").forEach { cmd ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BrandPrimaryOrange)
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = cmd,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BrandTextBlack,
            modifier = Modifier.testTag("quick_cmd_$cmd")
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Terminal Output Console Card (Dark charcoal with readable syntax colors)
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .clip(RoundedCornerShape(12.dp))
        .border(1.dp, LightBorder, RoundedCornerShape(12.dp)),
      color = CodeBlockBackground
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(entries) { entry ->
          val textColor = when (entry.type) {
            TerminalEntryType.COMMAND_INPUT -> TerminalGreen
            TerminalEntryType.STDOUT -> CodeBlockText
            TerminalEntryType.STDERR -> TerminalRed
            TerminalEntryType.SYSTEM_INFO -> TerminalCyan
            TerminalEntryType.EXIT_CODE -> TerminalYellow
          }
          Text(
            text = entry.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = textColor,
            lineHeight = 18.sp
          )
        }
        if (isRunning) {
          item {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(vertical = 4.dp)
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = BrandPrimaryOrange,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Executing command...",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = BrandPrimaryOrange
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Command Input: White background with subtle gray border
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = commandInput,
        onValueChange = { commandInput = it },
        placeholder = { Text("Enter command (e.g. ls, cat, echo)", fontSize = 13.sp, color = BrandTextSecondary) },
        singleLine = true,
        modifier = Modifier
          .weight(1f)
          .testTag("terminal_command_input"),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = PureWhiteBackground,
          unfocusedContainerColor = PureWhiteBackground,
          focusedBorderColor = BrandPrimaryOrange,
          unfocusedBorderColor = LightBorder,
          focusedTextColor = BrandTextBlack,
          unfocusedTextColor = BrandTextBlack
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = {
          if (commandInput.isNotBlank()) {
            viewModel.runTerminalCommand(commandInput)
            commandInput = ""
          }
        })
      )

      AltronPrimaryButton(
        text = "Run",
        icon = Icons.Default.PlayArrow,
        onClick = {
          if (commandInput.isNotBlank()) {
            viewModel.runTerminalCommand(commandInput)
            commandInput = ""
          }
        },
        enabled = commandInput.isNotBlank() && !isRunning
      )
    }
  }
}
