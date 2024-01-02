package io.github.deficuet.unitykt.internal.export.spirv

import io.github.deficuet.unitykt.cast

internal class ParsedInstruction(opCode: Int, val words: Array<UInt>) {
    val instruction = Instruction.opcodeMap.getValue(opCode)
    val operands = mutableListOf<ParsedOperand>()
    var resultType: Type? = null

    var name: String = ""
    var value: Any? = null

    init { parseOperand() }

    private fun parseOperand() {
        if (instruction.operands.isEmpty()) return
        var currentWord = 1
        var currentOperand = 0
        val varyingOperandValues = mutableListOf<Any>()
        var varyingOperand: Operand? = null

        while (currentWord < words.size) {
            val operand = instruction.operands[currentOperand]
            val (wordUsed, value) = operand.type.readValue(words, currentWord)
            if (operand.quantifier == OperandQuantifier.Varying) {
                varyingOperandValues.add(value)
                varyingOperand = operand
            } else {
                operands.add(ParsedOperand(value))
            }
            currentWord += wordUsed
            if (operand.quantifier != OperandQuantifier.Varying) currentOperand++
        }
        if (varyingOperand != null) {
            val varOperandValue = VaryingOperandValue(varyingOperandValues)
            operands.add(ParsedOperand(varOperandValue))
        }
    }

    fun resolveResultType(objects: Map<UInt, ParsedInstruction>) {
        if (instruction.operands.isNotEmpty() && instruction.operands[0].type is IdResultType) {
            resultType = objects.getValue(operands[0].value as UInt).resultType
        }
    }

    fun resolveReference(objects: Map<UInt, ParsedInstruction>) {
        for (operand in operands) {
            val v = operand.value
            if (v is ObjectReference) {
                v.apply { reference = objects.getValue(id) }
            }
        }
    }

    fun getResultId(): UInt {
        for (i in instruction.operands.indices) {
            if (instruction.operands[i].type is IdResult) {
                return operands[i].id
            }
        }
        return 0u
    }
}

internal class ParsedOperand(var value: Any) {
    val id get() = value.cast<ObjectReference>().id

    inline fun <reified T: Enum<T>> getSingleEnumValue(): T {
        val v = value as ValueEnumOperandValue<*>
        return if (v.value.isEmpty()) {
            v.key as T
        } else {
            v.value[0] as T
        }
    }
}

internal class VaryingOperandValue(val values: List<Any>) {
    fun toString(builder: StringBuilder) {
        for (i in values.indices) {
            val value = values[i]
            if (value is ObjectReference) {
                value.toString(builder)
            } else {
                builder.append(value)
            }
            if (i < values.size - 1) builder.append(' ')
        }
    }

    override fun toString(): String {
        return StringBuilder().apply { toString(this) }.toString()
    }
}

internal class ObjectReference(val id: UInt) {
    var reference: ParsedInstruction? = null
    override fun toString(): String = "%$id"
    fun toString(builder: StringBuilder) { builder.append(toString()) }
}

internal abstract class EnumOperandValue<T: Enum<T>> {
    abstract val enumClass: Class<T>
}

internal class ValueEnumOperandValue<T: Enum<T>>(
    val key: T, val value: Array<Any>,
    override val enumClass: Class<T>
): EnumOperandValue<T>()

internal class BitEnumOperandValue<T: Enum<T>>(
    val values: Map<UInt, Array<Any>>,
    override val enumClass: Class<T>
): EnumOperandValue<T>()
