package io.github.deficuet.unitykt.math

abstract class Vector {
    abstract val unit: Vector

    companion object {
        internal const val kEpsilonSqrt = 0.0031622776601683794     //sqrt(0.00001)
        internal const val kEpsilon2 = 1.0e-10    //0.00001 ** 2
    }
}