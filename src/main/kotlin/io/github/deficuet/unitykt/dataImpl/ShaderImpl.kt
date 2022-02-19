package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.PPtr
import io.github.deficuet.unitykt.data.Shader
import io.github.deficuet.unitykt.data.Texture
import io.github.deficuet.unitykt.export.EndianByteArrayWriter
import io.github.deficuet.unitykt.export.smolv.SmolvDecoder
import io.github.deficuet.unitykt.export.spirv.Disassembler
import io.github.deficuet.unitykt.export.spirv.Module
import io.github.deficuet.unitykt.util.*

class ShaderImpl internal constructor(reader: ObjectReader): NamedObjectImpl(reader) {
    val mScript: ByteArray
    val decompressedSize: UInt
    val mSubProgramBlob: ByteArray
    val mParsedForm: SerializedShader?
    val platforms: Array<ShaderCompilerPlatform>
    val offsets: Array<UInt>
    val compressedLengths: Array<UInt>
    val decompressedLengths: Array<UInt>
    val compressedBlob: ByteArray

    init {
        if (unityVersion >= intArrayOf(5, 5)) {
            mParsedForm = SerializedShader(reader)
            platforms = with(reader.readNextUIntArray()) {
                Array(size) { ShaderCompilerPlatform.of(this[it].toInt()) }
            }
            if (unityVersion >= intArrayOf(2019, 3)) {
                offsets = reader.readNestedUIntArray().let {
                    array -> Array(array.size) { array[it][0] }
                }
                compressedLengths = reader.readNestedUIntArray().let {
                    array -> Array(array.size) { array[it][0] }
                }
                decompressedLengths = reader.readNestedUIntArray().let {
                    array -> Array(array.size) { array[it][0] }
                }
            } else {
                offsets = reader.readNextUIntArray()
                compressedLengths = reader.readNextUIntArray()
                decompressedLengths = reader.readNextUIntArray()
            }
            compressedBlob = reader.readNextByteArray()
            reader.alignStream()
            reader.readArrayOf { PPtr<Texture>(reader) }     //m_Dependencies
            if (unityVersion[0] >= 2018) {
                reader.readArrayOf {
                    reader.readAlignedString()
                    PPtr<Shader>(reader)    // m_NonModifiableTextures
                }
            }
            reader += 1     //m_ShaderIsBaked
            reader.alignStream()
            mScript = byteArrayOf()
            decompressedSize = 0u
            mSubProgramBlob = byteArrayOf()
        } else {
            mScript = reader.readNextByteArray()
            reader.alignStream()
            reader.readAlignedString()      //m_PathName
            if (unityVersion >= intArrayOf(5, 3)) {
                decompressedSize = reader.readUInt()
                mSubProgramBlob = reader.readNextByteArray()
            } else {
                decompressedSize = 0u
                mSubProgramBlob = byteArrayOf()
            }
            mParsedForm = null
            platforms = emptyArray()
            offsets = emptyArray()
            compressedLengths = emptyArray()
            decompressedLengths = emptyArray()
            compressedBlob = byteArrayOf()
        }
    }

    val exportString by lazy {
        if (mSubProgramBlob.isNotEmpty()) {
            val decompressed = CompressUtils.lz4Decompress(mSubProgramBlob, decompressedSize.toInt())
            EndianByteArrayReader(decompressed).use { blobReader ->
                val program = ShaderProgram(blobReader, unityVersion)
                return@lazy exportHeader + program.export(mScript.decodeToString())
            }
        }
        if (compressedBlob.isNotEmpty()) {
            return@lazy exportHeader + convertSerializedShader()
        }
        return@lazy exportHeader + mScript.decodeToString()
    }

    private fun convertSerializedShader(): String {
        val programs = Array(platforms.size) {
            val compressedByte = ByteArray(compressedLengths[it].toInt())
            System.arraycopy(
                compressedBlob, offsets[it].toInt(),
                compressedByte, 0, compressedLengths[it].toInt()
            )
            val decompressedByte = CompressUtils.lz4Decompress(compressedByte, decompressedLengths[it].toInt())
            EndianByteArrayReader(decompressedByte, endian = EndianType.LittleEndian).use { blobReader ->
                ShaderProgram(blobReader, unityVersion)
            }
        }
        return StringBuilder().apply {
            append("Shader \"${mParsedForm!!.mName}\" {\n")
            //region convertSerializedProperties
            append("Properties {\n")
            for (prop in mParsedForm.mPropInfo.mProps) {
                prop.toString(this)
            }
            append("}\n")
            //endregion
            for (subShader in mParsedForm.mSubShaders) {
                //region convertSerializedSubShader
                append("SubShader {\n")
                if (subShader.mLOD != 0) {
                    append(" LOD ${subShader.mLOD}\n")
                }
                subShader.mTags.toString(this, 1)
                for (passe in subShader.mPasses) {
                    passe.toString(this, platforms, programs)
                }
                append("}\n")
                //endregion
            }
            if (mParsedForm.mFallbackName.isNotBlank()) {
                append("Fallback \"${mParsedForm.mFallbackName}\"\n")
            }
            if (mParsedForm.mCustomEditorName.isNotBlank()) {
                append("CustomEditor \"${mParsedForm.mCustomEditorName}\"\n")
            }
            append("}")
        }.toString()
    }

    companion object {
        internal const val exportHeader = "" +
                "//////////////////////////////////////////\n" +
                "//\n" +
                "// NOTE: This is *not* a valid shader file\n" +
                "//\n" +
                "//////////////////////////////////////////\n"
    }
}

internal class ShaderProgram(reader: EndianBinaryReader, version: IntArray) {
    private val entrySize = if (version >= intArrayOf(2019, 3)) 12 else 8
    val mSubPrograms = reader.readArrayIndexedOf {
        reader.position = 4L + it * entrySize
        val offset = reader.readInt()
        reader.position = offset.toLong()
        ShaderSubProgram(reader)
    }

    fun export(shader: String): String {
        return exportRegex.replace(shader) {
            mSubPrograms[it.groups[1]!!.value.toInt()].export()
        }
    }

    companion object {
        private val exportRegex = Regex("GpuProgramIndex (.+)")
    }
}

internal class ShaderSubProgram(private val reader: EndianBinaryReader) {
    private val mVersion = reader.readInt()
    private val mProgramType = ShaderGpuProgramType.of(reader.readInt())
    private val mKeywords: Array<String>
    private val mLocalKeywords: Array<String>
    private val mProgramCode: ByteArray

    init {
        reader += if (mVersion >= 201608170) 16 else 12
        mKeywords = reader.readNextStringArray()
        mLocalKeywords = if (mVersion in 201806140 until 202012090) {
            reader.readNextStringArray()
        } else emptyArray()
        mProgramCode = reader.readNextByteArray()
        reader.alignStream()
    }

    fun export(): String {
        val builder = StringBuilder()
        if (mKeywords.isNotEmpty()) {
            builder.append("Keywords { ")
            mKeywords.forEach { builder.append("\"$it\" ") }
            builder.append("}\n")
        }
        if (mLocalKeywords.isNotEmpty()) {
            builder.append("Local Keywords { ")
            mLocalKeywords.forEach { builder.append("\"$it\" ") }
            builder.append("}\n")
        }
        builder.append("\"")
        if (mProgramCode.isNotEmpty()) {
            when (mProgramType) {
                ShaderGpuProgramType.kShaderGpuProgramGLLegacy,
                ShaderGpuProgramType.kShaderGpuProgramGLES31AEP,
                ShaderGpuProgramType.kShaderGpuProgramGLES31,
                ShaderGpuProgramType.kShaderGpuProgramGLES3,
                ShaderGpuProgramType.kShaderGpuProgramGLES,
                ShaderGpuProgramType.kShaderGpuProgramGLCore32,
                ShaderGpuProgramType.kShaderGpuProgramGLCore41,
                ShaderGpuProgramType.kShaderGpuProgramGLCore43 -> {
                    builder.append(mProgramCode.decodeToString(Charsets.UTF_8))
                }
                ShaderGpuProgramType.kShaderGpuProgramDX9VertexSM20,
                ShaderGpuProgramType.kShaderGpuProgramDX9VertexSM30,
                ShaderGpuProgramType.kShaderGpuProgramDX9PixelSM20,
                ShaderGpuProgramType.kShaderGpuProgramDX9PixelSM30 -> {
                    builder.append("// shader disassembly not supported on DXBC")
                }
                ShaderGpuProgramType.kShaderGpuProgramDX10Level9Vertex,
                ShaderGpuProgramType.kShaderGpuProgramDX10Level9Pixel,
                ShaderGpuProgramType.kShaderGpuProgramDX11VertexSM40,
                ShaderGpuProgramType.kShaderGpuProgramDX11VertexSM50,
                ShaderGpuProgramType.kShaderGpuProgramDX11PixelSM40,
                ShaderGpuProgramType.kShaderGpuProgramDX11PixelSM50,
                ShaderGpuProgramType.kShaderGpuProgramDX11GeometrySM40,
                ShaderGpuProgramType.kShaderGpuProgramDX11GeometrySM50,
                ShaderGpuProgramType.kShaderGpuProgramDX11HullSM50,
                ShaderGpuProgramType.kShaderGpuProgramDX11DomainSM50 -> {
                    builder.append("// shader disassembly not supported on DXBC")
                }
                ShaderGpuProgramType.kShaderGpuProgramMetalVS,
                ShaderGpuProgramType.kShaderGpuProgramMetalFS -> {
                    val fourCC = reader.readUInt()
                    if (fourCC == 0xF00DCAFEu) {
                        val offset = reader.readInt()
                        reader.position = offset.toLong()
                    }
                    reader.readStringUntilNull()
                    val buff = reader.read(with(reader) { length - position }.toInt())
                    builder.append(buff.decodeToString(Charsets.UTF_8))
                }
                ShaderGpuProgramType.kShaderGpuProgramSPIRV -> {
                    builder.append(
                        try {
                            mProgramCode.covertToSpirV()
                        } catch (e: Exception) {
                            "// disassembly error ${e.message}\n"
                        }
                    )
                }
                ShaderGpuProgramType.kShaderGpuProgramConsoleVS,
                ShaderGpuProgramType.kShaderGpuProgramConsoleFS,
                ShaderGpuProgramType.kShaderGpuProgramConsoleHS,
                ShaderGpuProgramType.kShaderGpuProgramConsoleDS,
                ShaderGpuProgramType.kShaderGpuProgramConsoleGS -> {
                    builder.append(mProgramCode.decodeToString(Charsets.UTF_8))
                }
                else -> { builder.append("//shader disassembly not supported on $mProgramType") }
            }
        }
        builder.append("\"")
        return builder.toString()
    }

    companion object {
        private fun ByteArray.covertToSpirV(): String {
            val builder = StringBuilder()
            EndianByteArrayReader(this, endian = EndianType.LittleEndian).use { reader ->
                reader += 4
                var minOffset = reader.length
                for (i in 0..4) {
                    if (reader.position >= minOffset) break
                    val offset = reader.readInt()
                    val size = reader.readInt()
                    if (size > 0) {
                        if (offset < minOffset) minOffset = offset.toLong()
                        reader.withMark {
                            position = offset.toLong()
                            val decodedSize = SmolvDecoder.getDecodedBufferSize(reader)
                            if (decodedSize == 0) return "// disassembly error: Invalid SMOL-V shader header"
                            EndianByteArrayWriter(decodedSize, endianType = EndianType.LittleEndian).use { writer ->
                                if (SmolvDecoder.decode(this, size, writer)) {
                                    val module = Module.readFrom(writer.array)
                                    builder.append(Disassembler().disassemble(module))
                                }
                            }
                        }
                    }
                }
            }
            return builder.toString()
        }
    }
}

class Hash128 internal constructor(reader: EndianBinaryReader) {
    val bytes = reader.read(16)
}

class MatrixParameter internal constructor(reader: EndianBinaryReader) {
    val mNameIndex = reader.readInt()
    val mIndex = reader.readInt()
    val mArraySize = reader.readInt()
    val mType = reader.readSByte()
    val mRowCount = reader.readSByte()

    init { reader.alignStream() }
}

class VectorParameter internal constructor(reader: EndianBinaryReader) {
    val mNameIndex = reader.readInt()
    val mIndex = reader.readInt()
    val mArraySize = reader.readInt()
    val mType = reader.readSByte()
    val mDim = reader.readSByte()

    init { reader.alignStream() }
}

class StructParameter internal constructor(reader: EndianBinaryReader) {
    val mMatrixParams: Array<MatrixParameter>
    val mVectorParams: Array<VectorParameter>

    init {
        reader += 16    //m_NameIndex, m_Index, m_ArraySize, m_StructSize: Int
        mVectorParams = reader.readArrayOf { VectorParameter(reader) }
        mMatrixParams = reader.readArrayOf { MatrixParameter(reader) }
    }
}

class SamplerParameter internal constructor(reader: EndianBinaryReader) {
    val sampler = reader.readUInt()
    val bindPoint = reader.readInt()
}

@Suppress("EnumEntryName")
enum class TextureDimension(val id: Int) {
    kTexDimUnknown(-1),
    kTexDimNone(0),
    kTexDimAny(1),
    kTexDim2D(2),
    kTexDim3D(3),
    kTexDimCUBE(4),
    kTexDim2DArray(5),
    kTexDimCubeArray(6),
    kTexDimForce32Bit(Int.MAX_VALUE);

    companion object {
        fun of(value: Int): TextureDimension {
            return values().firstOrNull { it.id == value } ?: kTexDimUnknown
        }
    }
}

class SerializedTextureProperty internal constructor(reader: EndianBinaryReader) {
    val mDefaultName = reader.readAlignedString()
    val mTexDim = TextureDimension.of(reader.readInt())
}

@Suppress("EnumEntryName")
enum class SerializedPropertyType(val id: Int) {
    kColor(0),
    kVector(1),
    kFloat(2),
    kRange(3),
    kTexture(4);

    companion object {
        fun of(value: Int): SerializedPropertyType {
            return values().firstOrNull { it.id == value } ?: kColor
        }
    }
}

class SerializedProperty internal constructor(reader: EndianBinaryReader) {
    val mName = reader.readAlignedString()
    val mDescription = reader.readAlignedString()
    val mAttributes = reader.readNextStringArray()
    val mType = SerializedPropertyType.of(reader.readInt())
    val mFlags = reader.readUInt()
    val mDefValue = reader.readNextFloatArray(4)
    val mDefTexture = SerializedTextureProperty(reader)

    internal fun toString(builder: StringBuilder): StringBuilder {
        for (attribute in mAttributes) {
            builder.append("[$attribute] ")
        }
        builder.append("$mName (\"$mDescription\", ")
        builder.append(
            when (mType) {
                SerializedPropertyType.kColor -> "Color"
                SerializedPropertyType.kVector -> "Vector"
                SerializedPropertyType.kFloat -> "Float"
                SerializedPropertyType.kRange -> "Range(${mDefValue[1]}, ${mDefValue[2]})"
                SerializedPropertyType.kTexture -> {
                    when (mDefTexture.mTexDim) {
                        TextureDimension.kTexDimAny -> "any"
                        TextureDimension.kTexDim2D -> "2D"
                        TextureDimension.kTexDim3D -> "3D"
                        TextureDimension.kTexDimCUBE -> "Cube"
                        TextureDimension.kTexDim2DArray -> "2DArray"
                        TextureDimension.kTexDimCubeArray -> "CubeArray"
                        else -> ""
                    }
                }
            }
        )
        builder.append(") = ")
        builder.append(
            when (mType) {
                SerializedPropertyType.kColor,
                SerializedPropertyType.kVector -> {
                    "(${mDefValue[0]},${mDefValue[1]},${mDefValue[2]},${mDefValue[3]})"
                }
                SerializedPropertyType.kFloat,
                SerializedPropertyType.kRange -> {
                    mDefValue[0]
                }
                SerializedPropertyType.kTexture -> {
                    "\"${mDefTexture.mDefaultName}\" { }"
                }
            }
        )
        builder.append("\n")
        return builder
    }
}

class SerializedProperties internal constructor(reader: EndianBinaryReader) {
    val mProps = reader.readArrayOf { SerializedProperty(reader) }
}

class SerializedShaderFloatValue internal constructor(reader: EndianBinaryReader) {
    val value = reader.readFloat()
    val name = reader.readAlignedString()

    internal fun convertBlendFactor(): String {
        return when (value) {
            0f -> "Zero"
            2f -> "DstColor"
            3f -> "SrcColor"
            4f -> "OneMinusDstColor"
            5f -> "SrcAlpha"
            6f -> "OneMinusSrcColor"
            7f -> "DstAlpha"
            8f -> "OneMinusDstAlpha"
            9f -> "SrcAlphaSaturate"
            10f -> "OneMinusSrcAlpha"
            else -> "One"
        }
    }

    internal fun convertBlendOp(): String {
        return when (value) {
            1f -> "Sub"
            2f -> "RevSub"
            3f -> "Min"
            4f -> "Max"
            5f -> "LogicalClear"
            6f -> "LogicalSet"
            7f -> "LogicalCopy"
            8f -> "LogicalCopyInverted"
            9f -> "LogicalNoop"
            10f -> "LogicalInvert"
            11f -> "LogicalAnd"
            12f -> "LogicalNand"
            13f -> "LogicalOr"
            14f -> "LogicalNor"
            15f -> "LogicalXor"
            16f -> "LogicalEquiv"
            17f -> "LogicalAndReverse"
            18f -> "LogicalAndInverted"
            19f -> "LogicalOrReverse"
            20f -> "LogicalOrInverted"
            else -> "Add"
        }
    }

    internal fun convertStencilOp(): String {
        return when (value) {
            1f -> "Zero"
            2f -> "Replace"
            3f -> "IncrSat"
            4f -> "DecrSat"
            5f -> "Invert"
            6f -> "IncrWrap"
            7f -> "DecrWrap"
            else -> "Keep"
        }
    }

    internal fun convertStencilComp(): String {
        return when (value) {
            0f -> "Disabled"
            1f -> "Never"
            2f -> "Less"
            3f -> "Equal"
            4f -> "LEqual"
            5f -> "Greater"
            6f -> "NotEqual"
            7f -> "GEqual"
            else -> "Always"
        }
    }
}

class SerializedShaderRTBlendState internal constructor(reader: EndianBinaryReader) {
    val srcBlend = SerializedShaderFloatValue(reader)
    val destBlend = SerializedShaderFloatValue(reader)
    val srcBlendAlpha = SerializedShaderFloatValue(reader)
    val destBlendAlpha = SerializedShaderFloatValue(reader)
    val blendOp = SerializedShaderFloatValue(reader)
    val blendOpAlpha = SerializedShaderFloatValue(reader)
    val colMask = SerializedShaderFloatValue(reader)
}

class SerializedStencilOp internal constructor(reader: EndianBinaryReader) {
    val pass = SerializedShaderFloatValue(reader)
    val fail = SerializedShaderFloatValue(reader)
    val zFail = SerializedShaderFloatValue(reader)
    val comp = SerializedShaderFloatValue(reader)

    internal fun toString(builder: StringBuilder, suffix: String): StringBuilder {
        return builder.apply {
            append("   Comp$suffix ${comp.convertStencilComp()}")
            append("   Pass$suffix ${pass.convertStencilOp()}")
            append("   Fail$suffix ${fail.convertStencilOp()}")
            append("   ZFail$suffix ${zFail.convertStencilOp()}")
        }
    }
}

class SerializedShaderVectorValue internal constructor(reader: EndianBinaryReader) {
    val x = SerializedShaderFloatValue(reader)
    val y = SerializedShaderFloatValue(reader)
    val z = SerializedShaderFloatValue(reader)
    val w = SerializedShaderFloatValue(reader)
    val name = reader.readAlignedString()
}

@Suppress("EnumEntryName")
enum class FogMode(val id: Int) {
    kFogUnknown(-1),
    kFogDisabled(0),
    kFogLinear(1),
    kFogExp(2),
    kFogExp2(3);

    companion object {
        fun of(value: Int): FogMode {
            return values().firstOrNull { it.id == value } ?: kFogDisabled
        }
    }
}

class SerializedTagMap internal constructor(reader: EndianBinaryReader) {
    val tags = reader.readArrayOf { with(reader) { readAlignedString() to readAlignedString() } }

    internal fun toString(builder: StringBuilder, indent: Int): StringBuilder {
        if (tags.isNotEmpty()) {
            builder.append(" ".repeat(indent))
            builder.append("Tags { ")
            for (pair in tags) {
                builder.append("\"${pair.first}\" = \"${pair.second}\"")
            }
            builder.append("}\n")
        }
        return builder
    }
}

class SerializedShaderState internal constructor(reader: ObjectReader) {
    val mName = reader.readAlignedString()
    val rtBlend = reader.readArrayOf(8) { SerializedShaderRTBlendState(reader) }
    val rtSeparateBlend = reader.readBool()
    val zClip: SerializedShaderFloatValue?
    val zTest: SerializedShaderFloatValue
    val zWrite: SerializedShaderFloatValue
    val culling: SerializedShaderFloatValue
    val conservative: SerializedShaderFloatValue?
    val offsetFactor: SerializedShaderFloatValue
    val offsetUnits: SerializedShaderFloatValue
    val alphaToMask: SerializedShaderFloatValue
    val stencilOp: SerializedStencilOp
    val stencilOpFront: SerializedStencilOp
    val stencilOpBack: SerializedStencilOp
    val stencilReadMask: SerializedShaderFloatValue
    val stencilWriteMask: SerializedShaderFloatValue
    val stencilRef: SerializedShaderFloatValue
    val fogStart: SerializedShaderFloatValue
    val fogEnd: SerializedShaderFloatValue
    val fogDensity: SerializedShaderFloatValue
    val fogColor: SerializedShaderVectorValue
    val fogMode: FogMode
    val gpuProgramID: Int
    val mTags: SerializedTagMap
    val mLOD: Int
    val lighting: Boolean

    init {
        val version = reader.unityVersion
        reader.alignStream()
        zClip = if (version >= intArrayOf(2017, 2)) {
            SerializedShaderFloatValue(reader)
        } else null
        zTest = SerializedShaderFloatValue(reader)
        zWrite = SerializedShaderFloatValue(reader)
        culling = SerializedShaderFloatValue(reader)
        conservative = if (version[0] >= 2020) SerializedShaderFloatValue(reader) else null
        offsetFactor = SerializedShaderFloatValue(reader)
        offsetUnits = SerializedShaderFloatValue(reader)
        alphaToMask = SerializedShaderFloatValue(reader)
        stencilOp = SerializedStencilOp(reader)
        stencilOpFront = SerializedStencilOp(reader)
        stencilOpBack = SerializedStencilOp(reader)
        stencilReadMask = SerializedShaderFloatValue(reader)
        stencilWriteMask = SerializedShaderFloatValue(reader)
        stencilRef = SerializedShaderFloatValue(reader)
        fogStart = SerializedShaderFloatValue(reader)
        fogEnd = SerializedShaderFloatValue(reader)
        fogDensity = SerializedShaderFloatValue(reader)
        fogColor = SerializedShaderVectorValue(reader)
        fogMode = FogMode.of(reader.readInt())
        gpuProgramID = reader.readInt()
        mTags = SerializedTagMap(reader)
        mLOD = reader.readInt()
        lighting = reader.readBool()
        reader.alignStream()
    }

    internal fun toString(builder: StringBuilder): StringBuilder {
        if (mName.isNotEmpty()) builder.append("  Name \"$mName\"\n")
        if (mLOD != 0) builder.append("  LOD ${mLOD}\n")
        mTags.toString(builder, 2)
        rtBlend.toString(builder)
        if (alphaToMask.value > 0f) builder.append("  AlphaToMask On\n")
        if (zClip?.value != 1f) builder.append("  ZClip Off\n")
        if (zTest.value != 4f) {
            builder.append("  ZTest  ")
            when (zTest.value) {
                0f -> builder.append("Off")
                1f -> builder.append("Never")
                2f -> builder.append("Less")
                3f -> builder.append("Equal")
                5f -> builder.append("Greater")
                6f -> builder.append("NotEqual")
                7f -> builder.append("GEqual")
                8f -> builder.append("Always")
            }
            builder.append("\n")
        }
        if (zWrite.value != 1f) builder.append("  ZWrite Off\n")
        if (culling.value != 2f) {
            builder.append("  Cull ")
            when (culling.value) {
                0f -> builder.append("Off")
                1f -> builder.append("Front")
            }
            builder.append("\n")
        }
        if (offsetFactor.value != 0f || offsetUnits.value != 0f) {
            builder.append("  Offset ${offsetFactor.value}, ${offsetUnits.value}\n")
        }
        if (
            stencilRef.value != 0f ||
            stencilReadMask.value != 255f ||
            stencilWriteMask.value != 255f ||
            stencilOp.pass.value != 0f ||
            stencilOp.fail.value != 0f ||
            stencilOp.zFail.value != 0f ||
            stencilOp.comp.value != 8f ||
            stencilOpFront.pass.value != 0f ||
            stencilOpFront.fail.value != 0f ||
            stencilOpFront.zFail.value != 0f ||
            stencilOpFront.comp.value != 8f ||
            stencilOpBack.pass.value != 0f ||
            stencilOpBack.fail.value != 0f ||
            stencilOpBack.zFail.value != 0f ||
            stencilOpBack.comp.value != 8f
        ) {
            builder.append("  Stencil {\n")
            if (stencilRef.value != 0f) builder.append("   Ref ${stencilRef.value}\n")
            if (stencilReadMask.value != 255f) builder.append("   ReadMask ${stencilReadMask.value}\n")
            if (stencilWriteMask.value != 255f) builder.append("   WriteMask ${stencilWriteMask.value}\n")
            if (
                stencilOp.pass.value != 0f ||
                stencilOp.fail.value != 0f ||
                stencilOp.zFail.value != 0f ||
                stencilOp.comp.value != 8f
            ) {
                stencilOp.toString(builder, "")
            }
            if (
                stencilOpFront.pass.value != 0f ||
                stencilOpFront.fail.value != 0f ||
                stencilOpFront.zFail.value != 0f ||
                stencilOpFront.comp.value != 8f)
            {
                stencilOpFront.toString(builder, "Front")
            }
            if (
                stencilOpBack.pass.value != 0f ||
                stencilOpBack.fail.value != 0f ||
                stencilOpBack.zFail.value != 0f ||
                stencilOpBack.comp.value != 8f)
            {
                stencilOpBack.toString(builder, "Back")
            }
            builder.append("  }\n")
        }
        if (
            fogMode != FogMode.kFogUnknown ||
            fogColor.x.value != 0f ||
            fogColor.y.value != 0f ||
            fogColor.z.value != 0f ||
            fogColor.w.value != 0f ||
            fogDensity.value != 0f ||
            fogStart.value != 0f ||
            fogEnd.value != 0f
        ) {
            builder.append("  Fog {\n")
            if (fogMode != FogMode.kFogUnknown) {
                builder.append("   Mode  ")
                when (fogMode) {
                    FogMode.kFogDisabled -> builder.append("Off")
                    FogMode.kFogLinear -> builder.append("Linear")
                    FogMode.kFogExp -> builder.append("Exp")
                    FogMode.kFogExp2 -> builder.append("Exp2")
                    else -> {  }
                }
                builder.append("\n")
            }
            if (
                fogColor.x.value != 0f ||
                fogColor.y.value != 0f ||
                fogColor.z.value != 0f ||
                fogColor.w.value != 0f
            ) {
                builder.append(
                    "   Color (${fogColor.x.value},${fogColor.y.value},${fogColor.z.value},${fogColor.w.value})\n"
                )
            }
            if (fogDensity.value != 0f) {
                builder.append("   Density ${fogDensity.value}\n")
            }
            if (fogStart.value != 0f || fogEnd.value != 0f) {
                builder.append("   Range ${fogStart.value}, ${fogEnd.value}\n")
            }
            builder.append("\n")
        }
        return builder.apply {
            append("  Lighting ${if (lighting) "On" else "Off"}\n")
            append("  GpuProgramID $gpuProgramID")
        }
    }

    private fun Array<SerializedShaderRTBlendState>.toString(builder: StringBuilder): StringBuilder {
        for (i in indices) {
            val blend = get(i)
            if (
                blend.srcBlend.value != 1f ||
                blend.destBlend.value != 0f ||
                blend.srcBlendAlpha.value != 1f ||
                blend.destBlendAlpha.value != 0f
            ) {
                builder.append("  Blend ")
                if (i != 0 || rtSeparateBlend) {
                    builder.append("$i ")
                }
                builder.append("${blend.srcBlend.convertBlendFactor()} ${blend.destBlend.convertBlendFactor()}")
                if (blend.srcBlendAlpha.value != 1f || blend.destBlendAlpha.value != 0f) {
                    builder.append(
                        ", ${blend.srcBlendAlpha.convertBlendFactor()} ${blend.destBlendAlpha.convertBlendFactor()}"
                    )
                }
                builder.append("\n")
            }
            if (blend.blendOp.value != 0f || blend.blendOpAlpha.value != 0f) {
                builder.append("  BlendOp ")
                if (i != 0 || rtSeparateBlend) {
                    builder.append("$i ")
                }
                builder.append(blend.blendOp.convertBlendOp())
                if (blend.blendOpAlpha.value != 0f) {
                    builder.append(", ${blend.blendOpAlpha.convertBlendOp()}")
                }
                builder.append("\n")
            }
            val value = blend.colMask.value.toInt()
            if (value != 0xF) {
                builder.append("  ColorMask  ")
                if (value == 0) builder.append(0)
                else {
                    when {
                        value.and(0x2) != 0 -> builder.append("R")
                        value.and(0x4) != 0 -> builder.append("G")
                        value.and(0x8) != 0 -> builder.append("B")
                        value.and(0x1) != 0 -> builder.append("A")
                    }
                }
                builder.append(" $i\n")
            }
        }
        return builder
    }
}

class ShaderBindChannel internal constructor(reader: EndianBinaryReader) {
    val source = reader.readSByte()
    val target = reader.readSByte()
}

class ParserBindChannels internal constructor(reader: EndianBinaryReader) {
    val mChannels = reader.readArrayOf { ShaderBindChannel(reader) }
    val mSourceMap: UInt

    init {
        reader.alignStream()
        mSourceMap = reader.readUInt()
    }
}

class TextureParameter internal constructor(reader: ObjectReader) {
    val mNameIndex = reader.readInt()
    val mIndex = reader.readInt()
    val mSamplerIndex = reader.readInt()
    val mDim: Byte

    init {
        if (reader.unityVersion >= intArrayOf(2017, 3)) reader += 1     //m_MultiSampled: Boolean
        mDim = reader.readSByte()
        reader.alignStream()
    }
}

class BufferBinding internal constructor(reader: ObjectReader) {
    val mNameIndex = reader.readInt()
    val mIndex = reader.readInt()
    val mArraySize = if (reader.unityVersion[0] >= 2020) reader.readInt() else 0
}

class ConstantBuffer internal constructor(reader: ObjectReader) {
    val mNameIndex = reader.readInt()
    val mMatrixParams = reader.readArrayOf { MatrixParameter(reader) }
    val mVectorParams = reader.readArrayOf { VectorParameter(reader) }
    val mStructParams = if (reader.unityVersion >= intArrayOf(2017, 3)) {
        reader.readArrayOf { StructParameter(reader) }
    } else emptyArray()
    val mSize = reader.readInt()
    val mIsPartialCB = if (with(reader.unityVersion) {
            (this[0] == 2020 && this >= intArrayOf(2020, 3, 2)) ||
            (this[0] == 2021 && this >= intArrayOf(2021, 1, 4))
    }) {
        reader.readBool()
    } else false

    init { reader.alignStream() }
}

class UAVParameter internal constructor(reader: EndianBinaryReader) {
    val mNameIndex = reader.readInt()
    val mIndex = reader.readInt()
    val mOriginalIndex = reader.readInt()
}

@Suppress("EnumEntryName")
enum class ShaderGpuProgramType(val id: Int) {
    kShaderGpuProgramUnknown(0),
    kShaderGpuProgramGLLegacy(1),
    kShaderGpuProgramGLES31AEP(2),
    kShaderGpuProgramGLES31(3),
    kShaderGpuProgramGLES3(4),
    kShaderGpuProgramGLES(5),
    kShaderGpuProgramGLCore32(6),
    kShaderGpuProgramGLCore41(7),
    kShaderGpuProgramGLCore43(8),
    kShaderGpuProgramDX9VertexSM20(9),
    kShaderGpuProgramDX9VertexSM30(10),
    kShaderGpuProgramDX9PixelSM20(11),
    kShaderGpuProgramDX9PixelSM30(12),
    kShaderGpuProgramDX10Level9Vertex(13),
    kShaderGpuProgramDX10Level9Pixel(14),
    kShaderGpuProgramDX11VertexSM40(15),
    kShaderGpuProgramDX11VertexSM50(16),
    kShaderGpuProgramDX11PixelSM40(17),
    kShaderGpuProgramDX11PixelSM50(18),
    kShaderGpuProgramDX11GeometrySM40(19),
    kShaderGpuProgramDX11GeometrySM50(20),
    kShaderGpuProgramDX11HullSM50(21),
    kShaderGpuProgramDX11DomainSM50(22),
    kShaderGpuProgramMetalVS(23),
    kShaderGpuProgramMetalFS(24),
    kShaderGpuProgramSPIRV(25),
    kShaderGpuProgramConsoleVS(26),
    kShaderGpuProgramConsoleFS(27),
    kShaderGpuProgramConsoleHS(28),
    kShaderGpuProgramConsoleDS(29),
    kShaderGpuProgramConsoleGS(30),
    kShaderGpuProgramRayTracing(31);

    companion object {
        fun of(value: Int): ShaderGpuProgramType {
            return values().firstOrNull { it.id == value  } ?: kShaderGpuProgramUnknown
        }
    }
}

class SerializedProgramParameters internal constructor(reader: ObjectReader) {
    val mVectorParams = reader.readArrayOf { VectorParameter(reader) }
    val mMatrixParams = reader.readArrayOf { MatrixParameter(reader) }
    val mTextureParams = reader.readArrayOf { TextureParameter(reader) }
    val mBufferParams = reader.readArrayOf { BufferBinding(reader) }
    val mConstantBuffers = reader.readArrayOf { ConstantBuffer(reader) }
    val mConstantBufferBindings = reader.readArrayOf { BufferBinding(reader) }
    val mUAVParams = reader.readArrayOf { UAVParameter(reader) }
    val mSamplers = reader.readArrayOf { SamplerParameter(reader) }
}

class SerializedSubProgram internal constructor(reader: ObjectReader) {
    val mBlobIndex = reader.readUInt()
    val mChannels = ParserBindChannels(reader)
    val mKeywordIndices: Array<UShort>
    val mShaderHardwareTier: Byte
    val mGpuProgramType: ShaderGpuProgramType
    val mParameters: SerializedProgramParameters?
    val mVectorParams: Array<VectorParameter>
    val mMatrixParams: Array<MatrixParameter>
    val mTextureParams: Array<TextureParameter>
    val mBufferParams: Array<BufferBinding>
    val mConstantBuffers: Array<ConstantBuffer>
    val mConstantBufferBindings: Array<BufferBinding>
    val mUAVParams: Array<UAVParameter>
    val mSamplers: Array<SamplerParameter>

    init {
        val version = reader.unityVersion
        if (version[0] >= 2019 && version < intArrayOf(2021, 2)) {
            reader.readNextIntArray()   //m_GlobalKeywordIndices
            reader.alignStream()
            reader.readNextIntArray()   //m_LocalKeywordIndices
            reader.alignStream()
            mKeywordIndices = emptyArray()
        } else {
            mKeywordIndices = reader.readNextUShortArray()
            if (version[0] >= 2017) reader.alignStream()
        }
        mShaderHardwareTier = reader.readSByte()
        mGpuProgramType = ShaderGpuProgramType.of(reader.readSByte().toInt())
        reader.alignStream()
        if (
            (version[0] == 2020 && version >= intArrayOf(2020, 3, 2)) ||
            (version[0] == 2021 && version >= intArrayOf(2021, 2, 4))
        ) {
            mParameters = SerializedProgramParameters(reader)
            mVectorParams = emptyArray()
            mMatrixParams = emptyArray()
            mTextureParams = emptyArray()
            mBufferParams = emptyArray()
            mConstantBuffers = emptyArray()
            mConstantBufferBindings = emptyArray()
            mUAVParams = emptyArray()
            mSamplers = emptyArray()
        } else {
            mVectorParams = reader.readArrayOf { VectorParameter(reader) }
            mMatrixParams = reader.readArrayOf { MatrixParameter(reader) }
            mTextureParams = reader.readArrayOf { TextureParameter(reader) }
            mBufferParams = reader.readArrayOf { BufferBinding(reader) }
            mConstantBuffers = reader.readArrayOf { ConstantBuffer(reader) }
            mConstantBufferBindings = reader.readArrayOf { BufferBinding(reader) }
            mUAVParams = reader.readArrayOf { UAVParameter(reader) }
            mSamplers = if (version[0] >= 2017) reader.readArrayOf { SamplerParameter(reader) } else emptyArray()
            mParameters = null
        }
        if (version >= intArrayOf(2017, 2)) {
            reader += if (version[0] >= 2021) 8 else 4      //m_ShaderRequirements: Long/Int
        }
    }
}

class SerializedProgram internal constructor(reader: ObjectReader) {
    val mSubPrograms = reader.readArrayOf { SerializedSubProgram(reader) }
    val mCommonParameters = if (with(reader.unityVersion) {
        (this[0] == 2020 && this >= intArrayOf(2020, 3, 2)) ||
        (this[0] == 2021 && this >= intArrayOf(2021, 1, 4))
    }) SerializedProgramParameters(reader) else null
}

@Suppress("EnumEntryName")
enum class PassType(val id: Int) {
    kPassTypeNormal(0), kPassTypeUse(1), kPassTypeGrab(2);

    companion object {
        fun of(value: Int): PassType {
            return values().firstOrNull { it.id == value } ?: kPassTypeNormal
        }
    }
}

class SerializedPass internal constructor(reader: ObjectReader) {
    val mEditorDataHash: Array<Hash128>
    val mPlatforms: ByteArray
    val mLocalKeywordMask: Array<UShort>
    val mGlobalKeywordMask: Array<UShort>
    val mNameIndices: Array<Pair<String, Int>>
    val mType: PassType
    val mState: SerializedShaderState
    val mProgramMask: UInt
    val progVertex: SerializedProgram
    val progFragment: SerializedProgram
    val progGeometry: SerializedProgram
    val progHull: SerializedProgram
    val progDomain: SerializedProgram
    val progRayTracing: SerializedProgram?
    val mHasInstancingVariant: Boolean
    val mUseName: String
    val mName: String
    val mTextureName: String
    val mTags: SerializedTagMap
    val mSerializedKeywordStateMask: Array<UShort>

    init {
        val version = reader.unityVersion
        if (version >= intArrayOf(2020, 2)) {
            mEditorDataHash = reader.readArrayOf { Hash128(reader) }
            reader.alignStream()
            mPlatforms = reader.readNextByteArray()
            reader.alignStream()
            if (version < intArrayOf(2021, 2)) {
                mLocalKeywordMask = reader.readNextUShortArray()
                reader.alignStream()
                mGlobalKeywordMask = reader.readNextUShortArray()
                reader.alignStream()
            } else {
                mLocalKeywordMask = emptyArray()
                mGlobalKeywordMask = emptyArray()
            }
        } else {
            mEditorDataHash = emptyArray()
            mPlatforms = byteArrayOf()
            mLocalKeywordMask = emptyArray()
            mGlobalKeywordMask = emptyArray()
        }
        mNameIndices = reader.readArrayOf { with(reader) { readAlignedString() to readInt() } }
        mType = PassType.of(reader.readInt())
        mState = SerializedShaderState(reader)
        mProgramMask = reader.readUInt()
        progVertex = SerializedProgram(reader)
        progFragment = SerializedProgram(reader)
        progGeometry = SerializedProgram(reader)
        progHull = SerializedProgram(reader)
        progDomain = SerializedProgram(reader)
        progRayTracing = if (version >= intArrayOf(2019, 3)) SerializedProgram(reader) else null
        mHasInstancingVariant = reader.readBool()
        if (version[0] >= 2018) reader += 1     //m_HasProceduralInstancingVariant: Boolean
        reader.alignStream()
        mUseName = reader.readAlignedString()
        mName = reader.readAlignedString()
        mTextureName = reader.readAlignedString()
        mTags = SerializedTagMap(reader)
        if (version >= intArrayOf(2021, 2)) {
            mSerializedKeywordStateMask = reader.readNextUShortArray()
            reader.alignStream()
        } else {
            mSerializedKeywordStateMask = emptyArray()
        }
    }

    internal fun toString(
        builder: StringBuilder,
        platforms: Array<ShaderCompilerPlatform>,
        shaderPrograms: Array<ShaderProgram>
    ): StringBuilder {
        builder.append(
            when (mType) {
                PassType.kPassTypeNormal -> " Pass "
                PassType.kPassTypeUse -> " UsePass "
                PassType.kPassTypeGrab -> " GrabPass "
            }
        )
        if (mType == PassType.kPassTypeUse) {
            builder.append("\"$mUseName\"\n")
        } else {
            builder.append("{\n")
            if (mType == PassType.kPassTypeGrab) {
                if (mTextureName.isNotEmpty()) {
                    builder.append("  \"${mTextureName}\"\n")
                }
            } else {
                mState.toString(builder)
                if (progVertex.mSubPrograms.isNotEmpty()) {
                    builder.apply {
                        append("Program \"vp\" {\n")
                        progVertex.mSubPrograms.toString(this, platforms, shaderPrograms)
                        append("}\n")
                    }
                }
                if (progFragment.mSubPrograms.isNotEmpty()) {
                    builder.apply {
                        append("Program \"fp\" {\n")
                        progFragment.mSubPrograms.toString(this, platforms, shaderPrograms)
                        append("}\n")
                    }
                }
                if (progGeometry.mSubPrograms.isNotEmpty()) {
                    builder.apply {
                        append("Program \"gp\" {\n")
                        progGeometry.mSubPrograms.toString(this, platforms, shaderPrograms)
                        append("}\n")
                    }
                }
                if (progHull.mSubPrograms.isNotEmpty()) {
                    builder.apply {
                        append("Program \"hp\" {\n")
                        progHull.mSubPrograms.toString(this, platforms, shaderPrograms)
                        append("}\n")
                    }
                }
                if (progDomain.mSubPrograms.isNotEmpty()) {
                    builder.apply {
                        append("Program \"dp\" {\n")
                        progDomain.mSubPrograms.toString(this, platforms, shaderPrograms)
                        append("}\n")
                    }
                }
                if (progRayTracing?.mSubPrograms?.isNotEmpty() == true) {
                    builder.apply {
                        append("Program \"rtp\" {\n")
                        progRayTracing.mSubPrograms.toString(this, platforms, shaderPrograms)
                        append("}\n")
                    }
                }
            }
            builder.append("}\n")
        }
        return builder
    }

    private fun Array<SerializedSubProgram>.toString(
        builder: StringBuilder,
        platforms: Array<ShaderCompilerPlatform>,
        shaderPrograms: Array<ShaderProgram>
    ): StringBuilder {
        for (group in groupBy { it.mBlobIndex }) {
            for (program in group.value.groupBy { it.mGpuProgramType }) {
                for ((i, platform) in platforms.withIndex()) {
                    if (platform.checkProgramUsability(program.key)) {
                        for (subProgram in program.value) {
                            builder.append("SubProgram \"${platform.str} ")
                            if (program.value.size > 1) {
                                builder.append("hw_tier${"%02d"(subProgram.mShaderHardwareTier)} ")
                            }
                            builder.append(
                                "\" {\n" +
                                shaderPrograms[i].mSubPrograms[subProgram.mBlobIndex.toInt()].export() +
                                "\n}\n"
                            )
                        }
                        break
                    }
                }
            }
        }
        return builder
    }
}

class SerializedSubShader internal constructor(reader: ObjectReader) {
    val mPasses = reader.readArrayOf { SerializedPass(reader) }
    val mTags = SerializedTagMap(reader)
    val mLOD = reader.readInt()
}

class SerializedShaderDependency internal constructor(reader: EndianBinaryReader) {
    val from = reader.readAlignedString()
    val to = reader.readAlignedString()
}

class SerializedCustomEditorForRenderPipeline internal constructor(reader: EndianBinaryReader) {
    val customEditorName = reader.readAlignedString()
    val renderPipelineType = reader.readAlignedString()
}

class SerializedShader internal constructor(reader: ObjectReader) {
    val mPropInfo = SerializedProperties(reader)
    val mSubShaders = reader.readArrayOf { SerializedSubShader(reader) }
    val mKeywordNames: Array<String>
    val mKeywordFlags: ByteArray
    val mName: String
    val mCustomEditorName: String
    val mFallbackName: String
    val mDependencies: Array<SerializedShaderDependency>
    val mCustomEditorForRenderPipelines: Array<SerializedCustomEditorForRenderPipeline>
    val mDisableNoSubShadersMessage: Boolean

    init {
        val version = reader.unityVersion
        if (version >= intArrayOf(2021, 2)) {
            mKeywordNames = reader.readNextStringArray()
            mKeywordFlags = reader.readNextByteArray()
            reader.alignStream()
        } else {
            mKeywordNames = emptyArray()
            mKeywordFlags = byteArrayOf()
        }
        mName = reader.readAlignedString()
        mCustomEditorName = reader.readAlignedString()
        mFallbackName = reader.readAlignedString()
        mDependencies = reader.readArrayOf { SerializedShaderDependency(reader) }
        mCustomEditorForRenderPipelines = if (version[0] >= 2021) {
            reader.readArrayOf { SerializedCustomEditorForRenderPipeline(reader) }
        } else emptyArray()
        mDisableNoSubShadersMessage = reader.readBool()
        reader.alignStream()
    }
}

@Suppress("EnumEntryName")
enum class ShaderCompilerPlatform(val id: Int, val str: String = "unknown") {
    kShaderCompPlatformNone(-1),
    kShaderCompPlatformGL(0, "openGL"),
    kShaderCompPlatformD3D9(1, "d3d9"),
    kShaderCompPlatformXbox360(2, "xbox360"),
    kShaderCompPlatformPS3(3, "ps3"),
    kShaderCompPlatformD3D11(4, "d3d11"),
    kShaderCompPlatformGLES20(5, "gles"),
    kShaderCompPlatformNaCl(6, "glesdesktop"),
    kShaderCompPlatformFlash(7, "flash"),
    kShaderCompPlatformD3D11_9x(8, "d3d11_9x"),
    kShaderCompPlatformGLES3Plus(9, "gles3"),
    kShaderCompPlatformPSP2(10, "psp2"),
    kShaderCompPlatformPS4(11, "ps4"),
    kShaderCompPlatformXboxOne(12, "xboxone"),
    kShaderCompPlatformPSM(13, "psm"),
    kShaderCompPlatformMetal(14, "metal"),
    kShaderCompPlatformOpenGLCore(15, "glcore"),
    kShaderCompPlatformN3DS(16, "n3ds"),
    kShaderCompPlatformWiiU(17, "wiiu"),
    kShaderCompPlatformVulkan(18, "vulkan"),
    kShaderCompPlatformSwitch(19, "switch"),
    kShaderCompPlatformXboxOneD3D12(20, "xboxone_d3d12"),
    kShaderCompPlatformGameCoreXboxOne(21, "xboxone"),
    kShaderCompPlatformGameCoreScarlett(22, "xbox_scarlett"),
    kShaderCompPlatformPS5(23, "ps5"),
    kShaderCompPlatformPS5NGGC(24, "ps5_nggc");

    internal fun checkProgramUsability(programType: ShaderGpuProgramType): Boolean {
        return when (this) {
            kShaderCompPlatformGL -> programType == ShaderGpuProgramType.kShaderGpuProgramGLLegacy
            kShaderCompPlatformD3D9 -> {
                programType.equalsAnyOf(
                    ShaderGpuProgramType.kShaderGpuProgramDX9VertexSM20,
                    ShaderGpuProgramType.kShaderGpuProgramDX9VertexSM30,
                    ShaderGpuProgramType.kShaderGpuProgramDX9PixelSM20,
                    ShaderGpuProgramType.kShaderGpuProgramDX9PixelSM30
                )
            }
            kShaderCompPlatformXbox360, kShaderCompPlatformPS3,
            kShaderCompPlatformPSP2, kShaderCompPlatformPS4,
            kShaderCompPlatformXboxOne, kShaderCompPlatformN3DS,
            kShaderCompPlatformWiiU, kShaderCompPlatformSwitch,
            kShaderCompPlatformXboxOneD3D12, kShaderCompPlatformGameCoreXboxOne,
            kShaderCompPlatformGameCoreScarlett, kShaderCompPlatformPS5,
            kShaderCompPlatformPS5NGGC -> {
                programType.equalsAnyOf(
                    ShaderGpuProgramType.kShaderGpuProgramConsoleVS,
                    ShaderGpuProgramType.kShaderGpuProgramConsoleFS,
                    ShaderGpuProgramType.kShaderGpuProgramConsoleHS,
                    ShaderGpuProgramType.kShaderGpuProgramConsoleDS,
                    ShaderGpuProgramType.kShaderGpuProgramConsoleGS
                )
            }
            kShaderCompPlatformD3D11 -> {
                programType.equalsAnyOf(
                    ShaderGpuProgramType.kShaderGpuProgramDX11VertexSM40,
                    ShaderGpuProgramType.kShaderGpuProgramDX11VertexSM50,
                    ShaderGpuProgramType.kShaderGpuProgramDX11PixelSM40,
                    ShaderGpuProgramType.kShaderGpuProgramDX11PixelSM50,
                    ShaderGpuProgramType.kShaderGpuProgramDX11GeometrySM40,
                    ShaderGpuProgramType.kShaderGpuProgramDX11GeometrySM50,
                    ShaderGpuProgramType.kShaderGpuProgramDX11HullSM50,
                    ShaderGpuProgramType.kShaderGpuProgramDX11DomainSM50
                )
            }
            kShaderCompPlatformGLES20 -> programType == ShaderGpuProgramType.kShaderGpuProgramGLES
            kShaderCompPlatformNaCl -> throw UnsupportedFormatException("Unsupported platform")
            kShaderCompPlatformFlash -> throw UnsupportedFormatException("Unsupported platform")
            kShaderCompPlatformD3D11_9x -> {
                programType.equalsAnyOf(
                    ShaderGpuProgramType.kShaderGpuProgramDX10Level9Vertex,
                    ShaderGpuProgramType.kShaderGpuProgramDX10Level9Pixel
                )
            }
            kShaderCompPlatformGLES3Plus -> {
                programType.equalsAnyOf(
                    ShaderGpuProgramType.kShaderGpuProgramGLES31AEP,
                    ShaderGpuProgramType.kShaderGpuProgramGLES31,
                    ShaderGpuProgramType.kShaderGpuProgramGLES3
                )
            }
            kShaderCompPlatformPSM -> throw UnsupportedFormatException("Unknown")
            kShaderCompPlatformMetal -> {
                programType.equalsAnyOf(
                    ShaderGpuProgramType.kShaderGpuProgramMetalVS,
                    ShaderGpuProgramType.kShaderGpuProgramMetalFS
                )
            }
            kShaderCompPlatformOpenGLCore -> {
                programType.equalsAnyOf(
                    ShaderGpuProgramType.kShaderGpuProgramGLCore32,
                    ShaderGpuProgramType.kShaderGpuProgramGLCore41,
                    ShaderGpuProgramType.kShaderGpuProgramGLCore43
                )
            }
            kShaderCompPlatformVulkan -> programType == ShaderGpuProgramType.kShaderGpuProgramSPIRV
            else -> throw UnsupportedFormatException("Unsupported platform")
        }
    }

    companion object {
        fun of(value: Int): ShaderCompilerPlatform {
            return values().firstOrNull { it.id == value } ?: kShaderCompPlatformNone
        }
    }
}