package io.github.deficuet.unitykt.math

import kotlin.math.sqrt

class Vector4(x: Double, y: Double, z: Double, w: Double): Vector() {
    constructor(x: Float, y: Float, z: Float, w: Float):
            this(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())

    constructor(v2: Vector2, z: Double, w: Double): this(v2.x, v2.y, z, w)

    constructor(v3: Vector3, w: Double): this(v3.x, v3.y, v3.z, w)

    var x: Double = x
        private set

    var y: Double = y
        private set

    var z: Double = z
        private set

    var w: Double = w
        private set

    val vector2: Vector2 get() = Vector2(x, y)

    val vector3: Vector3 get() = Vector3(x, y, z)

    val color: Color get() = Color(x, y, z, w)

    private fun length2(): Double = x * x + y * y + z * z + w * w

    override fun normalize() {
        val length2 = length2()
        if (length2 > kEpsilonSqrt) {
            with(1 / sqrt(length2)) {
                x *= this
                y *= this
                z *= this
                w *= this
            }
        } else {
            x = 0.0; y = 0.0; z = 0.0; w = 0.0
        }
    }

    operator fun plus(other: Vector4) = Vector4(x + other.x, y + other.y, z + other.z, w + other.w)

    operator fun minus(other: Vector4) = Vector4(x - other.x, y - other.y, z - other.z, w - other.w)

    operator fun times(other: Vector4) = Vector4(x * other.x, y * other.y, z * other.z, w * other.w)

    operator fun div(other: Vector4) = Vector4(x / other.x, y / other.y, z / other.z, w / other.w)

    operator fun unaryMinus() = Vector4(-x, -y, -z, -w)

    operator fun <N> times(m: N) where N: Number, N: Comparable<N> =
        Vector4(x * m, y * m, z * m, w * m)

    operator fun <N> div(d: N) where N: Number, N: Comparable<N> =
        Vector4(x / d, y / d, z / d, w / d)

    infix fun approxEquals(other: Vector4): Boolean = minus(other).length2() < kEpsilon2

    override fun hashCode(): Int {
        return x.hashCode() xor (y.hashCode() shl 2) xor (z.hashCode() shr 2) xor (w.hashCode() shr 1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector4

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (w != other.w) return false

        return true
    }

    companion object {
        val Zero = Vector4(0.0, 0.0, 0.0, 0.0)
    }
}