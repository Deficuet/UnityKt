package io.github.deficuet.unitykt.enums

import kotlin.enums.EnumEntries

interface NumericalEnum<N: Comparable<N>> {
    val id: N
}

interface NumericalEnumMapping<N: Comparable<N>, E: Enum<E>> {
    fun of(value: N): E
}

abstract class NumericalEnumCompanion<N: Comparable<N>, E>(
    enumValues: EnumEntries<E>,
    private val default: E
): NumericalEnumMapping<N, E> where E: Enum<E>, E: NumericalEnum<N> {
    val cacheTable = enumValues.associateBy { it.id }

    override fun of(value: N) = cacheTable[value] ?: default
}
