package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.dataImpl.PlayerSettingImpl
import io.github.deficuet.unitykt.file.ObjectInfo
import io.github.deficuet.unitykt.file.SerializedFile
import io.github.deficuet.unitykt.util.ObjectReader

class PlayerSetting private constructor(
    private val container: ImplementationContainer<PlayerSettingImpl>
): Object(container) {
    internal constructor(assetFile: SerializedFile, info: ObjectInfo):
        this(ImplementationContainer(assetFile, info) { PlayerSettingImpl(ObjectReader(assetFile, info)) })

    val companyName: String get() = container.impl.companyName
    val productName: String get() = container.impl.productName
}