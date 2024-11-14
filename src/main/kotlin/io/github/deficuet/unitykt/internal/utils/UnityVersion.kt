package io.github.deficuet.unitykt.internal.utils

internal class UnityVersion {
    private val major: Int
    private val minor: Int
    private val patch: Int
    private val build: Int
    private val buildType: String

    constructor(vs: String) {
        val ret = VERSION_REGEX.matchEntire(vs) ?: throw IllegalArgumentException("Unknown unity version pattern")
        major = ret.groups["ma"]!!.value.toInt()
        minor = ret.groups["mi"]!!.value.toInt()
        patch = ret.groups["p"]!!.value.toInt()
        build = ret.groups["b"]?.value?.toInt() ?: 0
        buildType = ret.groups["bt"]?.value ?: ""
    }

    constructor(ma: Int, mi: Int, p: Int, bt: BuildType = BuildType.FINAL, b: Int = 1) {
        major = ma
        minor = mi
        patch = p
        build = b
        buildType = bt.symbol
    }

    operator fun get(i: Int): Int {
        return when (i) {
            0 -> major
            1 -> minor
            2 -> patch
            else -> throw IndexOutOfBoundsException(i)
        }
    }

    operator fun compareTo(v: Int) = major.compareTo(v)

    operator fun compareTo(v: IntArray): Int {
        for (i in 0 until minOf(3, v.size)) {
            val result = get(i).compareTo(v[i])
            if (result != 0) return result
        }
        return (3).compareTo(v.size)
    }

    companion object {
        val VERSION_REGEX = Regex("""(?<ma>\d+).(?<mi>\d+).(?<p>\d+)(?<bt>[a-z])?(?<b>\d+)?""")
    }
}

internal enum class BuildType(val symbol: String) {
    ALPHA("a"),
    BETA("b"),
    FINAL("f"),
    PATCH("p"),
    TUAN_JIE("t");
}
