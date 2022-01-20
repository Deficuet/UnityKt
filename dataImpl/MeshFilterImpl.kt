package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.Mesh
import io.github.deficuet.unitykt.util.ObjectReader

class MeshFilterImpl internal constructor(reader: ObjectReader): ComponentImpl(reader) {
    val mMesh = PPtr<Mesh>(reader)
}