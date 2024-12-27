package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.enums.ClassIDType
import io.github.deficuet.unitykt.metadata.UnityDataClassCompanion
import io.github.deficuet.unitykt.metadata.UnityObjectCompanion
import io.github.deficuet.unitykt.metadata.UnityObjectMetadata
import io.github.deficuet.unitykt.pptr.PPtr

class AssetBundle(metadata: UnityObjectMetadata): NamedObject(metadata) {
    val mPreloadTable: Array<PPtr<UnityObject>>     by bind("m_PreloadTable", PPtr)
    val mContainer: Map<String, List<AssetInfo>>    by bind("m_Container", null, AssetInfo)
    val mMainAsset: AssetInfo                       by bind("m_MainAsset", AssetInfo)
    val mRuntimeCompatibility: UInt                 by bind("m_RuntimeCompatibility")
    val mAssetBundleName: String                    by bind("m_AssetBundleName")
    val mDependencies: Array<String>                by bind("m_Dependencies")
    val mIsStreamedSceneAssetBundle: Boolean        by bind("m_IsStreamedSceneAssetBundle")
    val mExplicitDataLayout: Int                    by bind("m_ExplicitDataLayout")
    val mPathFlags: Int                             by bind("m_PathFlags")
    val mSceneHashes: Map<String, List<String>>     by bind("m_SceneHashes")

    companion object: UnityObjectCompanion<AssetBundle>(ClassIDType.AssetBundle, ::AssetBundle)
}

class AssetInfo(obj: UnityObject): UnityDataClass(obj) {
    val preloadIndex: Int           by bind("preloadIndex")
    val preloadSize: Int            by bind("preloadSize")
    val asset: PPtr<UnityObject>    by bind("asset", PPtr)

    companion object: UnityDataClassCompanion<AssetInfo>(AssetInfo::class.java, ::AssetInfo)
}
