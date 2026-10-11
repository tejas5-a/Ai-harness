package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AltronTopBar
import com.example.ui.components.ApprovalDialog
import com.example.ui.navigation.AltronBottomNavBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AiAgentScreen
import com.example.ui.screens.ApiProvidersScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.theme.AltronTheme
import com.example.ui.viewmodel.AltronViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: AltronViewModel = viewModel()
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()
      val currentScreen by viewModel.currentScreen.collectAsState()
      val activeProject by viewModel.activeProject.collectAsState()
      val pendingApproval by viewModel.pendingApprovalAction.collectAsState()
      val statusMessage by viewModel.statusMessage.collectAsState()

      val snackbarHostState = remember { SnackbarHostState() }

      LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
          snackbarHostState.showSnackbar(msg)
          viewModel.dismissStatusMessage()
        }
      }

      // BackHandler for sub-screens to return to Chat screen
      if (currentScreen != Screen.CHAT) {
        BackHandler {
          viewModel.setScreen(Screen.CHAT)
        }
      }

      AltronTheme(darkTheme = isDarkTheme) {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          snackbarHost = { SnackbarHost(snackbarHostState) },
          topBar = {
            AltronTopBar(
              activeProject = activeProject,
              onMenuClicked = { viewModel.setScreen(Screen.SETTINGS) }
            )
          },
          bottomBar = {
            AltronBottomNavBar(
              currentScreen = currentScreen,
              onScreenSelected = { screen -> viewModel.setScreen(screen) }
            )
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.background)
              .padding(innerPadding)
          ) {
            AnimatedContent(
              targetState = currentScreen,
              transitionSpec = { fadeIn() togetherWith fadeOut() },
              label = "ScreenTransition"
            ) { target ->
              when (target) {
                Screen.CHAT -> AiAgentScreen(viewModel = viewModel)
                Screen.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                Screen.SETTINGS -> ApiProvidersScreen(viewModel = viewModel)
              }
            }

            // Destructive / Risky Action Approval Dialog
            if (pendingApproval != null) {
              val (actionDesc, _) = pendingApproval!!
              ApprovalDialog(
                title = "Security & Approval Required",
                message = "The following action was requested:",
                commandOrFile = actionDesc,
                onConfirm = { viewModel.confirmApproval() },
                onDismiss = { viewModel.dismissApproval() }
              )
            }
          }
        }
      }
    }
  }
}
