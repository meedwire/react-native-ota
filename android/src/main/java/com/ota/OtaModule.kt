package com.ota

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactApplication
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File


data class ResponseHasUpdate(
  var otaVersion: String,
  var hasUpdate: Boolean,
  var fileName: String
)

class OtaServer(
  private val otaConfig: OtaConfig?,
  private val reactContext: ReactApplicationContext
) {
  fun getUpdate() {
    val headers = mapOf(
      "authorization" to "Bearer ${otaConfig?.environments?.development?.otaKey.toString()}",
    )

    val hasUpdate = try {
      val result = hasNewOtaUpdate()

      result
    } catch (e: Exception) {
      println(e.message)
      null
    }

    if (hasUpdate == null) {
      return
    }

    val byteArray = Http().get<ByteArray>(
      "${otaConfig?.url}/${hasUpdate.fileName}",
      GetOptions(
        headers = headers
      )
    )

    val files = byteArray.extractFromZip()

    if (files == null) {
      return;
    }

    val newBundle = files["main.jsbundle"]

    if (newBundle != null) {
      val bundleDir = File(reactContext.filesDir, "react_bundle")

      println(bundleDir.absolutePath.toString())

      val bundleFile = File(bundleDir, "index.android.bundle")
      bundleFile.parentFile?.mkdirs()
      bundleFile.writeBytes(newBundle)
      println("Bundle atualizado salvo em: ${bundleFile.absolutePath}")

      setCurrentVersionOta(hasUpdate.otaVersion)
    }
  }

  private fun hasNewOtaUpdate(): ResponseHasUpdate {
    val pInfo: PackageInfo = reactContext.packageManager.getPackageInfo(reactContext.packageName, 0)
    val version = pInfo.versionName

    val params = mapOf(
      "currentAppVersion" to version.toString(),
      "currentOtaVersion" to getCurrentVersionOta()
    )

    val headers = mapOf(
      "authorization" to "Bearer ${otaConfig?.environments?.development?.otaKey.toString()}",
    )

    val jsonResponse = Http().get<JSONObject>(
      url = otaConfig?.url.toString(),
      options = GetOptions(
        params = params,
        headers = headers
      )
    )

    return ResponseHasUpdate(
      otaVersion = jsonResponse.getString("otaVersion"),
      hasUpdate = jsonResponse.getBoolean("hasUpdate"),
      fileName = jsonResponse.getString("fileName"),
    )
  }

  private fun setCurrentVersionOta(version: String) {
    val sharedPreferences = reactContext.getSharedPreferences("OTA_Preferences", MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("current_version_ota", version)
    editor.apply()
  }

  private fun getCurrentVersionOta(): String {
    val sharedPreferences = reactContext.getSharedPreferences("OTA_Preferences", MODE_PRIVATE)
    return sharedPreferences.getString("current_version_ota", null) ?: "null"
  }
}

@ReactModule(name = OtaModule.NAME)
class OtaModule(private var reactContext: ReactApplicationContext) :
  NativeOtaSpec(reactContext) {
  private var otaConfig: OtaConfig? = null
  private var otaServer: OtaServer;

  init {
    getOtaConfig()
    subscribeAppLifecycle()
    otaServer = OtaServer(otaConfig, reactContext)
  }

  override fun getName(): String {
    return NAME
  }

  override fun setConfig(config: ReadableMap?): Boolean {
    config?.let {
      otaConfig = OtaConfig.fromReadableMap(config)
    }

    return false
  }

  private fun getOtaConfig() {
    otaConfig = OtaConfig.fromReadableMap(toReadableMap())
  }

  private fun toReadableMap(): WritableMap {
    val rootMap = Arguments.createMap()

    rootMap.putBoolean("enabled", true)
    val urlStringId = reactContext.resources.getIdentifier(
      "react_native_ota_url",
      "string",
      reactContext.packageName
    )
    val applicationStringId = reactContext.resources.getIdentifier(
      "react_native_ota_app_key",
      "string",
      reactContext.packageName
    )
    rootMap.putString("url", reactContext.resources.getString(urlStringId))

    // Environments
    val environmentsMap = Arguments.createMap()

    val developmentMap = Arguments.createMap()
    developmentMap.putBoolean("enabled", true)
    developmentMap.putString("strategy", "manual")
    developmentMap.putString("otaKey", reactContext.resources.getString(applicationStringId))
    developmentMap.putString("checkAutomatically", "WIFI_ONLY")
    developmentMap.putString("url", "")
    environmentsMap.putMap("development", developmentMap)

    val productionMap = Arguments.createMap()
    productionMap.putBoolean("enabled", true)
    productionMap.putString("strategy", "on-app-start")
    productionMap.putString("otaKey", "asdkjaskjdhjuiyansmdbamnsbdugnbv")
    productionMap.putString("checkAutomatically", "WIFI_ONLY")
    productionMap.putString("url", "")
    environmentsMap.putMap("production", productionMap)

    val stagingMap = Arguments.createMap()
    stagingMap.putBoolean("enabled", true)
    stagingMap.putString("strategy", "on-app-state-change")
    stagingMap.putString("otaKey", "asdkjaskjdhjuiyansmdbamnsbdugnbv")
    stagingMap.putString("checkAutomatically", "WIFI_ONLY")
    stagingMap.putString("url", "")
    environmentsMap.putMap("staging", stagingMap)

    rootMap.putMap("environments", environmentsMap)

    return rootMap
  }

  private fun subscribeAppLifecycle() {
    reactContext.addLifecycleEventListener(object : LifecycleEventListener {
      override fun onHostResume() {
        GlobalScope.launch(Dispatchers.IO) {
          otaServer.getUpdate()
        }

        Log.i("rnota", "App resume")
      }

      override fun onHostPause() {
        Log.i("rnota", "App pause")
      }

      override fun onHostDestroy() {
        Log.i("rnota", "App destroy")
      }
    })
  }

  companion object {
    const val NAME = "Ota"
  }
}
