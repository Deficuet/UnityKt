package io.github.deficuet.unitykt.math

import io.github.deficuet.unitykt.cast
import kotlin.math.sqrt

class Vector4(
    val x: Double, val y: Double,
    val z: Double, val w: Double
): Vector() {
    constructor(x: Float, y: Float, z: Float, w: Float):
            this(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())

    constructor(v2: Vector2, z: Double, w: Double): this(v2.x, v2.y, z, w)

    constructor(v3: Vector3, w: Double): this(v3.x, v3.y, v3.z, w)

    val vector2: Vector2 get() = Vector2(x, y)

    val vector3: Vector3 get() = Vector3(x, y, z)

    val color: Color get() = Color(x, y, z, w)

    val length2 get() = x * x + y * y + z * z + w * w

    override val unit: Vector4
        get() {
            return if(length2 > kEpsilonSqrt) {
                with(1 / sqrt(length2)) {
                    Vector4(x * this, y * this, z * this, w * this)
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
            3 -> w
            else -> throw IndexOutOfBoundsException("Vector4 has 4 components only.")
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

    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z
    operator fun component4() = w

    override fun hashCode(): Int {
        return x.hashCode()
            .xor(y.hashCode().shl(2))
            .xor(z.hashCode().shr(2))
            .xor(w.hashCode().shr(1))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return minus(other.cast()).length2 < kEpsilon2
    }

    override fun toString(): String {
        return "Vector(x, y, z, w) = ($x, $y, $z, $w)"
    }

    companion object {
        val Zero = Vector4(0.0, 0.0, 0.0, 0.0)
    }
}