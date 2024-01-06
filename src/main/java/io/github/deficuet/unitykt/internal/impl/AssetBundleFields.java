package io.github.deficuet.unitykt.internal.impl;

import io.github.deficuet.unitykt.classes.AssetInfo;
import io.github.deficuet.unitykt.pptr.PPtr;
import io.github.deficuet.unitykt.classes.UnityObject;
import io.github.deficuet.unitykt.internal.file.ObjectInfo;
import io.github.deficuet.unitykt.internal.file.SerializedFile;
import kotlin.UInt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

abstract class AssetBundleFields extends NamedObjectImpl {
    AssetBundleFields(@NotNull SerializedFile assetFile, @NotNull ObjectInfo info) {
        super(assetFile, info);
    }

    PPtr<UnityObject>[] fmPreloadTable;
    Map<String, List<AssetInfo>> fmContainer;
    AssetInfo fmMainAsset;
    UInt fmRuntimeCompatibility;
    String fmAssetBundleName;
    String[] fmDependencies;
    Boolean fmIsStreamedSceneAssetBundle;
    Integer fmExplicitDataLayout;
    Integer fmPathFlags;
}
