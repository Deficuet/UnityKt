package io.github.deficuet.unitykt.enums

@Suppress("EnumEntryName")
enum class BuildTarget(override val id: Int): NumericalEnum<Int> {
    NoTarget(-2),
    AnyPlayer(-1),
    ValidPlayer(1),
    StandaloneOSX(2),
    StandaloneOSXPPC(3),
    StandaloneOSXIntel(4),
    StandaloneWindows(5),
    WebPlayer(6),
    WebPlayerStreamed(7),
    Wii(8),
    iOS(9),
    PS3(10),
    XBOX360(11),
    Broadcom(12),
    Android(13),
    StandaloneGLESEmu(14),
    StandaloneGLES20Emu(15),
    NaCl(16),
    StandaloneLinux(17),
    FlashPlayer(18),
    StandaloneWindows64(19),
    WebGL(20),
    WSAPlayer(21),
    StandaloneLinux64(24),
    StandaloneLinuxUniversal(25),
    WP8Player(26),
    StandaloneOSXIntel64(27),
    BlackBerry(28),
    Tizen(29),
    PSP2(30),
    PS4(31),
    PSM(32),
    XboxOne(33),
    SamsungTV(34),
    N3DS(35),
    WiiU(36),
    tvOS(37),
    Switch(38),
    Lumin(39),
    Stadia(40),
    CloudRendering(41),
    GameCoreXboxSeries(42),
    GameCoreXboxOne(43),
    PS5(44),
    EmbeddedLinux(45),
    QNX(46),

    UnknownPlatform(9999);

    companion object: NumericalEnumCompanion<Int, BuildTarget>(BuildTarget.values(), NoTarget) {
        fun isDefined(v: Int): Boolean = cacheTable[v] != null
    }
}