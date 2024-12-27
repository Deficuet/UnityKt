package io.github.deficuet.unitykt.pptr

import java.io.File

class FileIdentifier private constructor(
    val type: Int,
    val path: String,
    val name: String
) {
    companion object {
        internal fun fromPath(type: Int, path: String) = FileIdentifier(type, path, File(path).name)
        internal fun fromName(type: Int, name: String) = FileIdentifier(type, "", name)
    }
}
