package io.github.deficuet.unitykt.math

import io.github.deficuet.unitykt.cast
import kotlin.math.sqrt

class Vector4(val x: Float, val y: Float, val z: Float, val w: Float): Vector<Vector4> {
    constructor(v2: Vector2, z: Float, w: Float): this(v2.x, v2.y, z, w)
    constructor(v3: Vector3, w: Float): this(v3.x, v3.y, v3.z, w)
    val vector2: Vector2 get() = Vector2(x, y)
    val vector3: Vector3 get() = Vector3(x, y, z)
    val color: Color get() = Color(x, y, z, w)

    private val length2 get() = x * x + y * y + z * z + w * w
    override val length get() = sqrt(length2)

    override val unit: Vector4
        get() {
            val l2 = length2
            return if (l2 > Vector.kEpsilon2) {
                with(1 / sqrt(l2)) {
                    Vector4(x * this, y * this, z * this, w * this)
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
            3 -> w
            else -> throw IndexOutOfBoundsException("Vector4 has 4 components only.")
        }
    }

    override operator fun plus(other: Vector4) = Vector4(x + other.x, y + other.y, z + other.z, w + other.w)
    override operator fun minus(other: Vector4) = Vector4(x - other.x, y - other.y, z - other.z, w - other.w)
    override operator fun times(other: Vector4) = Vector4(x * other.x, y * other.y, z * other.z, w * other.w)
    override operator fun div(other: Vector4) = Vector4(x / other.x, y / other.y, z / other.z, w / other.w)
    override operator fun unaryMinus() = Vector4(-x, -y, -z, -w)
    override infix fun dot(other: Vector4) = x * other.x + y * other.y + z * other.z + w * other.w

    override operator fun <N: Number> times(m: N): Vector4 {
        val n = m.toFloat()
        return Vector4(x * n, y * n, z * n, w * n)
    }

    override operator fun <N: Number> div(d: N): Vector4 {
        val n = d.toFloat()
        return Vector4(x / n, y / n, z / n, w / n)
    }

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
        return minus(other.cast()).length2 < Vector.kEpsilon2
    }

    override fun toString(): String {
        return "Vector4($x, $y, $z, $w)"
    }

    companion object {
        val Zero = Vector4(0f, 0f, 0f, 0f)
    }
}