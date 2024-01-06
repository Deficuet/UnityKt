package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface MeshFilter: Component {
    val mMesh: PPtr<Mesh>
}