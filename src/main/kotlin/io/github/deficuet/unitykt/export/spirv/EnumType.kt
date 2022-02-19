package io.github.deficuet.unitykt.export.spirv

import io.github.deficuet.unitykt.util.Reference

internal class EnumType<E: Enum<E>, P: ParameterFactory> private constructor(
    private val enumClass: Class<E>,
    private val factory: P
): OperandType() {
    override fun readValue(words: Array<UInt>, index: Int, value: Reference<Any>): Int {
        var wordsUsedForParameters = 0
        val enumValues = enumClass.enumConstants
        if (FlagsAttributeEnum.flagsClass.isAssignableFrom(enumClass)) {
            val result = mutableMapOf<UInt, Array<Any>>()
            for (enum in enumValues) {
                val bit = (enum as FlagsAttributeEnum<*>).numeric.toUInt()
                if (words[index].and(bit) != 0u || (bit == 0u && words[index] == 0u)) {
                    result[bit] = factory.createParameter(bit)?.let { param ->
                        Array(param.operandTypes.size) {
                            val pValueRef = Reference<Any>()
                            val pWordsUsed = param.operandTypes[it].readValue(
                                words, 1 + wordsUsedForParameters, pValueRef
                            )
                            wordsUsedForParameters += pWordsUsed
                            pValueRef.value
                        }
                    } ?: emptyArray()
                }
            }
            value.value = BitEnumOperandValue(result, enumClass)
        } else {
            val result: Array<Any> = factory.createParameter(words[index])?.let { param ->
                Array(param.operandTypes.size) {
                    val pValueRef = Reference<Any>()
                    val pWordsUsed = param.operandTypes[it].readValue(
                        words, 1 + wordsUsedForParameters, pValueRef
                    )
                    wordsUsedForParameters += pWordsUsed
                    pValueRef.value
                }
            } ?: emptyArray()
            value.value = ValueEnumOperandValue(
                enumValues.firstOrNull { (it as NumericalEnum<*>).id == words[index] } ?: enumValues.first(),
                result, enumClass
            )
        }
        return wordsUsedForParameters + 1
    }

    companion object {
        internal inline operator fun <reified T: Enum<T>, reified U: ParameterFactory> invoke(): EnumType<T, U> {
            return EnumType(T::class.java, U::class.java.constructors[0].newInstance() as U)
        }

        internal inline fun <reified T: Enum<T>> create() = EnumType<T, ParameterFactory>()
    }
}