package io.github.deficuet.unitykt.internal.export.smolv

import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.EndianByteArrayWriter
import io.github.deficuet.unitykt.util.useEndian
import io.github.deficuet.unitykt.util.withMark
import java.nio.ByteOrder

internal class SmolvDecoder private constructor() {
    companion object {
        private const val headerSize = 24
        private const val spirVHeaderMagic = 0x07230203u

        fun getDecodedBufferSize(reader: EndianBinaryReader): Int {
            if (reader.position + headerSize > reader.length) return 0
            return reader.withMark {
                skip(headerSize - 4)
                useEndian(ByteOrder.LITTLE_ENDIAN) {
                    readInt32()
                }
            }
        }

        fun decode(reader: EndianBinaryReader, size: Int, writer: EndianByteArrayWriter): Boolean {
            if (reader.length < headerSize) return false
            val inputEnd = reader.position + size
            val outputStart = writer.position
            val decodedSize: Int
            with(writer) {
                with(reader) {
                    writeUInt32(spirVHeaderMagic)
                    reader.skip(4)
                    writeUInt32(readUInt32())
                    writeUInt32(readUInt32())
                    writeInt32(readInt32())
                    writeUInt32(readUInt32())
                    decodedSize = readInt32()
                }
            }
            var prevResult = 0; var prevDecorate = 0
            while (reader.position < inputEnd) {
                val len: UInt; var op: SpvOp
                with(readLengthOp(reader)) { len = this.len; op = this.op }
                val wasSwizzle = op == SpvOp.VectorShuffleCompact
                if (wasSwizzle) op = SpvOp.VectorShuffle
                writer.writeUInt32(len.shl(16).or(op.id))
                var ioffs = 1u
                if (op.hasType) {
                    writer.writeUInt32(readVarInt(reader))
                    ioffs++
                }
                if (op.hasResult) {
                    val zds = prevResult + zigDecode(readVarInt(reader))
                    writer.writeInt32(zds)
                    prevResult = zds
                    ioffs++
                }
                if (op == SpvOp.Decorate || op == SpvOp.MemberDecorate) {
                    val zds = prevDecorate + readVarInt(reader).toInt()
                    writer.writeInt32(zds)
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
                while (i < relativeCount && ioffs < len) {
                    val v = readVarInt(reader)
                    val zd = if (inverted) zigDecode(v) else v.toInt()
                    writer.writeInt32(prevResult - zd)
                    i++; ioffs++
                }
                if (wasSwizzle && len <= 9u) {
                    val swizzle = reader.read().toUInt()
                    if (len > 5u) { writer.writeUInt32(swizzle.shr(6)) }
                    if (len > 6u) { writer.writeUInt32(swizzle.shr(4).and(3u)) }
                    if (len > 7u) { writer.writeUInt32(swizzle.shr(2).and(3u)) }
                    if (len > 8u) { writer.writeUInt32(swizzle.and(3u)) }
                } else if (op.varRest) {
                    while (ioffs < len) {
                        writer.writeUInt32(readVarInt(reader))
                        ioffs++
                    }
                } else {
                    while (ioffs < len) {
                        if (reader.position + 4 > reader.length) return false
                        writer.writeUInt32(reader.readUInt32())
                        ioffs++
                    }
                }
            }
            if (writer.position != outputStart + decodedSize) return false
            return true
        }

        private fun zigDecode(u: UInt): Int {
            return if (u.and(1u) != 0u) {
                u.shr(1).inv().toInt()
            } else {
                u.shr(1).toInt()
            }
        }

        private fun readVarInt(reader: EndianBinaryReader): UInt {
            var v = 0u; var shift = 0
            while (reader.position < reader.length) {
                val b = reader.read().toUInt()
                v = v.or(b.and(127u).shl(shift))
                shift += 7
                if (b.and(128u) == 0u) break
            }
            return v
        }

        private data class LengthOp(val len: UInt, val op: SpvOp)

        private fun readLengthOp(reader: EndianBinaryReader): LengthOp {
            val value = readVarInt(reader)
            var len = value.shr(20).shl(4).or(value.shr(4).and(0xFu))
            val op = SpvOp.of(value.shr(4).and(0xFFF0u).or(value.and(0xFu))).remap()
            len = decodeLen(op, len)
            return LengthOp(len, op)
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