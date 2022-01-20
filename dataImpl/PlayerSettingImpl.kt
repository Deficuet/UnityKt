package io.github.deficuet.unitykt.dataImpl

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class PlayerSettingImpl internal constructor(reader: ObjectReader): ObjectImpl(reader) {
    val companyName: String
    val productName: String

    init {
        if (unityVersion >= intArrayOf(5, 4)) {
            reader += 16    //productGUID: ByteArrat(16)
        }
        reader += 1     //AndroidProfiler: Boolean
        reader.alignStream()
        reader += 8     //defaultScreenOrientation, targetDevice: Int
        if (unityVersion < intArrayOf(5, 3)) {
            if (unityVersion[0] < 5) {
                reader += 4     //targetPlatform: Int
                if (unityVersion >= intArrayOf(4, 6)) {
                    reader += 4     //targetIOSGraphics: Int
                }
            }
            reader += 4     //targetResolution: Int
        } else {
            reader += 1     //useOnDemandResources: Boolean
            reader.alignStream()
        }
        if (unityVersion >= intArrayOf(3, 5)) {
            reader += 4     //accelerometerFrequency: Int
        }
        companyName = reader.readAlignedString()
        productName = reader.readAlignedString()
    }
}