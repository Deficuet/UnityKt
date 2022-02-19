package io.github.deficuet.unitykt.export.spirv

import io.github.deficuet.unitykt.util.Reference

internal open class OperandType {
    open fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = this::class
        return 1
    }
}

internal open class Literal: OperandType()

internal open class LiteralNumber: Literal()

internal class LiteralInteger: LiteralNumber() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = words[index]
        return 1
    }
}

internal class LiteralString: Literal() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        var wordsUsed = 1; var bytesUsed = 0
        val bytes = ByteArray((words.size - index) * 4)
        readWord@
        for (i in index until words.size) {
            val word = words[i]
            for (j in 0..3) {
                val b = word.shr(j * 8).and(0xFFu).toByte()
                if (b == 0.toByte()) break@readWord
                else bytes[bytesUsed++] = b
            }
            wordsUsed++
        }
        value.value = bytes.decodeToString(0, bytesUsed)
        return wordsUsed
    }
}

internal class LiteralContextDependentNumber: Literal()

internal class LiteralExtInstInteger: Literal() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = words[index]
        return 1
    }
}

internal class LiteralSpecConstantOpInteger: Literal() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        val result = mutableListOf<ObjectReference>()
        for (i in index until words.size) {
            result.add(ObjectReference(words[i]))
        }
        value.value = result
        return words.size - index
    }
}

internal class IdScope: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = Scope.of(words[index])
        return 1
    }
}

internal class IdMemorySemantics: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = MemorySemantics.of(words[index].toLong())
        return 1
    }
}

internal open class IdType: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = words[index]
        return 1
    }
}

internal class IdResult: IdType() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = ObjectReference(words[index])
        return 1
    }
}

internal class IdRef: IdType() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = ObjectReference(words[index])
        return 1
    }
}

internal class IdResultType: IdType()

//internal abstract class Parameter {
//    abstract val operandTypes: List<OperandType>
//
//    companion object {
//        operator fun invoke(types: List<OperandType>): Parameter {
//            return object : Parameter() { override val operandTypes = types }
//        }
//    }
//}

internal class PairIdRefIdRef: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = Pair(ObjectReference(words[index]), ObjectReference(words[index + 1]))
        return 2
    }
}

internal class PairIdRefLiteralInteger: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = Pair(ObjectReference(words[index]), words[index + 1])
        return 2
    }
}

internal class PairLiteralIntegerIdRef: OperandType() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        value.value = Pair(words[index], ObjectReference(words[index + 1]))
        return 2
    }
}

internal class Parameter(vararg types: OperandType) {
    val operandTypes = types
}

internal open class ParameterFactory {
    open fun createParameter(value: UInt): Parameter? {
        return null
    }
}