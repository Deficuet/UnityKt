package io.github.deficuet.unitykt.math

data class Rect(val x: Float, val y: Float, val width: Float, val height: Float) {
    override fun toString(): String {
        return "Rectf(pos(x = $x, y = $y), dim(width = $width, height = $height))"
    }
}
