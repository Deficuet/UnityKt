package io.github.deficuet.unitykt.math

import io.github.deficuet.unitykt.util.UnsupportedFormatException

internal operator fun <N> Double.times(x: N): Double where N: Number, N: Comparable<N> {
    val operator: (Double) -> Double = when (x) {
        is Byte    -> { { it * x } }
        is Short   -> { { it * x } }
        is Int     -> { { it * x } }
        is Long    -> { { it * x } }
        is Float   -> { { it * x } }
        is Double  -> { { it * x } }
        else -> throw UnsupportedFormatException("Numeric type ${x::class}")
    }
    return operator(this)
}

internal operator fun <N> Double.div(x: N): Double where N:Number, N: Comparable<N> {
    val operator: (Double) -> Double = when (x) {
        is Byte    -> { { it / x } }
        is Short   -> { { it / x } }
        is Int     -> { { it / x } }
        is Long    -> { { it / x } }
        is Float   -> { { it / x } }
        is Double  -> { { it / x } }
        else -> throw UnsupportedFormatException("Numeric type ${x::class}")
    }
    return operator(this)
}

inline fun <E> mutableList(builder: MutableList<E>.() -> Unit): MutableList<E> {
    return mutableListOf<E>().apply(builder)
}