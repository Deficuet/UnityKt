package io.github.deficuet.unitykt.math

import kotlin.math.sqrt

class Vector2(x: Double, y: Double): Vector() {
    constructor(x: Float, y: Float): this(x.toDouble(), y.toDouble())

    var x: Double = x
        private set

    var y: Double = y
        private set

    val vector3: Vector3 get() = Vector3(this, 0.0)

    val vector4: Vector4 get() = Vector4(this, 0.0, 0.0)

    val length2 get() = x * x + y * y

    override fun normalize() {
        if (length2 > kEpsilonSqrt) {
            with(1 / sqrt(length2)) {
                x *= this
                y *= this
            }
        } else {
            x = 0.0; y = 0.0
        }
    }

    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)

    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)

    operator fun times(other: Vector2) = Vector2(x * other.x, y * other.y)

    operator fun div(other: Vector2) = Vector2(x / other.x, y / other.y)

    operator fun unaryMinus() = Vector2(-x, -y)

    operator fun <N> times(m: N): Vector2 where N: Number, N: Comparable<N> = Vector2(x * m, y * m)

    operator fun <N> div(d: N): Vector2 where N: Number, N: Comparable<N> = Vector2(x / d, y / d)

    infix fun approxEquals(other: Vector2): Boolean = minus(other).length2 < kEpsilon2

    override fun hashCode(): Int {
        return x.hashCode() xor (y.hashCode() shl 2)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector2

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    companion object {
        val Zero = Vector2(0.0, 0.0)
    }
}
