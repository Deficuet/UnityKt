package io.github.deficuet.unitykt.export.spirv

internal class Meta private constructor() {
    enum class Tools(val id: Int, val vendor: String, val toolName: String = "") {
        Khronos0(0, "Khronos"),
        LunarG(1, "LunarG"),
        Valve(2, "Valve"),
        Codeplay(3, "Codeplay"),
        NVIDIA(4, "NVIDIA"),
        ARM(5, "ARM"),
        Khronos1(6, "Khronos", "LLVM/SPIR-V Translator"),
        Khronos2(7, "Khronos", "SPIR-V Tools Assembler"),
        Khronos3(8, "Khronos", "Glslang Reference Front End"),
        Qualcomm(9, "Qualcomm"),
        AMD(10, "AMD"),
        Intel(11, "Intel"),
        Imagination(12, "Imagination"),
        Google0(13, "Google", "Shaderc over Glslang"),
        Google1(14, "Google", "spiregg"),
        Google2(15, "Google", "rspirv"),
        X_LEGEND(16, "X_LEGEND", "Mesa-IR/SPIR-V Translator"),
        Khronos(17, "Khronos", "SPIR-V Tools Linker");

        companion object {
            fun of(value: Int): Tools? {
                return values().firstOrNull { it.id == value }
            }
        }
    }
}