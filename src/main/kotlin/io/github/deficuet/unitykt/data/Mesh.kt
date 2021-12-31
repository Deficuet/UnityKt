package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo
import java.util.*
import kotlin.NoSuchElementException
import kotlin.experimental.and

class Mesh {
}

internal class MeshHelper private constructor() {
    @Suppress("EnumEntryName")
    internal enum class VertexChannelFormat {
        kChannelFormatFloat,
        kChannelFormatFloat16,
        kChannelFormatColor,
        kChannelFormatByte,
        kChannelFormatUInt32;

        companion object {
            fun of(value: Int): VertexChannelFormat {
                return values()[value]
            }
        }
    }

    @Suppress("EnumEntryName")
    internal enum class VertexFormat2017 {
        kVertexFormatFloat,
        kVertexFormatFloat16,
        kVertexFormatColor,
        kVertexFormatUNorm8,
        kVertexFormatSNorm8,
        kVertexFormatUNorm16,
        kVertexFormatSNorm16,
        kVertexFormatUInt8,
        kVertexFormatSInt8,
        kVertexFormatUInt16,
        kVertexFormatSInt16,
        kVertexFormatUInt32,
        kVertexFormatSInt32;

        companion object {
            fun of(value: Int): VertexFormat2017 {
                return values()[value]
            }
        }
    }

    @Suppress("EnumEntryName")
    internal enum class VertexFormat(val size: Int) {
        kVertexFormatFloat(4),
        kVertexFormatFloat16(2),
        kVertexFormatUNorm8(1),
        kVertexFormatSNorm8(1),
        kVertexFormatUNorm16(2),
        kVertexFormatSNorm16(2),
        kVertexFormatUInt8(1),
        kVertexFormatSInt8(1),
        kVertexFormatUInt16(2),
        kVertexFormatSInt16(2),
        kVertexFormatUInt32(4),
        kVertexFormatSInt32(4);

        fun isIntFormat(format: VertexFormat) = format >= kVertexFormatUInt8

        fun bytesToFloatArray(input: ByteArray): List<Float> {
            val len = input.size / size
            val floats = mutableListOf<Float>()
            for (i in 0 until len) {

            }
        }

        companion object {
            fun of(value: Int): VertexFormat {
                return values()[value]
            }
        }
    }

    companion object {
        private val vertexFormatMap = mapOf(
            VertexChannelFormat.kChannelFormatFloat to VertexFormat.kVertexFormatFloat,
            VertexChannelFormat.kChannelFormatFloat16 to VertexFormat.kVertexFormatFloat16,
            VertexChannelFormat.kChannelFormatColor to VertexFormat.kVertexFormatUNorm8,
            VertexChannelFormat.kChannelFormatByte to VertexFormat.kVertexFormatUInt8,
            VertexChannelFormat.kChannelFormatUInt32 to VertexFormat.kVertexFormatUInt32
        )
        private val vertexFormat2017Map = mapOf(
            *(VertexFormat.values().map { vf ->
                VertexFormat2017.valueOf(vf.name) to vf
            } + listOf(VertexFormat2017.kVertexFormatColor to VertexFormat.kVertexFormatUNorm8))
                .toTypedArray()
        )
        internal fun Int.toVertexFormat(version: IntArray): VertexFormat {
            return if (version[0] < 2017) {
                val vcf = VertexChannelFormat.of(this)
                vertexFormatMap[vcf] ?: throw NoSuchElementException(vcf.name)
            } else if (version[0] < 2019) {
                val vf2017 = VertexFormat2017.of(this)
                vertexFormat2017Map[vf2017] ?: throw NoSuchElementException(vf2017.name)
            } else {
                VertexFormat.of(this)
            }
        }
    }
}

class MinMaxAABB internal constructor(reader: EndianBinaryReader) {
    val mMin = reader.readVector3()
    val mMax = reader.readVector3()
}

class CompressedMesh internal constructor(reader: ObjectReader) {
    val mVertices = PackedFloatVector(reader)
    val mUV = PackedFloatVector(reader)
    val mBindPoses = if (reader.unityVersion[0] < 5) PackedFloatVector(reader) else null
    val mNormals = PackedFloatVector(reader)
    val mTangents = PackedFloatVector(reader)
    val mWeights = PackedIntVector(reader)
    val mNormalSigns = PackedIntVector(reader)
    val mTangentSigns = PackedIntVector(reader)
    val mFloatColors = if (reader.unityVersion[0] >= 5) PackedFloatVector(reader) else null
    val mBoneIndices = PackedIntVector(reader)
    val mTriangles = PackedIntVector(reader)
    val mColors: PackedIntVector?
    val mUVInfo: UInt

    init {
        if (reader.unityVersion >= intArrayOf(3, 5)) {
            if (reader.unityVersion[0] < 5) {
                mColors = PackedIntVector(reader)
                mUVInfo = 0u
            } else {
                mUVInfo = reader.readUInt()
                mColors = null
            }
        } else {
            mUVInfo = 0u
            mColors = null
        }
    }
}

class StreamInfo {
    val channelMask: UInt
    val offset: UInt
    val stride: UInt
    val align: UInt
    val dividerOp: Byte
    val frequency: UShort

    internal constructor(reader: ObjectReader) {
        channelMask = reader.readUInt()
        offset = reader.readUInt()
        if (reader.unityVersion[0] < 4) {
            stride = reader.readUInt()
            align  = reader.readUInt()
            dividerOp = 0
            frequency = 0u
        } else {
            stride = reader.readByte().toUInt()
            dividerOp = reader.readByte()
            frequency = reader.readUShort()
            align = 0u
        }
    }

    internal constructor(data: StreamInfoInternal) {
        channelMask = data.channelMask
        offset = data.offset
        stride = data.stride
        align = data.align
        dividerOp = data.dividerOp
        frequency = data.frequency
    }
}

internal data class StreamInfoInternal(
    var channelMask: UInt = 0u,
    var offset: UInt = 0u,
    var stride: UInt = 0u,
    var align: UInt = 0u,
    var dividerOp: Byte = 0,
    var frequency: UShort = 0u
)

class ChannelInfo {
    val stream: Byte
    val offset: Byte
    val format: Byte
    val dimension: Byte

    internal constructor(reader: ObjectReader) {
        stream = reader.readByte()
        offset = reader.readByte()
        format = reader.readByte()
        dimension = reader.readByte() and 0xf
    }

    internal constructor(data: ChannelInfoInternal) {
        stream = data.stream
        offset = data.offset
        format = data.format
        dimension = data.dimension
    }
}

internal data class ChannelInfoInternal(
    var stream: Byte = 0,
    var offset: Byte = 0,
    var format: Byte = 0,
    var dimension: Byte = 0
)

class VertexData internal constructor(reader: ObjectReader) {
    val mCurrentChannels = if (reader.unityVersion[0] < 2018) reader.readUInt() else 0u
    val mVertexCount = reader.readUInt()
    val mChannels: List<ChannelInfo>
    val mStreams: List<StreamInfo>
    val mDataSize: ByteArray

    init {
        val version = reader.unityVersion
        if (version[0] >= 4) {
            mChannels = reader.readArrayOf { ChannelInfo(reader) }
        }
        if (version[0] < 5) {
            val streamSize = if (version[0] < 4) 4 else reader.readInt()
            mStreams = reader.readArrayOf(streamSize) { StreamInfo(reader) }
            if (version[0] < 4) {
                //getChannels
                val internalChannels = mutableListOf<ChannelInfoInternal>()
                for (i in 1..6) internalChannels.add(ChannelInfoInternal())
                for (s in mStreams.indices) {
                    val stream = mStreams[s]
                    val channelMask = BitSet.valueOf(longArrayOf(stream.channelMask.toLong()))
                    var offset = 0
                    for (j in 0..5) {
                        if (channelMask[j]) {
                            val channel = internalChannels[j]
                            channel.stream = s.toByte()
                            channel.offset = offset.toByte()
                            when (j) {
                                0, 1 -> {
                                    channel.format = 0
                                    channel.dimension = 3
                                }
                                2 -> {
                                    channel.format = 2
                                    channel.dimension = 4
                                }
                                3, 4 -> {
                                    channel.format = 0
                                    channel.dimension = 2
                                }
                                5 -> {
                                    channel.format = 0
                                    channel.dimension = 4
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}