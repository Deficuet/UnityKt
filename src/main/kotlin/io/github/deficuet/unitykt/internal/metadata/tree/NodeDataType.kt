package io.github.deficuet.unitykt.internal.metadata.tree

internal enum class NodeDataType(val isPrimitive: Boolean) {
    INT_8(true),
    UINT_8(true),
    INT_16(true),
    UINT_16(true),
    INT_32(true),
    UINT_32(true),
    INT_64(true),
    UINT_64(true),
    FLOAT(true),
    DOUBLE(true),
    BOOL(true),
    CHAR(false),
    STRING(true),
    MAP(false),
    TYPELESS(false),
    COMPOSITE(false);

    companion object {
        fun of(rawType: String): NodeDataType {
            return when(rawType) {
                "SInt8" -> INT_8
                "UInt8" -> UINT_8
                "char" -> CHAR
                "SInt16", "short" -> INT_16
                "UInt16", "unsigned short" -> UINT_16
                "SInt32", "int" -> INT_32
                "UInt32", "unsigned int", "Type*" -> UINT_32
                "SInt64", "long long" -> INT_64
                "UInt64", "unsigned long long", "FileSize" -> UINT_64
                "float" -> FLOAT
                "double" -> DOUBLE
                "bool" -> BOOL
                "string" -> STRING
                "map" -> MAP
                "TypelessData" -> TYPELESS
                else -> COMPOSITE
            }
        }
    }
}
