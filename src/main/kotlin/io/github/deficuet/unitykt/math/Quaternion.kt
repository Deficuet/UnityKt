package io.github.deficuet.unitykt.math

class Quaternion(val a: Double, val b: Double, val c: Double, val d: Double) {
    constructor(a: Float, b: Float, c: Float, d: Float):
        this(a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble())

    constructor(vararg data: Float): this(data[0], data[1], data[2], data[3])

    operator fun get(index: Int): Double {
        return when (index) {
            0 -> a
            1 -> b
            2 -> c
            3 -> d
            else -> throw IndexOutOfBoundsException("Quaternion has 4 components only.")
        }
    }

    infix fun dot(other: Quaternion) = a * other.a + b * other.b + c * other.c + d * other.d

    fun approxEquals(other: Quaternion): Boolean = dot(other) > 1.0 - kEpsilon

    override fun hashCode(): Int {
        return a.hashCode()
            .xor(b.hashCode().shl(2))
            .xor(c.hashCode().shr(2))
            .xor(d.hashCode().shr(1))
    }

    /**
     * @see approxEquals
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Quaternion

        if (a != other.a) return false
        if (b != other.b) return false
        if (c != other.c) return false
        if (d != other.d) return false

        return true
    }

    override fun toString(): String {
        return "Quaternion(a, b, c, d) = ($a, $b, $c, $d)"
    }

    companion object {
        internal const val kEpsilon = 0.000001
    }
}