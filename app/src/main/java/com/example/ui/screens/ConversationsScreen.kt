package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.AltronViewModel
import com.example.ui.components.AltronGlassCard

@Composable
fun ConversationsScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Text("Conversations", style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(16.dp))
    
    // Placeholder list
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      item {
        AltronGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("No conversations yet", modifier = Modifier.padding(16.dp))
        }
      }
    }
  }
}
