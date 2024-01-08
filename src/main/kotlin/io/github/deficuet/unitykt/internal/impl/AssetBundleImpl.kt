package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.AssetBundle
import io.github.deficuet.unitykt.classes.AssetInfo
import io.github.deficuet.unitykt.classes.UnityObject
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo
import io.github.deficuet.unitykt.util.readArrayOf

internal class AssetBundleImpl(
    assetFile: SerializedFile, info: ObjectInfo
): AssetBundle, AssetBundleFields(assetFile, info) {
    override val mPreloadTable: Array<PPtr<UnityObject>> get() {
        checkInitialize()
        return fmPreloadTable
    }
    override val mContainer: Map<String, List<AssetInfo>> get() {
        checkInitialize()
        return fmContainer
    }
    override val mMainAsset: AssetInfo get() {
        checkInitialize()
        return fmMainAsset
    }
    override val mRuntimeCompatibility: UInt get() {
        checkInitialize()
        return fmRuntimeCompatibility
    }
    override val mAssetBundleName: String get() {
        checkInitialize()
        return fmAssetBundleName
    }
    override val mDependencies: Array<String> get() {
        checkInitialize()
        return fmDependencies
    }
    override val mIsStreamedSceneAssetBundle: Boolean get() {
        checkInitialize()
        return fmIsStreamedSceneAssetBundle
    }
    override val mExplicitDataLayout: Int get() {
        checkInitialize()
        return fmExplicitDataLayout
    }
    override val mPathFlags: Int get() {
        checkInitialize()
        return fmPathFlags
    }

    override fun read() {
        super.read()
        fmPreloadTable = reader.readArrayOf { PPtrImpl(this) }
        fmContainer = reader.readArrayOf {
            readAlignedString() to AssetInfoImpl(this)
        }.groupBy({ it.first }, { it.second })
        fmMainAsset = AssetInfoImpl(reader)
        fmRuntimeCompatibility = reader.readUInt32()
        fmAssetBundleName = reader.readAlignedString()
        fmDependencies = reader.readAlignedStringArray()
        fmIsStreamedSceneAssetBundle = reader.readBool()
        reader.alignStream()
        fmExplicitDataLayout = if (unityVersion >= intArrayOf(2017, 3)) reader.readInt32() else 0
        fmPathFlags = if (unityVersion >= intArrayOf(2017, 1, 0)) reader.readInt32() else 0
    }
}

internal class AssetInfoImpl(reader: ObjectReader): AssetInfo {
    override val preloadIndex = reader.readInt32()
    override val preloadSize = reader.readInt32()
    override val asset = PPtrImpl<UnityObject>(reader)
}
