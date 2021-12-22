package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.file.*
import io.github.deficuet.unitykt.util.*

object AssetManager {
    val assetBundleMap = mutableMapOf<String, AssetBundleFile>()
    val assetFiles = mutableListOf<SerializedFile>()
    val assetFilesIndexMap = mutableMapOf<String, Int>()
    val resourceReaderMap = mutableMapOf<String, EndianBinaryReader>()

    fun loadFiles(vararg path: String) {
        if (path.any { !it.isFile() }) throw IllegalStateException("\"path\" must be a file")
    }

    private fun assignFile(info: FileTypeWithReader) {

    }
}