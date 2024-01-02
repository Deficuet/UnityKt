package io.github.deficuet.unitykt.math

import io.github.deficuet.unitykt.cast
import kotlin.math.sqrt

class Vector2(val x: Float, val y: Float): Vector<Vector2> {
    val vector3: Vector3 get() = Vector3(this, 0f)
    val vector4: Vector4 get() = Vector4(this, 0f, 0f)

    private val length2 get() = x * x + y * y
    override val length get() = sqrt(length2)

    override val unit: Vector2
        get() {
            val l2 = length2
            return if (l2 > Vector.kEpsilon2) {
                with(1 / sqrt(l2)) {
                    Vector2(x * this, y * this)
                }
            } else {
                Zero
            }
        }

    override operator fun get(index: Int): Float {
        return when (index) {
            0 -> x
            1 -> y
            else -> throw IndexOutOfBoundsException("Vector2 has 2 components only.")
        }
    }

    override operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    override operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    override operator fun times(other: Vector2) = Vector2(x * other.x, y * other.y)
    override operator fun div(other: Vector2) = Vector2(x / other.x, y / other.y)
    override operator fun unaryMinus() = Vector2(-x, -y)
    override infix fun dot(other: Vector2) = x * other.x + y * other.y

    override operator fun <N: Number> times(m: N): Vector2 {
        val n = m.toFloat()
        return Vector2(x * n, y * n)
    }

    override operator fun <N: Number> div(d: N): Vector2 {
        val n = d.toFloat()
        return Vector2(x / n, y / n)
    }

    operator fun component1() = x
    operator fun component2() = y

    override fun hashCode(): Int {
        return x.hashCode().xor(y.hashCode().shl(2))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return minus(other.cast()).length2 < Vector.kEpsilon2
    }

    override fun toString(): String {
        return "Vector2($x, $y)"
    }

    companion object {
        val Zero = Vector2(0f, 0f)
    }
}
