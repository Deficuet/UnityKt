package io.github.deficuet.unitykt.math

interface Vector<V> {
    val unit: V
    val length: Float

    operator fun get(index: Int): Float

    operator fun plus(other: V): V
    operator fun minus(other: V): V

    /**
     * Treat the "Vector" as a number tuple
     * @see dot
     */
    operator fun times(other: V): V

    /**
     * Treat the "Vector" as a number tuple
     */
    operator fun div(other: V): V
    operator fun <N: Number> times(m: N): V
    operator fun <N: Number> div(d: N): V
    infix fun dot(other: V): Float

    operator fun unaryMinus(): V

    companion object {
        internal const val kEpsilon2 = 1.0e-10f    //0.00001 ** 2
    }
}
