package io.github.deficuet.unitykt.data

import io.github.deficuet.unitykt.util.ObjectReader
import io.github.deficuet.unitykt.util.compareTo

class MonoScript internal constructor(reader: ObjectReader): NamedObject(reader) {
    val mClassName: String
    val mNameSpace: String
    val mAssemblyName: String

    init {
        if (unityVersion >= intArrayOf(3, 4)) reader += 4   //m_ExecutionOrder: Int
        reader += if (unityVersion < intArrayOf(5)) 4 else 16      //m_PropertiesHash: UInt/Bytes(16)
        if (unityVersion < intArrayOf(3)) reader.readAlignedString()
        mClassName = reader.readAlignedString()
        mNameSpace = if (unityVersion >= intArrayOf(3)) reader.readAlignedString() else ""
        mAssemblyName = reader.readAlignedString()
        if (unityVersion < intArrayOf(2018, 2)) reader += 1     //m_IsEditorScript: Boolean
    }
}