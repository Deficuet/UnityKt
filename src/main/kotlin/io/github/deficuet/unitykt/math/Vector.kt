package io.github.deficuet.unitykt.math

abstract class Vector {
    abstract fun normalize()

    companion object {
        internal const val kEpsilonSqrt = 0.0031622776601683794 //sqrt(0.00001)
        internal const val kEpsilon2 = kEpsilonSqrt * kEpsilonSqrt * kEpsilonSqrt * kEpsilonSqrt //0.00001 ** 2
    }
}