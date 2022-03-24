package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.Object
import io.github.deficuet.unitykt.data.PPtr
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class AssetBundleImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
    val mPreloadTable = reader.readArrayOf { PPtr<Object>(reader) }
    val mContainer = reader.readArrayOf { reader.readAlignedString() to AssetInfo(reader) }
    val mMainAsset = AssetInfo(reader)
    val mRuntimeCompatibility = reader.readUInt()
    val mAssetBundleName = reader.readAlignedString()
    val mDependencies = reader.readNextStringArray()
    val mIsStreamedSceneAssetBundle: Boolean
    val mExplicitDataLayout: Int
    val mPathFlags: Int

    init {
        mIsStreamedSceneAssetBundle = reader.readBool()
        reader.alignStream()
        mExplicitDataLayout = if (unityVersion >= intArrayOf(2017, 3)) reader.readInt() else 0
        mPathFlags = if (unityVersion >= intArrayOf(2017, 1, 0)) reader.readInt() else 0
    }
}

class AssetInfo internal constructor(reader: ObjectReader) {
    val preloadIndex = reader.readInt()
    val preloadSize = reader.readInt()
    val asset = PPtr<Object>(reader)
}