package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.*
import io.github.deficuet.unitykt.internal.export.smolv.SmolvDecoder
import io.github.deficuet.unitykt.internal.export.spirv.Disassembler
import io.github.deficuet.unitykt.internal.export.spirv.Module
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.*
import io.github.deficuet.unitykt.util.decodeToString
import java.nio.ByteOrder
import kotlin.text.Regex
import kotlin.text.StringBuilder
import kotlin.text.format
import kotlin.text.get
import kotlin.text.isNotBlank
import kotlin.text.isNotEmpty
import kotlin.text.repeat
import kotlin.text.toInt

internal class ShaderImpl(
    assetFile: SerializedFile, info: ObjectInfo
): Shader, ShaderFields(assetFile, info) {
    override val mScript: ByteArray get() {
        checkInitialize()
        return fmScript
    }
    override val decompressedSize: UInt get() {
        checkInitialize()
        return fDecompressedSize
    }
    override val mSubProgramBlob: ByteArray get() {
        checkInitialize()
        return fmSubProgramBlob
    }
    override val mParsedForm: SerializedShaderImpl? get() {
        checkInitialize()
        return fmParsedForm
    }
    override val platforms: Array<ShaderCompilerPlatform> get() {
        checkInitialize()
        return fPlatforms
    }
    override val offsets: Array<Array<UInt>> get() {
        checkInitialize()
        return fOffsets
    }
    override val compressedLengths: Array<Array<UInt>> get() {
        checkInitialize()
        return fCompressedLengths
    }
    override val decompressedLengths: Array<Array<UInt>> get() {
        checkInitialize()
        return fDecompressedLengths
    }
    override val compressedBlob: ByteArray get() {
        checkInitialize()
        return fCompressedBlob
    }

    override fun read() {
        super.read()
        if (unityVersion >= intArrayOf(5, 5)) {
            fmParsedForm = SerializedShaderImpl(reader)
            fPlatforms = with(reader.readUInt32Array()) {
                Array(size) { ShaderCompilerPlatform.of(this[it].toInt()) }
            }
            if (unityVersion >= intArrayOf(2019, 3)) {
                fOffsets = reader.readNestedUInt32Array()
                fCompressedLengths = reader.readNestedUInt32Array()
                fDecompressedLengths = reader.readNestedUInt32Array()
            } else {
                fOffsets = arrayOf(reader.readUInt32Array())
                fCompressedLengths = arrayOf(reader.readUInt32Array())
                fDecompressedLengths = arrayOf(reader.readUInt32Array())
            }
            fCompressedBlob = reader.readInt8Array()
            reader.alignStream()
            reader.readArrayOf { PPtrImpl<Shader>(this) }     //m_Dependencies
            if (unityVersion[0] >= 2018) {
                reader.readArrayOf {
                    reader.readAlignedString()
                    PPtrImpl<Texture>(this)    // m_NonModifiableTextures
                }
            }
            reader.skip(1)     //m_ShaderIsBaked
            reader.alignStream()

            fmScript = ByteArray(0)
            fDecompressedSize = 0u
            fmSubProgramBlob = ByteArray(0)
        } else {
            fmScript = reader.readInt8Array()
            reader.alignStream()
            reader.readAlignedString()      //m_PathName
            if (unityVersion >= intArrayOf(5, 3)) {
                fDecompressedSize = reader.readUInt32()
                fmSubProgramBlob = reader.readInt8Array()
            } else {
                fDecompressedSize = 0u
                fmSubProgramBlob = ByteArray(0)
            }
            fmParsedForm = null
            fPlatforms = emptyArray()
            fOffsets = emptyArray()
            fCompressedLengths = emptyArray()
            fDecompressedLengths = emptyArray()
            fCompressedBlob = ByteArray(0)
        }
    }

    override fun exportToString(): String {
        if (mSubProgramBlob.isNotEmpty()) {
            val decompressed = CompressUtils.lz4Decompress(mSubProgramBlob, decompressedSize.toInt())
            EndianByteArrayReader(decompressed).use { blobReader ->
                val program = ShaderProgramImpl(blobReader, unityVersion)
                return exportHeader + program.export(mScript.decodeToString())
            }
        }
        if (compressedBlob.isNotEmpty()) {
            return exportHeader + convertSerializedShader()
        }
        return exportHeader + mScript.decodeToString()
    }

    private fun convertSerializedShader(): String {
        val programsList = mutableListOf<ShaderProgramImpl>()
        for (i in platforms.indices) {
            for (j in offsets[i].indices) {
                val length = compressedLengths[i][j].toInt()
                val compressedByte = ByteArray(length)
                System.arraycopy(
                    compressedBlob, offsets[i][j].toInt(),
                    compressedByte, 0, length
                )
                val decompressedByte = CompressUtils.lz4Decompress(
                    compressedByte, decompressedLengths[i][j].toInt()
                )
                EndianByteArrayReader(decompressedByte, endian = ByteOrder.LITTLE_ENDIAN).use { blobReader ->
                    if (j == 0) {
                        programsList.add(ShaderProgramImpl(blobReader, unityVersion))
                    }
                    programsList[i].read(blobReader, j)
                }
            }
        }
        return StringBuilder().apply {
            append("Shader \"${mParsedForm!!.mName}\" {\n")
            //region convertSerializedProperties
            append("Properties {\n")
            for (prop in mParsedForm!!.mPropInfo.mProps) {
                prop.toString(this)
            }
            append("}\n")
            //endregion
            for (subShader in mParsedForm!!.mSubShaders) {
                //region convertSerializedSubShader
                append("SubShader {\n")
                if (subShader.mLOD != 0) {
                    append(" LOD ${subShader.mLOD}\n")
                }
                subShader.mTags.toString(this, 1)
                for (passe in subShader.mPasses) {
                    passe.toString(this, platforms, programsList)
                }
                append("}\n")
                //endregion
            }
            if (mParsedForm!!.mFallbackName.isNotBlank()) {
                append("Fallback \"${mParsedForm!!.mFallbackName}\"\n")
            }
            if (mParsedForm!!.mCustomEditorName.isNotBlank()) {
                append("CustomEditor \"${mParsedForm!!.mCustomEditorName}\"\n")
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

internal class ShaderSubProgramEntryImpl(reader: EndianBinaryReader, version: IntArray): ShaderSubProgramEntry {
    override val offset = reader.readInt32()
    override val length = reader.readInt32()
    override val segment = if (version >= intArrayOf(2019, 3)) reader.readInt32() else 0
}

internal class ShaderProgramImpl(reader: EndianBinaryReader, version: IntArray): ShaderProgram {
    override val entries: Array<ShaderSubProgramEntryImpl> = reader.readArrayOf {
        ShaderSubProgramEntryImpl(this, version)
    }
    override val mSubPrograms = Array<ShaderSubProgramImpl?>(entries.size) { null }

    override fun read(reader: EndianBinaryReader, segment: Int) {
        for ((i, e) in entries.withIndex()) {
            if (e.segment == segment) {
                reader.position = e.offset.toLong()
                mSubPrograms[i] = ShaderSubProgramImpl(reader)
            }
        }
    }

    override fun export(shader: String): String {
        return exportRegex.replace(shader) {
            mSubPrograms[it.groups["index"]!!.value.toInt()]?.export() ?: ""
        }
    }

    companion object {
        private val exportRegex = Regex("GpuProgramIndex (?<index>.+)")
    }
}

internal class ShaderSubProgramImpl(private val reader: EndianBinaryReader): ShaderSubProgram {
    private val mVersion = reader.readInt32()
    override val mProgramType = ShaderGpuProgramType.of(reader.readInt32())
    override val mKeywords: Array<String>
    override val mLocalKeywords: Array<String>
    override val mProgramCode: ByteArray

    init {
        reader.skip(if (mVersion >= 201608170) 16 else 12)
        mKeywords = reader.readAlignedStringArray()
        mLocalKeywords = if (mVersion in 201806140 until 202012090) {
            reader.readAlignedStringArray()
        } else emptyArray()
        mProgramCode = reader.readInt8Array()
        reader.alignStream()
    }

    override fun export(): String {
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
                ShaderGpuProgramType.GLLegacy,
                ShaderGpuProgramType.GLES31AEP,
                ShaderGpuProgramType.GLES31,
                ShaderGpuProgramType.GLES3,
                ShaderGpuProgramType.GLES,
                ShaderGpuProgramType.GLCore32,
                ShaderGpuProgramType.GLCore41,
                ShaderGpuProgramType.GLCore43 -> {
                    builder.append(mProgramCode.decodeToString())
                }
                ShaderGpuProgramType.DX9VertexSM20,
                ShaderGpuProgramType.DX9VertexSM30,
                ShaderGpuProgramType.DX9PixelSM20,
                ShaderGpuProgramType.DX9PixelSM30 -> {
                    builder.append("// shader disassembly not supported on DXBC")
                }
                ShaderGpuProgramType.DX10Level9Vertex,
                ShaderGpuProgramType.DX10Level9Pixel,
                ShaderGpuProgramType.DX11VertexSM40,
                ShaderGpuProgramType.DX11VertexSM50,
                ShaderGpuProgramType.DX11PixelSM40,
                ShaderGpuProgramType.DX11PixelSM50,
                ShaderGpuProgramType.DX11GeometrySM40,
                ShaderGpuProgramType.DX11GeometrySM50,
                ShaderGpuProgramType.DX11HullSM50,
                ShaderGpuProgramType.DX11DomainSM50 -> {
                    builder.append("// shader disassembly not supported on DXBC")
                }
                ShaderGpuProgramType.MetalVS,
                ShaderGpuProgramType.MetalFS -> {
                    val fourCC = reader.readUInt32()
                    if (fourCC == 0xF00DCAFEu) {
                        val offset = reader.readInt32()
                        reader.position = offset.toLong()
                    }
                    reader.readNullString()
                    val buff = reader.read(with(reader) { length - position }.toInt())
                    builder.append(buff.decodeToString())
                }
                ShaderGpuProgramType.SPIRV -> {
                    builder.append(
                        try {
                            mProgramCode.covertToSpirV()
                        } catch (e: Exception) {
                            "// spirv disassembly error ${e.message}\n"
                        }
                    )
                }
                ShaderGpuProgramType.ConsoleVS,
                ShaderGpuProgramType.ConsoleFS,
                ShaderGpuProgramType.ConsoleHS,
                ShaderGpuProgramType.ConsoleDS,
                ShaderGpuProgramType.ConsoleGS -> {
                    builder.append(mProgramCode.decodeToString())
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
            EndianByteArrayReader(this, endian = ByteOrder.LITTLE_ENDIAN).use { reader ->
                reader.skip(4)
                var minOffset = reader.length
                for (i in 0..4) {
                    if (reader.position >= minOffset) break
                    val offset = reader.readInt32()
                    val size = reader.readInt32()
                    if (size > 0) {
                        if (offset < minOffset) minOffset = offset.toLong()
                        reader.withMark {
                            position = offset.toLong()
                            val decodedSize = SmolvDecoder.getDecodedBufferSize(reader)
                            if (decodedSize == 0) {
                                builder.append("\n// disassembly error: Invalid SMOL-V shader header\n")
                                return@withMark
                            }
                            val writer = EndianByteArrayWriter(decodedSize, ByteOrder.LITTLE_ENDIAN)
                            if (SmolvDecoder.decode(this, size, writer)) {
                                val module = Module.readFrom(writer.data)
                                builder.append(Disassembler().disassemble(module))
                            }
                        }
                    }
                }
            }
            return builder.toString()
        }
    }
}

internal class Hash128Impl(reader: EndianBinaryReader): Hash128 {
    override val bytes = reader.read(16)
}

internal class MatrixParameterImpl(reader: EndianBinaryReader): MatrixParameter {
    override val mNameIndex = reader.readInt32()
    override val mIndex = reader.readInt32()
    override val mArraySize = reader.readInt32()
    override val mType = reader.readInt8()
    override val mRowCount = reader.readInt8()

    init { reader.alignStream() }
}

internal class VectorParameterImpl(reader: EndianBinaryReader): VectorParameter {
    override val mNameIndex = reader.readInt32()
    override val mIndex = reader.readInt32()
    override val mArraySize = reader.readInt32()
    override val mType = reader.readInt8()
    override val mDim = reader.readInt8()

    init { reader.alignStream() }
}

internal class StructParameterImpl(reader: EndianBinaryReader): StructParameter {
    override val mMatrixParams: Array<MatrixParameterImpl>
    override val mVectorParams: Array<VectorParameterImpl>

    init {
        reader.skip(16)    //m_NameIndex, m_Index, m_ArraySize, m_StructSize: Int
        mVectorParams = reader.readArrayOf { VectorParameterImpl(this) }
        mMatrixParams = reader.readArrayOf { MatrixParameterImpl(this) }
    }
}

internal class SamplerParameterImpl(reader: EndianBinaryReader): SamplerParameter {
    override val sampler = reader.readUInt32()
    override val bindPoint = reader.readInt32()
}

internal class SerializedTexturePropertyImpl(reader: EndianBinaryReader): SerializedTextureProperty {
    override val mDefaultName = reader.readAlignedString()
    override val mTexDim = TextureDimension.of(reader.readInt32())
}

internal class SerializedPropertyImpl(reader: EndianBinaryReader): SerializedProperty {
    override val mName = reader.readAlignedString()
    override val mDescription = reader.readAlignedString()
    override val mAttributes = reader.readAlignedStringArray()
    override val mType = SerializedPropertyType.of(reader.readInt32())
    override val mFlags = reader.readUInt32()
    override val mDefValue = reader.readFloatArray(4)
    override val mDefTexture = SerializedTexturePropertyImpl(reader)

    internal fun toString(builder: StringBuilder): StringBuilder {
        for (attribute in mAttributes) {
            builder.append("[$attribute] ")
        }
        builder.append("$mName (\"$mDescription\", ")
        builder.append(
            when (mType) {
                SerializedPropertyType.Color -> "Color"
                SerializedPropertyType.Vector -> "Vector"
                SerializedPropertyType.Float -> "Float"
                SerializedPropertyType.Range -> "Range(${mDefValue[1]}, ${mDefValue[2]})"
                SerializedPropertyType.Texture -> {
                    when (mDefTexture.mTexDim) {
                        TextureDimension.Any -> "any"
                        TextureDimension.Tex2D -> "2D"
                        TextureDimension.Tex3D -> "3D"
                        TextureDimension.Cube -> "Cube"
                        TextureDimension.Tex2DArray -> "2DArray"
                        TextureDimension.CubeArray -> "CubeArray"
                        else -> ""
                    }
                }
                SerializedPropertyType.Integer -> ""
            }
        )
        builder.append(") = ")
        builder.append(
            when (mType) {
                SerializedPropertyType.Color,
                SerializedPropertyType.Vector -> {
                    "(${mDefValue[0]},${mDefValue[1]},${mDefValue[2]},${mDefValue[3]})"
                }
                SerializedPropertyType.Float,
                SerializedPropertyType.Range -> {
                    mDefValue[0]
                }
                SerializedPropertyType.Texture -> {
                    "\"${mDefTexture.mDefaultName}\" { }"
                }
                SerializedPropertyType.Integer -> ""
            }
        )
        builder.append("\n")
        return builder
    }
}

internal class SerializedPropertiesImpl(reader: EndianBinaryReader): SerializedProperties {
    override val mProps: Array<SerializedPropertyImpl> = reader.readArrayOf {
        SerializedPropertyImpl(this)
    }
}

internal class SerializedShaderFloatValueImpl(reader: EndianBinaryReader): SerializedShaderFloatValue {
    override val value = reader.readFloat()
    override val name = reader.readAlignedString()

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

internal class SerializedShaderRTBlendStateImpl(reader: EndianBinaryReader): SerializedShaderRTBlendState {
    override val srcBlend = SerializedShaderFloatValueImpl(reader)
    override val destBlend = SerializedShaderFloatValueImpl(reader)
    override val srcBlendAlpha = SerializedShaderFloatValueImpl(reader)
    override val destBlendAlpha = SerializedShaderFloatValueImpl(reader)
    override val blendOp = SerializedShaderFloatValueImpl(reader)
    override val blendOpAlpha = SerializedShaderFloatValueImpl(reader)
    override val colMask = SerializedShaderFloatValueImpl(reader)
}

internal class SerializedStencilOpImpl(reader: EndianBinaryReader): SerializedStencilOp {
    override val pass = SerializedShaderFloatValueImpl(reader)
    override val fail = SerializedShaderFloatValueImpl(reader)
    override val zFail = SerializedShaderFloatValueImpl(reader)
    override val comp = SerializedShaderFloatValueImpl(reader)

    internal fun toString(builder: StringBuilder, suffix: String): StringBuilder {
        return builder.apply {
            append("   Comp$suffix ${comp.convertStencilComp()}\n")
            append("   Pass$suffix ${pass.convertStencilOp()}\n")
            append("   Fail$suffix ${fail.convertStencilOp()}\n")
            append("   ZFail$suffix ${zFail.convertStencilOp()}\n")
        }
    }
}

internal class SerializedShaderVectorValueImpl(reader: EndianBinaryReader): SerializedShaderVectorValue {
    override val x = SerializedShaderFloatValueImpl(reader)
    override val y = SerializedShaderFloatValueImpl(reader)
    override val z = SerializedShaderFloatValueImpl(reader)
    override val w = SerializedShaderFloatValueImpl(reader)
    override val name = reader.readAlignedString()
}

internal class SerializedTagMapImpl(reader: EndianBinaryReader): SerializedTagMap {
    override val tags = reader.readArrayOf { readAlignedString() to readAlignedString() }

    internal fun toString(builder: StringBuilder, indent: Int): StringBuilder {
        if (tags.isNotEmpty()) {
            builder.append(" ".repeat(indent))
            builder.append("Tags { ")
            for (pair in tags) {
                builder.append("\"${pair.first}\" = \"${pair.second}\" ")
            }
            builder.append("}\n")
        }
        return builder
    }
}

internal class SerializedShaderStateImpl(reader: ObjectReader): SerializedShaderState {
    override val mName = reader.readAlignedString()
    override val rtBlend: Array<SerializedShaderRTBlendStateImpl> = reader.readArrayOf(8) {
        SerializedShaderRTBlendStateImpl(this)
    }
    override val rtSeparateBlend = reader.readBool()
    override val zClip: SerializedShaderFloatValueImpl?
    override val zTest: SerializedShaderFloatValueImpl
    override val zWrite: SerializedShaderFloatValueImpl
    override val culling: SerializedShaderFloatValueImpl
    override val conservative: SerializedShaderFloatValueImpl?
    override val offsetFactor: SerializedShaderFloatValueImpl
    override val offsetUnits: SerializedShaderFloatValueImpl
    override val alphaToMask: SerializedShaderFloatValueImpl
    override val stencilOp: SerializedStencilOpImpl
    override val stencilOpFront: SerializedStencilOpImpl
    override val stencilOpBack: SerializedStencilOpImpl
    override val stencilReadMask: SerializedShaderFloatValueImpl
    override val stencilWriteMask: SerializedShaderFloatValueImpl
    override val stencilRef: SerializedShaderFloatValueImpl
    override val fogStart: SerializedShaderFloatValueImpl
    override val fogEnd: SerializedShaderFloatValueImpl
    override val fogDensity: SerializedShaderFloatValueImpl
    override val fogColor: SerializedShaderVectorValueImpl
    override val fogMode: FogMode
    override val gpuProgramID: Int
    override val mTags: SerializedTagMapImpl
    override val mLOD: Int
    override val lighting: Boolean

    init {
        val version = reader.unityVersion
        reader.alignStream()
        zClip = if (version >= intArrayOf(2017, 2)) {
            SerializedShaderFloatValueImpl(reader)
        } else null
        zTest = SerializedShaderFloatValueImpl(reader)
        zWrite = SerializedShaderFloatValueImpl(reader)
        culling = SerializedShaderFloatValueImpl(reader)
        conservative = if (version[0] >= 2020) SerializedShaderFloatValueImpl(reader) else null
        offsetFactor = SerializedShaderFloatValueImpl(reader)
        offsetUnits = SerializedShaderFloatValueImpl(reader)
        alphaToMask = SerializedShaderFloatValueImpl(reader)
        stencilOp = SerializedStencilOpImpl(reader)
        stencilOpFront = SerializedStencilOpImpl(reader)
        stencilOpBack = SerializedStencilOpImpl(reader)
        stencilReadMask = SerializedShaderFloatValueImpl(reader)
        stencilWriteMask = SerializedShaderFloatValueImpl(reader)
        stencilRef = SerializedShaderFloatValueImpl(reader)
        fogStart = SerializedShaderFloatValueImpl(reader)
        fogEnd = SerializedShaderFloatValueImpl(reader)
        fogDensity = SerializedShaderFloatValueImpl(reader)
        fogColor = SerializedShaderVectorValueImpl(reader)
        fogMode = FogMode.of(reader.readInt32())
        gpuProgramID = reader.readInt32()
        mTags = SerializedTagMapImpl(reader)
        mLOD = reader.readInt32()
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
            builder.append("  ZTest ")
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
            fogMode != FogMode.Unknown ||
            fogColor.x.value != 0f ||
            fogColor.y.value != 0f ||
            fogColor.z.value != 0f ||
            fogColor.w.value != 0f ||
            fogDensity.value != 0f ||
            fogStart.value != 0f ||
            fogEnd.value != 0f
        ) {
            builder.append("  Fog {\n")
            if (fogMode != FogMode.Unknown) {
                builder.append("   Mode ")
                when (fogMode) {
                    FogMode.Disabled -> builder.append("Off")
                    FogMode.Linear -> builder.append("Linear")
                    FogMode.Exp -> builder.append("Exp")
                    FogMode.Exp2 -> builder.append("Exp2")
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
            builder.append("  }\n")
        }
        return builder.apply {
            append("  Lighting ${if (lighting) "On" else "Off"}\n")
            append("  GpuProgramID ${gpuProgramID}\n")
        }
    }

    private fun Array<SerializedShaderRTBlendStateImpl>.toString(builder: StringBuilder): StringBuilder {
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
                builder.append("  ColorMask ")
                if (value == 0) builder.append(0)
                else {
                    if (value.and(0x2) != 0) builder.append("R")
                    if (value.and(0x4) != 0) builder.append("G")
                    if (value.and(0x8) != 0) builder.append("B")
                    if (value.and(0x1) != 0) builder.append("A")
                }
                builder.append(" $i\n")
            }
        }
        return builder
    }
}

internal class ShaderBindChannelImpl(reader: EndianBinaryReader): ShaderBindChannel {
    override val source = reader.readInt8()
    override val target = reader.readInt8()
}

internal class ParserBindChannelsImpl(reader: EndianBinaryReader): ParserBindChannels {
    override val mChannels: Array<ShaderBindChannelImpl> = reader.readArrayOf {
        ShaderBindChannelImpl(reader)
    }
    override val mSourceMap: UInt

    init {
        reader.alignStream()
        mSourceMap = reader.readUInt32()
    }
}

internal class TextureParameterImpl(reader: ObjectReader): TextureParameter {
    override val mNameIndex = reader.readInt32()
    override val mIndex = reader.readInt32()
    override val mSamplerIndex = reader.readInt32()
    override val mDim: Byte

    init {
        if (reader.unityVersion >= intArrayOf(2017, 3)) reader.skip(1)     //m_MultiSampled: Boolean
        mDim = reader.readInt8()
        reader.alignStream()
    }
}

internal class BufferBindingImpl(reader: ObjectReader): BufferBinding {
    override val mNameIndex = reader.readInt32()
    override val mIndex = reader.readInt32()
    override val mArraySize = if (reader.unityVersion[0] >= 2020) reader.readInt32() else 0
}

internal class ConstantBufferImpl(reader: ObjectReader): ConstantBuffer {
    override val mNameIndex = reader.readInt32()
    override val mMatrixParams: Array<MatrixParameterImpl> = reader.readArrayOf { MatrixParameterImpl(this) }
    override val mVectorParams: Array<VectorParameterImpl> = reader.readArrayOf { VectorParameterImpl(this) }
    override val mStructParams: Array<StructParameterImpl> = if (reader.unityVersion >= intArrayOf(2017, 3)) {
        reader.readArrayOf { StructParameterImpl(this) }
    } else emptyArray()
    override val mSize = reader.readInt32()
    override val mIsPartialCB = if (
        with(reader.unityVersion) {
            (this[0] == 2020 && this >= intArrayOf(2020, 3, 2)) ||
            (this >= intArrayOf(2021, 1, 4))
        }
    ) {
        reader.readBool()
    } else false

    init { reader.alignStream() }
}

internal class UAVParameterImpl(reader: EndianBinaryReader): UAVParameter {
    override val mNameIndex = reader.readInt32()
    override val mIndex = reader.readInt32()
    override val mOriginalIndex = reader.readInt32()
}

internal class SerializedProgramParametersImpl(reader: ObjectReader): SerializedProgramParameters {
    override val mVectorParams: Array<VectorParameterImpl> = reader.readArrayOf {
        VectorParameterImpl(this)
    }
    override val mMatrixParams: Array<MatrixParameterImpl> = reader.readArrayOf {
        MatrixParameterImpl(this)
    }
    override val mTextureParams: Array<TextureParameterImpl> = reader.readArrayOf {
        TextureParameterImpl(this)
    }
    override val mBufferParams: Array<BufferBindingImpl> = reader.readArrayOf {
        BufferBindingImpl(this)
    }
    override val mConstantBuffers: Array<ConstantBufferImpl> = reader.readArrayOf {
        ConstantBufferImpl(this)
    }
    override val mConstantBufferBindings: Array<BufferBindingImpl> = reader.readArrayOf {
        BufferBindingImpl(this)
    }
    override val mUAVParams: Array<UAVParameterImpl> = reader.readArrayOf {
        UAVParameterImpl(this)
    }
    override val mSamplers: Array<SamplerParameterImpl> = reader.readArrayOf {
        SamplerParameterImpl(this)
    }
}

internal class SerializedSubProgramImpl(reader: ObjectReader): SerializedSubProgram {
    override val mBlobIndex = reader.readUInt32()
    override val mChannels = ParserBindChannelsImpl(reader)
    override val mKeywordIndices: Array<UShort>
    override val mShaderHardwareTier: Byte
    override val mGpuProgramType: ShaderGpuProgramType
    override val mParameters: SerializedProgramParametersImpl?
    override val mVectorParams: Array<VectorParameterImpl>
    override val mMatrixParams: Array<MatrixParameterImpl>
    override val mTextureParams: Array<TextureParameterImpl>
    override val mBufferParams: Array<BufferBindingImpl>
    override val mConstantBuffers: Array<ConstantBufferImpl>
    override val mConstantBufferBindings: Array<BufferBindingImpl>
    override val mUAVParams: Array<UAVParameterImpl>
    override val mSamplers: Array<SamplerParameterImpl>

    init {
        val version = reader.unityVersion
        if (version[0] >= 2019 && version < intArrayOf(2021, 2)) {
            reader.readUInt16Array()   //m_GlobalKeywordIndices
            reader.alignStream()
            reader.readUInt16Array()   //m_LocalKeywordIndices
            reader.alignStream()
            mKeywordIndices = emptyArray()
        } else {
            mKeywordIndices = reader.readUInt16Array()
            if (version[0] >= 2017) reader.alignStream()
        }
        mShaderHardwareTier = reader.readInt8()
        mGpuProgramType = ShaderGpuProgramType.of(reader.readInt8().toInt())
        reader.alignStream()
        if (
            (version[0] == 2020 && version >= intArrayOf(2020, 3, 2)) ||
            (version >= intArrayOf(2021, 1, 1))
        ) {
            mParameters = SerializedProgramParametersImpl(reader)
            mVectorParams = emptyArray()
            mMatrixParams = emptyArray()
            mTextureParams = emptyArray()
            mBufferParams = emptyArray()
            mConstantBuffers = emptyArray()
            mConstantBufferBindings = emptyArray()
            mUAVParams = emptyArray()
            mSamplers = emptyArray()
        } else {
            mVectorParams = reader.readArrayOf { VectorParameterImpl(this) }
            mMatrixParams = reader.readArrayOf { MatrixParameterImpl(this) }
            mTextureParams = reader.readArrayOf { TextureParameterImpl(this) }
            mBufferParams = reader.readArrayOf { BufferBindingImpl(this) }
            mConstantBuffers = reader.readArrayOf { ConstantBufferImpl(this) }
            mConstantBufferBindings = reader.readArrayOf { BufferBindingImpl(this) }
            mUAVParams = reader.readArrayOf { UAVParameterImpl(this) }
            mSamplers = if (version[0] >= 2017) reader.readArrayOf {
                SamplerParameterImpl(this)
            } else emptyArray()
            mParameters = null
        }
        if (version >= intArrayOf(2017, 2)) {
            reader.skip(if (version[0] >= 2021) 8 else 4)      //m_ShaderRequirements: Long/Int
        }
    }
}

internal class SerializedProgramImpl(reader: ObjectReader): SerializedProgram {
    override val mSubPrograms: Array<SerializedSubProgramImpl> = reader.readArrayOf {
        SerializedSubProgramImpl(this)
    }
    override val mCommonParameters = if (with(reader.unityVersion) {
        (this[0] == 2020 && this >= intArrayOf(2020, 3, 2)) ||
        (this >= intArrayOf(2021, 1, 4))
    }) SerializedProgramParametersImpl(reader) else null
    override val mSerializedKeywordStateMask: Array<UShort>

    init {
        if (reader.unityVersion >= intArrayOf(2022, 1)) {
            mSerializedKeywordStateMask = reader.readUInt16Array()
            reader.alignStream()
        } else {
            mSerializedKeywordStateMask = emptyArray()
        }
    }
}

internal class SerializedPassImpl(reader: ObjectReader): SerializedPass {
    override val mEditorDataHash: Array<Hash128Impl>
    override val mPlatforms: ByteArray
    override val mLocalKeywordMask: Array<UShort>
    override val mGlobalKeywordMask: Array<UShort>
    override val mNameIndices: Map<String, List<Int>>
    override val mType: PassType
    override val mState: SerializedShaderStateImpl
    override val mProgramMask: UInt
    override val progVertex: SerializedProgramImpl
    override val progFragment: SerializedProgramImpl
    override val progGeometry: SerializedProgramImpl
    override val progHull: SerializedProgramImpl
    override val progDomain: SerializedProgramImpl
    override val progRayTracing: SerializedProgramImpl?
    override val mHasInstancingVariant: Boolean
    override val mUseName: String
    override val mName: String
    override val mTextureName: String
    override val mTags: SerializedTagMapImpl
    override val mSerializedKeywordStateMask: Array<UShort>

    init {
        val version = reader.unityVersion
        if (version >= intArrayOf(2020, 2)) {
            mEditorDataHash = reader.readArrayOf { Hash128Impl(this) }
            reader.alignStream()
            mPlatforms = reader.readInt8Array()
            reader.alignStream()
            if (version < intArrayOf(2021, 2)) {
                mLocalKeywordMask = reader.readUInt16Array()
                reader.alignStream()
                mGlobalKeywordMask = reader.readUInt16Array()
                reader.alignStream()
            } else {
                mLocalKeywordMask = emptyArray()
                mGlobalKeywordMask = emptyArray()
            }
        } else {
            mEditorDataHash = emptyArray()
            mPlatforms = ByteArray(0)
            mLocalKeywordMask = emptyArray()
            mGlobalKeywordMask = emptyArray()
        }
        mNameIndices = reader.readArrayOf {
            readAlignedString() to readInt32()
        }.groupBy({ it.first }, { it.second })
        mType = PassType.of(reader.readInt32())
        mState = SerializedShaderStateImpl(reader)
        mProgramMask = reader.readUInt32()
        progVertex = SerializedProgramImpl(reader)
        progFragment = SerializedProgramImpl(reader)
        progGeometry = SerializedProgramImpl(reader)
        progHull = SerializedProgramImpl(reader)
        progDomain = SerializedProgramImpl(reader)
        progRayTracing = if (version >= intArrayOf(2019, 3)) SerializedProgramImpl(reader) else null
        mHasInstancingVariant = reader.readBool()
        if (version[0] >= 2018) reader.skip(1)     //m_HasProceduralInstancingVariant: Boolean
        reader.alignStream()
        mUseName = reader.readAlignedString()
        mName = reader.readAlignedString()
        mTextureName = reader.readAlignedString()
        mTags = SerializedTagMapImpl(reader)
        if (version[0] == 2021 && version[1] >= 2) {
            mSerializedKeywordStateMask = reader.readUInt16Array()
            reader.alignStream()
        } else {
            mSerializedKeywordStateMask = emptyArray()
        }
    }

    internal fun toString(
        builder: StringBuilder,
        platforms: Array<ShaderCompilerPlatform>,
        shaderPrograms: List<ShaderProgramImpl>
    ): StringBuilder {
        builder.append(
            when (mType) {
                PassType.Normal -> " Pass "
                PassType.Use -> " UsePass "
                PassType.Grab -> " GrabPass "
            }
        )
        if (mType == PassType.Use) {
            builder.append("\"$mUseName\"\n")
        } else {
            builder.append("{\n")
            if (mType == PassType.Grab) {
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

    private fun Array<SerializedSubProgramImpl>.toString(
        builder: StringBuilder,
        platforms: Array<ShaderCompilerPlatform>,
        shaderPrograms: List<ShaderProgramImpl>
    ): StringBuilder {
        for (group in groupBy { it.mBlobIndex }) {
            for (program in group.value.groupBy { it.mGpuProgramType }) {
                for ((i, platform) in platforms.withIndex()) {
                    if (platform.checkProgramUsability(program.key)) {
                        val isTier = program.value.size > 1
                        for (subProgram in program.value) {
                            builder.append("SubProgram \"${platform.str} ")
                            if (isTier) {
                                builder.append("hw_tier${"%02d".format(subProgram.mShaderHardwareTier)} ")
                            }
                            builder.append("\" {\n")
                            builder.append(shaderPrograms[i].mSubPrograms[subProgram.mBlobIndex.toInt()]?.export())
                            builder.append("\n}\n")
                        }
                        break
                    }
                }
            }
        }
        return builder
    }
}

internal class SerializedSubShaderImpl(reader: ObjectReader): SerializedSubShader {
    override val mPasses: Array<SerializedPassImpl> = reader.readArrayOf { SerializedPassImpl(this) }
    override val mTags = SerializedTagMapImpl(reader)
    override val mLOD = reader.readInt32()
}

internal class SerializedShaderDependencyImpl(reader: EndianBinaryReader): SerializedShaderDependency {
    override val from = reader.readAlignedString()
    override val to = reader.readAlignedString()
}

internal class SerializedCustomEditorForRenderPipelineImpl(reader: EndianBinaryReader): SerializedCustomEditorForRenderPipeline {
    override val customEditorName = reader.readAlignedString()
    override val renderPipelineType = reader.readAlignedString()
}

internal class SerializedShaderImpl(reader: ObjectReader): SerializedShader {
    override val mPropInfo = SerializedPropertiesImpl(reader)
    override val mSubShaders: Array<SerializedSubShaderImpl> = reader.readArrayOf { SerializedSubShaderImpl(this) }
    override val mKeywordNames: Array<String>
    override val mKeywordFlags: ByteArray
    override val mName: String
    override val mCustomEditorName: String
    override val mFallbackName: String
    override val mDependencies: Array<SerializedShaderDependencyImpl>
    override val mCustomEditorForRenderPipelines: Array<SerializedCustomEditorForRenderPipelineImpl>
    override val mDisableNoSubShadersMessage: Boolean

    init {
        val version = reader.unityVersion
        if (version >= intArrayOf(2021, 2)) {
            mKeywordNames = reader.readAlignedStringArray()
            mKeywordFlags = reader.readInt8Array()
            reader.alignStream()
        } else {
            mKeywordNames = emptyArray()
            mKeywordFlags = ByteArray(0)
        }
        mName = reader.readAlignedString()
        mCustomEditorName = reader.readAlignedString()
        mFallbackName = reader.readAlignedString()
        mDependencies = reader.readArrayOf { SerializedShaderDependencyImpl(this) }
        mCustomEditorForRenderPipelines = if (version[0] >= 2021) {
            reader.readArrayOf { SerializedCustomEditorForRenderPipelineImpl(this) }
        } else emptyArray()
        mDisableNoSubShadersMessage = reader.readBool()
        reader.alignStream()
    }
}
