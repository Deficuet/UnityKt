package io.github.deficuet.unitykt.math

data class Color(val r: Double, val g: Double, val b: Double, val a: Double) {
    constructor(r: Float, g: Float, b: Float, a: Float):
        this(r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble())

    val vector4: Vector4
        get() = Vector4(r, g, b, a)

    operator fun plus(other: Color): Color =
        Color(r + other.r, g + other.g, b + other.b, a + other.a)

    operator fun minus(other: Color): Color =
        Color(r - other.r, g - other.g, b - other.b, a - other.a)

    operator fun times(other: Color): Color =
        Color(r * other.r, g * other.g, b * other.b, a * other.a)

    operator fun <N> times(x: N): Color where N: Number, N: Comparable<N> =
        Color(r * x, g * x, b * x, a * x)

    operator fun div(other: Color) =
        Color(r / other.r, g / other.g, b / other.b, a / other.a)

    operator fun <N> div(x: N): Color where N: Number, N: Comparable<N> =
        Color(r / x, g / x, b / x, a / x)

    //Auto generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Color
        if (r != other.r) return false
        if (g != other.g) return false
        if (b != other.b) return false
        if (a != other.a) return false
        return true
    }

    //Auto generated
    override fun hashCode(): Int {
        var result = r.hashCode()
        result = 31 * result + g.hashCode()
        result = 31 * result + b.hashCode()
        result = 31 * result + a.hashCode()
        return result
    }
}
