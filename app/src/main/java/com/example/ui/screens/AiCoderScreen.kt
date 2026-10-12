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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
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
import com.example.ui.navigation.Screen
import com.example.ui.theme.StitchBorder
import com.example.ui.theme.StitchCardBackground
import com.example.ui.theme.StitchError
import com.example.ui.theme.StitchMainBackground
import com.example.ui.theme.StitchPressedAccent
import com.example.ui.theme.StitchPrimaryAccent
import com.example.ui.theme.StitchPrimaryText
import com.example.ui.theme.StitchSecondaryBackground
import com.example.ui.theme.StitchSecondaryText
import com.example.ui.theme.StitchSoftAccent
import com.example.ui.theme.StitchSuccess
import com.example.ui.viewmodel.AltronViewModel

/**
 * SCREEN 3: AI CODER WORKSPACE
 * High fidelity implementation of the Google Stitch Coder Workspace:
 * - Sub-header with project context (AltronCore-Android, feat/agentic-loop)
 * - Agent mode & model pill
 * - Target files pill track (StatefulScreen.kt, NavigationHost.kt, build.gradle.kts)
 * - Conversation canvas with User prompt & attachment
 * - Agent Tool Executions expandable accordion (3 actions completed)
 * - AI Explanation Card (Claude 3.5 Sonnet)
 * - Interactive Code Diff Inspection card (+24, -8)
 * - Gradle verification badge (18 tests passed)
 * - Proposed Changes Action Bar (Apply Changes, Reject)
 * - Docked interactive AI composer drawer
 */
@Composable
fun AiCoderScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val activeProject by viewModel.activeProject.collectAsState()
  val activeProvider by viewModel.activeProvider.collectAsState()
  val pendingDiff by viewModel.pendingDiff.collectAsState()
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  var isTraceExpanded by remember { mutableStateOf(true) }
  var inputPrompt by remember { mutableStateOf("") }
  var isDiffApplied by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(StitchMainBackground)
  ) {
    // Top Context Sub-Header
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = StitchSecondaryBackground,
      shadowElevation = 1.dp,
      border = androidx.compose.foundation.BorderStroke(0.5.dp, StitchBorder)
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
          ) {
            IconButton(
              onClick = { viewModel.setScreen(Screen.HOME) },
              modifier = Modifier.size(34.dp)
            ) {
              Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column {
              Text(
                text = activeProject?.name ?: "AltronCore-Android",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = StitchPrimaryText
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ForkRight, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "feat/agentic-loop", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
              }
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Agent mode pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(StitchSoftAccent)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "AGENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryAccent)
              }
            }

            // Model Selector Button
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(StitchCardBackground)
                .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
                .clickable { viewModel.setScreen(Screen.SETTINGS) }
                .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Claude 3.5", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText)
                Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(14.dp))
              }
            }

            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
              Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Session status strip
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Session #CD-8812", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
            Text(" • ", fontSize = 10.sp, color = StitchSecondaryText)
            Text(text = "3 files staged", fontSize = 11.sp, color = StitchSuccess, fontWeight = FontWeight.Medium)
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Runtime: ", fontSize = 10.5.sp, color = StitchSecondaryText)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(StitchCardBackground)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(text = "KMP 2.0", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText)
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Target Files Pill Track
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          TargetFilePill("StatefulScreen.kt", "+14", isSelected = false)
          TargetFilePill("NavigationHost.kt", "+24 -8", isSelected = true)
          TargetFilePill("build.gradle.kts", "+2", isSelected = false)

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(StitchCardBackground)
              .border(1.dp, StitchBorder, RoundedCornerShape(20.dp))
              .clickable { viewModel.setScreen(Screen.FILE_EXPLORER) }
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text(text = "Attach", fontSize = 10.5.sp, color = StitchSecondaryText)
            }
          }
        }
      }
    }

    // Scrollable Conversation Area
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item { Spacer(modifier = Modifier.height(4.dp)) }

      // User Prompt Bubble
      item {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.End
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp, end = 2.dp)
          ) {
            Text(text = "You", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = StitchSecondaryText)
            Text(text = " • 14:02", fontSize = 11.sp, color = StitchSecondaryText)
          }

          Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = StitchSecondaryBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Refactor our root navigation stack to support autonomous state restoration across configuration changes and process death in KMP. Add the necessary serialization serializers and ensure test coverage passes.",
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                color = StitchPrimaryText
              )

              Spacer(modifier = Modifier.height(8.dp))

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(StitchCardBackground)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Attachment, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "ArchReviewSpec.md", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText)
                  Text(text = " • 4.2 KB", fontSize = 10.5.sp, color = StitchSecondaryText)
                }
              }
            }
          }
        }
      }

      // Agent Tool Executions accordion
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchSecondaryBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { isTraceExpanded = !isTraceExpanded },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSuccess.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(text = "Agent Tool Executions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                  Text(text = "3 actions completed • 1.4s compute", fontSize = 11.sp, color = StitchSuccess)
                }
              }

              Icon(
                imageVector = if (isTraceExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = StitchSecondaryText
              )
            }

            AnimatedVisibility(visible = isTraceExpanded) {
              Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                AgentExecutionStepRow(
                  name = "Read file",
                  target = "NavigationHost.kt",
                  details = "Parsed 142 lines AST • extracted RouteNavigator tokens"
                )
                AgentExecutionStepRow(
                  name = "Symbol search",
                  target = ":feature:nav",
                  details = "Found AppStateHolder in 3 downstream destination hosts"
                )
                AgentExecutionStepRow(
                  name = "Gradle Test Run",
                  target = "18 passed",
                  details = "./gradlew :feature:navigation:testDebugUnitTest"
                )
              }
            }
          }
        }
      }

      // AI Explanation Card
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
          shadowElevation = 1.dp
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(StitchPrimaryAccent),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Claude 3.5 Sonnet", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              }
              Text(text = "Autonomous Patch", fontSize = 11.sp, color = StitchSecondaryText)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "I've updated the root NavigationHost.kt with a decoupled Saver<Navigator, Any> implementation and wrapped child destination states in SaveableStateHolderProvider. This guarantees survivor state across configuration changes without leaking memory.",
              fontSize = 13.sp,
              lineHeight = 18.5.sp,
              color = StitchPrimaryText
            )
          }
        }
      }

      // Code Diff Inspection Card
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
          shadowElevation = 1.dp
        ) {
          Column {
            // Diff Header
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(StitchSecondaryBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "NavigationHost.kt", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchCardBackground)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(text = "Kotlin", fontSize = 10.sp, color = StitchSecondaryText)
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "+24", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchSuccess, fontFamily = FontFamily.Monospace)
                Text(text = "-8", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchError, fontFamily = FontFamily.Monospace)
              }
            }

            // Diff content lines
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState())
            ) {
              DiffLine(num = "41", prefix = " ", text = "@Composable", isDeleted = false, isAdded = false)
              DiffLine(num = "42", prefix = " ", text = "fun NavigationHost(", isDeleted = false, isAdded = false)
              DiffLine(num = "43", prefix = "-", text = "    val navigator = remember { NavigatorImpl() }", isDeleted = true, isAdded = false)
              DiffLine(num = "43", prefix = "+", text = "    val navigator = rememberSaveable(saver = NavigatorSaver) { NavigatorImpl() }", isDeleted = false, isAdded = true)
              DiffLine(num = "44", prefix = " ", text = "    val saveableHolder = rememberSaveableStateHolder()", isDeleted = false, isAdded = false)
              DiffLine(num = "45", prefix = "+", text = "    saveableHolder.SaveableStateProvider(key = destination.route) {", isDeleted = false, isAdded = true)
              DiffLine(num = "46", prefix = " ", text = "        destination.Content()", isDeleted = false, isAdded = false)
            }

            // Diff Toolbar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(StitchSecondaryBackground)
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchCardBackground)
                    .clickable { viewModel.setScreen(Screen.CODE_EDITOR) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Splitscreen, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Review Diff", fontSize = 11.sp, color = StitchPrimaryText)
                  }
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchCardBackground)
                    .clickable { viewModel.setScreen(Screen.CODE_EDITOR) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Open File", fontSize = 11.sp, color = StitchPrimaryText)
                  }
                }
              }

              IconButton(
                onClick = {
                  clipboardManager.setText(AnnotatedString("val navigator = rememberSaveable(saver = NavigatorSaver) { NavigatorImpl() }"))
                  android.widget.Toast.makeText(context, "Snippet copied!", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = StitchSecondaryText, modifier = Modifier.size(15.dp))
              }
            }
          }
        }
      }

      // Verification / Gradle Check Card
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchSecondaryBackground
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(StitchSuccess.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(14.dp))
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(text = "Gradle Check Status", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                Text(text = "All 18 unit tests green • Kotlin 2.0.20 Target", fontSize = 11.sp, color = StitchSuccess)
              }
            }
            Text(text = "0 warnings", fontSize = 10.5.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
          }
        }
      }

      // Proposed Changes Action Bar
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchSecondaryBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isDiffApplied) "Changes applied to workspace" else "Ready to merge staged diff",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = StitchPrimaryText
              )
              Text(text = "3 files modified", fontSize = 11.5.sp, color = StitchSecondaryText)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  isDiffApplied = true
                  viewModel.applyPendingDiff()
                  android.widget.Toast.makeText(context, "Applied 3 staged files to workspace!", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .testTag("coder_apply_changes_btn"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = StitchPrimaryAccent,
                  contentColor = Color.White
                )
              ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (isDiffApplied) "Applied (3 files)" else "Apply Changes (3 files)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  viewModel.rejectPendingDiff()
                  android.widget.Toast.makeText(context, "Staged diff rejected", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = StitchCardBackground,
                  contentColor = StitchPrimaryText
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
              ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = StitchError, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Reject", fontSize = 13.sp)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                  android.widget.Toast.makeText(context, "Restored previous state", android.widget.Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Undo last turn", fontSize = 11.sp, color = StitchSecondaryText)
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                  android.widget.Toast.makeText(context, "Auto-Commit enabled", android.widget.Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Configure Auto-Commit", fontSize = 11.sp, color = StitchSecondaryText)
              }
            }
          }
        }
      }

      // Assistant Insight Tip
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = StitchSecondaryBackground
        ) {
          Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Tip: You can say \"Revert build.gradle.kts\" to accept only code files while leaving build scripts untouched.",
              fontSize = 12.sp,
              lineHeight = 16.sp,
              color = StitchSecondaryText
            )
          }
        }
      }

      item { Spacer(modifier = Modifier.height(10.dp)) }
    }

    // Docked Interactive AI Composer Drawer
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = StitchSecondaryBackground,
      shadowElevation = 8.dp,
      border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
    ) {
      Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        // Context Pill Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(StitchCardBackground)
                .border(1.dp, StitchBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Attachment, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "+ Context: 2 files", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText)
              }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Autonomous execution", fontSize = 11.sp, color = StitchSecondaryText)
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { viewModel.setScreen(Screen.CHATS) }
          ) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "History", fontSize = 11.sp, color = StitchSecondaryText)
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Text Composer Card
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            OutlinedTextField(
              value = inputPrompt,
              onValueChange = { inputPrompt = it },
              placeholder = { Text(text = "Instruct Coder or ask to refine diff...", fontSize = 13.5.sp, color = StitchSecondaryText.copy(alpha = 0.7f)) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("coder_input_field"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
              ),
              maxLines = 4
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                  Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Attach", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                  Icon(imageVector = Icons.Default.DataObject, contentDescription = "Insert code", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                  Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
                }
              }

              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(StitchPrimaryAccent)
                  .clickable {
                    if (inputPrompt.isNotBlank()) {
                      viewModel.sendAgentMessage(inputPrompt)
                      inputPrompt = ""
                    }
                  }
                  .testTag("coder_send_btn"),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TargetFilePill(name: String, changes: String, isSelected: Boolean) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(if (isSelected) StitchSoftAccent else StitchCardBackground)
      .border(1.dp, if (isSelected) StitchPrimaryAccent else StitchBorder, RoundedCornerShape(20.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = if (isSelected) Icons.Default.EditNote else Icons.Default.Description,
        contentDescription = null,
        tint = if (isSelected) StitchPrimaryAccent else StitchSecondaryText,
        modifier = Modifier.size(12.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = name,
        fontSize = 10.5.sp,
        fontFamily = FontFamily.Monospace,
        color = if (isSelected) StitchPrimaryAccent else StitchPrimaryText,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = changes,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (changes.contains("-")) StitchPrimaryAccent else StitchSuccess,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
private fun AgentExecutionStepRow(name: String, target: String, details: String) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(8.dp),
    color = StitchCardBackground,
    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
  ) {
    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
      Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(15.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = name, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText, fontFamily = FontFamily.Monospace)
          Text(text = target, fontSize = 11.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
        }
        Text(text = details, fontSize = 10.5.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
      }
    }
  }
}

@Composable
private fun DiffLine(num: String, prefix: String, text: String, isDeleted: Boolean, isAdded: Boolean) {
  val bgColor = when {
    isDeleted -> Color(0xFFFDE8E6)
    isAdded -> Color(0xFFE6F4EA)
    else -> Color.Transparent
  }
  val textColor = when {
    isDeleted -> StitchError
    isAdded -> StitchSuccess
    else -> StitchPrimaryText
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = num, fontSize = 11.sp, color = StitchSecondaryText.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace, modifier = Modifier.width(28.dp))
    Text(text = prefix, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor, fontFamily = FontFamily.Monospace, modifier = Modifier.width(14.dp))
    Text(text = text, fontSize = 11.5.sp, color = textColor, fontFamily = FontFamily.Monospace)
  }
}
