package io.github.deficuet.unitykt.util

internal enum class EndianType {
    LittleEndian, BigEndian
}

internal abstract class EndianBinaryReader(
    private var endian: EndianType = EndianType.BigEndian,
    private var baseOffset: Int = 0
) {
    open var position = 0

    protected abstract val length: Long
    abstract val bytes: ByteArray

    abstract fun read(length: Int): ByteArray
}