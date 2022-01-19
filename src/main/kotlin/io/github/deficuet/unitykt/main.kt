package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.dataImpl.*

fun main() {
    val b = AssetManager.loadFile("F:/CS30Final/example\\baoduoliuhua")
    val mesh = b.objects.firstObjectOf<RectTransformImpl>()
    println(mesh.dump())
//    val tex = b.objects.firstObjectOf<Texture2D>()
//    println(tex.mTextureFormat)
//    ImageIO.write(
//        tex.image, "png",
//        File("F:\\UnityKt\\build\\libs\\artifacts\\unitykt_main_jar\\test.png")
//    )
    println("2018")
    val bb = Bar("abcd")
    println("end")
    println(bb.c)
    println(bb.a)
}

class ImplementationContainer<out T: FooImpl>(implConstructor: () -> T) {
    val obj by lazy(implConstructor)
}

open class Foo protected constructor(private val subObj: ImplementationContainer<FooImpl>) {
    constructor(p: String): this(ImplementationContainer { FooImpl(p) })

    val a get() = subObj.obj.a
    val b get() = subObj.obj.b
}

abstract class Med(private val subObj: ImplementationContainer<MedImpl>): Foo(subObj) {
    val d get() = subObj.obj.d
}

class Bar private constructor(private val obj: ImplementationContainer<BarImpl>): Med(obj) {
    constructor(p: String): this(ImplementationContainer { BarImpl(p) })
    val c get() = obj.obj.c
}

open class FooImpl(private val p: String) {
    val a = p[0]
    val b = p.hashCode()

    init {
        println("Foo")
    }
}

abstract class MedImpl(p: String): FooImpl(p) {
    val d = p[0].hashCode()

    init {
        println("med")
    }
}

class BarImpl(private val p: String): MedImpl(p) {
    val c = p == "abc"

    init {
        println("bar inited")
    }
}