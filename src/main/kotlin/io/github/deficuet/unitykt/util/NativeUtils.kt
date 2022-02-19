package io.github.deficuet.unitykt.util

import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NativeUtils private constructor() {
    companion object {
        private val nativeTempFolder by lazy {
            val temp = File(
                "${System.getProperty("java.io.tmpdir")}UnityKt-Deficuet"
            )
            if (!temp.exists()) {
                if (!temp.mkdir())
                    throw IllegalStateException("Can't create temp directory ${temp.canonicalPath}")
            }
            temp.apply { deleteOnExit() }
        }

        private val isPosix: Boolean get() {
            return try {
                FileSystems.getDefault().supportedFileAttributeViews().contains("posix")
            } catch (e: Exception) {
                false
            }
        }

        fun loadLibraryFromJar(name: String) {
            val tempFile = File(nativeTempFolder, name)
            NativeUtils::class.java.getResourceAsStream("/$name")!!.use {
                try {
                    Files.copy(it, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    tempFile.delete()
                    throw e
                }
            }
            try {
                System.load(tempFile.absolutePath)
            } finally {
                if (isPosix) {
                    tempFile.delete()
                } else {
                    tempFile.deleteOnExit()
                }
            }
        }
    }
}