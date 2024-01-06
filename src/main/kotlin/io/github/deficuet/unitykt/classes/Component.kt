package io.github.deficuet.unitykt.classes

import io.github.deficuet.unitykt.pptr.PPtr

interface Component: EditorExtension {
    val mGameObject: PPtr<GameObject>
}