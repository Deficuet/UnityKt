package io.github.deficuet.unitykt.math

import kotlin.math.sqrt

class Vector2(private var _x: Double, private var _y: Double): Vector() {
    constructor(x: Float, y: Float): this(x.toDouble(), y.toDouble())

    val x by ::_x
    val y by ::_y

    val vector3: Vector3 get() = Vector3(this, 0.0)

    val vector4: Vector4 get() = Vector4(this, 0.0, 0.0)

    val length2 get() = _x * _x + _y * _y

    override fun normalize() {
        if (length2 > kEpsilonSqrt) {
            with(1 / sqrt(length2)) {
                _x *= this
                _y *= this
            }
        } else {
            _x = 0.0; _y = 0.0
        }
    }

    operator fun plus(other: Vector2) = Vector2(_x + other.x, _y + other.y)

    operator fun minus(other: Vector2) = Vector2(_x - other.x, _y - other.y)

    operator fun times(other: Vector2) = Vector2(_x * other.x, _y * other.y)

    operator fun div(other: Vector2) = Vector2(_x / other.x, _y / other.y)

    operator fun unaryMinus() = Vector2(-_x, -_y)

    operator fun <N> times(m: N): Vector2 where N: Number, N: Comparable<N> = Vector2(_x * m, _y * m)

    operator fun <N> div(d: N): Vector2 where N: Number, N: Comparable<N> = Vector2(_x / d, _y / d)

    infix fun approxEquals(other: Vector2): Boolean = minus(other).length2 < kEpsilon2

    override fun hashCode(): Int {
        return _x.hashCode().xor(_y.hashCode().shl(2))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector2

        if (_x != other.x) return false
        if (_y != other.y) return false

        return true
    }

    override fun toString(): String {
        return "Vector(x, y) = ($_x, $_y)"
    }

    companion object {
        val Zero = Vector2(0.0, 0.0)
    }
}
