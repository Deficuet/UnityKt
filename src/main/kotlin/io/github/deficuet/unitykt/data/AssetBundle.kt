package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.*
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class AssetBundle private constructor(
    private val container: ImplementationContainer<AssetBundleImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { AssetBundleImpl(ObjectReader(assetFile, info)) })

    val mPreloadTable: Array<PPtr<Object>>          get() = container.impl.mPreloadTable
    val mContainer: Array<Pair<String, AssetInfo>>  get() = container.impl.mContainer
    val mMainAsset: AssetInfo                       get() = container.impl.mMainAsset
    val mRuntimeCompatibility: UInt                 get() = container.impl.mRuntimeCompatibility
    val mAssetBundleName: String                    get() = container.impl.mAssetBundleName
    val mDependencies: Array<String>                get() = container.impl.mDependencies
    val mIsStreamedSceneAssetBundle: Boolean        get() = container.impl.mIsStreamedSceneAssetBundle
    val mExplicitDataLayout: Int                    get() = container.impl.mExplicitDataLayout
    val mPathFlags: Int                             get() = container.impl.mPathFlags
}