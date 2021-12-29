package io.github.deficuet.unitykt.util

import io.github.deficuet.unitykt.AssetManager
import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.file.ResourceFile
import io.github.deficuet.unitykt.file.SerializedFile
import java.io.File

class ResourceReader {
    private val needSearch: Boolean
    private val path: String
    private val assetFile: SerializedFile?
    private val offset: Long
    private val size: Long
    private var reader: EndianBinaryReader?
        get() {
            if (field != null) return field
            else {
                val file = File(path)
                if (file.name in AssetManager.resourceFiles) {
                    return AssetManager.resourceFiles.getValue(file.name).reader
                }
                val dir = "${assetFile!!.root.directory}/${file.name}"
                return if (!File(dir).exists()) {
                    println("Can't find the resource file $dir")
                    null
                } else {
                    field = (ImportContext(dir, OffsetMode.MANUAL).files.getValue(file.name) as ResourceFile).reader
                    field
                }
            }
        }

    internal constructor(path: String, assetFile: SerializedFile, offset: Long, size: Long) {
        needSearch = true
        this.path = path
        this.assetFile = assetFile
        this.offset = offset
        this.size = size
        this.reader = null
    }

    internal constructor(reader: EndianBinaryReader, offset: Long, size: Long) {
        needSearch = false
        this.reader = reader
        this.offset = offset
        this.size = size
        this.assetFile = null
        this.path = ""
    }

    var bytes: ByteArray? = null
        private set
        get() {
            return if (field != null) field
            else {
                reader?.position = offset
                reader?.read(size.toInt()).also { if (it != null) field = it }
            }
        }

    fun read(buffer: ByteArray) {
        bytes?.let { System.arraycopy(it, 0, buffer, 0, buffer.size) }
    }
}