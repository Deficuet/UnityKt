package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader

abstract class Behaviour protected constructor(reader: ObjectReader): Component(reader) {
    val mEnabled = reader.readByte()
    init { reader.alignStream() }
}