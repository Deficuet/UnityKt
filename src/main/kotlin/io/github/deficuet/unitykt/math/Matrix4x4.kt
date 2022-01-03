package io.github.deficuet.unitykt.math

data class Matrix4x4(private val data: DoubleArray) {
    private constructor(dataBlock: () -> DoubleArray): this(dataBlock())

    init {
        if (data.size != 16)
            throw IllegalStateException("There must be sixteen and only sixteen input values for Matrix.")
    }

    operator fun get(column: Int, row: Int) = data[column + row * 4]

    operator fun get(index: Int) = data[index]

    operator fun set(column: Int, row: Int, value: Double) { data[column + row * 4] = value }

    operator fun set(index: Int, value: Double) { data[index] = value }

    fun row(index: Int) = with(data.sliceArray(index * 4..index * 4 + 3)) {
        Vector4(get(0), get(1), get(2), get(3))
    }

    fun column(index: Int) = Vector4(data[index], data[index + 4], data[index + 8], data[index + 12])

    fun scale(v3: Vector3) = Matrix4x4 {
        DoubleArray(16) { 0.0 }.apply {
            set(0, v3.x); set(5, v3.y); set(10, v3.z); set(15, 1.0)
        }
    }

    fun translate(v3: Vector3) = Matrix4x4 {
        DoubleArray(16) { if (it % 5 == 0) 1.0 else 0.0 }.apply {
            set(3, v3.x); set(7, v3.y); set(11, v3.z)
        }
    }

    fun rotate(q: Quaternion) = Matrix4x4 {
        val x = q.a * 2;    val y = q.b * 2;    val z = q.c * 2
        val xx = q.a * x;   val yy = q.b * y;   val zz = q.c * z
        val xy = q.a * y;   val xz = q.a * z;   val yz = q.b * z
        val wx = q.d * x;   val wy = q.d * y;   val wz = q.d * z
        doubleArrayOf(
            1 - yy - zz,    xy + wz,        xz - wy,        0.0,
            xy - wz,        1 - xx - zz,    yz + wx,        0.0,
            xz + wy,        yz - wx,        1 - xx - yy,    0.0,
            0.0,            0.0,            0.0,            1.0
        )
    }

    operator fun times(m: Matrix4x4) = Matrix4x4 {
        mutableList<Double> {
            val t = this@Matrix4x4
            for (tc in 0..3) {
                for (mr in 0..3) {
                    var sum = 0.0
                    for (i in 0..3) sum += t[tc, i] * m[i, mr]
                    add(sum)
                }
            }
        }.toDoubleArray()
    }

    override fun hashCode(): Int {
        return column(0).hashCode() xor (column(1).hashCode() shl 2)
            .xor(column(2).hashCode() shr 2) xor (column(3).hashCode() shr 1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Matrix4x4

        if (!data.contentEquals(other.data)) return false

        return true
    }

    companion object {
        //Secondary constructor in order to avoid signature conflict due to type erasure
        operator fun invoke(data: List<Float>) = Matrix4x4(data.map { it.toDouble() }.toDoubleArray())
        operator fun invoke(vararg data: Float) = invoke(data.toList())
    }
}
