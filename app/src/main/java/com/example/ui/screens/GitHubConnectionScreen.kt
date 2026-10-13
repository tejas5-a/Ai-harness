package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GitHubRepo
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
 * SCREEN 8: GITHUB CONNECTION & REPOSITORY HUB
 * Genuine implementation of GitHub integration for ALTRON CODE AI:
 * - Real token-based authentication (Personal Access Token / OAuth)
 * - Authenticated account display with avatar and repository metrics
 * - Searchable list of accessible repositories
 * - Disconnect & Refresh actions
 * - Error banner & Loading indicators
 */
@Composable
fun GitHubConnectionScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val gitHubAccount by viewModel.gitHubAccount.collectAsState()
  val gitHubRepos by viewModel.gitHubRepos.collectAsState()
  val isGitHubLoading by viewModel.isGitHubLoading.collectAsState()
  val gitHubStatus by viewModel.gitHubStatus.collectAsState()
  val gitHubError by viewModel.gitHubError.collectAsState()
  val context = LocalContext.current

  var tokenInput by remember { mutableStateOf("") }
  var repoSearchQuery by remember { mutableStateOf("") }

  val filteredRepos = gitHubRepos.filter { repo ->
    repoSearchQuery.isBlank() ||
        repo.name.contains(repoSearchQuery, ignoreCase = true) ||
        (repo.description?.contains(repoSearchQuery, ignoreCase = true) == true) ||
        (repo.language?.contains(repoSearchQuery, ignoreCase = true) == true)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(StitchMainBackground)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item { Spacer(modifier = Modifier.height(4.dp)) }

    // Top Navigation / Title Area
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { viewModel.setScreen(Screen.HOME) },
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(StitchSecondaryBackground)
              .testTag("github_back_btn")
          ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "GitHub Integration", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
        }

        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSecondaryBackground),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(20.dp))
        }
      }
    }

    // Explanation Banner
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StitchSecondaryBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "GitHub Cloud Repository Bridge", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Connect your GitHub account to analyze remote codebases, inspect file trees, review commits, and push updates directly from ALTRON CODE AI.",
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            color = StitchSecondaryText
          )
        }
      }
    }

    // Error Banner
    if (gitHubError != null) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchError.copy(alpha = 0.08f),
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchError.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StitchError, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = gitHubError ?: "Authentication error", fontSize = 12.sp, color = StitchError)
            }
            Text(
              text = "Retry",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = StitchPrimaryAccent,
              modifier = Modifier
                .clickable { viewModel.refreshGitHub() }
                .padding(start = 8.dp)
            )
          }
        }
      }
    }

    // State 1: DISCONNECTED -> Show Token Input Form
    if (gitHubAccount == null) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
          shadowElevation = 2.dp
        ) {
          Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(StitchSoftAccent),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.AddLink, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(22.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(text = "Connect with GitHub", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                Text(text = "Personal Access Token authentication", fontSize = 11.5.sp, color = StitchSecondaryText)
              }
            }

            Text(
              text = "Enter a GitHub Personal Access Token (classic `ghp_...` or fine-grained `github_pat_...`). Required scopes: `repo`, `read:user`.",
              fontSize = 12.sp,
              lineHeight = 16.sp,
              color = StitchSecondaryText
            )

            OutlinedTextField(
              value = tokenInput,
              onValueChange = { tokenInput = it },
              placeholder = { Text("ghp_xxxxxxxxxxxxxxxxxxxx", fontSize = 12.sp) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("github_token_input"),
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = StitchSecondaryBackground,
                unfocusedContainerColor = StitchSecondaryBackground,
                focusedBorderColor = StitchPrimaryAccent,
                unfocusedBorderColor = StitchBorder
              ),
              singleLine = true,
              enabled = !isGitHubLoading
            )

            Button(
              onClick = { viewModel.connectGitHub(tokenInput) },
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("github_connect_btn"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = StitchPrimaryAccent,
                contentColor = Color.White
              ),
              enabled = !isGitHubLoading && tokenInput.isNotBlank()
            ) {
              if (isGitHubLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Authenticating...", fontWeight = FontWeight.Bold)
              } else {
                Icon(imageVector = Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Connect GitHub Account", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    } else {
      // State 2: CONNECTED -> Show User Profile & Repositories
      val account = gitHubAccount!!

      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
          shadowElevation = 2.dp
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(StitchPrimaryAccent),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = account.username.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(text = account.name.ifBlank { account.username }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                  Text(text = "@${account.username}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(StitchSuccess.copy(alpha = 0.15f))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "Connected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchSuccess)
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Repository Statistics
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              color = StitchSecondaryBackground
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.FolderShared, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(text = "Repositories", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                    Text(text = "${account.publicRepos} Public • ${account.totalPrivateRepos} Private", fontSize = 11.sp, color = StitchSecondaryText)
                  }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  IconButton(
                    onClick = { viewModel.refreshGitHub() },
                    modifier = Modifier.size(32.dp).testTag("github_refresh_btn")
                  ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = StitchPrimaryAccent, modifier = Modifier.size(18.dp))
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Disconnect Action
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
              Button(
                onClick = { viewModel.disconnectGitHub() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = StitchError.copy(alpha = 0.12f),
                  contentColor = StitchError
                ),
                modifier = Modifier.testTag("github_disconnect_btn")
              ) {
                Icon(imageVector = Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Disconnect GitHub", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Repositories Header & Search
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Accessible Repositories (${filteredRepos.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
            if (isGitHubLoading) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), color = StitchPrimaryAccent, strokeWidth = 2.dp)
            }
          }

          OutlinedTextField(
            value = repoSearchQuery,
            onValueChange = { repoSearchQuery = it },
            placeholder = { Text("Search repositories...", fontSize = 12.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(18.dp)) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("github_repo_search_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = StitchSecondaryBackground,
              unfocusedContainerColor = StitchSecondaryBackground,
              focusedBorderColor = StitchPrimaryAccent,
              unfocusedBorderColor = StitchBorder
            ),
            singleLine = true
          )
        }
      }

      // Repositories List
      if (filteredRepos.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = StitchCardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(32.dp))
              Text(
                text = if (gitHubRepos.isEmpty()) "No repositories found" else "No matching repositories",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StitchPrimaryText
              )
              Text(
                text = if (gitHubRepos.isEmpty()) "Ensure your token has the 'repo' scope authorized." else "Try a different search keyword.",
                fontSize = 12.sp,
                color = StitchSecondaryText
              )
            }
          }
        }
      } else {
        items(filteredRepos) { repo ->
          GitHubRepoCard(
            repo = repo,
            onOpenWorkspace = { viewModel.selectGitHubRepo(repo) },
            onCloneToLocal = { viewModel.cloneGitHubRepo(repo) }
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

@Composable
private fun GitHubRepoCard(
  repo: GitHubRepo,
  onOpenWorkspace: () -> Unit,
  onCloneToLocal: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0x0A000000)),
    shape = RoundedCornerShape(12.dp),
    color = StitchCardBackground,
    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = repo.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(StitchSecondaryBackground)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = if (repo.isPrivate) "Private" else "Public",
                fontSize = 10.sp,
                color = StitchSecondaryText
              )
            }
          }
          if (!repo.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = repo.description, fontSize = 11.5.sp, color = StitchSecondaryText, maxLines = 2)
          }
        }

        if (repo.stargazersCount > 0) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = "${repo.stargazersCount}", fontSize = 11.sp, color = StitchSecondaryText)
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (!repo.language.isNullOrBlank()) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StitchPrimaryAccent))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = repo.language, fontSize = 11.sp, color = StitchSecondaryText)
            Spacer(modifier = Modifier.width(8.dp))
          }
          Text(text = "branch: ${repo.defaultBranch}", fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Button(
            onClick = onCloneToLocal,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = StitchSecondaryBackground,
              contentColor = StitchPrimaryText
            ),
            modifier = Modifier.height(32.dp)
          ) {
            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Clone", fontSize = 11.sp)
          }

          Button(
            onClick = onOpenWorkspace,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = StitchPrimaryAccent,
              contentColor = Color.White
            ),
            modifier = Modifier.height(32.dp)
          ) {
            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
