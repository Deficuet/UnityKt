package io.github.deficuet.unitykt.math

import io.github.deficuet.unitykt.cast
import kotlin.math.sqrt

class Vector3(val x: Double, val y: Double, val z: Double): Vector() {
    constructor(x: Float, y: Float, z: Float): this(x.toDouble(), y.toDouble(), z.toDouble())

    constructor(v2: Vector2, z: Double): this(v2.x, v2.y, z)

    val vector2: Vector2 get() = Vector2(x, y)

    val vector4: Vector4 get() = Vector4(this, 0.0)

    val length2 get() = x * x + y * y + z * z

    override val unit: Vector3
        get() {
            return if (length2 > kEpsilonSqrt) {
                with(1 / sqrt(length2)) {
                    Vector3(x * this, y * this, z * this)
                }
            } else {
                Zero
            }
        }

    operator fun get(index: Int): Double {
        return when (index) {
            0 -> x
            1 -> y
            2 -> z
            else -> throw IndexOutOfBoundsException("Vector3 has 3 components only.")
        }
    }

    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)

    operator fun times(other: Vector3) = Vector3(x * other.x, y * other.y, z * other.z)

    operator fun div(other: Vector3) = Vector3(x / other.x, y / other.y, z / other.z)

    operator fun unaryMinus() = Vector3(-x, -y, -z)

    operator fun <N> times(m: N) where N: Number, N: Comparable<N> = Vector3(x * m, y * m, z * m)

    operator fun <N> div(d: N) where N: Number, N: Comparable<N> = Vector3(x / d, y / d, z / d)

    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z

    override fun hashCode(): Int {
        return x.hashCode().xor(y.hashCode().shl(2)).xor(z.hashCode().shr(2))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return minus(other.cast()).length2 < kEpsilon2
    }

    override fun toString(): String {
        return "Vector(x, y, z) = ($x, $y, $z)"
    }

    companion object {
        val Zero = Vector3(0.0, 0.0, 0.0)
    }
}