package io.github.deficuet.unitykt.pptr

import io.github.deficuet.unitykt.data.UnityObject

interface PPtr<out T: UnityObject> {
    val mFileID: Int
    val mPathID: Long
    val isNull: Boolean
}
