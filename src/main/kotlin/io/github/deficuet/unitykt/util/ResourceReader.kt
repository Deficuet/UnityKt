package io.github.deficuet.unitykt.util

import io.github.deficuet.unitykt.ImportContext
import io.github.deficuet.unitykt.UnityAssetManager
import io.github.deficuet.unitykt.file.ResourceFile
import io.github.deficuet.unitykt.file.SerializedFile
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException

class ResourceReader internal constructor(
    private val path: String,
    private val assetFile: SerializedFile?,
    private val offset: Long,
    private val size: Long
): Closeable {
    private var shouldClose: Boolean = false
    private var reader: EndianBinaryReader? = null
        get() {
            if (field != null) return field
            else {
                val file = File(path)
                val manager = assetFile!!.root.manager
                if (file.name in manager.resourceFiles) {
                    return manager.resourceFiles.getValue(file.name).reader
                }
                val dir = "${assetFile.root.directory}/${file.name}"
                return if (!File(dir).exists()) {
                    throw FileNotFoundException("Can't find the resource file $dir")
                } else {
                    shouldClose = true
                    field = (ImportContext(dir, manager).files.getValue(file.name) as ResourceFile).reader
                    field
                }
            }
        }

    internal constructor(reader: EndianBinaryReader, offset: Long, size: Long):
            this("", null, offset, size) {
        this.reader = reader
    }

    fun read(): ByteArray {
        return with(reader!!) {
            absolutePosition = offset
            read(size.toInt())
        }
    }

    override fun close() {
        if (shouldClose) reader?.close()
    }

    fun registerToManager(m: UnityAssetManager): ResourceReader {
        m.otherReaderList.add(this)
        return this
    }
}