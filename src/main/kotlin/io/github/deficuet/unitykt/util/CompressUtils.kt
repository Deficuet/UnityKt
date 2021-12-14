package io.github.deficuet.unitykt.util

import SevenZip.Compression.LZMA.Decoder
import net.jpountz.lz4.LZ4BlockInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object CompressUtils {
    fun lzmaDecompress(data: ByteArray): ByteArray {
        val decoder = Decoder()
        val input = ByteArrayInputStream(data)
        val output = ByteArrayOutputStream()
        val prop = ByteArray(5)
        if (input.read(prop) != 5) {
            throw IllegalStateException("Input .lzma is too short")
        }
        var outSize = 0L
        for (i in 0 until 8) {
            val v = input.read()
            if (v < 0) throw IllegalStateException("Invalid data")
            outSize = outSize or ((v.toLong()) shl (8 * i))
        }
        decoder.SetDecoderProperties(prop)
        decoder.Code(input, output, outSize)
        return output.toByteArray()
    }

    fun lz4Decompress(data: ByteArray): ByteArray {
        val buf = ByteArray(2048)
        val output = ByteArrayOutputStream()
        var len: Int
        LZ4BlockInputStream(ByteArrayInputStream(data)).use { input ->
            while (input.read(buf).also { len = it } > 0) {
                output.write(buf, 0, len)
            }
        }
        return output.toByteArray()
    }
}