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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.data.model.TerminalEntry
import com.example.data.model.TerminalEntryType
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.AltronOrange
import com.example.ui.theme.AltronOrangeBright
import com.example.ui.theme.Graphite700
import com.example.ui.theme.Graphite800
import com.example.ui.theme.Graphite950
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

  var inputCommand by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(entries.size) {
    if (entries.isNotEmpty()) {
      listState.animateScrollToItem(entries.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Graphite950)
      .padding(12.dp)
  ) {
    // Terminal Top Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Graphite800, RoundedCornerShape(10.dp))
        .border(1.dp, Graphite700, RoundedCornerShape(10.dp))
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Terminal,
          contentDescription = null,
          tint = AltronOrange,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "altron@android:[${activeProject?.name ?: "sandbox"}]$",
          fontFamily = FontFamily.Monospace,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = TerminalGreen
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (isRunning) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = AltronOrange
          )
          Spacer(modifier = Modifier.width(8.dp))
        }

        IconButton(
          onClick = { viewModel.clearTerminal() },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Clear",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Quick Command Pills
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      val quickCommands = listOf("ls -la", "pwd", "df -h", "uname -a", "id", "cat README.md", "clear")
      quickCommands.forEach { cmd ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Graphite800)
            .border(0.8.dp, Graphite700, RoundedCornerShape(6.dp))
            .clickable {
              viewModel.runTerminalCommand(cmd)
            }
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = cmd,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = AltronOrangeBright
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Terminal Output Buffer (Scrollable)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .background(Color(0xFF07080A), RoundedCornerShape(8.dp))
        .border(1.dp, Graphite800, RoundedCornerShape(8.dp))
        .padding(10.dp)
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        items(entries) { entry ->
          TerminalEntryLine(entry = entry)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Command Prompt Input
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = inputCommand,
        onValueChange = { inputCommand = it },
        placeholder = { Text("Enter shell command (e.g. ls, mkdir, echo)...", fontSize = 12.sp) },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 12.5.sp,
          color = Color.White
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("terminal_input_field"),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AltronOrange,
          unfocusedBorderColor = Graphite700,
          focusedContainerColor = Graphite800,
          unfocusedContainerColor = Graphite800
        )
      )

      IconButton(
        onClick = {
          if (inputCommand.isNotBlank() && !isRunning) {
            val cmd = inputCommand
            inputCommand = ""
            viewModel.runTerminalCommand(cmd)
          }
        },
        enabled = inputCommand.isNotBlank() && !isRunning,
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(if (inputCommand.isNotBlank() && !isRunning) AltronOrange else Graphite800)
          .testTag("terminal_run_btn")
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Execute",
          tint = if (inputCommand.isNotBlank() && !isRunning) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}

@Composable
fun TerminalEntryLine(entry: TerminalEntry) {
  val result: Pair<Color, String> = when (entry.type) {
    TerminalEntryType.COMMAND_INPUT -> TerminalCyan to ""
    TerminalEntryType.STDOUT -> Color(0xFFE2E8F0) to ""
    TerminalEntryType.STDERR -> TerminalRed to "ERR: "
    TerminalEntryType.SYSTEM_INFO -> TerminalYellow to "ℹ "
    TerminalEntryType.EXIT_CODE -> TerminalGreen to ""
  }
  val color = result.first
  val prefix = result.second

  Text(
    text = "$prefix${entry.text}",
    fontFamily = FontFamily.Monospace,
    fontSize = 12.sp,
    lineHeight = 17.sp,
    color = color
  )
}
