package io.github.deficuet.unitykt

import io.github.deficuet.unitykt.dataImpl.*
import java.io.File
import javax.imageio.ImageIO

fun main() {
//    val b = AssetManager.loadFile("F:\\CS30Final\\example\\baoduoliuhua")
//    val mesh = b.objects.firstObjectOf<Mesh>()
//    println(mesh.dump())
//    val tex = b.objects.firstObjectOf<Texture2D>()
//    println(tex.mTextureFormat)
//    ImageIO.write(
//        tex.image, "png",
//        File("F:\\UnityKt\\build\\libs\\artifacts\\unitykt_main_jar\\test.png")
//    )
    println("2010")
    val b = Bar("2019")
    println(b.a)
}

abstract class MetaObject<T: FooImpl>(implConstructor: () -> T) {
    protected val objImpl by lazy(implConstructor)
}

open class Foo<T: FooImpl>(implConstructor: () -> T): MetaObject<T>(implConstructor) {
    val a get() = objImpl.a
    val b get() = objImpl.b

    companion object {
        operator fun invoke(p: String): Foo<FooImpl> = Foo { FooImpl(p) }
    }
}

class Bar(p: String): Foo<BarImpl>({ BarImpl(p) }) {
    val c get() = objImpl.c
}

open class FooImpl(private val p: String) {
    val a = p[0]
    val b = p.hashCode()

    init {
        println("initialized")
    }
}

class BarImpl(private val p: String): FooImpl(p) {
    val c = p == "abc"

    init {
        println("bar inited")
    }
}