package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.cast
import io.github.deficuet.unitykt.classes.AssetBundle
import io.github.deficuet.unitykt.classes.PPtr
import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.internal.file.FormatVersion
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

@PublishedApi
internal class PPtrImpl<out T: UnityObject>: PPtr<T> {
    override var mFileID: Int
    override var mPathID: Long
    override val isNull: Boolean
        get() = mPathID == 0L || mFileID < 0
    private val assetFile: SerializedFile

    internal constructor(reader: ObjectReader) {
        mFileID = reader.readInt32()
        mPathID = with(reader) {
            if (formatVersion < FormatVersion.Unknown_14) reader.readInt32().toLong()
            else reader.readInt64()
        }
        assetFile = reader.assetFile
    }

    internal constructor(fileId: Int, pathId: Long, assetFile: SerializedFile) {
        mFileID = fileId
        mPathID = pathId
        this.assetFile = assetFile
    }

    private var obj: T? = null

    fun getCache() = obj
    fun setCache(o: @UnsafeVariance T) { obj = o }

    fun getAssetFile(): SerializedFile? {
        if (mFileID == 0) {
            return assetFile
        }
        val fileIndex = mFileID - 1
        if (mFileID < 0 || fileIndex >= assetFile.externals.size) {
            return null
        }
        val manager = assetFile.root.manager
        val result = manager.assetFiles[assetFile.externals[fileIndex].name.lowercase()]
        if (result != null) return result
        if (manager.assetRootFolder == null) return null
        val bundle = assetFile.objectMap[1L] ?: return null
        if (bundle !is AssetBundle) return null
        if (fileIndex >= bundle.mDependencies.size) return null
        val dependencyName = bundle.mDependencies[fileIndex]
        try {
            manager.loadFile(
                manager.assetRootFolder.resolve(dependencyName),
                assetFile.root.manager.defaultReaderConfig
            )
        } catch (e: Exception) {
            println("An error occurred during loading dependency file ${dependencyName}: ${e.message}")
            return null
        }
        return manager.assetFiles[assetFile.externals[fileIndex].name.lowercase()]
    }

    internal fun setObj(other: @UnsafeVariance T) {
        val otherAssetFile = other.cast<UnityObjectImpl>().assetFile
        val name = otherAssetFile.name
        if (name.contentEquals(assetFile.name)) {
            mFileID = 0
        } else {
            mFileID = assetFile.externals.indexOfFirst { it.name.contentEquals(name) }
            if (mFileID == -1) {
                assetFile.externals.add(SerializedFile.FileIdentifier.fromName(name))
                mFileID = assetFile.externals.size
            } else {
                mFileID += 1
            }
        }
        val manager = assetFile.root.manager
        if (!manager.assetFiles.containsValue(otherAssetFile)) {
            manager.assetFiles[name.lowercase()] = otherAssetFile
        }
        mPathID = other.mPathID
        setCache(other)
    }

    fun getObjFrom(assetFile: SerializedFile) = assetFile.objectMap[mPathID]
}

@PublishedApi
internal inline fun <reified T: UnityObject> PPtrImpl<T>.safeGetObjInternal(): T? {
    val cache = getCache()
    if (cache != null) return cache
    return getAssetFile()?.let {
        val obj = getObjFrom(it)
        if (obj != null && obj is T) {
            setCache(obj)
            obj
        } else {
            null
        }
    }
}

@PublishedApi
internal inline fun <reified T: UnityObject> PPtrImpl<T>.getObjInternal(): T {
    return safeGetObjInternal()!!
}
