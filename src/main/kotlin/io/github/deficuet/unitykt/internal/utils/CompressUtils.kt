package io.github.deficuet.unitykt.internal.utils

import net.jpountz.lz4.LZ4Factory
import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream

internal class CompressUtils private constructor() {
    internal companion object {
        internal val GZIP_MAGIC = byteArrayOf(0x1F, -0x75)
        internal val BROTLI_MAGIC = byteArrayOf(0x62, 0x72, 0x6F, 0x74, 0x6C, 0x69)

        private val lz4Decompressor = LZ4Factory.nativeInstance().safeDecompressor()

        internal fun lzmaDecompress(data: ByteArray): ByteArray {
            val output = ByteArrayOutputStream()
            return LZMACompressorInputStream(ByteArrayInputStream(data)).use { lzma ->
                lzma.transferTo(output)
                output.toByteArray()
            }
        }

        internal fun lz4Decompress(data: ByteArray, uncompressedSize: Int): ByteArray {
            return lz4Decompressor.decompress(data, uncompressedSize)
        }

        internal fun gzipDecompress(data: ByteArray): ByteArray {
            val output = ByteArrayOutputStream()
            return GZIPInputStream(ByteArrayInputStream(data)).use { gzip ->
                gzip.transferTo(output)
                output.toByteArray()
            }
        }

        internal fun brotliDecompress(data: ByteArray): ByteArray {
            val output = ByteArrayOutputStream()
            return BrotliCompressorInputStream(ByteArrayInputStream(data)).use { brotli ->
                brotli.transferTo(output)
                output.toByteArray()
            }
        }
    }
}