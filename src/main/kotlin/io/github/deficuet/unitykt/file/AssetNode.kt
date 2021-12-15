package io.github.deficuet.unitykt.file

import io.github.deficuet.unitykt.util.AssetNodeOrReader

abstract class AssetNode: AssetNodeOrReader {
    abstract val files: Map<String, Any>
}