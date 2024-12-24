package com.ota

import com.ota.extensions.toUrlEncodedParams
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


data class GetOptions(
  val params: Map<String, String>? = null,
  val headers: Map<String, String>? = null
)


class Http {
  inline fun <reified T>get(url: String, options: GetOptions? = null): T {
    val fullUrl = if (options == null) url else "$url?${options.params?.toUrlEncodedParams()}"

    val connection = URL(fullUrl).openConnection() as HttpURLConnection
    connection.requestMethod = "GET"

    options?.headers?.forEach { (key, value) ->
      connection.setRequestProperty(key, value)
    }

    return try {
      connection.inputStream.use { inputStream ->
        val data = inputStream.readBytes()

        when (T::class) {
          String::class -> {
            String(data) as T
          }
          JSONObject::class -> {
            val jsonString = String(data)
            JSONObject(jsonString) as T
          }
          ByteArray::class -> {
            data as T
          }
          else -> throw IllegalArgumentException("Unsupported return type: ${T::class}")
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      throw e
    } finally {
      connection.disconnect()
    }
  }
}
