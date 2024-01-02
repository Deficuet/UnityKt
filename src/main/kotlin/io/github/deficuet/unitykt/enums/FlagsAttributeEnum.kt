package io.github.deficuet.unitykt.enums

import java.util.*

/**
 * C# &#91`Flags`&#93 attribute applied on enum class
 */
interface FlagsAttributeEnum {
    val bitPos: Int
    val value get() = if (bitPos == -1) 0u else 1u.shl(bitPos)

    companion object {
        internal val flagsAttributeEnumClass = FlagsAttributeEnum::class.java
    }
}

abstract class FlagsAttributeEnumCompanion<E>(
    private val enumClass: Class<E>,
    enumValues: Array<E>,
    private val mainZero: E
) where E: FlagsAttributeEnum, E: Enum<E> {
    private val nonZeroTable = enumValues.associateBy { it.bitPos }
    private val zeros = enumValues.filter { it.bitPos == -1 && it != mainZero }

    fun of(value: UInt): EnumSet<E> {
        if (value == 0u) return EnumSet.of(mainZero)
        val set = EnumSet.copyOf(zeros)
        for (i in 0..31) {
            if (value.and(1u.shl(i)) != 0u) {
                nonZeroTable[i]?.let { set.add(it) } ?: return EnumSet.noneOf(enumClass)
            }
        }
        return set
    }
}