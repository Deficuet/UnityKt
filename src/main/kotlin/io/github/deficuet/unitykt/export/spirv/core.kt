@file:Suppress("unused", "EnumEntryName")

package io.github.deficuet.unitykt.export.spirv

internal enum class ImageOperands(
    override val exp: Int, val parameter: Parameter?
): FlagsAttributeEnum<ImageOperands>
{
    None(-1, null),
    Bias(0, Parameter(IdRef())),
    Lod(1, Parameter(IdRef())),
    Grad(2, Parameter(IdRef(), IdRef())),
    ConstOffset(3, Parameter(IdRef())),
    Offset(4, Parameter(IdRef())),
    ConstOffsets(5, Parameter(IdRef())),
    Sample(6, Parameter(IdRef())),
    MinLod(7, Parameter(IdRef()));

    companion object: FlagsAttributeEnumCompanion<ImageOperands>()
}

internal class ImageOperandsParameterFactory: ParameterFactory() {
    override fun createParameter(value: UInt): Parameter? {
        return ImageOperands.values().firstOrNull { it.numeric == value.toInt() }?.parameter
    }
}

internal enum class FPFastMathMode(
    override val exp: Int
): FlagsAttributeEnum<FPFastMathMode> {
    None(-1),
    NotNaN(0),
    NotInf(1),
    NSZ(2),
    AllowRecip(3),
    Fast(4);

    companion object: FlagsAttributeEnumCompanion<FPFastMathMode>()
}

internal class FPFastMathModeParameterFactory: ParameterFactory()

internal enum class SelectionControl(override val exp: Int): FlagsAttributeEnum<SelectionControl> {
    None(-1),
    Flatten(0),
    DontFlatten(1);

    companion object: FlagsAttributeEnumCompanion<SelectionControl>()
}

internal class SelectionControlParameterFactory : ParameterFactory()

internal enum class LoopControl(
    override val exp: Int,
    val parameter: Parameter? = null
): FlagsAttributeEnum<LoopControl> {
    None(-1),
    Unroll(0),
    DontUnroll(1),
    DependencyInfinite(2),
    DependencyLength(3, Parameter(LiteralInteger()));

    companion object: FlagsAttributeEnumCompanion<LoopControl>()
}

internal class LoopControlParameterFactory: ParameterFactory() {
    override fun createParameter(value: UInt): Parameter? {
        return LoopControl.values().firstOrNull { it.numeric == value.toInt() }?.parameter
    }
}

internal enum class FunctionControl(override val exp: Int): FlagsAttributeEnum<FunctionControl> {
    None(-1),
    Inline(0),
    DontInline(1),
    Pure(2),
    Const(3),
}

internal class FunctionControlParameterFactory: ParameterFactory()

internal enum class MemorySemantics(override val exp: Int): FlagsAttributeEnum<MemorySemantics> {
    Relaxed(-1),
    None(-1),
    Acquire(1),
    Release(2),
    AcquireRelease(3),
    SequentiallyConsistent(4),
    UniformMemory(6),
    SubgroupMemory(7),
    WorkgroupMemory(8),
    CrossWorkgroupMemory(9),
    AtomicCounterMemory(10),
    ImageMemory(11);

    companion object: FlagsAttributeEnumCompanion<MemorySemantics>()
}

internal class MemorySemanticsParameterFactory: ParameterFactory()

internal enum class MemoryAccess(
    override val exp: Int,
    val parameter: Parameter? = null
): FlagsAttributeEnum<MemoryAccess> {
    None(-1),
    Volatile(0),
    Aligned(1, Parameter(LiteralInteger())),
    Nontemporal(2);

    companion object: FlagsAttributeEnumCompanion<MemoryAccess>()
}

internal class MemoryAccessParameterFactory: ParameterFactory() {
    override fun createParameter(value: UInt): Parameter? {
        return MemoryAccess.values().firstOrNull { it.numeric == value.toInt() }?.parameter
    }
}

internal enum class KernelProfilingInfo(override val exp: Int): FlagsAttributeEnum<KernelProfilingInfo> {
    None(-1),
    CmdExecTime(0);

    companion object: FlagsAttributeEnumCompanion<KernelProfilingInfo>()
}

internal class KernelProfilingInfoParameterFactory: ParameterFactory()

internal enum class SourceLanguage(override val id: UInt): NumericalEnum<UInt> {
    Unknown(0u),
    ESSL(1u),
    GLSL(2u),
    OpenCL_C(3u),
    OpenCL_CPP(4u),
    HLSL(5u);

    companion object: NumericalEnumCompanion<SourceLanguage, UInt>()
}

internal class SourceLanguageParameterFactory: ParameterFactory()

internal enum class ExecutionModel(override val id: UInt): NumericalEnum<UInt> {
    Vertex(0u),
    TessellationControl(1u),
    TessellationEvaluation(2u),
    Geometry(3u),
    Fragment(4u),
    GLCompute(5u),
    Kernel(6u);

    companion object: NumericalEnumCompanion<ExecutionModel, UInt>()
}
internal class ExecutionModelParameterFactory : ParameterFactory()

internal enum class AddressingModel(override val id: UInt): NumericalEnum<UInt> {
    Logical(0u),
    Physical32(1u),
    Physical64(2u);

    companion object: NumericalEnumCompanion<AddressingModel, UInt>()
}
internal class AddressingModelParameterFactory : ParameterFactory()

internal enum class MemoryModel(override val id: UInt): NumericalEnum<UInt> {
    Simple(0u),
    GLSL450(1u),
    OpenCL(2u);

    companion object: NumericalEnumCompanion<MemoryModel, UInt>()
}
internal class MemoryModelParameterFactory : ParameterFactory()

internal enum class ExecutionMode(
    override val id: UInt,
    val parameter: Parameter? = null
): NumericalEnum<UInt> {
    Invocations(0u, Parameter(LiteralInteger())),
    SpacingEqual(1u),
    SpacingFractionalEven(2u),
    SpacingFractionalOdd(3u),
    VertexOrderCw(4u),
    VertexOrderCcw(5u),
    PixelCenterInteger(6u),
    OriginUpperLeft(7u),
    OriginLowerLeft(8u),
    EarlyFragmentTests(9u),
    PointMode(10u),
    Xfb(11u),
    DepthReplacing(12u),
    DepthGreater(14u),
    DepthLess(15u),
    DepthUnchanged(16u),
    LocalSize(17u, Parameter(LiteralInteger(), LiteralInteger(), LiteralInteger())),
    LocalSizeHint(18u, Parameter(LiteralInteger(), LiteralInteger(), LiteralInteger())),
    InputPoints(19u),
    InputLines(20u),
    InputLinesAdjacency(21u),
    Triangles(22u),
    InputTrianglesAdjacency(23u),
    Quads(24u),
    Isolines(25u),
    OutputVertices(26u, Parameter(LiteralInteger())),
    OutputPoints(27u),
    OutputLineStrip(28u),
    OutputTriangleStrip(29u),
    VecTypeHint(30u, Parameter(LiteralInteger())),
    ContractionOff(31u),
    Initializer(33u),
    Finalizer(34u),
    SubgroupSize(35u, Parameter(LiteralInteger())),
    SubgroupsPerWorkgroup(36u, Parameter(LiteralInteger())),
    SubgroupsPerWorkgroupId(37u, Parameter(IdRef())),
    LocalSizeId(38u, Parameter(IdRef(), IdRef(), IdRef())),
    LocalSizeHintId(39u, Parameter(IdRef())),
    PostDepthCoverage(4446u),
    StencilRefReplacingEXT(5027u);

    companion object: NumericalEnumCompanion<ExecutionMode, UInt>()
}

internal class ExecutionModeParameterFactory: ParameterFactory() {
    override fun createParameter(value: UInt): Parameter? {
        return ExecutionMode.values().firstOrNull { it.id == value }?.parameter
    }
}

internal enum class StorageClass(override val id: UInt): NumericalEnum<UInt> {
    UniformConstant(0u),
    Input(1u),
    Uniform(2u),
    Output(3u),
    Workgroup(4u),
    CrossWorkgroup(5u),
    Private(6u),
    Function(7u),
    Generic(8u),
    PushConstant(9u),
    AtomicCounter(10u),
    Image(11u),
    StorageBuffer(12u);

    companion object: NumericalEnumCompanion<StorageClass, UInt>()
}

internal class StorageClassParameterFactory: ParameterFactory()

internal enum class Dim(override val id: UInt): NumericalEnum<UInt> {
    Dim1D(0u),
    Dim2D(1u),
    Dim3D(2u),
    Cube(3u),
    Rect(4u),
    Buffer(5u),
    SubpassData(6u);

    companion object: NumericalEnumCompanion<Dim, UInt>()
}

internal class DimParameterFactory: ParameterFactory()

internal enum class SamplerAddressingMode(override val id: UInt): NumericalEnum<UInt> {
    None(0u),
    ClampToEdge(1u),
    Clamp(2u),
    Repeat(3u),
    RepeatMirrored(4u);

    companion object: NumericalEnumCompanion<SamplerAddressingMode, UInt>()
}

internal class SamplerAddressingModeParameterFactory: ParameterFactory()

internal enum class SamplerFilterMode(override val id: UInt): NumericalEnum<UInt> {
    Nearest(0u),
    Linear(1u);

    companion object: NumericalEnumCompanion<SamplerFilterMode, UInt>()
}

internal class SamplerFilterModeParameterFactory: ParameterFactory()

internal enum class ImageFormat(override val id: UInt): NumericalEnum<UInt> {
    Unknown(0u),
    Rgba32f(1u),
    Rgba16f(2u),
    R32f(3u),
    Rgba8(4u),
    Rgba8Snorm(5u),
    Rg32f(6u),
    Rg16f(7u),
    R11fG11fB10f(8u),
    R16f(9u),
    Rgba16(10u),
    Rgb10A2(11u),
    Rg16(12u),
    Rg8(13u),
    R16(14u),
    R8(15u),
    Rgba16Snorm(16u),
    Rg16Snorm(17u),
    Rg8Snorm(18u),
    R16Snorm(19u),
    R8Snorm(20u),
    Rgba32i(21u),
    Rgba16i(22u),
    Rgba8i(23u),
    R32i(24u),
    Rg32i(25u),
    Rg16i(26u),
    Rg8i(27u),
    R16i(28u),
    R8i(29u),
    Rgba32ui(30u),
    Rgba16ui(31u),
    Rgba8ui(32u),
    R32ui(33u),
    Rgb10a2ui(34u),
    Rg32ui(35u),
    Rg16ui(36u),
    Rg8ui(37u),
    R16ui(38u),
    R8ui(39u);

    companion object: NumericalEnumCompanion<ImageFormat, UInt>()
}

internal class ImageFormatParameterFactory: ParameterFactory()

internal enum class ImageChannelOrder(override val id: UInt): NumericalEnum<UInt> {
    R(0u),
    A(1u),
    RG(2u),
    RA(3u),
    RGB(4u),
    RGBA(5u),
    BGRA(6u),
    ARGB(7u),
    Intensity(8u),
    Luminance(9u),
    Rx(10u),
    RGx(11u),
    RGBx(12u),
    Depth(13u),
    DepthStencil(14u),
    sRGB(15u),
    sRGBx(16u),
    sRGBA(17u),
    sBGRA(18u),
    ABGR(19u);

    companion object: NumericalEnumCompanion<ImageChannelOrder, UInt>()
}

internal class ImageChannelOrderParameterFactory: ParameterFactory()

internal enum class ImageChannelDataType(override val id: UInt): NumericalEnum<UInt> {
    SnormInt8(0u),
    SnormInt16(1u),
    UnormInt8(2u),
    UnormInt16(3u),
    UnormShort565(4u),
    UnormShort555(5u),
    UnormInt101010(6u),
    SignedInt8(7u),
    SignedInt16(8u),
    SignedInt32(9u),
    UnsignedInt8(10u),
    UnsignedInt16(11u),
    UnsignedInt32(12u),
    HalfFloat(13u),
    Float(14u),
    UnormInt24(15u),
    UnormInt101010_2(16u);

    companion object: NumericalEnumCompanion<ImageChannelDataType, UInt>()
}

internal class ImageChannelDataTypeParameterFactory: ParameterFactory()

internal enum class FPRoundingMode(override val id: UInt): NumericalEnum<UInt> {
    RTE(0u),
    RTZ(1u),
    RTP(2u),
    RTN(3u);

    companion object: NumericalEnumCompanion<FPRoundingMode, UInt>()
}

internal class FPRoundingModeParameterFactory: ParameterFactory()

internal enum class LinkageType(override val id: UInt): NumericalEnum<UInt> {
    Export(0u),
    Import(1u);

    companion object: NumericalEnumCompanion<LinkageType, UInt>()
}

internal class LinkageTypeParameterFactory: ParameterFactory()

internal enum class AccessQualifier(override val id: UInt): NumericalEnum<UInt> {
    ReadOnly(0u),
    WriteOnly(1u),
    ReadWrite(2u);

    companion object: NumericalEnumCompanion<AccessQualifier, UInt>()
}

internal class AccessQualifierParameterFactory: ParameterFactory()

internal enum class FunctionParameterAttribute(override val id: UInt): NumericalEnum<UInt> {
    Zext(0u),
    Sext(1u),
    ByVal(2u),
    Sret(3u),
    NoAlias(4u),
    NoCapture(5u),
    NoWrite(6u),
    NoReadWrite(7u);

    companion object: NumericalEnumCompanion<FunctionParameterAttribute, UInt>()
}

internal class FunctionParameterAttributeParameterFactory: ParameterFactory()

internal enum class BuiltIn(override val id: UInt): NumericalEnum<UInt> {
    Position(0u),
    PointSize(1u),
    ClipDistance(3u),
    CullDistance(4u),
    VertexId(5u),
    InstanceId(6u),
    PrimitiveId(7u),
    InvocationId(8u),
    Layer(9u),
    ViewportIndex(10u),
    TessLevelOuter(11u),
    TessLevelInner(12u),
    TessCoord(13u),
    PatchVertices(14u),
    FragCoord(15u),
    PointCoord(16u),
    FrontFacing(17u),
    SampleId(18u),
    SamplePosition(19u),
    SampleMask(20u),
    FragDepth(22u),
    HelperInvocation(23u),
    NumWorkgroups(24u),
    WorkgroupSize(25u),
    WorkgroupId(26u),
    LocalInvocationId(27u),
    GlobalInvocationId(28u),
    LocalInvocationIndex(29u),
    WorkDim(30u),
    GlobalSize(31u),
    EnqueuedWorkgroupSize(32u),
    GlobalOffset(33u),
    GlobalLinearId(34u),
    SubgroupSize(36u),
    SubgroupMaxSize(37u),
    NumSubgroups(38u),
    NumEnqueuedSubgroups(39u),
    SubgroupId(40u),
    SubgroupLocalInvocationId(41u),
    VertexIndex(42u),
    InstanceIndex(43u),
    SubgroupEqMaskKHR(4416u),
    SubgroupGeMaskKHR(4417u),
    SubgroupGtMaskKHR(4418u),
    SubgroupLeMaskKHR(4419u),
    SubgroupLtMaskKHR(4420u),
    BaseVertex(4424u),
    BaseInstance(4425u),
    DrawIndex(4426u),
    DeviceIndex(4438u),
    ViewIndex(4440u),
    BaryCoordNoPerspAMD(4992u),
    BaryCoordNoPerspCentroidAMD(4993u),
    BaryCoordNoPerspSampleAMD(4994u),
    BaryCoordSmoothAMD(4995u),
    BaryCoordSmoothCentroidAMD(4996u),
    BaryCoordSmoothSampleAMD(4997u),
    BaryCoordPullModelAMD(4998u),
    FragStencilRefEXT(5014u),
    ViewportMaskNV(5253u),
    SecondaryPositionNV(5257u),
    SecondaryViewportMaskNV(5258u),
    PositionPerViewNV(5261u),
    ViewportMaskPerViewNV(5262u);

    companion object: NumericalEnumCompanion<BuiltIn, UInt>()
}

internal class BuiltInParameterFactory: ParameterFactory()

internal enum class Decoration(
    override val id: UInt,
    val parameter: Parameter? = null
): NumericalEnum<UInt> {
    RelaxedPrecision(0u),
    SpecId(1u, Parameter(LiteralInteger())),
    Block(2u),
    BufferBlock(3u),
    RowMajor(4u),
    ColMajor(5u),
    ArrayStride(6u, Parameter(LiteralInteger())),
    MatrixStride(7u, Parameter(LiteralInteger())),
    GLSLShared(8u),
    GLSLPacked(9u),
    CPacked(10u),
    BuiltIn(11u, Parameter(EnumType.create<io.github.deficuet.unitykt.export.spirv.BuiltIn>())),
    NoPerspective(13u),
    Flat(14u),
    Patch(15u),
    Centroid(16u),
    Sample(17u),
    Invariant(18u),
    Restrict(19u),
    Aliased(20u),
    Volatile(21u),
    Constant(22u),
    Coherent(23u),
    NonWritable(24u),
    NonReadable(25u),
    Uniform(26u),
    SaturatedConversion(28u),
    Stream(29u, Parameter(LiteralInteger())),
    Location(30u, Parameter(LiteralInteger())),
    Component(31u, Parameter(LiteralInteger())),
    Index(32u),
    Binding(33u, Parameter(LiteralInteger())),
    DescriptorSet(34u, Parameter(LiteralInteger())),
    Offset(35u, Parameter(LiteralInteger())),
    XfbBuffer(36u, Parameter(LiteralInteger())),
    XfbStride(37u, Parameter(LiteralInteger())),
    FuncParamAttr(38u, Parameter(EnumType.create<FunctionParameterAttribute>())),
    FPRoundingMode(39u, Parameter(EnumType.create<io.github.deficuet.unitykt.export.spirv.FPRoundingMode>())),
    FPFastMathMode(40u, Parameter(EnumType.create<io.github.deficuet.unitykt.export.spirv.FPFastMathMode>())),
    LinkageAttributes(41u, Parameter(LiteralString(), EnumType.create<LinkageType>())),
    NoContraction(42u),
    InputAttachmentIndex(43u, Parameter(LiteralInteger())),
    Alignment(44u, Parameter(LiteralInteger())),
    MaxByteOffset(45u, Parameter(LiteralInteger())),
    AlignmentId(46u, Parameter(IdRef())),
    MaxByteOffsetId(47u, Parameter(IdRef())),
    ExplicitInterpAMD(4999u),
    OverrideCoverageNV(5248u),
    PassthroughNV(5250u),
    ViewportRelativeNV(5252u),
    SecondaryViewportRelativeNV(5256u, Parameter(LiteralInteger()));

    companion object: NumericalEnumCompanion<Decoration, UInt>()
}

internal class DecorationParameterFactory: ParameterFactory() {
    override fun createParameter(value: UInt): Parameter? {
        return Decoration.values().firstOrNull { it.id == value }?.parameter
    }
}

internal enum class Scope(override val id: UInt): NumericalEnum<UInt> {
    CrossDevice(0u),
    Device(1u),
    Workgroup(2u),
    Subgroup(3u),
    Invocation(4u);

    companion object: NumericalEnumCompanion<Scope, UInt>()
}

internal class ScopeParameterFactory: ParameterFactory()

internal enum class GroupOperation(override val id: UInt): NumericalEnum<UInt> {
    Reduce(0u),
    InclusiveScan(1u),
    ExclusiveScan(2u);

    companion object: NumericalEnumCompanion<GroupOperation, UInt>()
}

internal class GroupOperationParameterFactory: ParameterFactory()

internal enum class KernelEnqueueFlags(override val id: UInt): NumericalEnum<UInt> {
    NoWait(0u),
    WaitKernel(1u),
    WaitWorkGroup(2u);

    companion object: NumericalEnumCompanion<KernelEnqueueFlags, UInt>()
}

internal class KernelEnqueueFlagsParameterFactory: ParameterFactory()

internal enum class Capability(override val id: UInt): NumericalEnum<UInt> {
    Matrix(0u),
    Shader(1u),
    Geometry(2u),
    Tessellation(3u),
    Addresses(4u),
    Linkage(5u),
    Kernel(6u),
    Vector16(7u),
    Float16Buffer(8u),
    Float16(9u),
    Float64(10u),
    Int64(11u),
    Int64Atomics(12u),
    ImageBasic(13u),
    ImageReadWrite(14u),
    ImageMipmap(15u),
    Pipes(17u),
    Groups(18u),
    DeviceEnqueue(19u),
    LiteralSampler(20u),
    AtomicStorage(21u),
    Int16(22u),
    TessellationPointSize(23u),
    GeometryPointSize(24u),
    ImageGatherExtended(25u),
    StorageImageMultisample(27u),
    UniformBufferArrayDynamicIndexing(28u),
    SampledImageArrayDynamicIndexing(29u),
    StorageBufferArrayDynamicIndexing(30u),
    StorageImageArrayDynamicIndexing(31u),
    ClipDistance(32u),
    CullDistance(33u),
    ImageCubeArray(34u),
    SampleRateShading(35u),
    ImageRect(36u),
    SampledRect(37u),
    GenericPointer(38u),
    Int8(39u),
    InputAttachment(40u),
    SparseResidency(41u),
    MinLod(42u),
    Sampled1D(43u),
    Image1D(44u),
    SampledCubeArray(45u),
    SampledBuffer(46u),
    ImageBuffer(47u),
    ImageMSArray(48u),
    StorageImageExtendedFormats(49u),
    ImageQuery(50u),
    DerivativeControl(51u),
    InterpolationFunction(52u),
    TransformFeedback(53u),
    GeometryStreams(54u),
    StorageImageReadWithoutFormat(55u),
    StorageImageWriteWithoutFormat(56u),
    MultiViewport(57u),
    SubgroupDispatch(58u),
    NamedBarrier(59u),
    PipeStorage(60u),
    SubgroupBallotKHR(4423u),
    DrawParameters(4427u),
    SubgroupVoteKHR(4431u),
    StorageBuffer16BitAccess(4433u),
    StorageUniformBufferBlock16(4433u),
    UniformAndStorageBuffer16BitAccess(4434u),
    StorageUniform16(4434u),
    StoragePushConstant16(4435u),
    StorageInputOutput16(4436u),
    DeviceGroup(4437u),
    MultiView(4439u),
    VariablePointersStorageBuffer(4441u),
    VariablePointers(4442u),
    AtomicStorageOps(4445u),
    SampleMaskPostDepthCoverage(4447u),
    ImageGatherBiasLodAMD(5009u),
    FragmentMaskAMD(5010u),
    StencilExportEXT(5013u),
    ImageReadWriteLodAMD(5015u),
    SampleMaskOverrideCoverageNV(5249u),
    GeometryShaderPassthroughNV(5251u),
    ShaderViewportIndexLayerEXT(5254u),
    ShaderViewportIndexLayerNV(5254u),
    ShaderViewportMaskNV(5255u),
    ShaderStereoViewNV(5259u),
    PerViewAttributesNV(5260u),
    SubgroupShuffleINTEL(5568u),
    SubgroupBufferBlockIOINTEL(5569u),
    SubgroupImageBlockIOINTEL(5570u);

    companion object: NumericalEnumCompanion<Capability, UInt>()
}

internal class CapabilityParameterFactory: ParameterFactory()