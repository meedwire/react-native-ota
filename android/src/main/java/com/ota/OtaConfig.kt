package com.ota

import com.facebook.react.bridge.ReadableMap
import com.ota.extensions.getNonNullOrEmptyString

data class Environments(
  var development: EnvironmentDetails?,
  var production: EnvironmentDetails?,
  var staging: EnvironmentDetails?
)

data class EnvironmentDetails(
  var enabled: Boolean,
  var strategy: String,
  var otaKey: String,
  var checkAutomatically: String,
  var url: String
)

data class OtaConfig(
  var enabled: Boolean,
  var url: String?,
  var environments: Environments
) {
  companion object {
    fun fromReadableMap(config: ReadableMap): OtaConfig {
      val url = config.getNonNullOrEmptyString("url") ?: throw Error("Server OTA Url not configured see https://")

      val devConfig = config.getMap("environments")?.getMap("development")?.let {
        EnvironmentDetails(
          enabled = it.getBoolean("enabled"),
          strategy = it.getString("strategy") ?: "manual",
          otaKey = it.getString("otaKey") ?: "",
          checkAutomatically = it.getString("checkAutomatically") ?: "WIFI_ONLY",
          url = it.getString("url") ?: ""
        )
      }
      val stagingConfig = config.getMap("environments")?.getMap("development")?.let {
        EnvironmentDetails(
          enabled = it.getBoolean("enabled"),
          strategy = it.getString("strategy") ?: "on-app-start",
          otaKey = it.getString("otaKey") ?: "",
          checkAutomatically = it.getString("checkAutomatically") ?: "WIFI_ONLY",
          url = it.getString("url") ?: ""
        )
      }
      val prodConfig = config.getMap("environments")?.getMap("development")?.let {
        EnvironmentDetails(
          enabled = it.getBoolean("enabled"),
          strategy = it.getString("strategy") ?: "on-app-start",
          otaKey = it.getString("otaKey") ?: "",
          checkAutomatically = it.getString("checkAutomatically") ?: "WIFI_ONLY",
          url = it.getString("url") ?: ""
        )
      }

      val otaConfig = OtaConfig(
        enabled = config.getBoolean("enabled"),
        url = url,
        environments = Environments(
          development = devConfig,
          staging = stagingConfig,
          production = prodConfig
        )
      )

      return otaConfig
    }
  }
}


