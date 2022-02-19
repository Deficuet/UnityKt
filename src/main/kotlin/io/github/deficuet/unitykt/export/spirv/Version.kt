package io.github.deficuet.unitykt.export.spirv

internal data class Version(
    val major: Int = -1,
    val minor: Int = -1,
    val build: Int = -1,
    val revision: Int = -1
) {
    override fun toString(): String {
        return StringBuilder().apply {
            if (major != -1) append(major)
            if (minor != -1) append(".$minor")
            if (build != -1) append(".$build")
            if (revision != -1) append(".$revision")
        }.toString()
    }
}
