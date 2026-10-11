package com.example.ui.screens

import android.os.Build
import android.os.Environment
import android.os.StatFs
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.components.AltronSecondaryButton
import com.example.ui.components.PulsingAgentStatus
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.AltronOrange
import com.example.ui.theme.AltronOrangeBright
import com.example.ui.theme.AltronOrangeGlow
import com.example.ui.theme.AltronOrangeRim
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.Graphite400
import com.example.ui.theme.Graphite950
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.viewmodel.AltronViewModel

@Composable
fun DashboardScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val activeProject by viewModel.activeProject.collectAsState()
  val projects by viewModel.projects.collectAsState()
  val activeProvider by viewModel.activeProvider.collectAsState()
  val isAgentThinking by viewModel.isAgentThinking.collectAsState()

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Hero Banner Card with refined aesthetic
    AltronGlassCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("dashboard_hero_card"),
      hasGlowBorder = true
    ) {
      Column(
        modifier = Modifier.padding(24.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          PulsingAgentStatus(isThinking = isAgentThinking)
          StatusBadge(text = activeProvider.providerType.displayName, color = PrimaryOrange)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "ALTRON CODE AI",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = "Your Phone. Your AI. Your Coding Studio.",
          style = MaterialTheme.typography.bodyMedium.copy(
            color = AccentOrange,
            fontWeight = FontWeight.SemiBold
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Mobile-first on-device development workspace. Write code, leverage autonomous AI agents, execute terminal commands, and build APKs without a PC.",
          style = MaterialTheme.typography.bodyMedium,
          color = SecondaryText
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Action Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          AltronPrimaryButton(
            text = "Ask AI Agent",
            onClick = { viewModel.setScreen(Screen.AGENT) },
            modifier = Modifier
              .weight(1f)
              .testTag("dashboard_launch_agent_btn")
          )

          AltronSecondaryButton(
            text = "Terminal",
            onClick = { viewModel.setScreen(Screen.TERMINAL) },
            modifier = Modifier
              .weight(1f)
              .testTag("dashboard_open_terminal_btn")
          )
        }
      }
    }

    // Active Project Card
    AltronGlassCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("dashboard_active_project_card")
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "ACTIVE WORKSPACE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AltronOrange
          )
          if (activeProject != null) {
            StatusBadge(text = activeProject!!.type.badge, color = TerminalCyan)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeProject != null) {
          val proj = activeProject!!
          Text(
            text = proj.name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = proj.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            AltronSecondaryButton(
              text = "Code Editor",
              icon = Icons.Default.Code,
              onClick = { viewModel.setScreen(Screen.EDITOR) },
              modifier = Modifier.weight(1f)
            )
            AltronSecondaryButton(
              text = "Files",
              icon = Icons.Default.Folder,
              onClick = { viewModel.setScreen(Screen.PROJECTS) },
              modifier = Modifier.weight(1f)
            )
          }
        } else {
          Text(
            text = "No project selected. Create or select a workspace.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))
          AltronPrimaryButton(
            text = "Create First Project",
            icon = Icons.Default.Add,
            onClick = { viewModel.setScreen(Screen.PROJECTS) }
          )
        }
      }
    }

    // Studio Navigation Grid
    Text(
      text = "STUDIO MODULES",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ModuleShortcutCard(
        title = "Code Editor",
        subtitle = "Multi-tab syntax editor",
        icon = Icons.Default.Code,
        color = AltronOrange,
        onClick = { viewModel.setScreen(Screen.EDITOR) },
        modifier = Modifier.weight(1f)
      )

      ModuleShortcutCard(
        title = "Build Center",
        subtitle = "GitHub Actions & APK",
        icon = Icons.Default.Build,
        color = TerminalGreen,
        onClick = { viewModel.setScreen(Screen.GITHUB_BUILD) },
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ModuleShortcutCard(
        title = "API Providers",
        subtitle = "${activeProvider.selectedModel}",
        icon = Icons.Default.Settings,
        color = AltronAmber,
        onClick = { viewModel.setScreen(Screen.PROVIDERS) },
        modifier = Modifier.weight(1f)
      )

      ModuleShortcutCard(
        title = "Web Preview",
        subtitle = "Instant sandbox preview",
        icon = Icons.Default.Language,
        color = TerminalCyan,
        onClick = { viewModel.setScreen(Screen.PREVIEW) },
        modifier = Modifier.weight(1f)
      )
    }

    // System Environment Diagnostics Card
    AltronGlassCard(
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Memory,
            contentDescription = null,
            tint = AltronOrange,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "DEVICE & ENVIRONMENT METRICS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          MetricChip(label = "Android", value = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
          MetricChip(label = "Device", value = Build.MODEL.take(12))
          MetricChip(label = "Arch", value = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64")
        }
      }
    }

    // Recent Projects Quick Switch
    if (projects.isNotEmpty()) {
      Text(
        text = "RECENT PROJECTS",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      projects.take(3).forEach { project ->
        AltronGlassCard(
          modifier = Modifier.fillMaxWidth(),
          onClick = { viewModel.selectProject(project) }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(AltronOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Folder,
                  contentDescription = null,
                  tint = AltronOrange,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = project.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = project.type.displayName,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            if (project.id == activeProject?.id) {
              StatusBadge(text = "ACTIVE", color = TerminalGreen)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
fun ModuleShortcutCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  AltronGlassCard(
    modifier = modifier,
    onClick = onClick
  ) {
    Column(
      modifier = Modifier.padding(14.dp)
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = color,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 14.sp
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        maxLines = 1
      )
    }
  }
}

@Composable
fun MetricChip(label: String, value: String) {
  Column(
    modifier = Modifier
      .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = AltronOrange,
      fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )
  }
}
