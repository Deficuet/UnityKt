package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.*
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.pptr.PPtr
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo
import io.github.deficuet.unitykt.util.readArrayOf

internal abstract class RendererImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Renderer, RendererFields(assetFile, info) {
    final override val mMaterials: Array<out PPtr<Material>> get() {
        checkInitialize()
        return fmMaterials
    }
    final override val mStaticBatchInfo: StaticBatchInfo? get() {
        checkInitialize()
        return fmStaticBatchInfo
    }
    final override val mSubsetIndices: Array<out UInt> get() {
        checkInitialize()
        return fmSubsetIndices
    }

    override fun read() {
        super.read()
        val v43 = intArrayOf(4, 3)
        if (unityVersion[0] < 5) {
            //m_Enabled, m_CastShadows, m_ReceiveShadows: Boolean, m_LightmapIndex: UByte
            reader.skip(4)
        } else {
            if (unityVersion >= intArrayOf(5, 4)) {
                reader.skip(3)     //m_Enabled: Boolean, m_CastShadows, m_ReceiveShadows: UByte
                if (unityVersion >= intArrayOf(2017, 2)) {
                    reader.skip(1)     //m_DynamicOccludee: UByte
                }
                if (unityVersion[0] >= 2021) {
                    reader.skip(1)     //m_StaticShadowCaster: UByte
                }
                reader.skip(3)     //m_MotionVectors, m_LightProbeUsage, m_ReflectionProbeUsage: UByte
                if (unityVersion >= intArrayOf(2019, 3)) {
                    reader.skip(1)     //m_RayTracingMode: UByte
                }
                if (unityVersion[0] >= 2020) {
                    reader.skip(1)     //m_RayTraceProcedural: UByte
                }
                reader.alignStream()
            } else {
                reader.skip(1)     //m_Enabled: Boolean
                reader.alignStream()
                reader.skip(2)     //m_CastShadows: UByte, m_ReceiveShadows: Boolean
                reader.alignStream()
            }
            if (unityVersion[0] >= 2018) {
                reader.skip(4)     //m_RenderingLayerMask: UInt
            }
            if (unityVersion >= intArrayOf(2018, 3)) {
                reader.skip(4)     //m_RendererPriority
            }
            reader.skip(4)     //m_LightmapIndex, m_LightmapIndexDynamic: UShort
        }
        if (unityVersion[0] >= 3) {
            reader.skip(16)    //m_LightmapTilingOffset: Vector4
        }
        if (unityVersion[0] >= 5) {
            reader.skip(16)    //m_LightmapTilingOffsetDynamic: Vector4
        }
        fmMaterials = reader.readArrayOf { PPtrImpl(reader) }
        if (unityVersion[0] < 3) {
            reader.skip(16)    //m_LightmapTilingOffset: Vector4
            fmStaticBatchInfo = null
            fmSubsetIndices = emptyArray()
        } else {
            if (unityVersion >= intArrayOf(5, 5)) {
                fmStaticBatchInfo = StaticBatchInfoImpl(reader)
                fmSubsetIndices = emptyArray()
            } else {
                fmSubsetIndices = reader.readUInt32Array()
                fmStaticBatchInfo = null
            }
            PPtrImpl<Transform>(reader)     //m_StaticBatchRoot
        }
        if (unityVersion >= intArrayOf(5, 4)) {
            PPtrImpl<Transform>(reader)     //m_ProbeAnchor
            PPtrImpl<GameObject>(reader)    //m_LightProbeVolumeOverride
        } else if (unityVersion >= intArrayOf(3, 5)) {
            reader.skip(1)     //m_UseLightProbes: Boolean
            reader.alignStream()
            if (unityVersion[0] >= 5) {
                reader.skip(4)     //m_ReflectionProbeUsage: Int
            }
            PPtrImpl<Transform>(reader)     //m_LightProbeAnchor
        }
        if (unityVersion >= v43) {
            //m_SortingLayer: Short / m_SortingLayerID: UInt
            reader.skip(if (unityVersion.contentEquals(v43)) 2 else 4)
            reader.skip(2)     //m_SortingOrder: Short
            reader.alignStream()
        }
    }
}

internal class StaticBatchInfoImpl(reader: ObjectReader): StaticBatchInfo {
    override val firstSubMesh = reader.readUInt16()
    override val subMeshCount = reader.readUInt16()
}
