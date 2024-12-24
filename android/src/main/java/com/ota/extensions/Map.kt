package com.ota.extensions

import com.facebook.react.bridge.ReadableMap

fun <K, V> Map<K, V>.toUrlEncodedParams(): String {
  return this.entries.joinToString("&") { (key, value) ->
    "${java.net.URLEncoder.encode(key.toString(), "UTF-8")}=${java.net.URLEncoder.encode(value?.toString() ?: "", "UTF-8")}"
  }
}

fun ReadableMap.getNonNullOrEmptyString(key: String): String? {
  val value = this.getString(key)

  return if (value.isNullOrEmpty()){
    null
  }else {
    value
  }

}
