package io.github.deficuet.unitykt.math

operator fun <N: Number, V: Vector<V>> N.times(c: V) = c * this
operator fun <N: Number> N.times(c: Color) = c * this
operator fun <N: Number> N.times(m: Matrix4x4) = m * this
