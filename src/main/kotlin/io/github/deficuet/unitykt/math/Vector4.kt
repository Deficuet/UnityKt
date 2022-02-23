package io.github.deficuet.unitykt.math

import kotlin.math.sqrt

class Vector4(
    private var _x: Double, private var _y: Double,
    private var _z: Double, private var _w: Double
): Vector() {
    constructor(x: Float, y: Float, z: Float, w: Float):
            this(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())

    constructor(v2: Vector2, z: Double, w: Double): this(v2.x, v2.y, z, w)

    constructor(v3: Vector3, w: Double): this(v3.x, v3.y, v3.z, w)

    val x by ::_x
    val y by ::_y
    val z by ::_z
    val w by ::_w

    val vector2: Vector2 get() = Vector2(_x, _y)

    val vector3: Vector3 get() = Vector3(_x, _y, _z)

    val color: Color get() = Color(_x, _y, _z, _w)

    val length2 get() = _x * _x + _y * _y + _z * _z + _w * _w

    override fun normalize() {
        if (length2 > kEpsilonSqrt) {
            with(1 / sqrt(length2)) {
                _x *= this
                _y *= this
                _z *= this
                _w *= this
            }
        } else {
            _x = 0.0; _y = 0.0; _z = 0.0; _w = 0.0
        }
    }

    operator fun get(index: Int): Double {
        return when (index) {
            0 -> _x
            1 -> _y
            2 -> _z
            3 -> _w
            else -> throw IndexOutOfBoundsException("Vector4 has 4 components only.")
        }
    }

    operator fun plus(other: Vector4) = Vector4(_x + other.x, _y + other.y, _z + other.z, _w + other.w)

    operator fun minus(other: Vector4) = Vector4(_x - other.x, _y - other.y, _z - other.z, _w - other.w)

    operator fun times(other: Vector4) = Vector4(_x * other.x, _y * other.y, _z * other.z, _w * other.w)

    operator fun div(other: Vector4) = Vector4(_x / other.x, _y / other.y, _z / other.z, _w / other._w)

    operator fun unaryMinus() = Vector4(-_x, -_y, -_z, -_w)

    operator fun <N> times(m: N) where N: Number, N: Comparable<N> =
        Vector4(_x * m, _y * m, _z * m, _w * m)

    operator fun <N> div(d: N) where N: Number, N: Comparable<N> =
        Vector4(_x / d, _y / d, _z / d, _w / d)

    infix fun approxEquals(other: Vector4): Boolean = minus(other).length2 < kEpsilon2

    override fun hashCode(): Int {
        return _x.hashCode()
            .xor(_y.hashCode().shl(2))
            .xor(_z.hashCode().shr(2))
            .xor(_w.hashCode().shr(1))
    }

    /**
     * @see approxEquals
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector4

        if (_x != other.x) return false
        if (_y != other.y) return false
        if (_z != other.z) return false
        if (_w != other.w) return false

        return true
    }

    override fun toString(): String {
        return "Vector(x, y, z, w) = ($_x, $_y, $_z, $_w)"
    }

    companion object {
        val Zero = Vector4(0.0, 0.0, 0.0, 0.0)
    }
}