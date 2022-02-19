package io.github.deficuet.unitykt.export.spirv

internal interface NumericalEnum<T: Comparable<T>> {
    val id: T
}

internal abstract class NumericalEnumCompanion<T: Enum<T>, C: Comparable<C>>

@Suppress("unused")
internal inline fun <reified E: Enum<E>, N: Comparable<N>> NumericalEnumCompanion<E, N>.of(value: N): E {
    val values = enumValues<E>()
    return values.firstOrNull { (it as NumericalEnum<*>).id == value } ?: values.first()
}