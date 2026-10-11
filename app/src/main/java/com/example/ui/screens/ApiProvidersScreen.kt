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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AltronAmber
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.PureWhiteBackground
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
  var editingProvider by remember { mutableStateOf<ApiProviderConfig?>(null) }
  var providerToDelete by remember { mutableStateOf<ApiProviderConfig?>(null) }

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 840.dp)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(BrandPrimaryOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.VpnKey,
              contentDescription = null,
              tint = BrandPrimaryOrange,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "API Providers & Models",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = BrandTextBlack
              )
            )
            Text(
              text = "Manage BYOK credentials & model selection",
              fontSize = 12.sp,
              color = BrandTextSecondary
            )
          }
        }

        IconButton(
          onClick = { viewModel.loadProviders() },
          modifier = Modifier.testTag("reload_providers_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reload",
            tint = BrandTextBlack
          )
        }
      }

      // Security callout card
      AltronGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = TerminalGreen,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Zero-cloud leak guarantee: API keys are securely stored on-device and never transmitted to any third-party proxy.",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = BrandTextBlack
          )
        }
      }

      // Provider List
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(providers) { config ->
          val isActive = config.providerType == activeProvider.providerType
          ProviderCard(
            config = config,
            isActive = isActive,
            onSelect = { viewModel.selectDefaultProvider(config.providerType) },
            onEdit = { editingProvider = config },
            onTest = { viewModel.testProvider(config) },
            onFetchModels = { viewModel.fetchDynamicModels(config) },
            onClear = { providerToDelete = config }
          )
        }
      }
    }
  }

  // Edit / Add Modal
  if (editingProvider != null) {
    EditProviderDialog(
      config = editingProvider!!,
      onDismiss = { editingProvider = null },
      onSave = { updated ->
        viewModel.saveProvider(updated)
        editingProvider = null
      },
      onTest = { testConfig ->
        viewModel.testProvider(testConfig)
      }
    )
  }

  // Confirm Clear Key Dialog
  if (providerToDelete != null) {
    val target = providerToDelete!!
    Dialog(onDismissRequest = { providerToDelete = null }) {
      AltronGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "Clear API Key for ${target.providerType.displayName}?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrandTextBlack
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "This will remove the saved key and reset configuration for this provider on this device.",
            fontSize = 13.sp,
            color = BrandTextSecondary
          )
          Spacer(modifier = Modifier.height(18.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { providerToDelete = null }) {
              Text("Cancel", color = BrandTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                viewModel.removeProviderConfig(target.providerType)
                providerToDelete = null
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = TerminalRed,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Clear Key", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun ProviderCard(
  config: ApiProviderConfig,
  isActive: Boolean,
  onSelect: () -> Unit,
  onEdit: () -> Unit,
  onTest: () -> Unit,
  onFetchModels: () -> Unit,
  onClear: () -> Unit
) {
  AltronGlassCard(
    modifier = Modifier.fillMaxWidth(),
    hasGlowBorder = isActive,
    borderColor = if (isActive) BrandPrimaryOrange else LightBorder,
    backgroundColor = if (isActive) PureWhiteBackground else LightSurface
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header: Name, Active Badge, Edit & Delete
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = config.providerType.displayName,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = BrandTextBlack
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          if (isActive) {
            StatusBadge(text = "ACTIVE", color = BrandPrimaryOrange)
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          if (config.apiKey.isNotBlank()) {
            IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Clear Key",
                tint = BrandTextSecondary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
          IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Configuration",
              tint = BrandTextBlack,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Text(
        text = config.providerType.defaultCapabilities,
        fontSize = 11.sp,
        color = BrandTextSecondary
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Model & Key Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Model: ${config.selectedModel}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BrandTextBlack
          )
          Text(
            text = if (config.apiKey.isNotBlank()) "Key: ••••••••••••" else "No API Key Set",
            fontSize = 11.sp,
            color = if (config.apiKey.isNotBlank()) TerminalGreen else AltronAmber
          )
        }

        if (!isActive) {
          AltronPrimaryButton(
            text = "Select",
            onClick = onSelect,
            modifier = Modifier.height(34.dp)
          )
        }
      }

      // Connection test result badge if available
      if (config.lastTestedStatus != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (config.lastTestedSuccess == true) TerminalGreen.copy(alpha = 0.1f) else TerminalRed.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (config.lastTestedSuccess == true) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (config.lastTestedSuccess == true) TerminalGreen else TerminalRed,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = config.lastTestedStatus,
            fontSize = 11.sp,
            color = if (config.lastTestedSuccess == true) TerminalGreen else TerminalRed,
            maxLines = 2
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action buttons: Test Connection & Fetch Models
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onTest,
          modifier = Modifier.weight(1f).height(36.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.NetworkCheck,
            contentDescription = null,
            tint = BrandTextBlack,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Test Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandTextBlack)
        }

        OutlinedButton(
          onClick = onFetchModels,
          modifier = Modifier.weight(1f).height(36.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            tint = BrandTextBlack,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (config.dynamicModels.isNotEmpty()) "Models (${config.dynamicModels.size})" else "Fetch Models",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BrandTextBlack
          )
        }
      }
    }
  }
}

@Composable
fun EditProviderDialog(
  config: ApiProviderConfig,
  onDismiss: () -> Unit,
  onSave: (ApiProviderConfig) -> Unit,
  onTest: (ApiProviderConfig) -> Unit
) {
  var apiKey by remember { mutableStateOf(config.apiKey) }
  var selectedModel by remember { mutableStateOf(config.selectedModel) }
  var baseUrl by remember { mutableStateOf(config.baseUrl) }
  var isKeyVisible by remember { mutableStateOf(false) }
  var showModelMenu by remember { mutableStateOf(false) }

  val allAvailableModels = remember(config) {
    (config.providerType.verifiedModels + config.dynamicModels).distinct()
  }

  Dialog(onDismissRequest = onDismiss) {
    AltronGlassCard(modifier = Modifier.fillMaxWidth(), hasGlowBorder = true) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Configure ${config.providerType.displayName}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrandTextBlack
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // API Key input
        OutlinedTextField(
          value = apiKey,
          onValueChange = { apiKey = it },
          label = { Text("API Key") },
          singleLine = true,
          visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
              Icon(
                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null,
                tint = BrandTextBlack
              )
            }
          },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Model Selector with dropdown of available models
        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = selectedModel,
            onValueChange = { selectedModel = it },
            label = { Text("Model ID (e.g. ${config.providerType.defaultModel})") },
            singleLine = true,
            trailingIcon = {
              IconButton(onClick = { showModelMenu = true }) {
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = "Select from list",
                  tint = BrandTextBlack
                )
              }
            },
            modifier = Modifier.fillMaxWidth()
          )

          DropdownMenu(
            expanded = showModelMenu,
            onDismissRequest = { showModelMenu = false },
            modifier = Modifier.background(PureWhiteBackground)
          ) {
            allAvailableModels.forEach { m ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = m,
                    fontWeight = if (m == selectedModel) FontWeight.Bold else FontWeight.Normal,
                    color = if (m == selectedModel) BrandPrimaryOrange else BrandTextBlack
                  )
                },
                onClick = {
                  selectedModel = m
                  showModelMenu = false
                }
              )
            }
          }
        }

        if (config.providerType == ProviderType.CUSTOM_COMPATIBLE || config.providerType == ProviderType.OPENROUTER) {
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Base URL endpoint") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = {
              onTest(
                config.copy(
                  apiKey = apiKey,
                  selectedModel = selectedModel,
                  baseUrl = baseUrl
                )
              )
            },
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Test", fontSize = 12.sp, color = BrandTextBlack, fontWeight = FontWeight.Bold)
          }

          Row {
            TextButton(onClick = onDismiss) {
              Text("Cancel", color = BrandTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            AltronPrimaryButton(
              text = "Save",
              onClick = {
                onSave(
                  config.copy(
                    apiKey = apiKey,
                    selectedModel = selectedModel,
                    baseUrl = baseUrl
                  )
                )
              }
            )
          }
        }
      }
    }
  }
}
