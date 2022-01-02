package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.file.BuildTarget
import io.github.deficuet.unitykt.util.ObjectReader

abstract class EditorExtension internal constructor(reader: ObjectReader): Object(reader) {
    init {
        if (platform == BuildTarget.NoTarget) {
            PPtr<EditorExtension>(reader)   //m_PrefabParentObject
            PPtr<Object>(reader)            //m_PrefabInternal
        }
    }
}