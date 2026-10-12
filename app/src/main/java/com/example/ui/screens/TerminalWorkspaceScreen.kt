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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TerminalEntry
import com.example.data.model.TerminalEntryType
import com.example.ui.navigation.Screen
import com.example.ui.theme.StitchBorder
import com.example.ui.theme.StitchCardBackground
import com.example.ui.theme.StitchError
import com.example.ui.theme.StitchMainBackground
import com.example.ui.theme.StitchPrimaryAccent
import com.example.ui.theme.StitchPrimaryText
import com.example.ui.theme.StitchSecondaryBackground
import com.example.ui.theme.StitchSecondaryText
import com.example.ui.theme.StitchSoftAccent
import com.example.ui.theme.StitchSuccess
import com.example.ui.viewmodel.AltronViewModel

/**
 * SCREEN 6: TERMINAL WORKSPACE
 * Recreates the Google Stitch Terminal workspace design:
 * - Terminal header (AltronCore-Android, bash 5.2, Java 17, SDK 34)
 * - Quick command toolbar (./gradlew test, git status, adb logcat, kmp clean)
 * - Dark terminal output viewport with stdout streaming
 * - "Compiler Error Detected" floating action banner (Auto-Fix with Claude)
 * - Command input bar ($ prefix, history, run action)
 */
@Composable
fun TerminalWorkspaceScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val terminalEntries by viewModel.terminalEntries.collectAsState()
  val isRunning by viewModel.isTerminalRunning.collectAsState()
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  var commandInput by remember { mutableStateOf("altron-cli build --variant=debug") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(StitchMainBackground)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item { Spacer(modifier = Modifier.height(4.dp)) }

    // Terminal Header
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = StitchSecondaryBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = { viewModel.setScreen(Screen.HOME) }, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(4.dp))
              Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "AltronCore • bash 5.2 • #9921",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = StitchPrimaryText
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(StitchCardBackground)
                  .clickable {
                    android.widget.Toast.makeText(context, "Terminal session split", android.widget.Toast.LENGTH_SHORT).show()
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.VerticalSplit, contentDescription = "Split", tint = StitchSecondaryText, modifier = Modifier.size(16.dp))
              }
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(StitchCardBackground)
                  .clickable { viewModel.clearTerminal() },
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear", tint = StitchSecondaryText, modifier = Modifier.size(16.dp))
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(StitchSuccess.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Java 17.0.2", fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, color = StitchSuccess, fontWeight = FontWeight.Bold)
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(StitchCardBackground)
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(text = "SDK 34", fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(StitchCardBackground)
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(text = "Debug Variant", fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
            }
          }
        }
      }
    }

    // Quick Command Toolbar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TerminalQuickPill(icon = Icons.Default.PlayArrow, cmd = "./gradlew testDebugUnitTest", tint = StitchPrimaryAccent) {
          viewModel.executeTerminalCommand("./gradlew testDebugUnitTest")
        }
        TerminalQuickPill(icon = Icons.Default.Source, cmd = "git status -s", tint = StitchSecondaryText) {
          viewModel.executeTerminalCommand("git status -s")
        }
        TerminalQuickPill(icon = Icons.Default.Adb, cmd = "adb logcat --pid=altron", tint = StitchSuccess) {
          viewModel.executeTerminalCommand("adb logcat --pid=altron")
        }
        TerminalQuickPill(icon = Icons.Default.CleaningServices, cmd = "kmp clean", tint = StitchSecondaryText) {
          viewModel.executeTerminalCommand("kmp clean")
        }
      }
    }

    // Terminal Output Viewport (Dark Console Box)
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1A1917),
        shadowElevation = 4.dp
      ) {
        Column {
          // Viewport Header Strip
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF252422))
              .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFBA1A1A)))
              Spacer(modifier = Modifier.width(6.dp))
              Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFD97757)))
              Spacer(modifier = Modifier.width(6.dp))
              Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF38684D)))
              Spacer(modifier = Modifier.width(10.dp))
              Text(text = "stdout — live session", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFCAC6BE))
            }

            Row(
              modifier = Modifier.clickable {
                clipboardManager.setText(AnnotatedString("> Task :app:linkDebugExecutableLinuxX64 FAILED\nUnresolved reference: posix_spawn_file_actions_addchdir"))
                android.widget.Toast.makeText(context, "Terminal output copied!", android.widget.Toast.LENGTH_SHORT).show()
              },
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFFCAC6BE), modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Copy", fontSize = 11.sp, color = Color(0xFFCAC6BE))
            }
          }

          // Output Log Stream
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            SelectionContainer {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "> Task :app:preBuild UP-TO-DATE", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFF9ED2B1))
                Text(text = "> Task :app:compileDebugKotlin", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFF9ED2B1))
                Text(text = "w: /workspace/altron-core/src/commonMain/kotlin/SyncEngine.kt:42:15\n  Parameter 'dispatcher' is never used and could be removed", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFCAC6BE))
                Text(text = "> Task :app:linkDebugExecutableLinuxX64 FAILED", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFFFFDBD0), fontWeight = FontWeight.Bold)

                // Error Box
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFBA1A1A).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFBA1A1A), RoundedCornerShape(6.dp))
                    .padding(10.dp)
                ) {
                  Text(
                    text = "e: /workspace/altron-core/src/linuxX64Main/kotlin/PlatformIO.kt:18:22\nUnresolved reference: posix_spawn_file_actions_addchdir\nat com.altron.code.PlatformIO.initProcess(PlatformIO.kt:18)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    color = Color(0xFFFFDAD6),
                    lineHeight = 16.sp
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchPrimaryAccent))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = "Process exited with code 1. Awaiting next command...", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFCAC6BE))
                }
              }
            }
          }
        }
      }
    }

    // Compiler Error Detected Floating Banner
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StitchSoftAccent,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchPrimaryAccent.copy(alpha = 0.4f)),
        shadowElevation = 2.dp
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(StitchPrimaryAccent),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = "Compiler Error Detected", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              Text(text = "Hand off stack trace to Claude 3.5 Sonnet for auto-patch", fontSize = 11.sp, color = StitchSecondaryText)
            }
          }

          Button(
            onClick = {
              viewModel.sendAgentMessage("Fix this compilation error: Unresolved reference posix_spawn_file_actions_addchdir in PlatformIO.kt")
              viewModel.setScreen(Screen.CODER)
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryAccent, contentColor = Color.White),
            modifier = Modifier.testTag("terminal_autofix_btn")
          ) {
            Text(text = "Auto-Fix", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
          }
        }
      }
    }

    // Command Input Bar
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StitchCardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
        shadowElevation = 1.dp
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "$", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryAccent, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(start = 6.dp, end = 6.dp))

          OutlinedTextField(
            value = commandInput,
            onValueChange = { commandInput = it },
            modifier = Modifier
              .weight(1f)
              .testTag("terminal_command_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
          )

          IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
          }

          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(StitchPrimaryAccent)
              .clickable {
                if (commandInput.isNotBlank()) {
                  viewModel.executeTerminalCommand(commandInput)
                }
              }
              .testTag("terminal_run_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = Color.White, modifier = Modifier.size(20.dp))
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

@Composable
private fun TerminalQuickPill(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  cmd: String,
  tint: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(StitchCardBackground)
      .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 7.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(text = cmd, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText)
    }
  }
}
