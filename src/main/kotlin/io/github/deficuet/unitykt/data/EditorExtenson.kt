package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.file.BuildTarget
import io.github.deficuet.unitykt.util.ObjectReader

abstract class EditorExtenson(reader: ObjectReader): Object(reader) {
    init {
        if (platform == BuildTarget.NoTarget) {

        }
    }
}