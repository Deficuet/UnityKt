package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface MonoBehaviour: Behaviour {
    val mScript: PPtr<MonoScript>
    val mName: String
}