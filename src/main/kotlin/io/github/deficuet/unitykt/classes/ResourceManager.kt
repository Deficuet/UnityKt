package io.github.deficuet.unitykt.classes

interface ResourceManager: UnityObject {
    val mContainer: Map<String, List<PPtr<UnityObject>>>
}