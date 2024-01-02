package io.github.deficuet.unitykt.internal.impl

import io.github.deficuet.unitykt.classes.MonoScript
import io.github.deficuet.unitykt.internal.file.ObjectInfo
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.util.compareTo

internal class MonoScriptImpl(
    assetFile: SerializedFile, info: ObjectInfo
): MonoScript, MonoScriptFields(assetFile, info) {
    override val mClassName: String get() {
        checkInitialize()
        return fmClassName
    }
    override val mNameSpace: String get() {
        checkInitialize()
        return fmNameSpace
    }
    override val mAssemblyName: String get() {
        checkInitialize()
        return fmAssemblyName
    }

    override fun read() {
        super.read()
        if (unityVersion >= intArrayOf(3, 4)) reader.skip(4)   //m_ExecutionOrder: Int
        reader.skip(if (unityVersion[0] < 5) 4 else 16)      //m_PropertiesHash: UInt/Bytes(16)
        if (unityVersion < intArrayOf(3)) reader.readAlignedString()    //m_PathName: String
        fmClassName = reader.readAlignedString()
        fmNameSpace = if (unityVersion >= intArrayOf(3)) reader.readAlignedString() else ""
        fmAssemblyName = reader.readAlignedString()
        if (unityVersion < intArrayOf(2018, 2)) reader.skip(1)     //m_IsEditorScript: Boolean
    }
}