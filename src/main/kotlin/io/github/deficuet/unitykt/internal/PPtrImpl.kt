package io.github.deficuet.unitykt.internal

import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.internal.file.SerializedFile
import io.github.deficuet.unitykt.pptr.PPtr

internal class PPtrImpl<T: UnityObject>(
    override val mFileID: Int,
    override val mPathID: Long,
    private val serializedFile: SerializedFile
): PPtr<T> {
    override val isNull: Boolean
        get() = mPathID == 0L || mFileID < 0


}
