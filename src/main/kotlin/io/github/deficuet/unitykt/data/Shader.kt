package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.EndianBinaryReader
import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class Shader internal constructor(reader: ObjectReader): NamedObject(reader) {
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
                offsets = reader.readNestedUIntArray()[0]
                compressedLengths = reader.readNestedUIntArray()[0]
                decompressedLengths = reader.readNestedUIntArray()[0]
            } else {
                offsets = reader.readNextUIntArray()
                compressedLengths = reader.readNextUIntArray()
                decompressedLengths = reader.readNextUIntArray()
            }
            compressedBlob = reader.readNextByteArray()
            reader.alignStream()
            reader.readArrayOf { PPtr<Shader>(reader) }     //m_Dependencies
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
}

class SerializedProperties internal constructor(reader: EndianBinaryReader) {
    val mProps = reader.readArrayOf { SerializedProperty(reader) }
}

class SerializedShaderFloatValue internal constructor(reader: EndianBinaryReader) {
    val value = reader.readFloat()
    val name = reader.readAlignedString()
}

class SerializedShaderRTBlendState internal constructor(reader: EndianBinaryReader) {
    val secBlend = SerializedShaderFloatValue(reader)
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
    val zDail = SerializedShaderFloatValue(reader)
    val comp = SerializedShaderFloatValue(reader)
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
enum class ShaderGpuProgramType(val id: Byte) {
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
        fun of(value: Byte): ShaderGpuProgramType {
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
        mGpuProgramType = ShaderGpuProgramType.of(reader.readSByte())
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
enum class ShaderCompilerPlatform(val id: Int) {
    kShaderCompPlatformNone(-1),
    kShaderCompPlatformGL(0),
    kShaderCompPlatformD3D9(1),
    kShaderCompPlatformXbox360(2),
    kShaderCompPlatformPS3(3),
    kShaderCompPlatformD3D11(4),
    kShaderCompPlatformGLES20(5),
    kShaderCompPlatformNaCl(6),
    kShaderCompPlatformFlash(7),
    kShaderCompPlatformD3D11_9x(8),
    kShaderCompPlatformGLES3Plus(9),
    kShaderCompPlatformPSP2(10),
    kShaderCompPlatformPS4(11),
    kShaderCompPlatformXboxOne(12),
    kShaderCompPlatformPSM(13),
    kShaderCompPlatformMetal(14),
    kShaderCompPlatformOpenGLCore(15),
    kShaderCompPlatformN3DS(16),
    kShaderCompPlatformWiiU(17),
    kShaderCompPlatformVulkan(18),
    kShaderCompPlatformSwitch(19),
    kShaderCompPlatformXboxOneD3D12(20),
    kShaderCompPlatformGameCoreXboxOne(21),
    kShaderCompPlatformGameCoreScarlett(22),
    kShaderCompPlatformPS5(23),
    kShaderCompPlatformPS5NGGC(24);

    companion object {
        fun of(value: Int): ShaderCompilerPlatform {
            return values().firstOrNull { it.id == value } ?: kShaderCompPlatformNone
        }
    }
}