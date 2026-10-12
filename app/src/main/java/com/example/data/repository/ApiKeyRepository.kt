package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.ApiProviderConfig
import com.example.data.model.ProviderType

class ApiKeyRepository(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("altron_api_providers_v3", Context.MODE_PRIVATE)

  private val secureVault = EncryptedSecurityStorage(context)

  fun getAllProviders(): List<ApiProviderConfig> {
    return ProviderType.values().map { type ->
      getProviderConfig(type)
    }
  }

  fun getProviderConfig(type: ProviderType): ApiProviderConfig {
    // Check Keystore-backed secure storage first, fallback to prefs
    var secureKey = secureVault.getSecureValue("secure_key_${type.name}", "")
    if (secureKey.isBlank()) {
      val keyFromPrefs = prefs.getString("key_${type.name}", "") ?: ""
      if (keyFromPrefs.isNotBlank()) {
        secureKey = keyFromPrefs
        // Migrate to secure vault
        secureVault.saveSecureValue("secure_key_${type.name}", keyFromPrefs)
      }
    }

    val defaultApiKey = if (type == ProviderType.GEMINI && secureKey.isBlank()) {
      try {
        val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
        val buildKey = field.get(null) as? String ?: ""
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
      } catch (e: Throwable) {
        ""
      }
    } else {
      secureKey
    }

    val baseUrl = prefs.getString("url_${type.name}", type.defaultBaseUrl) ?: type.defaultBaseUrl
    val model = prefs.getString("model_${type.name}", type.defaultModel) ?: type.defaultModel
    val researchModel = prefs.getString("research_model_${type.name}", model) ?: model
    val codingModel = prefs.getString("coding_model_${type.name}", model) ?: model
    val isEnabled = prefs.getBoolean("enabled_${type.name}", defaultApiKey.isNotBlank())
    val isDefault = prefs.getString("default_provider", ProviderType.GEMINI.name) == type.name
    val lastStatus = prefs.getString("status_${type.name}", null)
    val lastSuccess = if (prefs.contains("success_${type.name}")) prefs.getBoolean("success_${type.name}", false) else null

    // Load any dynamically fetched models cached for this provider
    val dynamicModelsRaw = prefs.getString("dynamic_models_${type.name}", "") ?: ""
    val dynamicModels = if (dynamicModelsRaw.isNotBlank()) {
      dynamicModelsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    } else {
      emptyList()
    }

    return ApiProviderConfig(
      providerType = type,
      apiKey = defaultApiKey,
      baseUrl = baseUrl,
      selectedModel = model,
      isEnabled = isEnabled,
      isDefault = isDefault,
      lastTestedStatus = lastStatus,
      lastTestedSuccess = lastSuccess,
      dynamicModels = dynamicModels,
      capabilities = type.defaultCapabilities,
      researchModel = researchModel,
      codingModel = codingModel
    )
  }

  fun saveProviderConfig(config: ApiProviderConfig) {
    // Encrypt and store API key in Android Keystore vault
    secureVault.saveSecureValue("secure_key_${config.providerType.name}", config.apiKey.trim())

    prefs.edit()
      .putString("url_${config.providerType.name}", config.baseUrl.trim())
      .putString("model_${config.providerType.name}", config.selectedModel.trim())
      .putString("research_model_${config.providerType.name}", config.researchModel.trim())
      .putString("coding_model_${config.providerType.name}", config.codingModel.trim())
      .putBoolean("enabled_${config.providerType.name}", config.isEnabled)
      .apply()

    if (config.isDefault) {
      setDefaultProvider(config.providerType)
    }
  }

  fun saveFetchedModels(type: ProviderType, models: List<String>) {
    if (models.isNotEmpty()) {
      prefs.edit()
        .putString("dynamic_models_${type.name}", models.joinToString(","))
        .apply()
    }
  }

  fun clearProviderConfig(type: ProviderType) {
    secureVault.removeSecureValue("secure_key_${type.name}")
    prefs.edit()
      .remove("key_${type.name}")
      .remove("url_${type.name}")
      .remove("model_${type.name}")
      .remove("research_model_${type.name}")
      .remove("coding_model_${type.name}")
      .remove("enabled_${type.name}")
      .remove("status_${type.name}")
      .remove("success_${type.name}")
      .remove("dynamic_models_${type.name}")
      .apply()
  }

  fun setDefaultProvider(type: ProviderType) {
    prefs.edit().putString("default_provider", type.name).apply()
  }

  fun getDefaultProvider(): ApiProviderConfig {
    val defaultName = prefs.getString("default_provider", ProviderType.GEMINI.name) ?: ProviderType.GEMINI.name
    val type = try {
      ProviderType.valueOf(defaultName)
    } catch (e: Exception) {
      ProviderType.GEMINI
    }
    return getProviderConfig(type)
  }

  fun recordTestResult(type: ProviderType, success: Boolean, status: String) {
    prefs.edit()
      .putString("status_${type.name}", status)
      .putBoolean("success_${type.name}", success)
      .apply()
  }
}
