package com.pranav.lib_android.util;

import java.io.*
import java.util.zip.ZipFile

object ZipUtil {
  
  const val BUFFER_SIZE = 4096
  
  fun unzip(zipFilePath: File, destDirectory: String) {
    val destDir =  File(destDirectory).run {
      if (!exists())
        mkdirs()
    }

    ZipFile(zipFilePath).use { zip ->
      zip.entries().asSequence().forEach { entry ->
        zip.getInputStream(entry).use { input ->
          val output = destDirectory + File.separator + entry.name
          if (entry.isDirectory)
            File(output).mkdir()
          else
            extractFile(input, output)
        }
      }
    }
  }

  private fun extractFile(inputStream: InputStream, destFilePath: String) {
    val bos = FileOutputStream(destFilePath)
    inputStream.copyTo(bos)
  }

	fun copyFileFromAssets(context: Context, inputFile: String, fileName: String) {
		val in = context.getAssets().open(inputFile);
		val outputPath = context.getFilesDir() + "/" + fileName;
		val out = FileOutputStream(outputPath)
		in.copyTo(out)
	}
}
