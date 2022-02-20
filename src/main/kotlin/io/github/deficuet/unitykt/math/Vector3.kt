package io.github.deficuet.unitykt.math

import kotlin.math.sqrt

class Vector3(private var _x: Double, private var _y: Double, private var _z: Double): Vector() {
    constructor(x: Float, y: Float, z: Float): this(x.toDouble(), y.toDouble(), z.toDouble())

    constructor(v2: Vector2, z: Double): this(v2.x, v2.y, z)

    val x by ::_x
    val y by ::_y
    val z by ::_z

    val vector2: Vector2 get() = Vector2(_x, _y)

    val vector4: Vector4 get() = Vector4(this, 0.0)

    val length2 get() = _x * _x + _y * _y + _z * _z

    override fun normalize() {
        if (length2 > kEpsilonSqrt) {
            with(1 / sqrt(length2)) {
                _x *= this
                _y *= this
                _z *= this
            }
        } else {
            _x = 0.0; _y = 0.0; _z = 0.0
        }
    }

    operator fun plus(other: Vector3) = Vector3(_x + other.x, _y + other.y, _z + other.z)

    operator fun minus(other: Vector3) = Vector3(_x - other.x, _y - other.y, _z - other.z)

    operator fun times(other: Vector3) = Vector3(_x * other.x, _y * other.y, _z * other.z)

    operator fun div(other: Vector3) = Vector3(_x / other.x, _y / other.y, _z / other.z)

    operator fun unaryMinus() = Vector3(-_x, -_y, -_z)

    operator fun <N> times(m: N) where N: Number, N: Comparable<N> = Vector3(_x * m, _y * m, _z * m)

    operator fun <N> div(d: N) where N: Number, N: Comparable<N> = Vector3(_x / d, _y / d, _z / d)

    infix fun approxEquals(other: Vector3): Boolean = minus(other).length2 < kEpsilon2

    override fun hashCode(): Int {
        return _x.hashCode().xor(_y.hashCode().shl(2)).xor(_z.hashCode().shr(2))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3

        if (_x != other.x) return false
        if (_y != other.y) return false
        if (_z != other.z) return false

        return true
    }

    override fun toString(): String {
        return "Vector(x, y, z) = ($_x, $_y, $_z)"
    }

    companion object {
        val Zero = Vector3(0.0, 0.0, 0.0)
    }
}