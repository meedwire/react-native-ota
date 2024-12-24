package com.ota

import android.content.Context
import android.widget.Toast
import java.io.File

class Ota {
  companion object {
    fun getJSBundleFile(applicationContext: Context): String {
      val updatedBundleFile = File(applicationContext.filesDir, "react_bundle/index.android.bundle")

      if (updatedBundleFile.exists()){
        return updatedBundleFile.absolutePath
      }

      return "assets://index.android.bundle"
    }
  }
}
