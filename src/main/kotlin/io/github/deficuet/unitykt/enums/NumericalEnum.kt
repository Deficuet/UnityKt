package io.github.deficuet.unitykt.enums

import kotlin.enums.EnumEntries

/**
 * Should be implemented by Enums.
 */
interface NumericalEnum<N: Comparable<N>> {
    val id: N
}

/**
 * Should be implemented by the companion object of a [NumericalEnum] class.
 *
 * The type parameter [E] here is not constrained to be a subclass of [NumericalEnum] for convenience.
 */
interface NumericalEnumMapping<N: Comparable<N>, E: Enum<E>> {
    fun of(value: N): E
}

/**
 * Should be inherited by the companion object of a [NumericalEnum] class.
 */
abstract class NumericalEnumCompanion<N: Comparable<N>, E>(
    enumValues: EnumEntries<E>,
    private val default: E
): NumericalEnumMapping<N, E> where E: Enum<E>, E: NumericalEnum<N> {
    val cacheTable = enumValues.associateBy { it.id }

    override fun of(value: N) = cacheTable[value] ?: default
}
