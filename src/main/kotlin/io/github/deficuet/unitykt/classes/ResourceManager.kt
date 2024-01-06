package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface ResourceManager: UnityObject {
    val mContainer: Map<String, List<PPtr<UnityObject>>>
}