package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.AvatarConstant
import io.github.deficuet.unitykt.dataImpl.AvatarImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class Avatar private constructor(
    private val container: ImplementationContainer<AvatarImpl>
): NamedObject(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { AvatarImpl(ObjectReader(assetFile, info)) })

    val mAvatarSize: UInt                   get() = container.impl.mAvatarSize
    val mAvatar: AvatarConstant             get() = container.impl.mAvatar
    val mTOS: Array<Pair<UInt, String>>     get() = container.impl.mTOS
}