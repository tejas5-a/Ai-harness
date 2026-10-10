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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EditorTab
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.components.AltronSecondaryButton
import com.example.ui.navigation.Screen
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.AltronOrange
import com.example.ui.theme.AltronOrangeGlow
import com.example.ui.theme.AltronOrangeRim
import com.example.ui.theme.Graphite700
import com.example.ui.theme.Graphite800
import com.example.ui.theme.Graphite950
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.TerminalGreen
import com.example.ui.viewmodel.AltronViewModel

@Composable
fun EditorScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val openTabs by viewModel.openTabs.collectAsState()
  val activeTab by viewModel.activeTab.collectAsState()
  val activeContent by viewModel.activeContent.collectAsState()
  val isModified by viewModel.isContentModified.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Graphite950)
  ) {
    // Multi-tab Bar
    if (openTabs.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        openTabs.forEach { tab ->
          val isCurrent = tab.file.absolutePath == activeTab?.file?.absolutePath
          EditorTabPill(
            tab = tab,
            isSelected = isCurrent,
            onSelect = { viewModel.openFile(tab.file) },
            onClose = { viewModel.closeTab(tab) }
          )
        }
      }
    }

    // Editor Action Toolbar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // File path label
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (isModified) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(AltronOrange, CircleShape)
          )
          Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
          text = activeTab?.relativePath ?: "No file open",
          style = MaterialTheme.typography.bodyMedium,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp
        )
      }

      // Actions
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Save Button
        IconButton(
          onClick = { viewModel.saveActiveFile() },
          modifier = Modifier
            .size(36.dp)
            .background(if (isModified) AltronOrange else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .testTag("editor_save_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Save,
            contentDescription = "Save",
            tint = if (isModified) Color.Black else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
          )
        }

        // Ask AI about this file
        IconButton(
          onClick = {
            viewModel.sendAgentMessage("Explain and review file '${activeTab?.relativePath}':\n```\n$activeContent\n```")
            viewModel.setScreen(Screen.AGENT)
          },
          modifier = Modifier
            .size(36.dp)
            .background(AltronAmber.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
        ) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = "Ask AI",
            tint = AltronAmber,
            modifier = Modifier.size(18.dp)
          )
        }

        // Run in terminal
        IconButton(
          onClick = {
            if (activeTab != null) {
              val cmd = if (activeTab!!.file.name.endsWith(".py")) "python3 ${activeTab!!.file.name}"
              else if (activeTab!!.file.name.endsWith(".kt")) "kotlinc ${activeTab!!.file.name} -include-runtime -d app.jar"
              else "cat ${activeTab!!.file.name}"
              viewModel.runTerminalCommand(cmd)
            }
            viewModel.setScreen(Screen.TERMINAL)
          },
          modifier = Modifier
            .size(36.dp)
            .background(TerminalGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
        ) {
          Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = "Run in Terminal",
            tint = TerminalGreen,
            modifier = Modifier.size(18.dp)
          )
        }

        // Preview Web
        if (activeTab?.file?.name?.endsWith(".html") == true) {
          IconButton(
            onClick = { viewModel.setScreen(Screen.PREVIEW) },
            modifier = Modifier
              .size(36.dp)
              .background(AltronOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
          ) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = "Preview",
              tint = AltronOrange,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    // Code Editor Container with Line Numbers
    if (activeTab != null) {
      val lines = activeContent.lines()
      val lineCount = lines.size.coerceAtLeast(1)

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(Graphite950)
      ) {
        val verticalScrollState = rememberScrollState()

        Row(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(verticalScrollState)
            .padding(vertical = 8.dp)
        ) {
          // Line Numbers Gutter
          Column(
            modifier = Modifier
              .width(42.dp)
              .padding(end = 8.dp),
            horizontalAlignment = Alignment.End
          ) {
            for (i in 1..lineCount) {
              Text(
                text = "$i",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
              )
            }
          }

          // Vertical divider line
          Box(
            modifier = Modifier
              .width(1.dp)
              .fillMaxHeight()
              .background(Graphite800)
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Editable Code Body
          val horizontalScrollState = rememberScrollState()
          Box(
            modifier = Modifier
              .weight(1f)
              .horizontalScroll(horizontalScrollState)
          ) {
            BasicTextField(
              value = activeContent,
              onValueChange = { viewModel.updateActiveContent(it) },
              textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
              ),
              cursorBrush = SolidColor(AltronOrange),
              modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp)
                .testTag("code_editor_text_field")
            )
          }
        }
      }

      // Mobile Quick-Symbol Keyboard Helper Bar
      MobileSymbolBar(onInsertSymbol = { symbol ->
        viewModel.updateActiveContent(activeContent + symbol)
      })
    } else {
      // Empty Editor State
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "No File Open",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Select a file from Projects to start coding.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(16.dp))
          AltronPrimaryButton(
            text = "Open Projects",
            onClick = { viewModel.setScreen(Screen.PROJECTS) }
          )
        }
      }
    }
  }
}

@Composable
fun EditorTabPill(
  tab: EditorTab,
  isSelected: Boolean,
  onSelect: () -> Unit,
  onClose: () -> Unit
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) AltronOrangeGlow else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
      .border(
        1.dp,
        if (isSelected) AltronOrangeRim else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        RoundedCornerShape(8.dp)
      )
      .clickable(onClick = onSelect)
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = tab.file.name,
      fontSize = 11.5.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) AltronOrange else MaterialTheme.colorScheme.onSurface,
      fontFamily = FontFamily.Monospace
    )
    Spacer(modifier = Modifier.width(6.dp))
    Icon(
      imageVector = Icons.Default.Close,
      contentDescription = "Close Tab",
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .size(14.dp)
        .clickable(onClick = onClose)
    )
  }
}

@Composable
fun MobileSymbolBar(onInsertSymbol: (String) -> Unit) {
  val symbols = listOf("{", "}", "(", ")", "[", "]", ";", ":", "=", "<", ">", "\"", "'", "/", "$", "!", "&", "|", "+", "-")

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Graphite800)
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 8.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    symbols.forEach { sym ->
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(MaterialTheme.colorScheme.surface)
          .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
          .clickable { onInsertSymbol(sym) },
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = sym,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = AltronOrange
        )
      }
    }
  }
}
