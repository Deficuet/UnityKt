package io.github.deficuet.unitykt.util

import net.jpountz.lz4.LZ4Factory
import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream

internal class CompressUtils private constructor() {
    companion object {
        val GZIP_MAGIC = byteArrayOf(0x1F, -0x75)
        val BROTLI_MAGIC = byteArrayOf(0x62, 0x72, 0x6F, 0x74, 0x6C, 0x69)

        private val lz4Decompressor = LZ4Factory.nativeInstance().safeDecompressor()

        fun lzmaDecompress(data: ByteArray): ByteArray {
            val output = ByteArrayOutputStream()
            return LZMACompressorInputStream(ByteArrayInputStream(data)).use { lzma ->
                val buf = ByteArray(32768)
                var bytesRead = 0
                while (lzma.read(buf).also { bytesRead += it } > 0) {
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
                val buf = ByteArray(32768)
                var bytesRead: Int
                while (gzip.read(buf).also { bytesRead = it } > 0) {
                    output.write(buf, 0, bytesRead)
                }
                output.toByteArray()
            }
        }

        fun brotliDecompress(data: ByteArray): ByteArray {
            val output = ByteArrayOutputStream()
            return BrotliCompressorInputStream(ByteArrayInputStream(data)).use { brotli ->
                val buf = ByteArray(32768)
                var bytesRead: Int
                while (brotli.read(buf).also { bytesRead = it } > 0) {
                    output.write(buf, 0, bytesRead)
                }
                output.toByteArray()
            }
        }
    }
}
