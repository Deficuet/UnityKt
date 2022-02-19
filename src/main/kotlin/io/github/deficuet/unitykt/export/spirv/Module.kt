package io.github.deficuet.unitykt.export.spirv

import io.github.deficuet.unitykt.util.EndianByteArrayReader
import io.github.deficuet.unitykt.util.EndianType

internal class Module private constructor(
    val header: Header,
    val instructions: List<ParsedInstruction>
) {
    data class Header(
        val version: Version,
        val generatorName: String,
        val generatorVendor: String,
        val generatorVersion: Int,
        val bound: UInt,
        val reserved: UInt
    )

    private val objects = mutableMapOf<UInt, ParsedInstruction>()
    private val debugInstructions = setOf(
        "OpSourceContinued", "OpSource", "OpSourceExtension", "OpName",
        "OpMemberName", "OpString", "OpLine", "OpNoLine", "OpModuleProcessed"
    )

    init {
        val debugInstructions = mutableListOf<ParsedInstruction>()
        val entryPoints = mutableListOf<ParsedInstruction>()
        for (parsedInst in instructions) {
            if (parsedInst.isDebug) {
                debugInstructions.add(parsedInst)
                continue
            }
            if (parsedInst.instruction.name == "OpEntryPoint") {
                entryPoints.add(parsedInst)
                continue
            }
            if (parsedInst.instruction.name.startsWith("OpType")) {
                parsedInst.processType()
            }
            parsedInst.resolveResultType(objects)
            if (parsedInst.hasResult) {
                objects[parsedInst.resultId] = parsedInst
            }
            when (parsedInst.instruction.name) {
                "OpSpecConstant", "OpConstant" -> {
                    val t = parsedInst.resultType
                    assert(t is ScalarType)
                    val constant = (parsedInst.resultType as ScalarType).convertConstant(
                        parsedInst.words, 3
                    )
                    parsedInst.operands[2].value = constant
                    parsedInst.value = constant
                }
            }
        }
        for (debugInst in debugInstructions) {
            when (debugInst.instruction.name) {
                "OpMemberName" -> {
                    val t = objects[debugInst.words[1]]!!.resultType as StructType
                    t[debugInst.operands[1].value as UInt] = debugInst.operands[2].value as String
                }
                "OpName" -> {
                    val t = objects[debugInst.words[1]]!!
                    t.name = debugInst.operands[1].value as String
                }
            }
        }
        for (instruction in instructions) {
            instruction.resolveReference(objects)
        }
    }

    private val ParsedInstruction.isDebug get() = debugInstructions.contains(instruction.name)

    private fun ParsedInstruction.processType() {
        when(instruction.name) {
            "OpTypeInt" -> {
                resultType = IntegerType(words[2].toInt(), words[3] == 1u)
            }
            "OpTypeFloat" -> {
                resultType = FloatingPointType(words[2].toInt())
            }
            "OpTypeVector" -> {
                resultType = VectorType(objects[words[2]]!!.resultType as ScalarType, words[3].toInt())
            }
            "OpTypeMatrix" -> {
                resultType = MatrixType(objects[words[2]]!!.resultType as VectorType, words[3].toInt())
            }
            "OpTypeArray" -> {
                val constant = objects[words[3]]!!.value
                var size = 0
                when (constant) {
                    is UShort -> size = constant.toInt()
                    is UInt -> size = constant.toInt()
                    is ULong -> size = constant.toInt()
                    is Short -> size = constant.toInt()
                    is Int -> size = constant
                    is Long -> size = constant.toInt()
                }
                resultType = ArrayType(objects[words[2]]!!.resultType!!, size)
            }
            "OpTypeRuntimeArray" -> {
                resultType = RuntimeArrayType()
            }
            "OpTypeBool" -> resultType = BoolType()
            "OpTypeOpaque" -> resultType = OpaqueType()
            "OpTypeVoid" -> resultType = VoidType()
            "OpTypeImage" -> {
                val dim = operands[2].getSingleEnumValue<Dim>()
                val isArray = (operands[4].value as UInt) != 0u
                val isMultiSampled = (operands[5].value as UInt) != 0u
                resultType = ImageType(
                    dim, isArray, isMultiSampled,
                    if (operands.size > 8) operands[8].getSingleEnumValue() else AccessQualifier.ReadOnly
                )
            }
            "OpTypeSampler" -> resultType = SamplerType()
            "OpTypeSampledImage" -> resultType = SampledImageType(objects[words[2]]!!.resultType as ImageType)
            "OpTypeFunction" -> {
                val parameterTypes = mutableListOf<Type>()
                for (j in 3 until words.size) {
                    parameterTypes.add(objects[words[j]]!!.resultType!!)
                }
                resultType = FunctionType()
            }
            "OpTypeForwardPointer" -> resultType = PointerType(StorageClass.of(words[2]))
            "OpTypePointer" -> {
                if (words[1] in objects) {
                    val pt = resultType as PointerType
                    assert(pt.storageClass == StorageClass.of(words[2]))
                    pt.resolveForwardReference(objects[words[3]]!!.resultType!!)
                } else {
                    resultType = PointerType(StorageClass.of(words[2]), objects[words[3]]!!.resultType)
                }
            }
            "OpTypeStruct" -> {
                val member = mutableListOf<Type>()
                for (j in 2 until words.size) {
                    member.add(objects[words[j]]!!.resultType!!)
                }
                resultType = StructType(member)
            }
        }
    }

    private fun ScalarType.convertConstant(words: Array<UInt>, index: Int): Any {
        when (this) {
            is IntegerType -> {
                if (signed) {
                    when (width) {
                        16 -> return words[index].toShort()
                        32 -> return words[index].toInt()
                        64 -> return words[index].toULong().or(words[index + 1].toULong().shl(32)).toLong()
                    }
                } else {
                    when (width) {
                        16 -> return words[index].toUShort()
                        32 -> return words[index]
                        64 -> return words[index].toULong().or(words[index + 1].toULong().shl(32))
                    }
                }
                throw IllegalStateException("Can't construct integer literal.")
            }
            is FloatingPointType -> {
                return when (width) {
                    32 -> Float.fromBits(words[0].toInt())
                    64 -> Double.fromBits(
                        words[index].toULong().or(words[index + 1].toULong().shl(32)).toLong()
                    )
                    else -> throw IllegalStateException("Can't construct floating point literal.")
                }
            }
        }
        throw IllegalStateException("Can't construct literal constant.")
    }

    companion object {
        fun readFrom(data: ByteArray): Module {
            val reader = EndianByteArrayReader(data, endian = EndianType.LittleEndian)
            reader += 4     //magic
            val versionNum = reader.readUInt()
            val majorVersion = versionNum.shr(16).toInt()
            val minorVersion = versionNum.shr(8).and(0xFFu).toInt()
            val version = Version(majorVersion, minorVersion)
            val generatorMagic = reader.readUInt()
            val generatorTool = generatorMagic.shr(16).toInt()
            var vendor = "unknown"; var name = ""
            Meta.Tools.of(generatorTool)?.let {
                vendor = it.vendor
                name = it.toolName
            }
            val header = Header(
                version, name, vendor, generatorMagic.and(0xFFFFu).toInt(),
                reader.readUInt(), reader.readUInt()
            )
            val instructions = mutableListOf<ParsedInstruction>()
            while (reader.position != reader.length) {
                val instructionStart = reader.readUInt()
                val wordCount = instructionStart.shr(16).toUShort()
                val opCode = instructionStart.and(0xFFFFu).toInt()
                val words = Array(wordCount.toInt()) { 0u }
                words[0] = instructionStart
                for (i in 1 until wordCount.toInt()) {
                    words[i] = reader.readUInt()
                }
                instructions.add(ParsedInstruction(opCode, words))
            }
            return Module(header, instructions)
        }
    }
}