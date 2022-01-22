package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.ShaderImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Shader private constructor(
    private val container: ImplementationContainer<ShaderImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { ShaderImpl(ObjectReader(assetFile, info)) })

    val mScript get() = container.impl.mScript
    val decompressedSize get() = container.impl.decompressedSize
    val mSubProgramBlob get() = container.impl.mSubProgramBlob
    val mParsedForm get() = container.impl.mParsedForm
    val platforms get() = container.impl.platforms
    val offsets get() = container.impl.offsets
    val compressedLengths get() = container.impl.compressedLengths
    val decompressedLengths get() = container.impl.decompressedLengths
    val compressedBlob get() = container.impl.compressedBlob
}