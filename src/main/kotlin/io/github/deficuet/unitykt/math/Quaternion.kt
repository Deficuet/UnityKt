package io.github.deficuet.unitykt.math

data class Quaternion(val x: Float, val y: Float, val z: Float, val w: Float) {
    operator fun get(index: Int): Float {
        return when (index) {
            0 -> x
            1 -> y
            2 -> z
            3 -> w
            else -> throw IndexOutOfBoundsException(index)
        }
    }

    infix fun dot(other: Quaternion) = x * other.x + y * other.y + z * other.z + w * other.w

    override fun hashCode(): Int {
        return x.hashCode()
            .xor(y.hashCode().shl(2))
            .xor(z.hashCode().shr(2))
            .xor(w.hashCode().shr(1))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return dot(other as Quaternion) > 1f - ERROR_MARGIN
    }

    override fun toString(): String {
        return "Quaternionf(x = $x, y = $y, z = $z, w = $w)"
    }

    companion object {
        val Zero = Quaternion(0f, 0f, 0f, 0f)
        private const val ERROR_MARGIN = 0.000001f
    }
}