package io.github.deficuet.unitykt.util

import SevenZip.Compression.LZMA.Decoder
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
}