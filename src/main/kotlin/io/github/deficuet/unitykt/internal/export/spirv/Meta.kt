package io.github.deficuet.unitykt.internal.export.spirv

internal class Meta private constructor() {
    internal data class ToolInfo(val vendor: String, val toolName: String = "")

    companion object {
        internal const val magic = 119734787u
        internal val tools by lazy {
            mapOf(
                0 to ToolInfo("Khronos"),
                1 to ToolInfo("LunarG"),
                2 to ToolInfo("Valve"),
                3 to ToolInfo("Codeplay"),
                4 to ToolInfo("NVIDIA"),
                5 to ToolInfo("ARM"),
                6 to ToolInfo("Khronos", "LLVM/SPIR-V Translator"),
                7 to ToolInfo("Khronos", "SPIR-V Tools Assembler"),
                8 to ToolInfo("Khronos", "Glslang Reference Front End"),
                9 to ToolInfo("Qualcomm"),
                10 to ToolInfo("AMD"),
                11 to ToolInfo("Intel"),
                12 to ToolInfo("Imagination"),
                13 to ToolInfo("Google", "Shaderc over Glslang"),
                14 to ToolInfo("Google", "spiregg"),
                15 to ToolInfo("Google", "rspirv"),
                16 to ToolInfo("X_LEGEND", "Mesa-IR/SPIR-V Translator"),
                17 to ToolInfo("Khronos", "SPIR-V Tools Linker")
            )
        }
    }
}