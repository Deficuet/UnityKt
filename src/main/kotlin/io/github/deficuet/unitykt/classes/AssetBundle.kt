package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface AssetBundle: NamedObject {
    val mPreloadTable: Array<out PPtr<UnityObject>>
    val mContainer: Map<String, List<AssetInfo>>
    val mMainAsset: AssetInfo
    val mRuntimeCompatibility: UInt
    val mAssetBundleName: String
    val mDependencies: Array<out String>
    val mIsStreamedSceneAssetBundle: Boolean
    val mExplicitDataLayout: Int
    val mPathFlags: Int
}

interface AssetInfo {
    val preloadIndex: Int
    val preloadSize: Int
    val asset: PPtr<UnityObject>
}
