package io.github.deficuet.unitykt.math

import io.github.deficuet.unitykt.cast
import kotlin.math.sqrt

class Vector2(val x: Double, val y: Double): Vector() {
    constructor(x: Float, y: Float): this(x.toDouble(), y.toDouble())

    val vector3: Vector3 get() = Vector3(this, 0.0)

    val vector4: Vector4 get() = Vector4(this, 0.0, 0.0)

    val length2 get() = x * x + y * y

    override val unit: Vector2
        get() {
            return if (length2 > kEpsilonSqrt) {
                with(1 / sqrt(length2)) {
                    Vector2(x * this, y * this)
                }
            } else {
                Zero
            }
        }

    operator fun get(index: Int): Double {
        return when (index) {
            0 -> x
            1 -> y
            else -> throw IndexOutOfBoundsException("Vector2 has 2 components only.")
        }
    }

    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)

    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)

    operator fun times(other: Vector2) = Vector2(x * other.x, y * other.y)

    operator fun div(other: Vector2) = Vector2(x / other.x, y / other.y)

    operator fun unaryMinus() = Vector2(-x, -y)

    operator fun <N> times(m: N): Vector2 where N: Number, N: Comparable<N> = Vector2(x * m, y * m)

    operator fun <N> div(d: N): Vector2 where N: Number, N: Comparable<N> = Vector2(x / d, y / d)

    operator fun component1() = x
    operator fun component2() = y

    override fun hashCode(): Int {
        return x.hashCode().xor(y.hashCode().shl(2))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return minus(other.cast()).length2 < kEpsilon2
    }

    override fun toString(): String {
        return "Vector(x, y) = ($x, $y)"
    }

    companion object {
        val Zero = Vector2(0.0, 0.0)
    }
}
