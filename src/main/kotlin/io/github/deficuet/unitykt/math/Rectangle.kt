package io.github.deficuet.unitykt.math

data class Rectangle(val x: Double, val y: Double, val w: Double, val h: Double) {
    constructor(x: Float, y: Float, w: Float, h: Float): this(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())

    override fun toString(): String {
        return "Rectangle at ($x, $y) with width and height ($w, $h)"
    }
}