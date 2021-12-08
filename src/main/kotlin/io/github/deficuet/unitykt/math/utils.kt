package io.github.deficuet.unitykt.math

//insignificant
//internal fun qrSqrt(x: Double): Double {    // f(x) = 1 / sqrt(x)
//    var y = x
//    val x2 = x * 0.5
//    y = Double.fromBits(0x5FE6EB50C7B537A9 - (y.toBits() shr 1))
//    y *= (1.5 - (x2 * y * y))
//    y *= (1.5 - (x2 * y * y))
//    return y
//}

internal operator fun <N> Double.times(x: N): Double where N: Number, N: Comparable<N> {
    val operator: (Double) -> Double = when (x) {
        is Byte    -> { { it * x } }
        is Short   -> { { it * x } }
        is Int     -> { { it * x } }
        is Long    -> { { it * x } }
        is Float   -> { { it * x } }
        is Double  -> { { it * x } }
        else -> throw IllegalStateException("Unsupported numeric value ${x::class}")
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
        else -> throw IllegalStateException("Unsupported numeric value ${x::class}")
    }
    return operator(this)
}