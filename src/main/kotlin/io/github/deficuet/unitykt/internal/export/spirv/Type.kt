package io.github.deficuet.unitykt.internal.export.spirv

internal abstract class Type {
    open fun toString(builder: StringBuilder): StringBuilder = builder.append(toString())

    override fun toString(): String {
        return ""
    }
}

internal class VoidType: Type() {
    override fun toString(): String {
        return "void"
    }
}

internal abstract class ScalarType: Type()

internal class BoolType: ScalarType() {
    override fun toString(): String {
        return "bool"
    }
}

internal class IntegerType(val width: Int, val signed: Boolean): ScalarType() {
    override fun toString(): String {
        return "${if (signed) "i" else "u"}$width"
    }
}

internal class FloatingPointType(val width: Int): ScalarType() {
    override fun toString(): String {
        return "f$width"
    }
}

internal class VectorType(private val componentType: ScalarType, private val componentCount: Int): Type() {
    override fun toString(): String {
        return "${componentType}_$componentCount"
    }
}

internal class MatrixType(private val columnType: VectorType, private val columnCount: Int): Type() {
    override fun toString(): String {
        return "${columnType}x$columnCount"
    }
}

internal class ImageType(
    private val dim: Dim, private val isArray: Boolean,
    private val isMultiSampled: Boolean,
    private val accessQualifier: AccessQualifier
): Type() {
    override fun toString(builder: StringBuilder): StringBuilder {
        when (accessQualifier) {
            AccessQualifier.ReadWrite -> builder.append("read_writer ")
            AccessQualifier.WriteOnly -> builder.append("write_only ")
            AccessQualifier.ReadOnly -> builder.append("read_only ")
        }
        builder.append("Texture")
        builder.append(
            when (dim) {
                Dim.Dim1D -> "1D"
                Dim.Dim2D -> "2D"
                Dim.Dim3D -> "3D"
                Dim.Cube -> "Cube"
                else -> ""
            }
        )
        if (isMultiSampled) builder.append("MS")
        if (isArray) builder.append("Array")
        return builder
    }

    override fun toString(): String {
        return StringBuilder().apply { toString(this) }.toString()
    }
}

internal class SamplerType: Type() {
    override fun toString(): String {
        return "sampler"
    }
}

internal class SampledImageType(private val imageType: ImageType): Type() {
    override fun toString(): String {
        return "${imageType}Sampled"
    }
}

internal class ArrayType(private val elementType: Type, private val elementCount: Int): Type() {
    override fun toString(): String {
        return "${elementType}[$elementCount]"
    }
}

internal class RuntimeArrayType: Type()

internal class StructType(private val memberTypes: Array<Type>): Type() {
    private val memberNames = Array(memberTypes.size) { "" }

    operator fun set(member: UInt, name: String) {
        memberNames[member.toInt()] = name
    }

    override fun toString(builder: StringBuilder): StringBuilder {
        builder.append("struct {")
        for (i in memberTypes.indices) {
            val member = memberTypes[i]
            member.toString(builder)
            if (memberNames[i].isNotEmpty()) {
                builder.append(" ${memberNames[i]}")
            }
            builder.append(';')
            if (i < memberTypes.size - 1) {
                builder.append(' ')
            }
        }
        builder.append('}')
        return builder
    }

    override fun toString(): String {
        return StringBuilder().apply { toString(this) }.toString()
    }
}

internal class OpaqueType: Type()

internal class PointerType(val storageClass: StorageClass, var type: Type? = null): Type() {
    fun resolveForwardReference(t: Type) { type = t }

    override fun toString(): String {
        return "$storageClass ${if (type != null) "$type" else ""}*"
    }
}

internal class FunctionType: Type()
