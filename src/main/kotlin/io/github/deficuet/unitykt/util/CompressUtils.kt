package io.github.deficuet.unitykt.util

import SevenZip.Compression.LZMA.Decoder
import net.jpountz.lz4.LZ4Factory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import org.tukaani.xz.ArrayCache
import org.tukaani.xz.lz.LZDecoder
import org.tukaani.xz.lzma.LZMADecoder
import org.tukaani.xz.rangecoder.RangeDecoderFromStream
import kotlin.experimental.and

class CompressUtils private constructor() {
    companion object {
        val GZIP_MAGIC = byteArrayOf(0x1F, 0x8B)
        val BROTLI_MAGIC = byteArrayOf("brotli")

        private val lz4Decompressor = LZ4Factory.fastestInstance().fastDecompressor()

        fun lzmaDecompress(data: ByteArray): ByteArray {
            val decoder = Decoder()
            val preInput = ByteArrayInputStream(data)
            val props = ByteArray(5)
            if (preInput.read(props) != 5) {
                throw IllegalStateException("Input .lzma is too short")
            }
            var outSize = 0L
            for (i in 0 until 8) {
                val v = preInput.read()
                if (v < 0) throw IllegalStateException("Invalid input data")
                outSize = outSize or (v.toLong().shl(8 * i))
            }
            val input = with(data) { ByteArrayInputStream(sliceArray(5 until size)) }
            val output = ByteArrayOutputStream()
            decoder.SetDecoderProperties(props)
            decoder.Code(input, output, outSize)
            return output.toByteArray()
//            var props = input.read()
//            val dictSize = input.read() +
//                input.read().shl(8) +
//                input.read().shl(16) +
//                input.read().shl(24)
//            val outputStream = LZDecoder(dictSize, null, ArrayCache.getDefaultCache())
//            val lc = props % 9
//            props /= 9
//            val decoder = LZMADecoder(
//                outputStream, RangeDecoderFromStream(input),
//                lc, props % 5, props / 5
//            )
//            decoder.decode()
//            val output = ByteArray(uncompressedSize)
//            outputStream.flush(output, 0)
//            return output
        }

        fun lz4Decompress(data: ByteArray, uncompressedSize: Int): ByteArray {
            return lz4Decompressor.decompress(data, uncompressedSize)
        }

        fun gzipDecompress(data: ByteArray): ByteArray {
            val output = ByteArrayOutputStream()
            return GZIPInputStream(ByteArrayInputStream(data)).use { gzip ->
                val buf = ByteArray(1024)
                var bytesRead: Int
                while (gzip.read(buf).also { bytesRead = it } > 0) {
                    output.write(buf, 0, bytesRead)
                }
                output.toByteArray()
            }
        }
    }
}