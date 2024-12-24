package com.ota

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

fun ByteArray.extractFromZip(): MutableMap<String, ByteArray> {
  val files = mutableMapOf<String, ByteArray>()

  ByteArrayInputStream(this).use { byteStream ->
    ZipInputStream(byteStream).use { zipStream ->
      var entry = zipStream.nextEntry
      while (entry != null) {
        if (!entry.isDirectory) {
          val buffer = zipStream.readBytes()
          files[entry.name] = buffer
        }
        zipStream.closeEntry()
        entry = zipStream.nextEntry
      }
    }
  }

  return files
}
