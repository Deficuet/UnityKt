package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.file.BuildTarget
import io.github.deficuet.unitykt.util.ObjectReader

abstract class EditorExtensionImpl internal constructor(reader: ObjectReader): ObjectImpl(reader) {
    init {
        if (platform == BuildTarget.NoTarget) {
            PPtr<EditorExtensionImpl>(reader)   //m_PrefabParentObject
            PPtr<ObjectImpl>(reader)            //m_PrefabInternal
        }
    }
}