package io.github.deficuet.unitykt.util

import net.jpountz.lz4.LZ4Factory
import org.tukaani.xz.ArrayCache
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import org.tukaani.xz.LZMA2InputStream
import org.tukaani.xz.lz.LZDecoder
import org.tukaani.xz.lzma.LZMADecoder
import org.tukaani.xz.rangecoder.RangeDecoderFromStream

class CompressUtils private constructor() {
    companion object {
        val GZIP_MAGIC = byteArrayOf(0x1F, 0x8B)
        val BROTLI_MAGIC = byteArrayOf("brotli")

        private val lz4Decompressor = LZ4Factory.fastestInstance().fastDecompressor()

        fun lzmaDecompress(data: ByteArray): ByteArray {
            val input = ByteArrayInputStream(data).apply { read() }
            val dictSize = input.read() +
                input.read().shl(8) +
                input.read().shl(16) +
                input.read().shl(24)
            val output = ByteArrayOutputStream()
            val decoder = LZMADecoder(
                LZDecoder(dictSize, null, ArrayCache.getDefaultCache()),
                RangeDecoderFromStream(input),

            )
            return LZMA2InputStream(input, dictSize).use { lzma ->
                val buf = ByteArray(2048)
                var bytesRead: Int
                while (lzma.read(buf).also { bytesRead = it } > 0) {
                    output.write(buf, 0, bytesRead)
                }
                output.toByteArray()
            }
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