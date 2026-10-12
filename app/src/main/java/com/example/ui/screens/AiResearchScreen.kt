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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.CodeBlockBackground
import com.example.ui.theme.CodeBlockHeader
import com.example.ui.theme.CodeBlockText
import com.example.ui.theme.StitchBorder
import com.example.ui.theme.StitchCardBackground
import com.example.ui.theme.StitchMainBackground
import com.example.ui.theme.StitchPrimaryAccent
import com.example.ui.theme.StitchPrimaryText
import com.example.ui.theme.StitchSecondaryBackground
import com.example.ui.theme.StitchSecondaryText
import com.example.ui.theme.StitchSoftAccent
import com.example.ui.theme.StitchSuccess
import com.example.ui.theme.StitchWarning
import com.example.ui.viewmodel.AltronViewModel

/**
 * SCREEN 2: AI RESEARCH WORKSPACE
 * Recreates the Google Stitch Research Workspace design:
 * - Session Sub-header with back button and model indicator
 * - Active Research Query Studio card
 * - Relevant Explorations topic chips
 * - Pipeline Execution Status bar
 * - Verified Sources horizontal card scroll
 * - Synthesized Deep Research Report with code snippet
 * - Follow-up questions & conversational input
 */
@Composable
fun AiResearchScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val activeProvider by viewModel.activeProvider.collectAsState()
  val isSearching by viewModel.isSearchingWeb.collectAsState()
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  var queryText by remember {
    mutableStateOf("Jetpack Compose multiplatform state restoration patterns in Kotlin 2.0")
  }
  var followUpText by remember { mutableStateOf("") }
  var activeMode by remember { mutableStateOf("Deep Web & Docs") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(StitchMainBackground)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Sub-header
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f, fill = false)
        ) {
          IconButton(
            onClick = { viewModel.setScreen(Screen.HOME) },
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(StitchSecondaryBackground)
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Back",
              tint = StitchPrimaryText,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Research Workspace",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = StitchPrimaryText
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchPrimaryAccent))
            }
            Text(
              text = "AltronCore / Deep Technical Synthesis",
              fontSize = 11.sp,
              color = StitchSecondaryText,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(StitchSecondaryBackground)
              .clickable { viewModel.setScreen(Screen.SETTINGS) }
              .padding(horizontal = 8.dp, vertical = 5.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Claude 3.5", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText)
              Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(14.dp))
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          IconButton(
            onClick = { queryText = "" },
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(StitchSoftAccent)
          ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "New Session", tint = StitchPrimaryAccent, modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    // Active Synthesis Studio Card
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = StitchCardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
        shadowElevation = 2.dp
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.TravelExplore, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = "ACTIVE SYNTHESIS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryAccent, letterSpacing = 0.5.sp)
            }
            Text(text = "Session #RS-204", fontSize = 11.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
          }

          Spacer(modifier = Modifier.height(10.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(StitchSecondaryBackground)
              .padding(10.dp)
          ) {
            Text(text = queryText, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = StitchPrimaryText, lineHeight = 20.sp)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Scope mode pills
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("Deep Web & Docs", "GitHub Repos", "ArXiv / Papers").forEach { mode ->
              val isSel = activeMode == mode
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSel) StitchPrimaryAccent else StitchSecondaryBackground)
                  .clickable { activeMode = mode }
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text(
                  text = mode,
                  fontSize = 11.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSel) Color.White else StitchSecondaryText
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Comprehensive (8 sources)", fontSize = 11.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(StitchPrimaryAccent)
                .clickable {
                  android.widget.Toast.makeText(context, "Regenerating deep synthesis...", android.widget.Toast.LENGTH_SHORT).show()
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Regenerate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }
      }
    }

    // Relevant Exploration Chips
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "RELEVANT EXPLORATIONS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = StitchSecondaryText, letterSpacing = 0.5.sp)
          Text(text = "Tap to pivot", fontSize = 10.5.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            "Compose Lifecycle 2.8",
            "KMP Memory Model",
            "Coroutines Flow Best Practices",
            "Gradle 8.7 KTS Migrations"
          ).forEach { topic ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(StitchCardBackground)
                .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
                .clickable { queryText = topic }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = topic, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchPrimaryText)
              }
            }
          }
        }
      }
    }

    // Pipeline Execution Status Bar
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StitchSecondaryBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(StitchSuccess))
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = "Synthesis Complete", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
            }
            Text(text = "8 sources queried in 3.4s", fontSize = 11.sp, color = StitchSuccess, fontFamily = FontFamily.Monospace)
          }

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            listOf("1. Query Expansion", "2. Web Crawl (8/8)", "3. Extraction & Rerank", "4. Synthesis").forEach { step ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(StitchCardBackground)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(text = step, fontSize = 10.sp, color = StitchSuccess, fontFamily = FontFamily.Monospace)
                }
              }
            }
          }
        }
      }
    }

    // Verified Sources Horizontal Scroll
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Verified Sources (5 cited)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          }
          Text(text = "View all", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = StitchPrimaryAccent)
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ResearchSourceCard(
            domain = "developer.android.com",
            match = "98% Match",
            title = "State and UI in Jetpack Compose",
            snippet = "Official guide detailing rememberSaveable behavior, custom Saver contracts, and bundle-driven lifecycles.",
            tag = "AndroidX 1.7 Spec"
          )
          ResearchSourceCard(
            domain = "kotlinlang.org",
            match = "94% Match",
            title = "Multiplatform State Preservation",
            snippet = "Best practices for cross-target state preservation without Android Framework bundle dependencies.",
            tag = "Kotlin 2.0 Docs"
          )
          ResearchSourceCard(
            domain = "github.com/JetBrains",
            match = "89% Match",
            title = "compose-multiplatform #4120",
            snippet = "Discussion resolving SaveableStateRegistry disposal traps in iOS and Desktop navigation graphs.",
            tag = "Issue Threads"
          )
        }
      }
    }

    // Synthesized Deep Research Report Card
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = StitchCardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
        shadowElevation = 2.dp
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Executive Takeaway
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(StitchSecondaryBackground)
              .border(
                width = 1.dp,
                color = StitchBorder,
                shape = RoundedCornerShape(8.dp)
              )
              .padding(12.dp)
          ) {
            Row {
              Box(
                modifier = Modifier
                  .width(4.dp)
                  .height(48.dp)
                  .background(StitchPrimaryAccent, RoundedCornerShape(2.dp))
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "EXECUTIVE TAKEAWAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryAccent)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "With Kotlin 2.0 and Compose Multiplatform 1.7+, relying on platform-specific lifecycle bundles creates retention leaks on iOS/Desktop. The recommended production pattern leverages a decoupled Saver<T, Any> paired with an ambient SaveableStateHolder across multiplatform screen stacks.",
                  fontSize = 12.5.sp,
                  lineHeight = 18.sp,
                  color = StitchPrimaryText
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Section 1: Recommended Architecture
          Text(text = "1. Recommended Architecture", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          Spacer(modifier = Modifier.height(6.dp))
          Row(verticalAlignment = Alignment.Top) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Target Agnostic Saver: Define serialization primitives via kotlinx.serialization instead of Android Parcelable.",
              fontSize = 12.5.sp,
              lineHeight = 17.sp,
              color = StitchPrimaryText
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.Top) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Scoped Key Registry: Encapsulate route navigation keys into a hierarchical SaveableStateProvider.",
              fontSize = 12.5.sp,
              lineHeight = 17.sp,
              color = StitchPrimaryText
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Embedded Code Snippet Card
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = CodeBlockBackground
          ) {
            Column {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(CodeBlockHeader)
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = "StatefulScreen.kt", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                Row(
                  modifier = Modifier.clickable {
                    clipboardManager.setText(AnnotatedString("""
@Composable
fun <T : Any> StatefulScreen(
    key: String,
    saveableStateHolder: SaveableStateHolder,
    content: @Composable () -> Unit
) {
    saveableStateHolder.SaveableStateProvider(key) {
        content()
    }
}
                    """.trimIndent()))
                    android.widget.Toast.makeText(context, "Code copied!", android.widget.Toast.LENGTH_SHORT).show()
                  },
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "Copy", fontSize = 11.sp, color = StitchSecondaryText)
                }
              }

              SelectionContainer {
                Text(
                  text = """
@Composable
fun <T : Any> StatefulScreen(
    key: String,
    saveableStateHolder: SaveableStateHolder,
    content: @Composable () -> Unit
) {
    saveableStateHolder.SaveableStateProvider(key) {
        // Preserves tree node across Desktop window resize
        // and iOS view controller recreation in Kotlin 2.0
        content()
    }
}
                  """.trimIndent(),
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.5.sp,
                  lineHeight = 16.sp,
                  color = CodeBlockText,
                  modifier = Modifier
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Section 2: Gotchas & Edge Cases
          Text(text = "2. Gotchas & Edge Cases", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = StitchSecondaryBackground
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StitchWarning, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(text = "iOS Serialization Constraints", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                Text(
                  text = "On Kotlin/Native targets, primitive maps serialized via non-frozen states may trigger IllegalStateException. Always ensure custom savers return serializable JSON strings.",
                  fontSize = 11.5.sp,
                  lineHeight = 16.sp,
                  color = StitchSecondaryText
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Section 3: Empirical Metrics
          Text(text = "3. Empirical Metrics & Benchmarks", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
              color = StitchSecondaryBackground
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "MEMORY FOOTPRINT", fontSize = 10.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
                Text(text = "-34.2%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StitchSuccess)
                Text(text = "No lingering view refs", fontSize = 10.sp, color = StitchSecondaryText)
              }
            }

            Surface(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
              color = StitchSecondaryBackground
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "RECREATION LATENCY", fontSize = 10.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
                Text(text = "<11.8ms", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryAccent)
                Text(text = "Cold restore bench", fontSize = 10.sp, color = StitchSecondaryText)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Utility Toolbar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .border(androidx.compose.foundation.BorderStroke(0.5.dp, StitchBorder), RoundedCornerShape(8.dp))
              .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(StitchSecondaryBackground)
                  .clickable {
                    clipboardManager.setText(AnnotatedString("Deep Technical Synthesis: Compose Multiplatform 1.7 state restoration patterns"))
                    android.widget.Toast.makeText(context, "Report copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                  }
                  .padding(horizontal = 8.dp, vertical = 6.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = StitchPrimaryText, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "Copy Report", fontSize = 11.sp, color = StitchPrimaryText)
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(StitchSecondaryBackground)
                  .clickable {
                    viewModel.saveCodeBlockToFile("StatefulScreen.kt", "// StatefulScreen\n")
                    android.widget.Toast.makeText(context, "Saved to project workspace!", android.widget.Toast.LENGTH_SHORT).show()
                  }
                  .padding(horizontal = 8.dp, vertical = 6.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.DriveFileMove, contentDescription = null, tint = StitchPrimaryText, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "Save to Project", fontSize = 11.sp, color = StitchPrimaryText)
                }
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = StitchSecondaryText, modifier = Modifier.size(15.dp))
              }
              IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = StitchSecondaryText, modifier = Modifier.size(15.dp))
              }
            }
          }
        }
      }
    }

    // Follow-up Questions Card
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = StitchCardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(text = "Explore Follow-ups", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
          Spacer(modifier = Modifier.height(8.dp))

          listOf(
            "How does this compare to Voyager navigation?",
            "Generate test harness for this Custom Saver pattern",
            "Inject this architecture into active module codebase"
          ).forEach { prompt ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(StitchSecondaryBackground)
                .clickable {
                  followUpText = prompt
                }
                .padding(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = prompt, fontSize = 12.sp, color = StitchPrimaryText, modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(14.dp))
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Follow-up Prompt Bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(StitchSecondaryBackground)
              .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = followUpText,
              onValueChange = { followUpText = it },
              placeholder = { Text("Ask follow-up question about this research...", fontSize = 12.sp, color = StitchSecondaryText) },
              modifier = Modifier.weight(1f),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
              ),
              maxLines = 2
            )

            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(StitchPrimaryAccent)
                .clickable {
                  if (followUpText.isNotBlank()) {
                    followUpText = ""
                    android.widget.Toast.makeText(context, "Synthesizing follow-up response...", android.widget.Toast.LENGTH_SHORT).show()
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(15.dp))
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun ResearchSourceCard(
  domain: String,
  match: String,
  title: String,
  snippet: String,
  tag: String
) {
  Surface(
    modifier = Modifier
      .width(220.dp)
      .height(130.dp),
    shape = RoundedCornerShape(10.dp),
    color = StitchCardBackground,
    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = domain, fontSize = 10.sp, color = StitchSecondaryText, fontFamily = FontFamily.Monospace)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(StitchSuccess.copy(alpha = 0.15f))
              .padding(horizontal = 4.dp, vertical = 2.dp)
          ) {
            Text(text = match, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StitchSuccess)
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText, maxLines = 1)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = snippet, fontSize = 10.5.sp, lineHeight = 14.sp, color = StitchSecondaryText, maxLines = 2)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = tag, fontSize = 10.sp, color = StitchPrimaryAccent, fontWeight = FontWeight.Medium)
        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(12.dp))
      }
    }
  }
}
