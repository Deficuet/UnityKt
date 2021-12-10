package io.github.deficuet.unitykt.math

import kotlin.math.sqrt

data class Vector3(var x: Double, var y: Double, var z: Double): Vector() {
    constructor(x: Float, y: Float, z: Float): this(x.toDouble(), y.toDouble(), z.toDouble())

    constructor(v2: Vector2, z: Double): this(v2.x, v2.y, z)

    val vector2: Vector2 get() = Vector2(x, y)

    val vector4: Vector4 get() = Vector4(this, 0.0)

    private fun length2(): Double = x * x + y * y + z * z

    override fun normalize() {
        val length2 = length2()
        if (length2 > kEpsilonSqrt) {
            with(1 / sqrt(length2)) {
                x *= this
                y *= this
                z *= this
            }
        } else {
            x = 0.0; y = 0.0; z = 0.0
        }
    }

    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)

    operator fun times(other: Vector3) = Vector3(x * other.x, y * other.y, z * other.z)

    operator fun div(other: Vector3) = Vector3(x / other.x, y / other.y, z / other.z)

    operator fun unaryMinus() = Vector3(-x, -y, -z)

    operator fun <N> times(m: N) where N: Number, N: Comparable<N> = Vector3(x * m, y * m, z * m)

    operator fun <N> div(d: N) where N: Number, N: Comparable<N> = Vector3(x / d, y / d, z / d)

    infix fun approxEquals(other: Vector3): Boolean = minus(other).length2() < kEpsilon2

    override fun hashCode(): Int {
        return x.hashCode() xor (y.hashCode() shl 2) xor (z.hashCode() shr 2)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    companion object {
        val Zero = Vector3(0.0, 0.0, 0.0)
    }
}