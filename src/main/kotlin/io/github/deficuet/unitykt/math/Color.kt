package io.github.deficuet.unitykt.math

class Color(val r: Float, val g: Float, val b: Float, val a: Float) {
    val vector4: Vector4 get() = Vector4(r, g, b, a)

    operator fun plus(other: Color) = Color(r + other.r, g + other.g, b + other.b, a + other.a)
    operator fun minus(other: Color) = Color(r - other.r, g - other.g, b - other.b, a - other.a)
    operator fun times(other: Color) = Color(r * other.r, g * other.g, b * other.b, a * other.a)
    operator fun div(other: Color) = Color(r / other.r, g / other.g, b / other.b, a / other.a)

    operator fun <N: Number> times(x: N): Color {
        val n = x.toFloat()
        return Color(r * n, g * n, b * n, a * n)
    }

    operator fun <N: Number> div(x: N): Color {
        val n = x.toFloat()
        return Color(r / n, g / n, b / n, a / n)
    }

    override fun toString(): String {
        return "Color(R: $r, G: $g, B: $b, A: $a)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Color
        return r == other.r && g == other.g && b == other.b && a == other.a
    }

    override fun hashCode(): Int {
        return r.hashCode()
            .xor(g.hashCode().shl(2))
            .xor(b.hashCode().shr(2))
            .xor(a.hashCode().shr(1))
    }
}
