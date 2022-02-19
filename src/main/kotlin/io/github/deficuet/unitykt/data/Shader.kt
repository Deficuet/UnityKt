package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.SerializedShader
import io.github.deficuet.unitykt.dataImpl.ShaderCompilerPlatform
import io.github.deficuet.unitykt.dataImpl.ShaderImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Shader private constructor(
    private val container: ImplementationContainer<ShaderImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { ShaderImpl(ObjectReader(assetFile, info)) })

    val mScript: ByteArray                          get() = container.impl.mScript
    val decompressedSize: UInt                      get() = container.impl.decompressedSize
    val mSubProgramBlob: ByteArray                  get() = container.impl.mSubProgramBlob
    val mParsedForm: SerializedShader?              get() = container.impl.mParsedForm
    val platforms: Array<ShaderCompilerPlatform>    get() = container.impl.platforms
    val offsets: Array<UInt>                        get() = container.impl.offsets
    val compressedLengths: Array<UInt>              get() = container.impl.compressedLengths
    val decompressedLengths: Array<UInt>            get() = container.impl.decompressedLengths
    val compressedBlob: ByteArray                   get() = container.impl.compressedBlob

    val exportString: String                        get() = container.impl.exportString
}