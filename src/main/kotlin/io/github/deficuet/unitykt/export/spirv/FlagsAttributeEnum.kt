package io.github.deficuet.unitykt.export.spirv

import java.util.*

internal interface FlagsAttributeEnum<E: Enum<E>> {
    val exp: Int

    val numeric: Int get() = if (exp == -1) 0 else 1.shl(exp)

    companion object {
        val flagsClass = FlagsAttributeEnum::class.java
    }
}

internal abstract class FlagsAttributeEnumCompanion<E: Enum<E>>

internal inline fun <reified E: Enum<E>> FlagsAttributeEnumCompanion<E>.of(value: Long): EnumSet<E> {
    val enums = enumValues<E>()
    val enumClass = E::class.java
    val zeros = enums.filter { (it as FlagsAttributeEnum<*>).exp == -1 }.toMutableList()
    if (value == 0L) {
        return if (zeros.isEmpty()) EnumSet.noneOf(enumClass) else EnumSet.of(zeros[0])
    } else {
        with(zeros) { if (isNotEmpty()) removeFirst() }
        val bits = BitSet.valueOf(longArrayOf(value))
        val exps = enums.map { (it as FlagsAttributeEnum<*>).exp }
        val result = if (zeros.isEmpty()) EnumSet.noneOf(enumClass) else EnumSet.copyOf(zeros)
        for (i in 0..31) {
            if (bits[i]) {
                if (i !in exps) return EnumSet.noneOf(enumClass)
                result += enums.last { (it as FlagsAttributeEnum<*>).exp == i }
            }
        }
        return result
    }
}