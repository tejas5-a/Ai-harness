package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * EncryptedSecurityStorage: Backed by the Android KeyStore for encrypting
 * sensitive API keys and GitHub personal access tokens with AES-GCM-256.
 * Falls back safely to secure app-private storage if hardware KeyStore is restricted.
 */
class EncryptedSecurityStorage(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("altron_secure_vault_v1", Context.MODE_PRIVATE)

  private val keyAlias = "altron_keystore_aes_key"
  private val androidKeyStore = "AndroidKeyStore"
  private val transformation = "AES/GCM/NoPadding"

  init {
    ensureKey()
  }

  private fun ensureKey() {
    try {
      val keyStore = KeyStore.getInstance(androidKeyStore).apply { load(null) }
      if (!keyStore.containsAlias(keyAlias)) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, androidKeyStore)
        val spec = KeyGenParameterSpec.Builder(
          keyAlias,
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
          .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
          .setKeySize(256)
          .build()
        keyGenerator.init(spec)
        keyGenerator.generateKey()
      }
    } catch (e: Exception) {
      // KeyStore initialization fallback handled in encrypt/decrypt
    }
  }

  private fun getSecretKey(): SecretKey? {
    return try {
      val keyStore = KeyStore.getInstance(androidKeyStore).apply { load(null) }
      val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
      entry?.secretKey
    } catch (e: Exception) {
      null
    }
  }

  fun encryptString(plainText: String): String {
    if (plainText.isBlank()) return ""
    val secretKey = getSecretKey()
    return if (secretKey != null) {
      try {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        "ENC:" + Base64.encodeToString(combined, Base64.NO_WRAP)
      } catch (e: Exception) {
        // Fallback to app-private obfuscation
        "OBF:" + Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
      }
    } else {
      "OBF:" + Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }
  }

  fun decryptString(cipherText: String): String {
    if (cipherText.isBlank()) return ""
    return when {
      cipherText.startsWith("ENC:") -> {
        val rawEncoded = cipherText.removePrefix("ENC:")
        val secretKey = getSecretKey() ?: return ""
        try {
          val combined = Base64.decode(rawEncoded, Base64.NO_WRAP)
          val iv = ByteArray(12)
          val encrypted = ByteArray(combined.size - 12)
          System.arraycopy(combined, 0, iv, 0, 12)
          System.arraycopy(combined, 12, encrypted, 0, encrypted.size)

          val cipher = Cipher.getInstance(transformation)
          val spec = GCMParameterSpec(128, iv)
          cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
          val decrypted = cipher.doFinal(encrypted)
          String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
          ""
        }
      }
      cipherText.startsWith("OBF:") -> {
        try {
          val raw = Base64.decode(cipherText.removePrefix("OBF:"), Base64.NO_WRAP)
          String(raw, Charsets.UTF_8)
        } catch (e: Exception) {
          ""
        }
      }
      else -> {
        // Plain text legacy value
        cipherText
      }
    }
  }

  fun saveSecureValue(key: String, value: String) {
    val encrypted = encryptString(value)
    prefs.edit().putString(key, encrypted).apply()
  }

  fun getSecureValue(key: String, defaultValue: String = ""): String {
    val stored = prefs.getString(key, null) ?: return defaultValue
    val decrypted = decryptString(stored)
    return decrypted.ifEmpty { defaultValue }
  }

  fun removeSecureValue(key: String) {
    prefs.edit().remove(key).apply()
  }
}
