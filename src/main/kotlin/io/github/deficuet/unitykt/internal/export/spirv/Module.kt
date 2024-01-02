package io.github.deficuet.unitykt.internal.export.spirv

import io.github.deficuet.unitykt.util.EndianByteArrayReader
import java.nio.ByteOrder

internal class Module private constructor(
    val header: Header,
    val instructions: List<ParsedInstruction>
) {
    internal data class Header(
        val versionMajor: Int,
        val versionMinor: Int,
        val generatorName: String,
        val generatorVendor: String,
        val generatorVersion: Int,
        val bound: UInt,
        val reserved: UInt
    )

    private val objects = mutableMapOf<UInt, ParsedInstruction>()

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
                processType(parsedInst)
            }
            parsedInst.resolveResultType(objects)
            val id = parsedInst.getResultId()
            if (id != 0u) {
                objects[id] = parsedInst
            }
            when (parsedInst.instruction.name) {
                "OpSpecConstant", "OpConstant" -> {
                    val t = parsedInst.resultType
                    assert(t is ScalarType)
                    val constant = convertConstant(parsedInst.resultType as ScalarType, parsedInst.words)
                    if (constant != null) {
                        parsedInst.operands[2].value = constant
                    }
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

    private fun processType(p: ParsedInstruction) {
        when(p.instruction.name) {
            "OpTypeInt" -> {
                p.resultType = IntegerType(p.words[2].toInt(), p.words[3] == 1u)
            }
            "OpTypeFloat" -> {
                p.resultType = FloatingPointType(p.words[2].toInt())
            }
            "OpTypeVector" -> {
                p.resultType = VectorType(objects.getValue(p.words[2]).resultType as ScalarType, p.words[3].toInt())
            }
            "OpTypeMatrix" -> {
                p.resultType = MatrixType(objects.getValue(p.words[2]).resultType as VectorType, p.words[3].toInt())
            }
            "OpTypeArray" -> {
                val i = objects.getValue(p.words[3])
                val constant = i.value
                var size = 0
                when (constant) {
                    is UShort -> size = constant.toInt()
                    is UInt -> size = constant.toInt()
                    is ULong -> size = constant.toInt()
                    is Short -> size = constant.toInt()
                    is Int -> size = constant
                    is Long -> size = constant.toInt()
                }
                p.resultType = ArrayType(i.resultType!!, size)
            }
            "OpTypeRuntimeArray" -> {
                p.resultType = RuntimeArrayType()
            }
            "OpTypeBool" -> p.resultType = BoolType()
            "OpTypeOpaque" -> p.resultType = OpaqueType()
            "OpTypeVoid" -> p.resultType = VoidType()
            "OpTypeImage" -> {
                val dim = p.operands[2].getSingleEnumValue<Dim>()
                val isArray = (p.operands[4].value as UInt) != 0u
                val isMultiSampled = (p.operands[5].value as UInt) != 0u
                p.resultType = ImageType(
                    dim, isArray, isMultiSampled,
                    if (p.operands.size > 8) p.operands[8].getSingleEnumValue() else AccessQualifier.ReadOnly
                )
            }
            "OpTypeSampler" -> p.resultType = SamplerType()
            "OpTypeSampledImage" -> p.resultType = SampledImageType(objects.getValue(p.words[2]).resultType as ImageType)
            "OpTypeFunction" -> {
                p.resultType = FunctionType()
            }
            "OpTypeForwardPointer" -> p.resultType = PointerType(StorageClass.of(p.words[2]))
            "OpTypePointer" -> {
                if (p.words[1] in objects) {
                    val pt = p.resultType as PointerType
                    assert(pt.storageClass == StorageClass.of(p.words[2]))
                    pt.resolveForwardReference(objects.getValue(p.words[3]).resultType!!)
                } else {
                    p.resultType = PointerType(StorageClass.of(p.words[2]), objects.getValue(p.words[3]).resultType)
                }
            }
            "OpTypeStruct" -> {
                val member = Array(p.words.size - 2) {
                    objects.getValue(p.words[it + 2]).resultType!!
                }
                p.resultType = StructType(member)
            }
        }
    }

    private fun convertConstant(s: ScalarType, words: Array<UInt>, index: Int = 3): Any? {
        when (s) {
            is IntegerType -> {
                if (s.signed) {
                    when (s.width) {
                        16 -> return words[index].toShort()
                        32 -> return words[index].toInt()
                        64 -> return words[index].toULong().or(words[index + 1].toULong().shl(32)).toLong()
                    }
                } else {
                    when (s.width) {
                        16 -> return words[index].toUShort()
                        32 -> return words[index]
                        64 -> return words[index].toULong().or(words[index + 1].toULong().shl(32))
                    }
                }
                throw IllegalStateException("Can't construct integer literal.")
            }
            is FloatingPointType -> {
                return when (s.width) {
                    32 -> Float.fromBits(words[0].toInt())
                    64 -> Double.fromBits(
                        words[index].toULong().or(words[index + 1].toULong().shl(32)).toLong()
                    )
                    else -> throw IllegalStateException("Can't construct floating point literal.")
                }
            }
        }
        return null
    }

    private val ParsedInstruction.isDebug get() = debugInstructions.contains(instruction.name)

    companion object {
        internal fun readFrom(data: ByteArray): Module {
            val reader = EndianByteArrayReader(data)
            val magic = reader.readUInt32()
            if (magic != Meta.magic) {
                if (
                    magic.shl(24)
                        .or(magic.and(0xFF00u).shl(8))
                        .or(magic.shr(8).and(0xFF00u))
                        .or(magic.shr(24)) != Meta.magic
                ) {
                    throw IllegalStateException("Invalid Spir-V magic number")
                }
                reader.endian = ByteOrder.LITTLE_ENDIAN
            }
            val versionNum = reader.readUInt32()
            val generatorMagic = reader.readUInt32()
            val generatorTool = generatorMagic.shr(16).toInt()
            var vendor = "unknown"; var name = ""
            Meta.tools[generatorTool]?.let {
                vendor = it.vendor
                name = it.toolName
            }
            val header = Header(
                versionNum.shr(16).toInt(),
                versionNum.shr(8).and(0xFFu).toInt(),
                name, vendor, generatorMagic.and(0xFFFFu).toInt(),
                reader.readUInt32(), reader.readUInt32()
            )
            val instructions = mutableListOf<ParsedInstruction>()
            while (reader.position != reader.length) {
                val instructionStart = reader.readUInt32()
                val wordCount = instructionStart.shr(16).toUShort()
                val opCode = instructionStart.and(0xFFFFu).toInt()
                val words = Array(wordCount.toInt()) { 0u }
                words[0] = instructionStart
                for (i in 1 until wordCount.toInt()) {
                    words[i] = reader.readUInt32()
                }
                instructions.add(ParsedInstruction(opCode, words))
            }
            return Module(header, instructions)
        }

        private val debugInstructions = setOf(
            "OpSourceContinued", "OpSource", "OpSourceExtension", "OpName",
            "OpMemberName", "OpString", "OpLine", "OpNoLine", "OpModuleProcessed"
        )
    }
}