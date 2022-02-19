package io.github.deficuet.unitykt.export.spirv

import java.util.*

@Suppress("unused")
internal enum class DisassemblyOptions(override val exp: Int): FlagsAttributeEnum<DisassemblyOptions> {
    None(-1),
    ShowTypes(0),
    ShowNames(1);

    companion object: FlagsAttributeEnumCompanion<DisassemblyOptions>() {
        val Default = of(3)
    }
}

internal class Disassembler {
    private val builder = StringBuilder()
    private operator fun String.unaryPlus() { builder.append(this) }

    fun disassemble(
        module: Module,
        options: EnumSet<DisassemblyOptions> = DisassemblyOptions.Default
    ): String {
        + "; SPIR-v\n"
        + "; Version: ${module.header.version}\n"
        + "; Generator: ${
            with(module.header) {
                if (generatorName.isBlank()) {
                    "unknown; $generatorVersion\n"
                } else {
                    "$generatorVendor $generatorName; $generatorVersion\n"
                }
            }
        }"
        + "; Bound: ${module.header.bound}\n"
        + "; Schema: ${module.header.reserved}\n"
        val lines = Array(module.instructions.size + 1) { "" }
        lines[0] = builder.toString()
        builder.clear()
        for (i in module.instructions.indices) {
            module.instructions[i].disassemble(options)
            lines[i + 1] = builder.toString()
            builder.clear()
        }
        val longestPrefix = lines.maxOf { it.indexOf('=') }.coerceAtMost(50)
        + lines[0]
        for (j in 1 until lines.size) {
            val line = lines[j]
            val index = line.indexOf('=')
            if (index == -1) {
                + "${" ".repeat(longestPrefix + 4)}$line"
            } else {
                val pad = (longestPrefix - index).coerceAtLeast(0)
                + " ".repeat(pad)
                builder.append(line, 0, index)
                + "="
                builder.append(line, index + 1, line.length)
            }
            + "\n"
        }
        val result = builder.toString()
        builder.clear()
        return result
    }

    private fun ParsedInstruction.disassemble(options: EnumSet<DisassemblyOptions>) {
        if (operands.isEmpty()) {
            + instruction.name
            return
        }
        var currentOperand = 0
        if (instruction.operands[currentOperand].type is IdResultType) {
            if (DisassemblyOptions.ShowTypes in options) {
                resultType!!.toString(builder).append(" ")
            }
            ++currentOperand
        }
        if (currentOperand < operands.size && instruction.operands[currentOperand].type is IdResult) {
            if (DisassemblyOptions.ShowNames !in options || name.isBlank()) {
                operands[currentOperand].value.disassembleOperand(options)
            } else {
                + name
            }
            + " = "
            ++currentOperand
        }
        + "${instruction.name} "
        while (currentOperand < operands.size) {
            operands[currentOperand].value.disassembleOperand(options)
            + " "
            currentOperand++
        }
    }

    private fun Any.disassembleOperand(options: EnumSet<DisassemblyOptions>) {
        when (this) {
            is Class<*> -> + simpleName
            is String -> + "\"$this\""
            is ObjectReference -> {
                if (DisassemblyOptions.ShowNames in options && reference != null && reference!!.name.isNotBlank()) {
                    + reference!!.name
                } else {
                    toString(builder)
                }
            }
            is BitEnumOperandValue<*> -> {
                for (key in values.keys) {
                    + enumClass.enumConstants.first { (it as FlagsAttributeEnum<*>).numeric.toUInt() == key }.name
                    val value = values.getValue(key)
                    if (value.isNotEmpty()) {
                        + " "
                        value.forEach { it.disassembleOperand(options) }
                    }
                }
            }
            is ValueEnumOperandValue<*> -> {
                + key.name
                if (value.isNotEmpty()) {
                    + " "
                    value.forEach { it.disassembleOperand(options) }
                }
            }
            is VaryingOperandValue -> toString(builder)
            else -> + toString()
        }
    }
}