package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.GameObject
import io.github.deficuet.unitykt.data.Material
import io.github.deficuet.unitykt.data.PPtr
import io.github.deficuet.unitykt.data.Transform
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

abstract class RendererImpl internal constructor(reader: ObjectReader): ComponentImpl(reader) {
    val mMaterials: Array<PPtr<Material>>
    val mStaticBatchInfo: StaticBatchInfo?
    val mSubsetIndices: Array<UInt>

    init {
        val v43 = intArrayOf(4, 3)
        if (unityVersion[0] < 5) {
            //m_Enabled, m_CastShadows, m_ReceiveShadows: Boolean, m_LightmapIndex: UByte
            reader += 4
        } else {
            if (unityVersion >= intArrayOf(5, 4)) {
                reader += 3     //m_Enabled: Boolean, m_CastShadows, m_ReceiveShadows: UByte
                if (unityVersion >= intArrayOf(2017, 2)) {
                    reader += 1     //m_DynamicOccludee: UByte
                }
                if (unityVersion[0] >= 2021) {
                    reader += 1     //m_StaticShadowCaster: UByte
                }
                reader += 3     //m_MotionVectors, m_LightProbeUsage, m_ReflectionProbeUsage: UByte
                if (unityVersion >= intArrayOf(2019, 3)) {
                    reader += 1     //m_RayTracingMode: UByte
                }
                if (unityVersion[0] >= 2020) {
                    reader += 1     //m_RayTraceProcedural: UByte
                }
                reader.alignStream()
            } else {
                reader += 1     //m_Enabled: Boolean
                reader.alignStream()
                reader += 2     //m_CastShadows: UByte, m_ReceiveShadows: Boolean
                reader.alignStream()
            }
            if (unityVersion[0] >= 2018) {
                reader += 4     //m_RenderingLayerMask: UInt
            }
            if (unityVersion >= intArrayOf(2018, 3)) {
                reader += 4     //m_RendererPriority
            }
            reader += 4     //m_LightmapIndex, m_LightmapIndexDynamic: UShort
        }
        if (unityVersion[0] >= 3) {
            reader += 16    //m_LightmapTilingOffset: Vector4
        }
        if (unityVersion[0] >= 5) {
            reader += 16    //m_LightmapTilingOffsetDynamic: Vector4
        }
        mMaterials = reader.readArrayOf { PPtr(reader) }
        if (unityVersion[0] < 3) {
            reader += 16    //m_LightmapTilingOffset: Vector4
            mStaticBatchInfo = null
            mSubsetIndices = emptyArray()
        } else {
            if (unityVersion >= intArrayOf(5, 5)) {
                mStaticBatchInfo = StaticBatchInfo(reader)
                mSubsetIndices = emptyArray()
            } else {
                mSubsetIndices = reader.readNextUIntArray()
                mStaticBatchInfo = null
            }
            PPtr<Transform>(reader)     //m_StaticBatchRoot
        }
        if (unityVersion >= intArrayOf(5, 4)) {
            PPtr<Transform>(reader)     //m_ProbeAnchor
            PPtr<GameObject>(reader)    //m_LightProbeVolumeOverride
        } else if (unityVersion >= intArrayOf(3, 5)) {
            reader += 1     //m_UseLightProbes: Boolean
            reader.alignStream()
            if (unityVersion[0] >= 5) {
                reader += 4     //m_ReflectionProbeUsage: Int
            }
            PPtr<Transform>(reader)     //m_LightProbeAnchor
        }
        if (unityVersion >= v43) {
            //m_SortingLayer: Short / m_SortingLayerID: UInt
            reader += if (unityVersion.contentEquals(v43)) 2 else 4
            reader += 2     //m_SortingOrder: Short
            reader.alignStream()
        }
    }
}

class StaticBatchInfo internal constructor(reader: ObjectReader) {
    val firstSubMesh = reader.readUShort()
    val subMeshCount = reader.readUShort()
}