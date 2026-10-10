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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ApiProviderConfig
import com.example.data.model.ProviderType
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
import com.example.ui.theme.TerminalRed
import com.example.ui.viewmodel.AltronViewModel

@Composable
fun ApiProvidersScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val providers by viewModel.providers.collectAsState()
  val activeProvider by viewModel.activeProvider.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header Banner
    AltronGlassCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("api_providers_header_card"),
      hasGlowBorder = true
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "AI PROVIDERS & MODELS",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = AltronOrange
            )
            Text(
              text = "Model Management Hub",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          StatusBadge(
            text = "ACTIVE: ${activeProvider.providerType.displayName}",
            color = TerminalGreen
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Security Notice
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .border(1.dp, TerminalGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = TerminalGreen,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "ZERO-LEAK SECURITY: API keys are stored in private local SharedPreferences only. Each provider's credentials are fully isolated.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Providers List
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(providers) { config ->
        ProviderConfigCard(
          config = config,
          isDefault = config.providerType == activeProvider.providerType,
          onSave = { updated -> viewModel.saveProvider(updated) },
          onSetDefault = { viewModel.selectDefaultProvider(config.providerType) },
          onTestConnection = { updated -> viewModel.testProvider(updated) },
          onFetchModels = { viewModel.fetchDynamicModels(config) },
          onRemove = { viewModel.removeProviderConfig(config.providerType) }
        )
      }
    }
  }
}

@Composable
fun ProviderConfigCard(
  config: ApiProviderConfig,
  isDefault: Boolean,
  onSave: (ApiProviderConfig) -> Unit,
  onSetDefault: () -> Unit,
  onTestConnection: (ApiProviderConfig) -> Unit,
  onFetchModels: () -> Unit,
  onRemove: () -> Unit
) {
  var keyText by remember(config.apiKey) { mutableStateOf(config.apiKey) }
  var baseUrlText by remember(config.baseUrl) { mutableStateOf(config.baseUrl) }
  var selectedModel by remember(config.selectedModel) { mutableStateOf(config.selectedModel) }
  var isKeyVisible by remember { mutableStateOf(false) }
  var showCustomModelDialog by remember { mutableStateOf(false) }

  // Combined models: verified official models + dynamically fetched online models
  val allAvailableModels = remember(config.dynamicModels, config.providerType) {
    (config.dynamicModels + config.providerType.verifiedModels).distinct()
  }

  AltronGlassCard(
    modifier = Modifier.fillMaxWidth(),
    hasGlowBorder = isDefault
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = config.providerType.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Docs: ${config.providerType.docsUrl}",
            fontSize = 10.sp,
            color = AltronOrangeBright,
            fontFamily = FontFamily.Monospace
          )
        }

        if (isDefault) {
          StatusBadge(text = "DEFAULT", color = AltronOrange)
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Capabilities Badge
      Text(
        text = "⚡ Capabilities: ${config.capabilities}",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = FontFamily.SansSerif
      )

      Spacer(modifier = Modifier.height(12.dp))

      // API Key Field with visibility toggle
      OutlinedTextField(
        value = keyText,
        onValueChange = { keyText = it },
        label = { Text("API Key") },
        singleLine = true,
        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
          IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
            Icon(
              imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = "Toggle Visibility",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AltronOrange,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Custom Base URL field
      OutlinedTextField(
        value = baseUrlText,
        onValueChange = { baseUrlText = it },
        label = { Text("API Endpoint Base URL") },
        singleLine = true,
        trailingIcon = {
          if (baseUrlText != config.providerType.defaultBaseUrl) {
            IconButton(onClick = { baseUrlText = config.providerType.defaultBaseUrl }) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset URL",
                tint = AltronAmber,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AltronOrange,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Model Selection Header with "Fetch Online" and "Custom Model"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Active Model: $selectedModel",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = AltronOrange
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          // Fetch Online Models button
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(TerminalCyan.copy(alpha = 0.15f))
              .border(0.8.dp, TerminalCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .clickable { onFetchModels() }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = TerminalCyan,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sync Models", fontSize = 10.sp, color = TerminalCyan, fontWeight = FontWeight.Bold)
            }
          }

          // Custom Model Entry button
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(AltronAmber.copy(alpha = 0.15f))
              .border(0.8.dp, AltronAmber.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .clickable { showCustomModelDialog = true }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = AltronAmber,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Custom", fontSize = 10.sp, color = AltronAmber, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Model Selector Pills
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        allAvailableModels.forEach { modelName ->
          val isSelected = selectedModel == modelName
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) AltronOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
              .border(
                1.dp,
                if (isSelected) AltronOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                RoundedCornerShape(8.dp)
              )
              .clickable { selectedModel = modelName }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = modelName,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) AltronOrangeBright else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      // Last Tested Status Message
      if (config.lastTestedStatus != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              if (config.lastTestedSuccess == true) TerminalGreen.copy(alpha = 0.15f) else TerminalRed.copy(alpha = 0.15f),
              RoundedCornerShape(6.dp)
            )
            .border(
              1.dp,
              if (config.lastTestedSuccess == true) TerminalGreen.copy(alpha = 0.4f) else TerminalRed.copy(alpha = 0.4f),
              RoundedCornerShape(6.dp)
            )
            .padding(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = config.lastTestedStatus ?: "",
            fontSize = 11.sp,
            color = if (config.lastTestedSuccess == true) TerminalGreen else TerminalRed,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Button actions row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AltronSecondaryButton(
          text = "Test",
          icon = Icons.Default.NetworkCheck,
          onClick = {
            val updated = config.copy(
              apiKey = keyText,
              baseUrl = baseUrlText,
              selectedModel = selectedModel,
              isEnabled = keyText.isNotBlank()
            )
            onSave(updated)
            onTestConnection(updated)
          },
          modifier = Modifier.weight(1f)
        )

        AltronPrimaryButton(
          text = if (isDefault) "Active" else "Set Active",
          icon = if (isDefault) Icons.Default.Check else null,
          onClick = {
            val updated = config.copy(
              apiKey = keyText,
              baseUrl = baseUrlText,
              selectedModel = selectedModel,
              isEnabled = keyText.isNotBlank()
            )
            onSave(updated)
            onSetDefault()
          },
          modifier = Modifier.weight(1f)
        )

        // Remove / Reset button
        IconButton(
          onClick = onRemove,
          modifier = Modifier
            .size(48.dp)
            .background(TerminalRed.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(1.dp, TerminalRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Remove Configuration",
            tint = TerminalRed,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }

  // Custom Model ID Entry Modal
  if (showCustomModelDialog) {
    CustomModelModal(
      currentModel = selectedModel,
      onDismiss = { showCustomModelDialog = false },
      onConfirm = { customId ->
        selectedModel = customId
        val updated = config.copy(
          apiKey = keyText,
          baseUrl = baseUrlText,
          selectedModel = customId,
          isEnabled = keyText.isNotBlank()
        )
        onSave(updated)
        showCustomModelDialog = false
      }
    )
  }
}

@Composable
fun CustomModelModal(
  currentModel: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var text by remember { mutableStateOf(currentModel) }

  Dialog(onDismissRequest = onDismiss) {
    AltronGlassCard(modifier = Modifier.fillMaxWidth(), hasGlowBorder = true) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Enter Custom Model ID",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Enter any exact model name supported by your provider or private server (e.g. gpt-4o, claude-3-7-sonnet, deepseek-coder).",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          label = { Text("Model Identifier") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.width(8.dp))
          AltronPrimaryButton(
            text = "Use Model",
            onClick = {
              if (text.isNotBlank()) onConfirm(text.trim())
            },
            enabled = text.isNotBlank()
          )
        }
      }
    }
  }
}
