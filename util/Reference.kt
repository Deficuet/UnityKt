package io.github.deficuet.unitykt.util

class IntRef(value: Int) {
    var value = value
        private set

    operator fun plusAssign(o: Int) { value += o }
    operator fun compareTo(o: Int) = value.compareTo(o)
    operator fun plus(o: Int) = value + o
    operator fun plus(u: UInt) = value.toUInt() + u
}

class StringRef(var value: String = "")