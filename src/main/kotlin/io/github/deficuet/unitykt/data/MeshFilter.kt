package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

class MeshFilter internal constructor(reader: ObjectReader): Component(reader) {
    val mMesh = PPtr<Mesh>(reader)
}