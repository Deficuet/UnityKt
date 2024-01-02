package io.github.deficuet.unitykt.classes

interface MonoBehaviour: Behaviour {
    val mScript: PPtr<MonoScript>
    val mName: String
}