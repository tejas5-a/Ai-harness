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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.StitchBorder
import com.example.ui.theme.StitchCardBackground
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
 * SCREEN 1: HOME DASHBOARD
 * Faithfully maps Google Stitch Home Dashboard design to Jetpack Compose:
 * - Editorial greeting ("What are we building today?")
 * - Prompt Studio Card
 * - 4 Main Action Cards Grid (Research, Coder, Build APK, GitHub)
 * - Daemon Status Banner
 * - Recent Projects
 * - Recent Chats
 */
@Composable
fun HomeScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val activeProject by viewModel.activeProject.collectAsState()
  val activeProvider by viewModel.activeProvider.collectAsState()
  val projects by viewModel.projects.collectAsState()
  val conversations by viewModel.conversations.collectAsState()

  var promptText by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(StitchMainBackground)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Editorial Warm Greeting Area
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(StitchPrimaryAccent)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "AGENTIC WORKSPACE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = StitchSecondaryText,
            letterSpacing = 1.sp
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "What are we building today?",
          fontSize = 24.sp,
          lineHeight = 30.sp,
          fontWeight = FontWeight.Bold,
          color = StitchPrimaryText,
          letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Research", fontSize = 12.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
          Text(" • ", fontSize = 10.sp, color = StitchPrimaryAccent)
          Text("Code", fontSize = 12.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
          Text(" • ", fontSize = 10.sp, color = StitchPrimaryAccent)
          Text("Build", fontSize = 12.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
          Text(" • ", fontSize = 10.sp, color = StitchPrimaryAccent)
          Text("Ship", fontSize = 12.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
        }
      }
    }

    // Prompt Studio Card
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp), spotColor = Color(0x0A000000)),
        shape = RoundedCornerShape(14.dp),
        color = StitchCardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Pill bar: Model & Project
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(StitchSecondaryBackground)
                .clickable { viewModel.setScreen(Screen.SETTINGS) }
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = activeProvider.selectedModel,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = StitchPrimaryText
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.ExpandMore,
                  contentDescription = null,
                  tint = StitchSecondaryText,
                  modifier = Modifier.size(14.dp)
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(StitchSecondaryBackground)
                  .clickable { viewModel.setScreen(Screen.PROJECTS) }
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = StitchPrimaryAccent,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = activeProject?.name ?: "AltronCore",
                    fontSize = 11.sp,
                    color = StitchSecondaryText
                  )
                }
              }

              Spacer(modifier = Modifier.width(6.dp))

              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(StitchSecondaryBackground)
                  .clickable { viewModel.setScreen(Screen.PROJECTS) },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "New Project",
                  tint = StitchSecondaryText,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Active Input Field
          OutlinedTextField(
            value = promptText,
            onValueChange = { promptText = it },
            placeholder = {
              Text(
                text = "Ask ALTRON or paste a traceback, diff, or prompt...",
                fontSize = 14.sp,
                color = StitchSecondaryText.copy(alpha = 0.7f)
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("home_prompt_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent
            ),
            maxLines = 4
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Accessory Dock & Submit
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { viewModel.setScreen(Screen.CODER) },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.AttachFile,
                  contentDescription = "Attach",
                  tint = StitchSecondaryText,
                  modifier = Modifier.size(18.dp)
                )
              }
              IconButton(
                onClick = { viewModel.setScreen(Screen.CODE_EDITOR) },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Code,
                  contentDescription = "Insert code",
                  tint = StitchSecondaryText,
                  modifier = Modifier.size(18.dp)
                )
              }
              IconButton(
                onClick = {},
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = "Voice",
                  tint = StitchSecondaryText,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(StitchPrimaryAccent)
                .clickable {
                  if (promptText.isNotBlank()) {
                    viewModel.sendAgentMessage(promptText)
                    viewModel.setScreen(Screen.CODER)
                  } else {
                    viewModel.setScreen(Screen.CODER)
                  }
                }
                .testTag("home_submit_prompt_btn"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Submit Prompt",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }

    // 4 Main Action Cards Grid (2x2)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Card 1: Research
          HomeActionCard(
            title = "Research",
            badge = "AI Agent",
            badgeBg = StitchSecondaryBackground,
            badgeColor = StitchSecondaryText,
            desc = "Explore docs, repos & live APIs",
            footerText = "Browse web",
            icon = Icons.Default.Explore,
            iconBg = StitchSoftAccent,
            iconTint = StitchPrimaryAccent,
            onClick = { viewModel.setScreen(Screen.RESEARCH) },
            modifier = Modifier.weight(1f)
          )

          // Card 2: Coder
          HomeActionCard(
            title = "Coder",
            badge = "3.5 Sonnet",
            badgeBg = StitchSecondaryBackground,
            badgeColor = StitchSecondaryText,
            desc = "Autonomous multi-file agent",
            footerText = "Start pair",
            icon = Icons.Default.Terminal,
            iconBg = StitchSecondaryBackground,
            iconTint = StitchPrimaryText,
            onClick = { viewModel.setScreen(Screen.CODER) },
            modifier = Modifier.weight(1f)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Card 3: Build APK
          HomeActionCard(
            title = "Build APK",
            badge = "Gradle 8.5",
            badgeBg = StitchSecondaryBackground,
            badgeColor = StitchSecondaryText,
            desc = "Compile, sign & distribute debug",
            footerText = "Run pipeline",
            icon = Icons.Default.Android,
            iconBg = StitchSuccess.copy(alpha = 0.15f),
            iconTint = StitchSuccess,
            onClick = { viewModel.setScreen(Screen.APK_BUILDER) },
            modifier = Modifier.weight(1f)
          )

          // Card 4: GitHub
          HomeActionCard(
            title = "GitHub",
            badge = "synced",
            badgeBg = StitchSuccess.copy(alpha = 0.15f),
            badgeColor = StitchSuccess,
            desc = "Sync PRs, diffs & tree status",
            footerText = "main • 0 ahead",
            icon = Icons.Default.AccountTree,
            iconBg = StitchSecondaryBackground,
            iconTint = StitchPrimaryText,
            onClick = { viewModel.setScreen(Screen.GITHUB_WORKSPACE) },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Status Banner (Daemon Ready)
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StitchSecondaryBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
          ) {
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(StitchCardBackground),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = StitchPrimaryAccent,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Daemon Ready: AltronRuntime",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StitchPrimaryText,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "RAM: 3.2GB / 8GB • 14 tasks processed",
                fontSize = 11.sp,
                color = StitchSecondaryText
              )
            }
          }
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(StitchSuccess)
          )
        }
      }
    }

    // Recent Projects
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Projects",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = StitchPrimaryText
          )
          Text(
            text = "View all (${projects.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = StitchPrimaryAccent,
            modifier = Modifier.clickable { viewModel.setScreen(Screen.PROJECTS) }
          )
        }

        if (projects.isEmpty()) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = StitchCardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "No projects yet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Create a project or clone a repository to get started with ALTRON CODE AI.",
                fontSize = 12.sp,
                color = StitchSecondaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        } else {
          projects.take(3).forEach { proj ->
            RecentProjectCard(
              name = proj.name,
              description = proj.description.ifBlank { "Workspace Project" },
              lang = proj.type.displayName,
              branch = "main",
              time = "Active",
              onOpen = {
                viewModel.selectProject(proj)
                viewModel.setScreen(Screen.FILE_EXPLORER)
              }
            )
          }
        }
      }
    }

    // Recent Chats
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Chats",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = StitchPrimaryText
          )
          Text(
            text = "All chats",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = StitchPrimaryAccent,
            modifier = Modifier.clickable { viewModel.setScreen(Screen.CHATS) }
          )
        }

        RecentChatPreviewCard(
          title = "Refactor Gradle KTS build dependencies",
          snippet = "I've optimized the multi-module build cache, replaced implementation flags with compileOnly for shared annotations...",
          model = "Claude 3.5 Sonnet",
          badge = "4 file changes",
          status = "Build passing",
          time = "14:20",
          onClick = { viewModel.setScreen(Screen.CHATS) }
        )

        RecentChatPreviewCard(
          title = "Implement Material 3 BottomSheetScaffold",
          snippet = "Here is the idiomatic Jetpack Compose state management pattern for anchoring nested modal sheets with an explicit animateTo...",
          model = "Claude 3.5 Sonnet",
          badge = "2 snippets",
          status = "Archived context",
          time = "Yesterday",
          onClick = { viewModel.setScreen(Screen.CHATS) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun HomeActionCard(
  title: String,
  badge: String,
  badgeBg: Color,
  badgeColor: Color,
  desc: String,
  footerText: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconBg: Color,
  iconTint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .shadow(elevation = 1.dp, shape = RoundedCornerShape(14.dp), spotColor = Color(0x0A000000))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    color = StitchCardBackground,
    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(iconBg),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(badgeBg)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(text = badge, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = badgeColor)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = StitchPrimaryText
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = desc,
          fontSize = 12.sp,
          lineHeight = 16.sp,
          color = StitchSecondaryText
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = footerText,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = StitchPrimaryAccent,
          fontFamily = FontFamily.Monospace
        )
        Icon(
          imageVector = Icons.Default.ArrowForward,
          contentDescription = null,
          tint = StitchPrimaryAccent,
          modifier = Modifier.size(12.dp)
        )
      }
    }
  }
}

@Composable
private fun RecentProjectCard(
  name: String,
  description: String,
  lang: String,
  branch: String,
  time: String,
  onOpen: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = StitchCardBackground,
    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = null,
              tint = StitchPrimaryAccent,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(text = description, fontSize = 12.sp, color = StitchSecondaryText)
        }
        Text(text = time, fontSize = 11.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(StitchSecondaryBackground)
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(text = lang, fontSize = 10.5.sp, color = StitchSecondaryText)
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(StitchSoftAccent)
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(text = branch, fontSize = 10.5.sp, color = StitchPrimaryAccent, fontWeight = FontWeight.Medium)
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSecondaryBackground)
            .clickable(onClick = onOpen)
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Open", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = StitchPrimaryText)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = StitchPrimaryText, modifier = Modifier.size(13.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun RecentChatPreviewCard(
  title: String,
  snippet: String,
  model: String,
  badge: String,
  status: String,
  time: String,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    color = StitchCardBackground,
    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = StitchPrimaryText, maxLines = 1, modifier = Modifier.weight(1f))
        Text(text = time, fontSize = 11.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(text = snippet, fontSize = 12.sp, lineHeight = 16.sp, color = StitchSecondaryText, maxLines = 2)

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSecondaryBackground)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(text = model, fontSize = 10.5.sp, color = StitchSecondaryText)
        }
        Text("•", fontSize = 10.sp, color = StitchSecondaryText)
        Text(text = badge, fontSize = 10.5.sp, color = StitchSecondaryText)
        Text("•", fontSize = 10.sp, color = StitchSecondaryText)
        Text(text = status, fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = StitchSuccess)
      }
    }
  }
}
