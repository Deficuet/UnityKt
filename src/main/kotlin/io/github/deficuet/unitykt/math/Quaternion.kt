package io.github.deficuet.unitykt.math

data class Quaternion(var x: Double, var y: Double, var z: Double, var w: Double) {
    constructor(x: Float, y: Float, z: Float, w: Float):
        this(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())

//    val data: List<Double> get() = listOf(x, y, z, w)

    infix fun dot(other: Quaternion) = x * other.x + y * other.y + z * other.z + w * other.w

    fun approxEquals(other: Quaternion): Boolean = dot(other) > 1.0 - kEpsilon

    override fun hashCode(): Int {
        return x.hashCode() xor (y.hashCode() shl 2) xor (z.hashCode() shr 2) xor (w.hashCode() shr 1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Quaternion

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (w != other.w) return false

        return true
    }

    companion object {
        internal const val kEpsilon = 0.000001
    }
}