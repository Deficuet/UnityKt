package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader

abstract class BehaviourImpl internal constructor(reader: ObjectReader): ComponentImpl(reader) {
    val mEnabled = reader.readByte()
    init { reader.alignStream() }
}