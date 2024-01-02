package io.github.deficuet.unitykt.util

import io.github.deficuet.unitykt.internal.UnityAssetManagerImpl
import io.github.deficuet.unitykt.internal.file.SerializedFile
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException

internal class ResourceReader(
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
                var file = File(path)
                val manager = assetFile!!.root.manager
                if (file.name in manager.resourceFiles) {
                    return manager.resourceFiles.getValue(file.name).reader
                }
                file = File("${assetFile.root.parent}/${file.name}")
                return if (!file.exists()) {
                    throw FileNotFoundException("Can't find the resource file ${file.name}")
                } else {
                    shouldClose = true
                    field = EndianBinaryFileReader(file)
                    field
                }
            }
        }

    constructor(
        reader: EndianBinaryReader,
        offset: Long,
        size: Long
    ): this("", null, offset, size) {
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

    fun registerToManager(m: UnityAssetManagerImpl): ResourceReader {
        m.otherReaderList.add(this)
        return this
    }
}