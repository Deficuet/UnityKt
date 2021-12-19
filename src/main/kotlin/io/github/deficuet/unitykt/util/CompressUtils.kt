package io.github.deficuet.unitykt.util

import SevenZip.Compression.LZMA.Decoder
import com.nixxcode.jvmbrotli.dec.BrotliInputStream
import net.jpountz.lz4.LZ4Factory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream

class CompressUtils private constructor() {
    companion object {
        val GZIP_MAGIC = byteArrayOf(0x1F, 0x8B)
        val BROTLI_MAGIC = byteArrayOf("brotli")

        private val lz4Decompressor = LZ4Factory.fastestInstance().fastDecompressor()

        fun lzmaDecompress(data: ByteArray): ByteArray {
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
            val output = ByteArrayOutputStream()
            return with(Decoder()) {
                SetDecoderProperties(props)
                Code(
                    with(data) { ByteArrayInputStream(sliceArray(5 until size)) },
                    output, outSize
                )
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

        fun brotliDecompress(data: ByteArray): ByteArray {
            val output = ByteArrayOutputStream()
            return BrotliInputStream(ByteArrayInputStream(data)).use { brotli ->
                val buf = ByteArray(2048)
                var bytesRead: Int
                while (brotli.read(buf).also { bytesRead = it } > 0) {
                    output.write(buf, 0, bytesRead)
                }
                output.toByteArray()
            }
        }
    }
}