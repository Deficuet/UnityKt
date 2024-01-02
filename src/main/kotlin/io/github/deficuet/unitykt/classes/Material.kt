package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.math.Color
import io.github.deficuet.unitykt.math.Vector2

interface Material: NamedObject {
    val mShader: PPtr<Shader>
    val mSavedProperties: UnityPropertySheet
}

interface UnityTexEnv {
    val mTexture: PPtr<Texture>
    val mScale: Vector2
    val mOffset: Vector2
}

interface UnityPropertySheet {
    val mTexEnvs: Map<String, List<UnityTexEnv>>
    val mInts: Map<String, List<Int>>
    val mFloats: Map<String, List<Float>>
    val mColors: Map<String, List<Color>>
}
