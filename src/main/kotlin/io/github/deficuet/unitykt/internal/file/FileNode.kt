package io.github.deficuet.unitykt.internal.file

import io.github.deficuet.unitykt.cast
import io.github.deficuet.unitykt.internal.ImportContextImpl

internal interface FileNode {
    val parent: FileNode
    val root: ImportContextImpl
        get() = if (parent is ImportContextImpl) parent.cast() else parent.root
}
