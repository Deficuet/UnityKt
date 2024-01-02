package io.github.deficuet.unitykt.enums

abstract class OrdinalEnumCompanion<E: Enum<E>>(
    val array: Array<E>
) {
    fun of(num: Int) = array[num]
}