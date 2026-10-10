package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.AltronGlassCard
import com.example.ui.components.AltronPrimaryButton
import com.example.ui.navigation.Screen
import com.example.ui.theme.AltronOrange
import com.example.ui.theme.Graphite700
import com.example.ui.theme.Graphite800
import com.example.ui.theme.Graphite950
import com.example.ui.viewmodel.AltronViewModel
import java.io.File

@Composable
fun WebPreviewScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val webFile by viewModel.webPreviewFile.collectAsState()
  val activeProject by viewModel.activeProject.collectAsState()

  val fileUrl = if (webFile != null && webFile!!.exists()) {
    "file://${webFile!!.absolutePath}"
  } else {
    val fallbackHtml = activeProject?.rootDir?.let { File(it, "index.html") }
    if (fallbackHtml != null && fallbackHtml.exists()) "file://${fallbackHtml.absolutePath}" else null
  }

  var currentUrl by remember(fileUrl) { mutableStateOf(fileUrl ?: "about:blank") }
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(12.dp)
  ) {
    // Address Bar & Controls
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Language,
        contentDescription = null,
        tint = AltronOrange,
        modifier = Modifier.size(20.dp)
      )

      Spacer(modifier = Modifier.width(8.dp))

      OutlinedTextField(
        value = currentUrl,
        onValueChange = { currentUrl = it },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.weight(1f),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = Color.Transparent,
          unfocusedBorderColor = Color.Transparent
        )
      )

      IconButton(
        onClick = {
          webViewInstance?.loadUrl(currentUrl)
        },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Reload",
          tint = AltronOrange,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Preview Container
    if (fileUrl != null || currentUrl != "about:blank") {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(14.dp))
          .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
          .background(Color.White)
      ) {
        AndroidView(
          modifier = Modifier.fillMaxSize(),
          factory = { context ->
            WebView(context).apply {
              settings.javaScriptEnabled = true
              settings.domStorageEnabled = true
              settings.allowFileAccess = true
              settings.allowContentAccess = true
              webViewClient = object : WebViewClient() {}
              loadUrl(currentUrl)
              webViewInstance = this
            }
          },
          update = { webView ->
            if (webView.url != currentUrl) {
              webView.loadUrl(currentUrl)
            }
          }
        )
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        AltronGlassCard(modifier = Modifier.padding(24.dp)) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "No index.html Found",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Create an index.html in your project to preview your web application live.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            AltronPrimaryButton(
              text = "Open Projects",
              onClick = { viewModel.setScreen(Screen.PROJECTS) }
            )
          }
        }
      }
    }
  }
}
