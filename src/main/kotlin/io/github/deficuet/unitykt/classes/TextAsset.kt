package io.github.deficuet.unitykt.classes

import java.nio.charset.Charset

interface TextAsset: NamedObject {
    val mScript: ByteArray

    fun getText(charset: Charset = Charsets.UTF_8): String
}