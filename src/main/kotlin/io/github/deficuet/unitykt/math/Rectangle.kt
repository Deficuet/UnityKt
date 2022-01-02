package io.github.deficuet.unitykt.math

data class Rectangle(var x: Double, var y: Double, var w: Double, var h: Double) {
    constructor(x: Float, y: Float, w: Float, h: Float): this(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
}