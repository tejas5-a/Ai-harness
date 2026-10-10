package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.ApiProviderConfig
import com.example.data.model.ProviderType

class ApiKeyRepository(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("altron_api_providers_v3", Context.MODE_PRIVATE)

  fun getAllProviders(): List<ApiProviderConfig> {
    return ProviderType.values().map { type ->
      getProviderConfig(type)
    }
  }

  fun getProviderConfig(type: ProviderType): ApiProviderConfig {
    val keyFromPrefs = prefs.getString("key_${type.name}", "") ?: ""
    val defaultApiKey = if (type == ProviderType.GEMINI && keyFromPrefs.isBlank()) {
      try {
        val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
        val buildKey = field.get(null) as? String ?: ""
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
      } catch (e: Throwable) {
        ""
      }
    } else {
      keyFromPrefs
    }

    val baseUrl = prefs.getString("url_${type.name}", type.defaultBaseUrl) ?: type.defaultBaseUrl
    val model = prefs.getString("model_${type.name}", type.defaultModel) ?: type.defaultModel
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
      capabilities = type.defaultCapabilities
    )
  }

  fun saveProviderConfig(config: ApiProviderConfig) {
    prefs.edit()
      .putString("key_${config.providerType.name}", config.apiKey.trim())
      .putString("url_${config.providerType.name}", config.baseUrl.trim())
      .putString("model_${config.providerType.name}", config.selectedModel.trim())
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
    prefs.edit()
      .remove("key_${type.name}")
      .remove("url_${type.name}")
      .remove("model_${type.name}")
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
