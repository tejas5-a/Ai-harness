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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * SCREEN 5: CODE EDITOR
 * Pixel-faithful reproduction of Google Stitch Code Editor design:
 * - Top File Header (NavigationHost.kt, Kotlin, Saved status)
 * - Search & Replace floating toolbar (NavController 2/14)
 * - Code Editor Surface with line numbers 41-58, syntax coloring & active line highlight (line 43)
 * - Bottom editor metadata toolbar (Find, Replace, 4 spaces, 184 lines • 4.6 KB • UTF-8)
 * - "Ask AI about NavigationHost.kt" floating sheet with 5 action chips and prompt input
 */
@Composable
fun CodeEditorScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val activeTab by viewModel.activeTab.collectAsState()
  val activeContent by viewModel.activeContent.collectAsState()
  val isModified by viewModel.isContentModified.collectAsState()
  val context = LocalContext.current

  var searchQuery by remember { mutableStateOf("NavController") }
  var isSearchVisible by remember { mutableStateOf(true) }
  var isAiSheetExpanded by remember { mutableStateOf(true) }
  var aiPromptText by remember { mutableStateOf("") }

  val fileName = activeTab?.file?.name ?: "NavigationHost.kt"

  // Code editor lines preview matching Stitch design
  val codeLines = listOf(
    Pair(41, "@Composable"),
    Pair(42, "fun AltronNavigationHost("),
    Pair(43, "    val navController = rememberSaveable { NavController() }"),
    Pair(44, "    val currentRoute = navController.currentBackStackEntryAsState()"),
    Pair(45, ""),
    Pair(46, "    Scaffold("),
    Pair(47, "        bottomBar = { AltronBottomBar(navController) }"),
    Pair(48, "    ) { innerPadding ->"),
    Pair(49, "        NavHost("),
    Pair(50, "            navController = navController,"),
    Pair(51, "            startDestination = \"home\","),
    Pair(52, "            modifier = Modifier.padding(innerPadding)"),
    Pair(53, "        ) {"),
    Pair(54, "            composable(\"home\") { HomeScreen() }"),
    Pair(55, "            composable(\"projects\") { ProjectsScreen() }"),
    Pair(56, "        }"),
    Pair(57, "    }"),
    Pair(58, "}")
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(StitchMainBackground)
  ) {
    // Top Bar / File Header
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = StitchSecondaryBackground,
      border = androidx.compose.foundation.BorderStroke(0.5.dp, StitchBorder),
      shadowElevation = 1.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { viewModel.setScreen(Screen.FILE_EXPLORER) },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
          }

          Spacer(modifier = Modifier.width(4.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(5.dp))
              Text(text = fileName, fontSize = 13.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(StitchSuccess.copy(alpha = 0.15f))
                  .padding(horizontal = 5.dp, vertical = 1.dp)
              ) {
                Text(text = "Kotlin", fontSize = 10.sp, color = StitchSuccess, fontWeight = FontWeight.Medium)
              }
            }
            Text(text = "app/src/main/java/altron/core/navigation/", fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(StitchCardBackground)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = if (isModified) "Unsaved" else "Saved (14:02)", fontSize = 11.sp, color = StitchSuccess, fontWeight = FontWeight.Medium)
            }
          }

          IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
          }
        }
      }
    }

    // Search & Replace Floating Toolbar
    if (isSearchVisible) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        color = StitchSecondaryBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(StitchCardBackground)
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = searchQuery, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText, modifier = Modifier.weight(1f))
            Text(text = "2/14", fontSize = 11.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
          }

          Spacer(modifier = Modifier.width(8.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(StitchCardBackground)
                .clickable {},
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.ExpandLess, contentDescription = "Prev", tint = StitchSecondaryText, modifier = Modifier.size(16.dp))
            }
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(StitchCardBackground)
                .clickable {},
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.ExpandMore, contentDescription = "Next", tint = StitchSecondaryText, modifier = Modifier.size(16.dp))
            }
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(StitchCardBackground)
                .clickable { isSearchVisible = false },
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = StitchSecondaryText, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }

    // Code Editor Content Surface (Scrollable)
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 14.dp)
    ) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
          shadowElevation = 1.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 10.dp)
              .horizontalScroll(rememberScrollState())
          ) {
            codeLines.forEach { (lineNum, lineText) ->
              val isLine43 = lineNum == 43
              val rowBg = if (isLine43) StitchSoftAccent.copy(alpha = 0.5f) else Color.Transparent
              val lineBorder = if (isLine43) StitchPrimaryAccent else Color.Transparent

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(rowBg)
                  .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Line Number
                Box(
                  modifier = Modifier
                    .width(36.dp)
                    .padding(end = 8.dp),
                  contentAlignment = Alignment.CenterEnd
                ) {
                  Text(
                    text = "$lineNum",
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isLine43) StitchPrimaryAccent else StitchSecondaryText.copy(alpha = 0.6f),
                    fontWeight = if (isLine43) FontWeight.Bold else FontWeight.Normal
                  )
                }

                // Left Active Indicator Border
                Box(modifier = Modifier.width(2.dp).height(18.dp).background(lineBorder))

                Spacer(modifier = Modifier.width(6.dp))

                // Syntax Highlighted Code
                SelectionContainer {
                  Text(
                    text = formatSyntaxLine(lineText),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                  )
                }
              }
            }
          }
        }
      }

      // Bottom Editor Toolbar
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = StitchSecondaryBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
          ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
              Row(modifier = Modifier.clickable { isSearchVisible = true }, verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "Find", fontSize = 11.sp, color = StitchPrimaryText)
              }
              Text(text = "  |  ", fontSize = 11.sp, color = StitchSecondaryText)
              Row(modifier = Modifier.clickable { isSearchVisible = true }, verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.FindReplace, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "Replace", fontSize = 11.sp, color = StitchPrimaryText)
              }
              Text(text = "  |  ", fontSize = 11.sp, color = StitchSecondaryText)
              Text(text = "4 spaces", fontSize = 11.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = StitchSecondaryBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
          ) {
            Text(
              text = "184 lines • 4.6 KB • UTF-8",
              fontSize = 10.5.sp,
              fontFamily = FontFamily.Monospace,
              color = StitchSecondaryText,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      // "Ask AI about NavigationHost.kt" Sheet
      item {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
          shadowElevation = 3.dp
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { isAiSheetExpanded = !isAiSheetExpanded },
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
                Text(text = "Ask AI about $fileName", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              }

              Icon(
                imageVector = if (isAiSheetExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = StitchSecondaryText
              )
            }

            AnimatedVisibility(visible = isAiSheetExpanded) {
              Column(modifier = Modifier.padding(top = 10.dp)) {
                // 5 Action Chips
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  EditorAiPromptChip(icon = Icons.Default.Psychology, label = "Explain code", tint = StitchPrimaryAccent) {
                    aiPromptText = "Explain the architecture of $fileName"
                  }
                  EditorAiPromptChip(icon = Icons.Default.BugReport, label = "Find bugs & leaks", tint = StitchError) {
                    aiPromptText = "Check $fileName for memory leaks and race conditions"
                  }
                  EditorAiPromptChip(icon = Icons.Default.Sync, label = "Refactor state", tint = StitchSuccess) {
                    aiPromptText = "Refactor state restoration in $fileName"
                  }
                  EditorAiPromptChip(icon = Icons.Default.Description, label = "Add KDoc", tint = StitchSecondaryText) {
                    aiPromptText = "Add clean KDoc documentation for $fileName"
                  }
                  EditorAiPromptChip(icon = Icons.Default.Science, label = "Unit tests", tint = StitchPrimaryAccent) {
                    aiPromptText = "Generate unit tests for $fileName"
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Docked prompt input
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StitchSecondaryBackground)
                    .border(1.dp, StitchBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Attach", tint = StitchSecondaryText, modifier = Modifier.size(16.dp))
                  }

                  OutlinedTextField(
                    value = aiPromptText,
                    onValueChange = { aiPromptText = it },
                    placeholder = { Text("Ask Claude 3.5 about this file...", fontSize = 12.sp, color = StitchSecondaryText) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedContainerColor = Color.Transparent,
                      unfocusedContainerColor = Color.Transparent,
                      focusedBorderColor = Color.Transparent,
                      unfocusedBorderColor = Color.Transparent
                    ),
                    maxLines = 2
                  )

                  IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic", tint = StitchSecondaryText, modifier = Modifier.size(16.dp))
                  }

                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(StitchPrimaryAccent)
                      .clickable {
                        if (aiPromptText.isNotBlank()) {
                          viewModel.sendAgentMessage(aiPromptText)
                          aiPromptText = ""
                          viewModel.setScreen(Screen.CODER)
                        }
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                  }
                }
              }
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(24.dp)) }
    }
  }
}

@Composable
private fun EditorAiPromptChip(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  tint: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(StitchSecondaryBackground)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(5.dp))
      Text(text = label, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = StitchPrimaryText)
    }
  }
}

private fun formatSyntaxLine(line: String): androidx.compose.ui.text.AnnotatedString {
  return buildAnnotatedString {
    when {
      line.trim().startsWith("@Composable") -> {
        withStyle(SpanStyle(color = StitchSuccess, fontWeight = FontWeight.SemiBold)) {
          append(line)
        }
      }
      line.contains("fun ") -> {
        val parts = line.split("fun ")
        append(parts[0])
        withStyle(SpanStyle(color = StitchPrimaryAccent, fontWeight = FontWeight.Bold)) {
          append("fun ")
        }
        withStyle(SpanStyle(color = StitchPrimaryText)) {
          append(parts.getOrElse(1) { "" })
        }
      }
      line.contains("val ") -> {
        val parts = line.split("val ")
        append(parts[0])
        withStyle(SpanStyle(color = StitchPrimaryAccent, fontWeight = FontWeight.Bold)) {
          append("val ")
        }
        withStyle(SpanStyle(color = StitchPrimaryText)) {
          append(parts.getOrElse(1) { "" })
        }
      }
      line.contains("\"") -> {
        withStyle(SpanStyle(color = StitchPrimaryText)) {
          append(line)
        }
      }
      else -> {
        withStyle(SpanStyle(color = StitchPrimaryText)) {
          append(line)
        }
      }
    }
  }
}
