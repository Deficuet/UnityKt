package io.github.deficuet.unitykt.math

class Quaternion(val a: Float, val b: Float, val c: Float, val d: Float) {
    operator fun get(index: Int): Float {
        return when (index) {
            0 -> a
            1 -> b
            2 -> c
            3 -> d
            else -> throw IndexOutOfBoundsException("Quaternion has 4 components only.")
        }
    }

    infix fun dot(other: Quaternion) = a * other.a + b * other.b + c * other.c + d * other.d

    override fun hashCode(): Int {
        return a.hashCode()
            .xor(b.hashCode().shl(2))
            .xor(c.hashCode().shr(2))
            .xor(d.hashCode().shr(1))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return dot(other as Quaternion) > 1f - kEpsilon
    }

    override fun toString(): String {
        return "Quaternion($a, $b, $c, $d)"
    }

    companion object {
        val Zero = Quaternion(0f, 0f, 0f, 0f)
        private const val kEpsilon = 0.000001f
    }
}