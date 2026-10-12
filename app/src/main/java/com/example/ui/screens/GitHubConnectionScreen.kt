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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
 * SCREEN 8: GITHUB CONNECTION
 * Exact implementation of Google Stitch GitHub Integration design:
 * - Top Navigation header
 * - Info banner ("Sync Repositories with Claude 3.5")
 * - Connected Account Card (@altron-dev, status, configure, disconnect)
 * - Link an Organization card (Connect GitHub Account)
 * - Recent Webhooks / Live Sync stream
 */
@Composable
fun GitHubConnectionScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val gitHubAccount by viewModel.gitHubAccount.collectAsState()
  val context = LocalContext.current

  var isConnected by remember { mutableStateOf(true) }

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
            Text(text = "Sync Repositories with Claude 3.5", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Connect your GitHub account to let ALTRON CODE analyze your codebases, create automated pull requests, and manage version control effortlessly.",
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            color = StitchSecondaryText
          )
        }
      }
    }

    // Connected Account Card
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
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(text = "Altron Developer", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                Text(text = "@altron-dev", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
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

          // Repository Access Status
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
                  Text(text = "Repository Access", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                  Text(text = "All repositories accessible", fontSize = 11.sp, color = StitchSecondaryText)
                }
              }

              Text(
                text = "Configure",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StitchPrimaryAccent,
                modifier = Modifier.clickable {
                  android.widget.Toast.makeText(context, "GitHub permissions config opened", android.widget.Toast.LENGTH_SHORT).show()
                }
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Disconnect Action
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
              onClick = {
                isConnected = !isConnected
                android.widget.Toast.makeText(context, "GitHub account disconnected", android.widget.Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = StitchError.copy(alpha = 0.12f),
                contentColor = StitchError
              )
            ) {
              Icon(imageVector = Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = "Disconnect GitHub", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Link a New Organization
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = StitchSecondaryBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(StitchSoftAccent),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(26.dp))
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(text = "Link a New Organization", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Authorize access to private enterprise repositories or alternate personal accounts.",
            fontSize = 12.sp,
            color = StitchSecondaryText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              android.widget.Toast.makeText(context, "Opening GitHub OAuth Authorize dialog...", android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryAccent, contentColor = Color.White)
          ) {
            Icon(imageVector = Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Connect GitHub Account", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Sync Activity Stream / Recent Webhooks
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Recent Webhooks", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          Text(text = "Live Sync", fontSize = 11.5.sp, color = StitchSecondaryText)
        }

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            WebhookRow("altron-core/main.rs", "2m ago", isSyncing = false)
            WebhookRow("altron-ui/tailwind.config.js", "14m ago", isSyncing = false)
            WebhookRow("ai-refactor/branch-v2", "Syncing...", isSyncing = true)
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

@Composable
private fun WebhookRow(target: String, time: String, isSyncing: Boolean) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CheckCircle,
        contentDescription = null,
        tint = if (isSyncing) StitchPrimaryAccent else StitchSuccess,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = target, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText)
    }
    Text(
      text = time,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      color = if (isSyncing) StitchPrimaryAccent else StitchSecondaryText,
      fontWeight = if (isSyncing) FontWeight.Bold else FontWeight.Normal
    )
  }
}
