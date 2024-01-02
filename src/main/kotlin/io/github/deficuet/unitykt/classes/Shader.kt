package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.enums.NumericalEnum
import io.github.deficuet.unitykt.enums.NumericalEnumCompanion
import io.github.deficuet.unitykt.util.EndianBinaryReader

interface Shader: NamedObject {
    val mScript: ByteArray
    val decompressedSize: UInt
    val mSubProgramBlob: ByteArray
    val mParsedForm: SerializedShader?
    val platforms: Array<out ShaderCompilerPlatform>
    val offsets: Array<out Array<out UInt>>
    val compressedLengths: Array<out Array<out UInt>>
    val decompressedLengths: Array<out Array<out UInt>>
    val compressedBlob: ByteArray

    fun exportToString(): String
}

interface ShaderSubProgramEntry {
    val offset: Int
    val length: Int
    val segment: Int
}

interface ShaderProgram {
    val entries: Array<out ShaderSubProgramEntry>
    val mSubPrograms: Array<out ShaderSubProgram?>

    fun read(reader: EndianBinaryReader, segment: Int)
    fun export(shader: String): String
}

interface ShaderSubProgram {
    val mProgramType: ShaderGpuProgramType
    val mKeywords: Array<out String>
    val mLocalKeywords: Array<out String>
    val mProgramCode: ByteArray

    fun export(): String
}

interface Hash128 {
    val bytes: ByteArray
}

interface MatrixParameter {
    val mNameIndex: Int
    val mIndex: Int
    val mArraySize: Int
    val mType: Byte
    val mRowCount: Byte
}

interface VectorParameter {
    val mNameIndex: Int
    val mIndex: Int
    val mArraySize: Int
    val mType: Byte
    val mDim: Byte
}

interface StructParameter {
    val mMatrixParams: Array<out MatrixParameter>
    val mVectorParams: Array<out VectorParameter>
}

interface SamplerParameter {
    val sampler: UInt
    val bindPoint: Int
}

enum class TextureDimension(override val id: Int): NumericalEnum<Int> {
    Unknown(-1),
    None(0),
    Any(1),
    Tex2D(2),
    Tex3D(3),
    Cube(4),
    Tex2DArray(5),
    CubeArray(6);

    companion object: NumericalEnumCompanion<Int, TextureDimension>(values(), Unknown)
}

interface SerializedTextureProperty {
    val mDefaultName: String
    val mTexDim: TextureDimension
}

enum class SerializedPropertyType(override val id: Int): NumericalEnum<Int> {
    Color(0),
    Vector(1),
    Float(2),
    Range(3),
    Texture(4),
    Integer(5);

    companion object: NumericalEnumCompanion<Int, SerializedPropertyType>(values(), Color)
}

interface SerializedProperty {
    val mName: String
    val mDescription: String
    val mAttributes: Array<out String>
    val mType: SerializedPropertyType
    val mFlags: UInt
    val mDefValue: FloatArray
    val mDefTexture: SerializedTextureProperty
}

interface SerializedProperties {
    val mProps: Array<out SerializedProperty>
}

interface SerializedShaderFloatValue {
    val value: Float
    val name: String
}

interface SerializedShaderRTBlendState  {
    val srcBlend: SerializedShaderFloatValue
    val destBlend: SerializedShaderFloatValue
    val srcBlendAlpha: SerializedShaderFloatValue
    val destBlendAlpha: SerializedShaderFloatValue
    val blendOp: SerializedShaderFloatValue
    val blendOpAlpha: SerializedShaderFloatValue
    val colMask: SerializedShaderFloatValue
}

interface SerializedStencilOp {
    val pass: SerializedShaderFloatValue
    val fail: SerializedShaderFloatValue
    val zFail: SerializedShaderFloatValue
    val comp: SerializedShaderFloatValue
}

interface SerializedShaderVectorValue {
    val x: SerializedShaderFloatValue
    val y: SerializedShaderFloatValue
    val z: SerializedShaderFloatValue
    val w: SerializedShaderFloatValue
    val name: String
}

enum class FogMode(override val id: Int): NumericalEnum<Int> {
    Unknown(-1),
    Disabled(0),
    Linear(1),
    Exp(2),
    Exp2(3);

    companion object: NumericalEnumCompanion<Int, FogMode>(values(), Disabled)
}

interface SerializedTagMap {
    val tags: Array<out Pair<String, String>>
}

interface SerializedShaderState {
    val mName: String
    val rtBlend: Array<out SerializedShaderRTBlendState>
    val rtSeparateBlend: Boolean
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
}

interface ShaderBindChannel {
    val source: Byte
    val target: Byte
}

interface ParserBindChannels {
    val mChannels: Array<out ShaderBindChannel>
    val mSourceMap: UInt
}

interface TextureParameter {
    val mNameIndex: Int
    val mIndex: Int
    val mSamplerIndex: Int
    val mDim: Byte
}

interface BufferBinding {
    val mNameIndex: Int
    val mIndex: Int
    val mArraySize: Int
}

interface ConstantBuffer {
    val mNameIndex: Int
    val mMatrixParams: Array<out MatrixParameter>
    val mVectorParams: Array<out VectorParameter>
    val mStructParams: Array<out StructParameter>
    val mSize: Int
    val mIsPartialCB: Boolean
}

interface UAVParameter {
    val mNameIndex: Int
    val mIndex: Int
    val mOriginalIndex: Int
}

enum class ShaderGpuProgramType(override val id: Int): NumericalEnum<Int> {
    Unknown(0),
    GLLegacy(1),
    GLES31AEP(2),
    GLES31(3),
    GLES3(4),
    GLES(5),
    GLCore32(6),
    GLCore41(7),
    GLCore43(8),
    DX9VertexSM20(9),
    DX9VertexSM30(10),
    DX9PixelSM20(11),
    DX9PixelSM30(12),
    DX10Level9Vertex(13),
    DX10Level9Pixel(14),
    DX11VertexSM40(15),
    DX11VertexSM50(16),
    DX11PixelSM40(17),
    DX11PixelSM50(18),
    DX11GeometrySM40(19),
    DX11GeometrySM50(20),
    DX11HullSM50(21),
    DX11DomainSM50(22),
    MetalVS(23),
    MetalFS(24),
    SPIRV(25),
    ConsoleVS(26),
    ConsoleFS(27),
    ConsoleHS(28),
    ConsoleDS(29),
    ConsoleGS(30),
    RayTracing(31),
    PS5NGGC(32);

    companion object: NumericalEnumCompanion<Int, ShaderGpuProgramType>(values(), Unknown)
}

interface SerializedProgramParameters {
    val mVectorParams: Array<out VectorParameter>
    val mMatrixParams: Array<out MatrixParameter>
    val mTextureParams: Array<out TextureParameter>
    val mBufferParams: Array<out BufferBinding>
    val mConstantBuffers: Array<out ConstantBuffer>
    val mConstantBufferBindings: Array<out BufferBinding>
    val mUAVParams: Array<out UAVParameter>
    val mSamplers: Array<out SamplerParameter>
}

interface SerializedSubProgram {
    val mBlobIndex: UInt
    val mChannels: ParserBindChannels
    val mKeywordIndices: Array<out UShort>
    val mShaderHardwareTier: Byte
    val mGpuProgramType: ShaderGpuProgramType
    val mParameters: SerializedProgramParameters?
    val mVectorParams: Array<out VectorParameter>
    val mMatrixParams: Array<out MatrixParameter>
    val mTextureParams: Array<out TextureParameter>
    val mBufferParams: Array<out BufferBinding>
    val mConstantBuffers: Array<out ConstantBuffer>
    val mConstantBufferBindings: Array<out BufferBinding>
    val mUAVParams: Array<out UAVParameter>
    val mSamplers: Array<out SamplerParameter>
}

interface SerializedProgram {
    val mSubPrograms: Array<out SerializedSubProgram>
    val mCommonParameters: SerializedProgramParameters?
    val mSerializedKeywordStateMask: Array<out UShort>
}

enum class PassType(override val id: Int): NumericalEnum<Int> {
    Normal(0),
    Use(1),
    Grab(2);

    companion object: NumericalEnumCompanion<Int, PassType>(values(), Normal)
}

interface SerializedPass {
    val mEditorDataHash: Array<out Hash128>
    val mPlatforms: ByteArray
    val mLocalKeywordMask: Array<out UShort>
    val mGlobalKeywordMask: Array<out UShort>
    val mNameIndices: Map<String, List<Int>>
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
    val mSerializedKeywordStateMask: Array<out UShort>
}

interface SerializedSubShader {
    val mPasses: Array<out SerializedPass>
    val mTags: SerializedTagMap
    val mLOD: Int
}

interface SerializedShaderDependency {
    val from: String
    val to: String
}

interface SerializedCustomEditorForRenderPipeline {
    val customEditorName: String
    val renderPipelineType: String
}

interface SerializedShader {
    val mPropInfo: SerializedProperties
    val mSubShaders: Array<out SerializedSubShader>
    val mKeywordNames: Array<out String>
    val mKeywordFlags: ByteArray
    val mName: String
    val mCustomEditorName: String
    val mFallbackName: String
    val mDependencies: Array<out SerializedShaderDependency>
    val mCustomEditorForRenderPipelines: Array<out SerializedCustomEditorForRenderPipeline>
    val mDisableNoSubShadersMessage: Boolean
}

@Suppress("EnumEntryName")
enum class ShaderCompilerPlatform(override val id: Int, val str: String = "unknown"): NumericalEnum<Int> {
    None(-1),
    GL(0, "openGL"),
    D3D9(1, "d3d9"),
    Xbox360(2, "xbox360"),
    PS3(3, "ps3"),
    D3D11(4, "d3d11"),
    GLES20(5, "gles"),
    NaCl(6, "glesdesktop"),
    Flash(7, "flash"),
    D3D11_9x(8, "d3d11_9x"),
    GLES3Plus(9, "gles3"),
    PSP2(10, "psp2"),
    PS4(11, "ps4"),
    XboxOne(12, "xboxone"),
    PSM(13, "psm"),
    Metal(14, "metal"),
    OpenGLCore(15, "glcore"),
    N3DS(16, "n3ds"),
    WiiU(17, "wiiu"),
    Vulkan(18, "vulkan"),
    Switch(19, "switch"),
    XboxOneD3D12(20, "xboxone_d3d12"),
    GameCoreXboxOne(21, "xboxone"),
    GameCoreScarlett(22, "xbox_scarlett"),
    PS5(23, "ps5"),
    PS5NGGC(24, "ps5_nggc");

    internal fun checkProgramUsability(programType: ShaderGpuProgramType): Boolean {
        return when (this) {
            GL -> programType == ShaderGpuProgramType.GLLegacy
            D3D9 -> programType in setD3D9

            Xbox360, PS3,
            PSP2, PS4,
            XboxOne, N3DS,
            WiiU, Switch,
            XboxOneD3D12, GameCoreXboxOne,
            GameCoreScarlett, PS5 -> programType in setXbox360ToPS5

            PS5NGGC -> programType == ShaderGpuProgramType.PS5NGGC
            D3D11 -> programType in setD3D11
            GLES20 -> programType == ShaderGpuProgramType.GLES
            NaCl -> false
            Flash -> false
            D3D11_9x -> programType in setD3D11_9x
            GLES3Plus -> programType in setGLES3Plus
            PSM -> false
            Metal -> programType in setMetal
            OpenGLCore -> programType in setOpenGLCore
            Vulkan -> programType == ShaderGpuProgramType.SPIRV
            else -> false
        }
    }

    companion object: NumericalEnumCompanion<Int, ShaderCompilerPlatform>(values(), None) {
        private val setD3D9 = setOf(
            ShaderGpuProgramType.DX9VertexSM20,
            ShaderGpuProgramType.DX9VertexSM30,
            ShaderGpuProgramType.DX9PixelSM20,
            ShaderGpuProgramType.DX9PixelSM30
        )
        private val setXbox360ToPS5 = setOf(
            ShaderGpuProgramType.ConsoleVS,
            ShaderGpuProgramType.ConsoleFS,
            ShaderGpuProgramType.ConsoleHS,
            ShaderGpuProgramType.ConsoleDS,
            ShaderGpuProgramType.ConsoleGS
        )
        private val setD3D11 = setOf(
            ShaderGpuProgramType.DX11VertexSM40,
            ShaderGpuProgramType.DX11VertexSM50,
            ShaderGpuProgramType.DX11PixelSM40,
            ShaderGpuProgramType.DX11PixelSM50,
            ShaderGpuProgramType.DX11GeometrySM40,
            ShaderGpuProgramType.DX11GeometrySM50,
            ShaderGpuProgramType.DX11HullSM50,
            ShaderGpuProgramType.DX11DomainSM50
        )
        private val setD3D11_9x = setOf(
            ShaderGpuProgramType.DX10Level9Vertex,
            ShaderGpuProgramType.DX10Level9Pixel
        )
        private val setGLES3Plus = setOf(
            ShaderGpuProgramType.GLES31AEP,
            ShaderGpuProgramType.GLES31,
            ShaderGpuProgramType.GLES3
        )
        private val setMetal = setOf(
            ShaderGpuProgramType.MetalVS,
            ShaderGpuProgramType.MetalFS
        )
        private val setOpenGLCore = setOf(
            ShaderGpuProgramType.GLCore32,
            ShaderGpuProgramType.GLCore41,
            ShaderGpuProgramType.GLCore43
        )
    }
}
