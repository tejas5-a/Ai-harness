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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.StitchBorder
import com.example.ui.theme.StitchCardBackground
import com.example.ui.theme.StitchMainBackground
import com.example.ui.theme.StitchPressedAccent
import com.example.ui.theme.StitchPrimaryAccent
import com.example.ui.theme.StitchPrimaryText
import com.example.ui.theme.StitchSecondaryBackground
import com.example.ui.theme.StitchSecondaryText
import com.example.ui.theme.StitchSoftAccent
import com.example.ui.theme.StitchSuccess
import com.example.ui.viewmodel.AltronViewModel

/**
 * SCREEN 7: APK BUILDER
 * Faithfully matches Google Stitch Compiler Studio design:
 * - Breadcrumb & Status Header (Compiler Studio, Release v2.4 Signed)
 * - Build Variant selector (Debug, Release, Benchmark, App Bundle)
 * - Target Architecture selector (arm64-v8a, armeabi-v7a, x86_64, Universal)
 * - Min SDK & Target SDK cards (API 34)
 * - Keystore configuration card (production-release.jks)
 * - "Build Release APK" action button
 * - Live pipeline logs
 * - Build Output artifact card (app-arm64-v8a-release.apk, 24.8 MB) with actions:
 *   Install on Device, Download APK, View in Explorer, ProGuard Map
 */
@Composable
fun ApkBuilderScreen(
  viewModel: AltronViewModel,
  modifier: Modifier = Modifier
) {
  val apkBuildState by viewModel.apkBuildState.collectAsState()
  val isBuilding by viewModel.isBuildingApk.collectAsState()
  val context = LocalContext.current

  var selectedVariant by remember { mutableStateOf("Release (Signed)") }
  var selectedArch by remember { mutableStateOf("arm64-v8a") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(StitchMainBackground)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item { Spacer(modifier = Modifier.height(4.dp)) }

    // Breadcrumb & Status Header
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.setScreen(Screen.HOME) }, modifier = Modifier.size(32.dp)) {
              Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
            }
            Text(text = "AltronCore-Android", fontSize = 11.5.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = StitchSecondaryText, modifier = Modifier.size(14.dp))
            Text(text = "APK & Bundle Builder", fontSize = 11.5.sp, color = StitchPrimaryText, fontWeight = FontWeight.Medium)
          }

          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
              Icon(imageVector = Icons.Default.Tune, contentDescription = "Settings", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
              Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = StitchSecondaryText, modifier = Modifier.size(18.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Compiler Studio",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = StitchPrimaryText,
            letterSpacing = (-0.5).sp
          )

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(StitchSuccess.copy(alpha = 0.15f))
              .padding(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
              Spacer(modifier = Modifier.width(5.dp))
              Text(text = "Release v2.4 (Signed)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchSuccess)
            }
          }
        }
      }
    }

    // Build Variant Selector
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "BUILD VARIANT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchSecondaryText, letterSpacing = 0.5.sp)

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = StitchSecondaryBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            listOf("Debug", "Release (Signed)", "Benchmark", "App Bundle (.aab)").forEach { variant ->
              val isSel = selectedVariant == variant
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSel) StitchCardBackground else Color.Transparent)
                  .border(if (isSel) 1.dp else 0.dp, if (isSel) StitchBorder else Color.Transparent, RoundedCornerShape(8.dp))
                  .clickable { selectedVariant = variant }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = variant,
                  fontSize = 11.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSel) StitchPrimaryText else StitchSecondaryText,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }

    // Target Architecture Pills
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "TARGET ARCHITECTURE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchSecondaryText, letterSpacing = 0.5.sp)

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("arm64-v8a", "armeabi-v7a", "x86_64", "Universal").forEach { arch ->
            val isSel = selectedArch == arch
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSel) StitchPrimaryAccent else StitchSecondaryBackground)
                .border(1.dp, if (isSel) StitchPrimaryAccent else StitchBorder, RoundedCornerShape(8.dp))
                .clickable { selectedArch = arch }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSel) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                  text = arch,
                  fontSize = 11.5.sp,
                  fontFamily = FontFamily.Monospace,
                  color = if (isSel) Color.White else StitchSecondaryText,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }
        }
      }
    }

    // SDK Config Grid
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Min SDK Version", fontSize = 11.sp, color = StitchSecondaryText)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "API 34", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              Text(text = "Android 14", fontSize = 11.sp, color = StitchSuccess)
            }
          }
        }

        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Target SDK Version", fontSize = 11.sp, color = StitchSecondaryText)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "API 34", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
              Text(text = "Optimized", fontSize = 11.sp, color = StitchSuccess)
            }
          }
        }
      }
    }

    // Keystore Configuration Card
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = StitchCardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(StitchSoftAccent),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(text = "production-release.jks", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                Text(text = "Alias: altron_key", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(StitchSuccess.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(text = "Verified", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = StitchSuccess)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .border(
                width = 1.dp,
                color = StitchBorder,
                shape = RoundedCornerShape(8.dp)
              )
              .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "SHA1: 8F:4C:2A:9B:D3:1F:22:98...", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable {
                android.widget.Toast.makeText(context, "Keystore config opened", android.widget.Toast.LENGTH_SHORT).show()
              }
            ) {
              Text(text = "Configure", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryAccent)
              Spacer(modifier = Modifier.width(2.dp))
              Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = StitchPrimaryAccent, modifier = Modifier.size(12.dp))
            }
          }
        }
      }
    }

    // Build Action Button
    item {
      Button(
        onClick = {
          viewModel.startApkBuild("Release")
          android.widget.Toast.makeText(context, "Starting Gradle build pipeline...", android.widget.Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("apk_build_action_btn"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = StitchPrimaryAccent,
          contentColor = Color.White
        )
      ) {
        Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Build Release APK (Variant: $selectedArch)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }

    // Live Pipeline Logs
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "LIVE PIPELINE LOGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchSecondaryText, letterSpacing = 0.5.sp)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Gradle v8.5 Active", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSuccess)
          }
        }

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF1C1C18)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(text = "> Configure project :app", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFF88726C))
            Text(text = "> Task :app:preBuild UP-TO-DATE", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFF88726C))
            Text(text = "> Task :app:compileReleaseKotlin UP-TO-DATE", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFFFFB59E))
            Text(text = "> Task :app:bundleReleaseClassesWithDependencies", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFFF4F0EA))
            Text(text = "> Task :app:mergeReleaseNativeLibs", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFFF4F0EA))
            Text(text = "> Task :app:signReleaseBundle SUCCESS", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFF9ED2B1))
            Text(text = "> Generated APK: app-arm64-v8a-release.apk (24.8 MB)", fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFDBD0))
          }
        }
      }
    }

    // Build Output & Artifact Card
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "BUILD ARTIFACT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchSecondaryText, letterSpacing = 0.5.sp)

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = StitchCardBackground,
          border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StitchSuccess.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(text = "app-arm64-v8a-release.apk", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = StitchPrimaryText)
                  Text(text = "24.8 MB • Build time: 42.1s", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StitchSecondaryText)
                }
              }

              Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StitchSecondaryBackground)
                .padding(8.dp)
            ) {
              Text(
                text = "SHA-256: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = StitchSecondaryText,
                maxLines = 1
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = {
                    android.widget.Toast.makeText(context, "Installing package on device...", android.widget.Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.weight(1f).height(40.dp),
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryAccent, contentColor = Color.White)
                ) {
                  Icon(imageVector = Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "Install on Device", fontSize = 12.sp)
                }

                Button(
                  onClick = {
                    android.widget.Toast.makeText(context, "Downloading APK to Downloads folder...", android.widget.Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.weight(1f).height(40.dp),
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = StitchSecondaryBackground, contentColor = StitchPrimaryText),
                  border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                  Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = StitchPrimaryText, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "Download APK", fontSize = 12.sp)
                }
              }

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { viewModel.setScreen(Screen.FILE_EXPLORER) },
                  modifier = Modifier.weight(1f).height(40.dp),
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = StitchSecondaryBackground, contentColor = StitchPrimaryText),
                  border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                  Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = StitchPrimaryText, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "View in Explorer", fontSize = 12.sp)
                }

                Button(
                  onClick = {
                    android.widget.Toast.makeText(context, "ProGuard mappings opened", android.widget.Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.weight(1f).height(40.dp),
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = StitchSecondaryBackground, contentColor = StitchPrimaryText),
                  border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                  Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = StitchPrimaryText, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "ProGuard Map", fontSize = 12.sp)
                }
              }
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}
