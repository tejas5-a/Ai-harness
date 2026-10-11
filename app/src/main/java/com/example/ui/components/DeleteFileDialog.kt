package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BrandPrimaryOrange
import com.example.ui.theme.BrandTextBlack
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurfaceElevated
import com.example.ui.theme.PureWhiteBackground
import com.example.ui.theme.TerminalRed

/**
 * DeleteFileConfirmationDialog:
 * Title: Delete this file?
 * Message: Are you sure you want to delete "[filename]"? This action may not be reversible.
 * Buttons: Cancel, Delete File
 */
@Composable
fun DeleteFileConfirmationDialog(
  fileName: String,
  hasUnsavedChanges: Boolean = false,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = PureWhiteBackground),
      border = BorderStroke(1.dp, LightBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = TerminalRed,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Delete this file?",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = BrandTextBlack
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Are you sure you want to delete \"$fileName\"? This action may not be reversible.",
          fontSize = 14.sp,
          color = BrandTextSecondary,
          lineHeight = 20.sp
        )

        if (hasUnsavedChanges) {
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(TerminalRed.copy(alpha = 0.1f))
              .border(1.dp, TerminalRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Text(
              text = "⚠️ Warning: This file currently has unsaved edits in the editor. Deleting it will discard those changes.",
              fontSize = 12.sp,
              color = TerminalRed,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = BrandTextSecondary, fontWeight = FontWeight.Medium)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(
              containerColor = TerminalRed,
              contentColor = androidx.compose.ui.graphics.Color.White
            ),
            modifier = Modifier.defaultMinSize(minHeight = 44.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Delete File", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
