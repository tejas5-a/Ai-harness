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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.TapAndPlay
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
 * SCREEN 9: GITHUB REPOSITORY WORKSPACE
 * Recreates the Google Stitch GitHub Repository Workspace:
 * - Repository header with branch selector (main) and New Branch / Push actions
 * - Interactive Tabs: Files, Commits (142), Changes (3), PRs (5)
 * - Tab 1 Files: hierarchical repo listing and README preview
 * - Tab 2 Commits: commit history with authors, timestamps, commit hashes
 * - Tab 3 Changes: commit composer box and color-coded diff view
 * - Tab 4 PRs: pull requests with Open & Merged status pills
 */
@Composable
fun GitHubWorkspaceScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var activeTab by remember { mutableStateOf("Files") }
  var commitSummary by remember { mutableStateOf("feat: add autonomous navigation loop") }
  var commitDesc by remember { mutableStateOf("Integrates decoupled Saver with SaveableStateHolderProvider across multiplatform routes.") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(StitchMainBackground)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item { Spacer(modifier = Modifier.height(4.dp)) }

    // Repository Header & Branch Selector
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
              IconButton(onClick = { viewModel.setScreen(Screen.HOME) }, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
              }
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(StitchSoftAccent),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(text = "altron-core", fontSize = 11.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
                Text(text = "android", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Button(
                onClick = {
                  android.widget.Toast.makeText(context, "New branch modal opened", android.widget.Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StitchSecondaryBackground, contentColor = StitchPrimaryText),
                border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                modifier = Modifier.height(36.dp)
              ) {
                Icon(imageVector = Icons.Default.CallSplit, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "New Branch", fontSize = 11.sp)
              }

              Button(
                onClick = {
                  android.widget.Toast.makeText(context, "Pushed local commits to origin/main!", android.widget.Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryAccent, contentColor = Color.White),
                modifier = Modifier.height(36.dp)
              ) {
                Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Push", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Branch selector & status
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(StitchSecondaryBackground)
                .clickable {
                  android.widget.Toast.makeText(context, "Branch switched: main", android.widget.Toast.LENGTH_SHORT).show()
                }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Lan, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "main", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, color = StitchPrimaryText)
                Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(14.dp))
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "3 local changes", fontSize = 11.sp, color = StitchSuccess)
            }
          }
        }
      }
    }

    // Navigation Tabs
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        WorkspaceTabPill("Files", Icons.Default.Folder, null, isSelected = activeTab == "Files") { activeTab = "Files" }
        WorkspaceTabPill("Commits", Icons.Default.History, "142", isSelected = activeTab == "Commits") { activeTab = "Commits" }
        WorkspaceTabPill("Changes", Icons.Default.CallSplit, "3", isSelected = activeTab == "Changes") { activeTab = "Changes" }
        WorkspaceTabPill("PRs", Icons.Default.TapAndPlay, "5", isSelected = activeTab == "PRs") { activeTab = "PRs" }
      }
    }

    // Tab 1: Files
    if (activeTab == "Files") {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Breadcrumbs
          Text(text = "altron-core / android / app/src/main", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)

          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = StitchCardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
          ) {
            Column {
              RepoFileRow(name = "java/com/altron/ai", isFolder = true, time = "Yesterday 14:20")
              RepoFileRow(name = "res/layout", isFolder = true, time = "3 hours ago")
              RepoFileRow(name = "MainActivity.kt", isFolder = false, time = "Just now", badge = "Modified")
              RepoFileRow(name = "AndroidManifest.xml", isFolder = false, time = "2 days ago")
              RepoFileRow(name = "build.gradle.kts", isFolder = false, time = "Last week")
            }
          }

          // README Card
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = StitchCardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Book, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "README.md", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Altron Code Android Companion is an intelligent workspace module connecting Claude 3.5 directly to local device compilers and git worktrees.",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = StitchSecondaryText
              )
              Spacer(modifier = Modifier.height(8.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(StitchSecondaryBackground)
                  .padding(10.dp)
              ) {
                Text(text = "./gradlew assembleDebug --claude-autofix", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText)
              }
            }
          }
        }
      }
    }

    // Tab 2: Commits
    if (activeTab == "Commits") {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Column {
            CommitItemRow(author = "AC", message = "feat: integrate Claude 3.5 inline diff streaming", details = "altron-ai • committed 2 hours ago", hash = "#a89f21")
            CommitItemRow(author = "JD", message = "fix(gradle): resolve Kotlin 1.9 compiler warning", details = "jdoe • committed 5 hours ago", hash = "#f412d0")
            CommitItemRow(author = "AC", message = "chore: initialize Altron workspace repository", details = "altron-ai • yesterday", hash = "#9b812a")
          }
        }
      }
    }

    // Tab 3: Changes
    if (activeTab == "Changes") {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Commit Composer Box
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = StitchCardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(text = "Commit Staged Changes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = commitSummary,
                onValueChange = { commitSummary = it },
                placeholder = { Text("Summary (e.g., feat: add auto-completion listener)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = StitchSecondaryBackground,
                  unfocusedContainerColor = StitchSecondaryBackground,
                  focusedBorderColor = StitchPrimaryAccent,
                  unfocusedBorderColor = StitchBorder
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
              )

              Spacer(modifier = Modifier.height(6.dp))

              OutlinedTextField(
                value = commitDesc,
                onValueChange = { commitDesc = it },
                placeholder = { Text("Detailed description of changes...") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = StitchSecondaryBackground,
                  unfocusedContainerColor = StitchSecondaryBackground,
                  focusedBorderColor = StitchPrimaryAccent,
                  unfocusedBorderColor = StitchBorder
                ),
                shape = RoundedCornerShape(8.dp)
              )

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = "3 files modified", fontSize = 11.5.sp, color = StitchSecondaryText)
                Button(
                  onClick = {
                    android.widget.Toast.makeText(context, "Committed staged changes locally!", android.widget.Toast.LENGTH_SHORT).show()
                  },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryAccent, contentColor = Color.White)
                ) {
                  Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "Commit Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          // File Diff Card
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = StitchCardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
          ) {
            Column {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(StitchSecondaryBackground)
                  .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = "app/src/main/MainActivity.kt", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                }
                Text(text = "+14 -3", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
              }

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFFFAF8F5))
                  .padding(8.dp)
                  .horizontalScroll(rememberScrollState())
              ) {
                DiffPreviewLine(num = "42", prefix = " ", text = "class MainActivity : ComponentActivity() {", color = StitchSecondaryText)
                DiffPreviewLine(num = "43", prefix = "-", text = "    private val aiModel = \"claude-3-haiku\"", color = StitchError, bg = StitchError.copy(alpha = 0.08f))
                DiffPreviewLine(num = "43", prefix = "+", text = "    private val aiModel = \"claude-3-5-sonnet\"", color = StitchSuccess, bg = StitchSuccess.copy(alpha = 0.08f))
                DiffPreviewLine(num = "44", prefix = "+", text = "    private val isStreamingEnabled = true", color = StitchSuccess, bg = StitchSuccess.copy(alpha = 0.08f))
                DiffPreviewLine(num = "45", prefix = " ", text = "    override fun onCreate(savedInstanceState: Bundle?) {", color = StitchSecondaryText)
              }
            }
          }
        }
      }
    }

    // Tab 4: PRs
    if (activeTab == "PRs") {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Column {
            PrItemRow(title = "Refactor Terminal Emulator Engine", author = "altron-ai", number = "#42", status = "Open", isOpen = true)
            PrItemRow(title = "Add Jetpack Compose Material 3 Tokens", author = "jdoe", number = "#41", status = "Merged", isOpen = false)
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

@Composable
private fun WorkspaceTabPill(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  badge: String? = null,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) StitchPrimaryAccent else StitchSecondaryBackground)
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 7.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Color.White else StitchSecondaryText, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) Color.White else StitchPrimaryText
      )
      if (badge != null) {
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.25f) else StitchCardBackground)
            .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
          Text(text = badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else StitchSecondaryText)
        }
      }
    }
  }
}

@Composable
private fun RepoFileRow(name: String, isFolder: Boolean, time: String, badge: String? = null) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = if (isFolder) Icons.Default.Folder else Icons.Default.Description,
        contentDescription = null,
        tint = if (isFolder) StitchSuccess else StitchPrimaryAccent,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(text = name, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      if (badge != null) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StitchSoftAccent)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(text = badge, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryAccent)
        }
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(text = time, fontSize = 11.sp, color = StitchSecondaryText)
    }
  }
}

@Composable
private fun CommitItemRow(author: String, message: String, details: String, hash: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top
  ) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(StitchSoftAccent),
        contentAlignment = Alignment.Center
      ) {
        Text(text = author, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryAccent)
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(text = message, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = StitchPrimaryText)
        Text(text = details, fontSize = 11.sp, color = StitchSecondaryText)
      }
    }
    Text(text = hash, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
  }
}

@Composable
private fun PrItemRow(title: String, author: String, number: String, status: String, isOpen: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
      Text(text = "$number opened by $author", fontSize = 11.sp, color = StitchSecondaryText)
    }
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .background(if (isOpen) StitchSuccess.copy(alpha = 0.15f) else StitchSecondaryBackground)
        .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
      Text(text = status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isOpen) StitchSuccess else StitchSecondaryText)
    }
  }
}

@Composable
private fun DiffPreviewLine(num: String, prefix: String, text: String, color: Color, bg: Color = Color.Transparent) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(bg)
      .padding(vertical = 1.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = num, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText.copy(alpha = 0.6f), modifier = Modifier.width(28.dp))
    Text(text = prefix, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = color, modifier = Modifier.width(12.dp))
    Text(text = text, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace, color = color)
  }
}
