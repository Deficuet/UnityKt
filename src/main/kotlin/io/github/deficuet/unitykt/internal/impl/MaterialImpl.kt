package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.*
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo
import io.github.deficuet.unitykt.util.readArrayOf

internal class MaterialImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Material, MaterialFields(assetFile, info) {
    override val mShader: PPtr<Shader> get() {
        checkInitialize()
        return fmShader
    }
    override val mSavedProperties: UnityPropertySheet get() {
        checkInitialize()
        return fmSavedProperties
    }

    override fun read() {
        super.read()
        fmShader = PPtrImpl(reader)
        if (unityVersion[0] == 4 && unityVersion[1] >= 1) {
            reader.readAlignedStringArray()    //m_ShaderKeywords
        }
        if (unityVersion >= intArrayOf(2021, 3)) {
            reader.readAlignedStringArray()
            reader.readAlignedStringArray()
        } else if (unityVersion[0] >= 5) {
            reader.readAlignedString()      //m_ShaderKeywords
        }
        if (unityVersion[0] >= 5) {
            reader.skip(4)     //m_LightmapFlags: UInt
        }
        if (unityVersion >= intArrayOf(5, 6)) {
            reader.skip(1)     //m_EnableInstancingVariants: Boolean
            reader.alignStream()
        }
        if (unityVersion >= intArrayOf(4, 3)) {
            reader.skip(4)     //m_CustomRenderQueue: Int
        }
        if (unityVersion >= intArrayOf(5, 1)) {
            reader.readArrayOf {
                reader.readAlignedString()      //first
                reader.readAlignedString()      //second
            }
        }
        if (unityVersion >= intArrayOf(5, 6)) {
            reader.readAlignedStringArray()
        }
        fmSavedProperties = UnityPropertySheetImpl(reader)
    }
}

internal class UnityTexEnvImpl(reader: ObjectReader): UnityTexEnv {
    override val mTexture = PPtrImpl<Texture>(reader)
    override val mScale = reader.readVector2()
    override val mOffset = reader.readVector2()
}

internal class UnityPropertySheetImpl(reader: ObjectReader): UnityPropertySheet {
    override val mTexEnvs = reader.readArrayOf {
        reader.readAlignedString() to UnityTexEnvImpl(reader)
    }.groupBy({ it.first }, { it.second })
    override val mInts = if (reader.unityVersion[0] >= 2021) {
        reader.readArrayOf {
            readAlignedString() to readInt32()
        }.groupBy({ it.first }, { it.second })
    } else {
        emptyMap()
    }
    override val mFloats = reader.readArrayOf {
        with(reader) { readAlignedString() to readFloat() }
    }.groupBy({ it.first }, { it.second })
    override val mColors = reader.readArrayOf {
        with(reader) { readAlignedString() to readColor4() }
    }.groupBy({ it.first }, { it.second })
}
