package io.github.deficuet.unitykt

data class ReaderConfig internal constructor(
    /**
     * @see [OffsetMode]
     */
    var offsetMode: OffsetMode = OffsetMode.MANUAL,

    /**
     * Skip specific number of bytes before reading.
     *
     * Works under [OffsetMode.MANUAL] mode only.
     */
    var manualOffset: Long = 0
) {
    companion object {
        val default = ReaderConfig()
    }
}

enum class OffsetMode {
    MANUAL,

    /**
     * Reader will seek to the first non-zero byte automatically.
     */
    AUTO
}
