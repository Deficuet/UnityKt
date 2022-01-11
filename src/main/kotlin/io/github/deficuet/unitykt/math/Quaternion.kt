package io.github.deficuet.unitykt.math

class Quaternion(a: Double, b: Double, c: Double, d: Double) {
    constructor(x: Float, y: Float, z: Float, w: Float):
        this(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())

    constructor(vararg data: Float): this(data[0], data[1], data[2], data[3])

    var a: Double = a
        private set

    var b: Double = b
        private set

    var c: Double = c
        private set

    var d: Double = d
        private set

    infix fun dot(other: Quaternion) = a * other.a + b * other.b + c * other.c + d * other.d

    fun approxEquals(other: Quaternion): Boolean = dot(other) > 1.0 - kEpsilon

    override fun hashCode(): Int {
        return a.hashCode() xor (b.hashCode() shl 2) xor (c.hashCode() shr 2) xor (d.hashCode() shr 1)
    }

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