package io.github.deficuet.unitykt.internal.export.spirv

import io.github.deficuet.unitykt.cast
import io.github.deficuet.unitykt.enums.FlagsAttributeEnum
import io.github.deficuet.unitykt.enums.FlagsAttributeEnumCompanion
import java.util.*

internal enum class DisassemblyOptions(override val bitPos: Int): FlagsAttributeEnum {
    None(-1),
    ShowTypes(0),
    ShowNames(1);

    companion object: FlagsAttributeEnumCompanion<DisassemblyOptions>(DisassemblyOptions::class.java, values(), None) {
        val Default = of(3u)
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
        + "; Version: ${module.header.versionMajor}.${module.header.versionMinor}\n"
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
            disassembleInstruction(module.instructions[i], options)
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

    private fun disassembleInstruction(p: ParsedInstruction, options: EnumSet<DisassemblyOptions>) {
        if (p.operands.isEmpty()) {
            + p.instruction.name
            return
        }
        var currentOperand = 0
        if (p.instruction.operands[currentOperand].type is IdResultType) {
            if (DisassemblyOptions.ShowTypes in options) {
                p.resultType!!.toString(builder).append(" ")
            }
            ++currentOperand
        }
        if (currentOperand < p.operands.size && p.instruction.operands[currentOperand].type is IdResult) {
            if (DisassemblyOptions.ShowNames !in options || p.name.isBlank()) {
                disassembleOperand(p.operands[currentOperand].value, options)
            } else {
                + p.name
            }
            + " = "
            ++currentOperand
        }
        + "${p.instruction.name} "
        while (currentOperand < p.operands.size) {
            disassembleOperand(p.operands[currentOperand].value, options)
            + " "
            currentOperand++
        }
    }

    private fun disassembleOperand(o: Any, options: EnumSet<DisassemblyOptions>) {
        when (o) {
            is Class<*> -> + o.simpleName
            is String -> + "\"$o\""
            is ObjectReference -> {
                if (DisassemblyOptions.ShowNames in options && o.reference != null && o.reference!!.name.isNotBlank()) {
                    + o.reference!!.name
                } else {
                    o.toString(builder)
                }
            }
            is BitEnumOperandValue<*> -> {
                for (key in o.values.keys) {
                    + o.enumClass.enumConstants.first { it.cast<FlagsAttributeEnum>().value == key }.name
                    val value = o.values.getValue(key)
                    if (value.isNotEmpty()) {
                        + " "
                        value.forEach { disassembleOperand(it, options) }
                    }
                }
            }
            is ValueEnumOperandValue<*> -> {
                + o.key.name
                if (o.value.isNotEmpty()) {
                    + " "
                    o.value.forEach { disassembleOperand(it, options) }
                }
            }
            is VaryingOperandValue -> o.toString(builder)
            else -> + java.lang.String.valueOf(o)
        }
    }
}
