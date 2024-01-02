package io.github.deficuet.unitykt.enums

interface NumericalEnum<N: Comparable<N>> {
    val id: N
}

interface NumericalEnumMap<N: Comparable<N>, E: Enum<E>> {
    fun of(value: N): E
}

abstract class NumericalEnumCompanion<N: Comparable<N>, E>(
    enumValues: Array<E>,
    private val default: E
): NumericalEnumMap<N, E> where E: Enum<E>, E: NumericalEnum<N> {
    val cacheTable: Map<N, E> = enumValues.associateBy { it.id }

    override fun of(value: N): E = cacheTable[value] ?: default
}
