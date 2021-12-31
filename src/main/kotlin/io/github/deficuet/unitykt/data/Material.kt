package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class Material internal constructor(reader: ObjectReader): NamedObject(reader) {
    val mShader = PPtr<Shader>(reader)
    val mSavedProperties: UnityPropertySheet

    init {
        if (unityVersion[0] == 4 && unityVersion[1] >= 1) {
            reader.readNextStringArray()    //m_ShaderKeywords
        }
        if (unityVersion[0] >= 5) {
            reader.readAlignedString()      //m_ShaderKeywords
            reader += 4     //m_LightmapFlags: UInt
        }
        if (unityVersion >= intArrayOf(5, 6)) {
            reader += 1     //m_EnableInstancingVariants: Boolean
            reader.alignStream()
        }
        if (unityVersion >= intArrayOf(4, 3)) {
            reader += 4     //m_CustomRenderQueue: Int
        }
        if (unityVersion >= intArrayOf(5, 1)) {
            reader.readArrayOf {
                reader.readAlignedString()      //first
                reader.readAlignedString()      //second
            }
        }
        if (unityVersion >= intArrayOf(5, 6)) {
            reader.readNextStringArray()
        }
        mSavedProperties = UnityPropertySheet(reader)
    }
}

class UnityTexEnv internal constructor(reader: ObjectReader) {
    val mTexture = PPtr<Texture>(reader)
    val mScale = reader.readVector2()
    val mOffset = reader.readVector2()
}

class UnityPropertySheet internal constructor(reader: ObjectReader) {
    val mTexEnvs = reader.readArrayOf { reader.readAlignedString() to UnityTexEnv(reader) }.toMap()
    val mInts = if (reader.unityVersion[0] >= 2021) {
        reader.readArrayOf { with(reader) { readAlignedString() to readInt() } }.toMap()
    } else emptyMap()
    val mFloats = reader.readArrayOf { with(reader) { readAlignedString() to readFloat() } }.toMap()
    val mColors = reader.readArrayOf { with(reader) { readAlignedString() to readColor4() } }.toMap()
}