package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.metadata.UnityObjectMetadata

abstract class NamedObject(metadata: UnityObjectMetadata): EditorExtension(metadata) {
    val mName: String   by bind("m_Name")
}
