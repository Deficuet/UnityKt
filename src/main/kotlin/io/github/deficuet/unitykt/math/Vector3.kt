package io.github.deficuet.unitykt.math

import io.github.deficuet.unitykt.cast
import kotlin.math.sqrt

class Vector3(val x: Float, val y: Float, val z: Float): Vector<Vector3> {
    constructor(v2: Vector2, z: Float): this(v2.x, v2.y, z)
    val vector2: Vector2 get() = Vector2(x, y)
    val vector4: Vector4 get() = Vector4(this, 0f)

    private val length2 get() = x * x + y * y + z * z
    override val length get() = sqrt(length2)

    override val unit: Vector3
        get() {
            val l2 = length2
            return if (l2 > Vector.kEpsilon2) {
                with(1 / sqrt(l2)) {
                    Vector3(x * this, y * this, z * this)
                }
            } else {
                Zero
            }
        }

    override operator fun get(index: Int): Float {
        return when (index) {
            0 -> x
            1 -> y
            2 -> z
            else -> throw IndexOutOfBoundsException("Vector3 has 3 components only.")
        }
    }

    override operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    override operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)

    /**
     * Treat the "Vector" as a number tuple
     * @see dot
     * @see cross
     */
    override operator fun times(other: Vector3) = Vector3(x * other.x, y * other.y, z * other.z)
    override operator fun div(other: Vector3) = Vector3(x / other.x, y / other.y, z / other.z)
    override operator fun unaryMinus() = Vector3(-x, -y, -z)
    override infix fun dot(other: Vector3) = x * other.x + y * other.y + z * other.z

    infix fun cross(o: Vector3): Vector3 {
        return Vector3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
    }

    override operator fun <N: Number> times(m: N): Vector3 {
        val n = m.toFloat()
        return Vector3(x * n, y * n, z * n)
    }

    override operator fun <N: Number> div(d: N): Vector3 {
        val n = d.toFloat()
        return Vector3(x / n, y / n, z / n)
    }

    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z

    override fun hashCode(): Int {
        return x.hashCode().xor(y.hashCode().shl(2)).xor(z.hashCode().shr(2))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return minus(other.cast()).length2 < Vector.kEpsilon2
    }

    override fun toString(): String {
        return "Vector3($x, $y, $z)"
    }

    companion object {
        val Zero = Vector3(0f, 0f, 0f)
    }
}