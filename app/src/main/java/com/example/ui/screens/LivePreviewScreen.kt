package com.example.ui.screens

import android.os.Build
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.navigation.Screen
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.PureWhiteBackground
import com.example.ui.theme.TerminalRed
import com.example.ui.viewmodel.AltronViewModel
import java.io.File

@Composable
fun LivePreviewScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val previewFile by viewModel.webPreviewFile.collectAsState()
  var hasRendererCrashed by remember { mutableStateOf(false) }
  var reloadTrigger by remember { mutableStateOf(0) }
  var activeWebViewInstance by remember { mutableStateOf<WebView?>(null) }

  // Clean up WebView when leaving the screen
  DisposableEffect(Unit) {
    onDispose {
      activeWebViewInstance?.apply {
        stopLoading()
        clearHistory()
        loadUrl("about:blank")
        destroy()
      }
      activeWebViewInstance = null
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { viewModel.setScreen(Screen.CHAT) }) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Chat",
            tint = BrandTextBlack
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
          Text(
            text = "Live Web Preview",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = BrandTextBlack
            )
          )
          Text(
            text = previewFile?.name ?: "No HTML file selected",
            fontSize = 12.sp,
            color = BrandTextSecondary
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
          hasRendererCrashed = false
          reloadTrigger++
        }) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reload Preview",
            tint = BrandTextBlack
          )
        }
        Icon(
          imageVector = Icons.Default.OpenInBrowser,
          contentDescription = null,
          tint = BrandPrimaryOrange,
          modifier = Modifier.size(24.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // WebView Preview container
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .clip(RoundedCornerShape(12.dp))
        .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
        .background(PureWhiteBackground)
    ) {
      if (hasRendererCrashed) {
        // Crash Recovery Screen
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
          ) {
            Text(
              text = "Preview renderer stopped",
              fontWeight = FontWeight.Bold,
              color = BrandTextBlack,
              fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "The page had complex resources or memory limits. Tap reload to restart cleanly.",
              fontSize = 12.sp,
              color = BrandTextSecondary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = {
                hasRendererCrashed = false
                reloadTrigger++
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = BrandPrimaryOrange,
                contentColor = BrandTextBlack
              ),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Reload Preview", fontWeight = FontWeight.Bold, color = BrandTextBlack)
            }
          }
        }
      } else if (previewFile != null && previewFile!!.exists()) {
        val fileToLoad = previewFile!!
        val content = remember(fileToLoad.lastModified(), reloadTrigger) {
          try {
            fileToLoad.readText()
          } catch (e: Exception) {
            "<html><body><h3>Error reading file: ${e.localizedMessage}</h3></body></html>"
          }
        }

        AndroidView(
          factory = { ctx ->
            WebView(ctx).apply {
              // Crash-resilient software layer configuration
              setLayerType(View.LAYER_TYPE_SOFTWARE, null)

              settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                cacheMode = WebSettings.LOAD_NO_CACHE
                useWideViewPort = true
                loadWithOverviewMode = true
              }

              webViewClient = object : WebViewClient() {
                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                  hasRendererCrashed = true
                  view?.apply {
                    stopLoading()
                    loadUrl("about:blank")
                  }
                  return true // Handled gracefully to prevent app crash
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                  super.onReceivedError(view, request, error)
                }
              }

              loadDataWithBaseURL("https://local.altron/", content, "text/html", "UTF-8", null)
              activeWebViewInstance = this
            }
          },
          update = { webView ->
            activeWebViewInstance = webView
            if (!hasRendererCrashed) {
              webView.loadDataWithBaseURL("https://local.altron/", content, "text/html", "UTF-8", null)
            }
          },
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "No previewable HTML file found in this project.",
              fontWeight = FontWeight.Medium,
              color = BrandTextBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Generate or save an index.html file to view live preview.",
              fontSize = 12.sp,
              color = BrandTextSecondary
            )
          }
        }
      }
    }
  }
}
