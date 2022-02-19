package io.github.deficuet.unitykt.export.smolv

import io.github.deficuet.unitykt.export.EndianByteArrayWriter
import io.github.deficuet.unitykt.export.spirv.of
import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.EndianType
import io.github.deficuet.unitykt.util.Reference

internal class SmolvDecoder private constructor() {
    companion object {
        private const val headerSize = 24
        private const val spirVHeaderMagic = 0x07230203u
        private val UInt.zigDecode: Int get() {
            return if (this.and(1u) != 0u) {
                this.shr(1).inv().toInt()
            } else {
                this.shr(1).toInt()
            }
        }

        fun getDecodedBufferSize(reader: EndianBinaryReader): Int {
            if (reader.position + headerSize > reader.length) return 0
            return reader.withMark {
                position += headerSize - 4
                val endian = reader.endian
                resetEndian(EndianType.LittleEndian)
                val size = readInt()
                resetEndian(endian)
                size
            }
        }

        fun decode(reader: EndianBinaryReader, size: Int, writer: EndianByteArrayWriter): Boolean {
            if (reader.length < headerSize) return false
            val inputEnd = reader.position + size
            val outputStart = writer.position
            val decodedSize: Int
            with(writer) {
                with(reader) {
                    writeUInt(spirVHeaderMagic)
                    reader += 4
                    writeUInt(readUInt())
                    writeUInt(readUInt())
                    writeInt(readInt())
                    writeUInt(readUInt())
                    decodedSize = readInt()
                }
            }
            var prevResult = 0; var prevDecorate = 0
            while (reader.position < inputEnd) {
                val inStrLenRef = Reference<UInt>()
                val opRef = Reference<SpvOp>()
                readLengthOp(reader, inStrLenRef, opRef)
                var op = opRef.value; val inStrLen = inStrLenRef.value
                val wasSwizzle = op == SpvOp.VectorShuffleCompact
                if (wasSwizzle) op = SpvOp.VectorShuffle
                writer.writeUInt(inStrLen.shl(16).or(op.id))
                var ioffs = 1u
                if (op.hasType) {
                    writer.writeUInt(readVarInt(reader))
                    ioffs++
                }
                if (op.hasResult) {
                    val zds = prevResult + readVarInt(reader).zigDecode
                    writer.writeInt(zds)
                    prevResult = zds
                    ioffs++
                }
                if (op == SpvOp.Decorate || op == SpvOp.MemberDecorate) {
                    val zds = prevDecorate + readVarInt(reader).toInt()
                    writer.writeInt(zds)
                    prevDecorate = zds
                    ioffs++
                }
                var relativeCount = op.deltaFromResult
                var inverted = false
                if (relativeCount < 0) {
                    inverted = true
                    relativeCount = -relativeCount
                }
                var i = 0
                while (i < relativeCount && ioffs < inStrLen) {
                    val v = readVarInt(reader)
                    val zd = if (inverted) v.zigDecode else v.toInt()
                    writer.writeInt(prevResult - zd)
                    i++; ioffs++
                }
                if (wasSwizzle && inStrLen <= 9u) {
                    val swizzle = reader.readByte().toUInt()
                    if (inStrLen > 5u) { writer.writeUInt(swizzle.shr(6)) }
                    if (inStrLen > 6u) { writer.writeUInt(swizzle.shr(4).and(3u)) }
                    if (inStrLen > 7u) { writer.writeUInt(swizzle.shr(2).and(3u)) }
                    if (inStrLen > 8u) { writer.writeUInt(swizzle.and(3u)) }
                } else if (op.varRest) {
                    while (ioffs < inStrLen) {
                        writer.writeUInt(readVarInt(reader))
                        ioffs++
                    }
                } else {
                    while (ioffs < inStrLen) {
                        if (reader.position + 4 > reader.length) return false
                        writer.writeUInt(reader.readUInt())
                        ioffs++
                    }
                }
            }
            if (writer.position != outputStart + decodedSize) return false
            return true
        }

        private fun readVarInt(reader: EndianBinaryReader): UInt {
            var v = 0u; var shift = 0
            while (reader.position < reader.length) {
                val b = reader.readByte().toUInt()
                v = v.or(b.and(127u).shl(shift))
                shift += 7
                if (b.and(128u) == 0u) break
            }
            return v
        }

        private fun readLengthOp(
            reader: EndianBinaryReader,
            len: Reference<UInt>,
            op: Reference<SpvOp>
        ): Boolean {
            len.value = 0u; op.value = SpvOp.Nop
            val value = readVarInt(reader)
            len.value = value.shr(20).shl(4).or(value.shr(4).and(0xFu))
            op.value = SpvOp.of(value.shr(4).and(0xFFF0u).or(value.and(0xFu))).remap()
            len.value = decodeLen(op.value, len.value)
            return true
        }

        private fun decodeLen(op: SpvOp, len: UInt): UInt {
            var value = len + 1u
            value += when (op) {
                SpvOp.VectorShuffle -> 4u
                SpvOp.VectorShuffleCompact -> 4u
                SpvOp.Decorate -> 2u
                SpvOp.Load -> 3u
                SpvOp.AccessChain -> 3u
                else -> 0u
            }
            return value
        }
    }
}