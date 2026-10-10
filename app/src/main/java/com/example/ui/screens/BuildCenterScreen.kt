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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.components.AltronSecondaryButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.AltronOrange
import com.example.ui.theme.AltronOrangeBright
import com.example.ui.theme.Graphite700
import com.example.ui.theme.Graphite800
import com.example.ui.theme.Graphite950
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.viewmodel.AltronViewModel

@Composable
fun BuildCenterScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val activeProject by viewModel.activeProject.collectAsState()
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Banner
    AltronGlassCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("build_center_header_card"),
      hasGlowBorder = true
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "BUILD CENTER",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = AltronOrange
            )
            Text(
              text = "Mobile APK Build Hub",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          StatusBadge(text = "GITHUB ACTIONS READY", color = TerminalGreen)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Build production Android APKs from your phone without owning a PC, using automated GitHub Actions CI/CD workflows.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        AltronPrimaryButton(
          text = "Save Workflow to Active Project",
          icon = Icons.Default.Save,
          onClick = {
            viewModel.exportGithubWorkflow()
          },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    // Step-by-Step Workflow Guide
    Text(
      text = "HOW TO BUILD APK DIRECTLY FROM PHONE",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = AltronOrange
    )

    BuildStepCard(
      stepNumber = "1",
      title = "Create GitHub Repository",
      description = "Open github.com on your phone (or GitHub mobile app) and create a repository for your project."
    )

    BuildStepCard(
      stepNumber = "2",
      title = "Push or Upload Code with Workflow",
      description = "Ensure .github/workflows/android-build.yml is present in the repository root. Tap 'Save Workflow' above to generate it."
    )

    BuildStepCard(
      stepNumber = "3",
      title = "GitHub Actions Builds APK in Cloud",
      description = "GitHub's Ubuntu runner automatically installs JDK 17, compiles with Gradle, tests the code, and creates the APK."
    )

    BuildStepCard(
      stepNumber = "4",
      title = "Download & Install on Phone",
      description = "Go to the Actions tab on GitHub, click the completed workflow run, download 'AltronCode-Debug-APK', and install it!"
    )

    // Platform Limitations & Honesty Section
    AltronGlassCard(
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = AltronAmber,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Platform Architecture Transparency",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "• On-Device Shell: Executes POSIX/Toybox Linux commands directly in the app sandbox.\n• Remote Build Runner: Native Android OS does not include the 3GB Android SDK Build Tools daemon. By delegating APK compilation to GitHub Actions, your phone doesn't overheat or run out of memory, yet you receive fully signed APKs directly on your device.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 20.sp
        )
      }
    }

    // Environment Diagnostics
    AltronGlassCard(
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "DIAGNOSTIC METRICS",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = TerminalCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        val stat = StatFs(Environment.getDataDirectory().path)
        val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
        val freeGb = String.format("%.2f GB", freeBytes / (1024.0 * 1024.0 * 1024.0))

        val runtime = Runtime.getRuntime()
        val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMemMb = runtime.maxMemory() / (1024 * 1024)

        DiagnosticRow(label = "Android Version", value = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        DiagnosticRow(label = "Internal Storage Available", value = freeGb)
        DiagnosticRow(label = "JVM Heap Memory", value = "$usedMemMb MB / $maxMemMb MB")
        DiagnosticRow(label = "Architecture ABI", value = Build.SUPPORTED_ABIS.joinToString(", "))
        DiagnosticRow(label = "Active Project Directory", value = activeProject?.rootDir?.name ?: "None")
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
fun BuildStepCard(
  stepNumber: String,
  title: String,
  description: String
) {
  AltronGlassCard(
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(AltronOrange.copy(alpha = 0.2f))
          .border(1.dp, AltronOrange, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = stepNumber,
          fontWeight = FontWeight.Black,
          color = AltronOrangeBright,
          fontSize = 13.sp
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
      }
    }
  }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      fontFamily = FontFamily.Monospace
    )
  }
}
