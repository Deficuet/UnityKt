package io.github.deficuet.unitykt.classes

interface Component: EditorExtension {
    val mGameObject: PPtr<GameObject>
}