package io.github.deficuet.unitykt.pptr

import io.github.deficuet.unitykt.data.UnityDataClass
import io.github.deficuet.unitykt.data.UnityObject
import io.github.deficuet.unitykt.metadata.UnityDataClassCompanion

class PPtr<out T: UnityObject>(obj: UnityObject): UnityDataClass(obj) {
    val mFileID: Int    by bind("m_FileID")
    val mPathID: Long   by bind("m_PathID")

    val isNull get() = mPathID == 0L || mFileID < 0

    companion object: UnityDataClassCompanion<PPtr<*>>(PPtr::class.java) {
        override fun load(obj: UnityObject): PPtr<*> {
            return PPtr<UnityObject>(obj)
        }
    }
}
