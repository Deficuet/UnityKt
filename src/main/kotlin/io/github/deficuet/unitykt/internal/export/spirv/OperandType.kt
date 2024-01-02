package io.github.deficuet.unitykt.internal.export.spirv

import io.github.deficuet.unitykt.cast
import io.github.deficuet.unitykt.enums.FlagsAttributeEnum
import io.github.deficuet.unitykt.enums.NumericalEnumMap

internal data class ReadValueResult(val wordsUsed: Int, val value: Any)

internal open class OperandType {
    open fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(1, this::class.java)
    }
}

internal open class Literal: OperandType()

internal open class LiteralNumber: Literal()

internal class LiteralInteger: LiteralNumber() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(1, words[index])
    }
}

internal class LiteralString: Literal() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        var wordsUsed = 1; var bytesUsed = 0
        val bytes = ByteArray((words.size - index) * 4)
        readWord@
        for (i in index until words.size) {
            val word = words[i]
            for (j in 0..3) {
                val b = word.shr(j * 8).and(0xFFu)
                if (b == 0u) break@readWord
                else bytes[bytesUsed++] = b.toByte()
            }
            wordsUsed++
        }
        return ReadValueResult(wordsUsed, bytes.decodeToString(0, bytesUsed))
    }
}

internal class LiteralContextDependentNumber: Literal()

internal class LiteralExtInstInteger: Literal() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(1, words[index])
    }
}

internal class LiteralSpecConstantOpInteger: Literal() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        val result = mutableListOf<ObjectReference>()
        for (i in index until words.size) {
            result.add(ObjectReference(words[i]))
        }
        return ReadValueResult(words.size - index, result)
    }
}

internal class Parameter(vararg types: OperandType) {
    val operandTypes = types
}

internal open class ParameterFactory {
    open fun createParameter(value: UInt): Parameter? {
        return null
    }

    companion object {
        internal val defaultInst = ParameterFactory()
    }
}

internal class EnumType<E: Enum<E>, P: ParameterFactory>(
    private val enumClass: Class<E>,
    private val enumValues: Array<E>,
    private val enumCompanion: Any,
    private val factory: P
): OperandType() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        var wordsUsed = 0
        val value: EnumOperandValue<E>
        if (FlagsAttributeEnum.flagsAttributeEnumClass.isAssignableFrom(enumClass)) {
            val result = mutableMapOf<UInt, Array<Any>>()
            for (enum in enumValues) {
                val bit = enum.cast<FlagsAttributeEnum>().value
                if (words[index].and(bit) != 0u || (bit == 0u && words[index] == 0u)) {
                    result[bit] = factory.createParameter(bit)?.let { param ->
                        Array(param.operandTypes.size) {
                            val (pWordsUsed, pValue) = param.operandTypes[it].readValue(
                                words, 1 + wordsUsed
                            )
                            wordsUsed += pWordsUsed
                            pValue
                        }
                    } ?: emptyArray()
                }
            }
            value = BitEnumOperandValue(result, enumClass)
        } else {
            val result = factory.createParameter(words[index])?.let { param ->
                Array(param.operandTypes.size) {
                    val (pWordsUsed, pValue) = param.operandTypes[it].readValue(
                        words, 1 + wordsUsed
                    )
                    wordsUsed += pWordsUsed
                    pValue
                }
            } ?: emptyArray()
            value = ValueEnumOperandValue(
                enumCompanion.cast<NumericalEnumMap<UInt, E>>().of(words[index]),
                result, enumClass
            )
        }
        return ReadValueResult(wordsUsed, value)
    }

    companion object {
        fun <E: Enum<E>> new(
            enumClass: Class<E>,
            enumValues: Array<E>,
            enumCompanion: Any
        ): EnumType<E, ParameterFactory> {
            return EnumType(enumClass, enumValues, enumCompanion, ParameterFactory.defaultInst)
        }
    }
}

internal class IdScope: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(1, Scope.of(words[index]))
    }
}

internal class IdMemorySemantics: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(1, MemorySemantics.of(words[index]))
    }
}

internal open class IdType: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(1, words[index])
    }
}

internal class IdResult: IdType() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(1, ObjectReference(words[index]))
    }
}

internal class IdRef: IdType() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(1, ObjectReference(words[index]))
    }
}

internal class IdResultType: IdType()

internal class PairIdRefIdRef: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(2, Pair(ObjectReference(words[index]), ObjectReference(words[index + 1])))
    }
}

internal class PairIdRefLiteralInteger: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(2, Pair(ObjectReference(words[index]), words[index + 1]))
    }
}

internal class PairLiteralIntegerIdRef: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int): ReadValueResult {
        return ReadValueResult(2, Pair(words[index], ObjectReference(words[index + 1])))
    }
}
