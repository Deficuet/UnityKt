package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.PlayerSettings
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.compareTo

internal class PlayerSettingsImpl(
    assetFile: SerializedFile, info: ObjectInfo
): PlayerSettings, PlayerSettingsFields(assetFile, info) {
    override val companyName: String get() {
        checkInitialize()
        return fCompanyName
    }
    override val productName: String get() {
        checkInitialize()
        return fProductName
    }

    override fun read() {
        super.read()
        if (unityVersion >= intArrayOf(5, 4)) {
            reader.skip(16)    //productGUID: ByteArray(16)
        }
        reader.skip(1)     //AndroidProfiler: Boolean
        reader.alignStream()
        reader.skip(8)     //defaultScreenOrientation, targetDevice: Int
        if (unityVersion < intArrayOf(5, 3)) {
            if (unityVersion[0] < 5) {
                reader.skip(4)     //targetPlatform: Int
                if (unityVersion >= intArrayOf(4, 6)) {
                    reader.skip(4)     //targetIOSGraphics: Int
                }
            }
            reader.skip(4)     //targetResolution: Int
        } else {
            reader.skip(1)     //useOnDemandResources: Boolean
            reader.alignStream()
        }
        if (unityVersion >= intArrayOf(3, 5)) {
            reader.skip(4)     //accelerometerFrequency: Int
        }
        fCompanyName = reader.readAlignedString()
        fProductName = reader.readAlignedString()
    }
}