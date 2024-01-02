package io.github.deficuet.unitykt.internal

import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal class NativeUtils private constructor() {
    companion object {
        private val nativeTempFolder by lazy {
            val temp = File(
                "${System.getProperty("java.io.tmpdir")}UnityKt_Deficuet"
            )
            if (!temp.exists()) {
                if (!temp.mkdir())
                    throw IllegalStateException("Can't create temp directory ${temp.canonicalPath}")
            }
            temp
        }

        private val isPosix: Boolean get() {
            return try {
                FileSystems.getDefault().supportedFileAttributeViews().contains("posix")
            } catch (e: Exception) {
                false
            }
        }

        fun loadLibraryFromJar(name: String, ext: String) {
            val f = File.createTempFile("${name}_", ".${ext}", nativeTempFolder)
            NativeUtils::class.java.getResourceAsStream("/${name}.${ext}")!!.use {
                try {
                    Files.copy(it, f.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    f.delete()
                    throw e
                }
            }
            try {
                System.load(f.absolutePath)
            } finally {
                try {
                    if (isPosix) {
                        f.delete()
                    } else {
                        f.deleteOnExit()
                    }
                } catch (_: Throwable) {  }
            }
        }
    }
}