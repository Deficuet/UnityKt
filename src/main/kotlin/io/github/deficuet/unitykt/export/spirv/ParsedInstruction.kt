package io.github.deficuet.unitykt.export.spirv

import io.github.deficuet.unitykt.util.Reference

internal class ParsedInstruction(opCode: Int, val words: Array<UInt>) {
    val instruction = Instruction.fromOpcode.getValue(opCode)
    val operands = mutableListOf<ParsedOperand>()
    val resultId: UInt get() {
        for (i in instruction.operands.indices) {
            if (instruction.operands[i].type is IdResult) {
                return operands[i].id
            }
        }
        return 0u
    }
    val hasResult get() = resultId != 0u
    var resultType: Type? = null
    var name: String = ""
    var value: Any? = null

    init { parseOperands() }

    private fun parseOperands() {
        if (instruction.operands.isEmpty()) return
        var currentWord = 1
        var currentOperand = 0
        val varyingOperandValues = mutableListOf<Any>()
        var varyingOperand: Operand? = null

        while (currentWord < words.size) {
            val operand = instruction.operands[currentOperand]
            val valueRef = Reference<Any>()
            val wordUsed = operand.type.readValue(words, currentWord, valueRef)
            if (operand.quantifier == OperandQuantifier.Varying) {
                varyingOperandValues.add(valueRef.value)
                varyingOperand = operand
            } else {
                operands.add(ParsedOperand(valueRef.value, operand))
            }
            currentWord += wordUsed
            if (operand.quantifier != OperandQuantifier.Varying) currentOperand++
        }
        if (varyingOperand != null) {
            val varOperandValue = VaryingOperandValue(varyingOperandValues)
            operands.add(ParsedOperand(varOperandValue, varyingOperand))
        }
    }

    fun resolveResultType(objects: Map<UInt, ParsedInstruction>) {
        if (instruction.operands.isNotEmpty() && instruction.operands[0].type is IdResultType) {
            resultType = objects.getValue(operands[0].value as UInt).resultType
        }
    }

    fun resolveReference(objects: Map<UInt, ParsedInstruction>) {
        for (operand in operands) {
            if (operand.value is ObjectReference) {
                (operand.value as ObjectReference).apply { reference = objects.getValue(id) }
            }
        }
    }
}

internal class ParsedOperand(
    var value: Any, val operand: Operand
) {
    val id get() = (value as ObjectReference).id

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