package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.data.EditorExtension
import io.github.deficuet.unitykt.data.Object
import io.github.deficuet.unitykt.data.PPtr
import io.github.deficuet.unitykt.file.BuildTarget
import io.github.deficuet.unitykt.util.ObjectReader

abstract class EditorExtensionImpl internal constructor(reader: ObjectReader): ObjectImpl(reader) {
    init {
        if (platform == BuildTarget.NoTarget) {
            PPtr<EditorExtension>(reader)   //m_PrefabParentObject
            PPtr<Object>(reader)            //m_PrefabInternal
        }
    }
}