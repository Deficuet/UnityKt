package io.github.deficuet.unitykt.internal.metadata

import io.github.deficuet.unitykt.internal.metadata.tree.TypeTreeImpl
import io.github.deficuet.unitykt.metadata.SerializedType

internal class SerializedTypeImpl(
    override val classID: Int,
    override val isStrippedType: Boolean,
    override var scriptTypeIndex: Short,
    override val typeTree: TypeTreeImpl,
    override val scriptID: ByteArray,
    override val oldTypeHash: ByteArray,
    override val typeDependencies: IntArray,
    override val className: String,
    override val nameSpace: String,
    override val asmName: String,
): SerializedType
