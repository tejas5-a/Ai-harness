package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.viewmodel.AltronViewModel

@Composable
fun ApiProvidersScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val providers by viewModel.providers.collectAsState()
  val activeProvider by viewModel.activeProvider.collectAsState()
  var editingProvider by remember { mutableStateOf<ApiProviderConfig?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
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

      IconButton(onClick = { viewModel.loadProviders() }) {
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
          text = "Zero-cloud leak guarantee: API keys are securely stored on-device and never transmitted to any proxy.",
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
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(providers) { config ->
        val isActive = config.providerType == activeProvider.providerType
        ProviderCard(
          config = config,
          isActive = isActive,
          onSelect = { viewModel.selectDefaultProvider(config.providerType) },
          onEdit = { editingProvider = config }
        )
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
      }
    )
  }
}

@Composable
fun ProviderCard(
  config: ApiProviderConfig,
  isActive: Boolean,
  onSelect: () -> Unit,
  onEdit: () -> Unit
) {
  AltronGlassCard(
    modifier = Modifier.fillMaxWidth(),
    hasGlowBorder = isActive,
    borderColor = if (isActive) BrandPrimaryOrange else LightBorder,
    backgroundColor = if (isActive) PureWhiteBackground else LightSurface
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
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

        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = BrandTextBlack,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

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
            modifier = Modifier.height(36.dp)
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
  onSave: (ApiProviderConfig) -> Unit
) {
  var apiKey by remember { mutableStateOf(config.apiKey) }
  var selectedModel by remember { mutableStateOf(config.selectedModel) }
  var baseUrl by remember { mutableStateOf(config.baseUrl) }
  var isKeyVisible by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    AltronGlassCard(modifier = Modifier.fillMaxWidth(), hasGlowBorder = true) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Configure ${config.providerType.displayName}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = BrandTextBlack
        )

        Spacer(modifier = Modifier.height(14.dp))

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

        OutlinedTextField(
          value = selectedModel,
          onValueChange = { selectedModel = it },
          label = { Text("Model Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        if (config.providerType == ProviderType.CUSTOM_COMPATIBLE) {
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Custom Base URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
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
