package io.github.deficuet.unitykt.math

/**
 * ```
 *                     column no.
 *                0     1     2     3
 *              -----------------------
 *           0  [   ]|[   ]|[   ]|[   ]
 * row no.   1  [   ]|[   ]|[   ]|[   ]
 *           2  [   ]|[   ]|[   ]|[   ]
 *           3  [   ]|[   ]|[   ]|[   ]
 * ```
 * @param data `float[4][4]` Array of 4 **column** `float[4]`
 */
class Matrix4x4(private val data: Array<FloatArray>) {
    private constructor(dataBlock: () -> Array<FloatArray>): this(dataBlock())

    operator fun get(row: Int, column: Int) = data[column][row]

    /**
     * Count by row
     */
    operator fun get(index: Int) = data[index % 4][index / 4]
    operator fun set(row: Int, column: Int, value: Float) { data[column][row] = value }

    /**
     * Count by row
     */
    operator fun set(index: Int, value: Float) { data[index % 4][index / 4] = value }

    /**
     * Row vector
     */
    fun row(index: Int) = Vector4(data[0][index], data[1][index], data[2][index], data[3][index])

    /**
     * Column vector
     */
    fun column(index: Int) = with(data[index]) { Vector4(get(0), get(1), get(2), get(3)) }

    fun transpose() = Matrix4x4 {
        Array(4) { col ->
            FloatArray(4) { row ->
                data[row][col]
            }
        }
    }

    operator fun plus(m: Matrix4x4) = Matrix4x4 {
        Array(4) { col ->
            FloatArray(4) { row ->
                data[col][row] + m[row, col]
            }
        }
    }

    operator fun minus(m: Matrix4x4) = Matrix4x4 {
        Array(4) { col ->
            FloatArray(4) { row ->
                data[col][row] - m[row, col]
            }
        }
    }

    /**
     * For matrix, usually A * B != B * A
     */
    operator fun times(m: Matrix4x4) = Matrix4x4 {
        Array(4) { col ->
            FloatArray(4) { row ->
                data[0][row] * m[0, col] +
                data[1][row] * m[1, col] +
                data[2][row] * m[2, col] +
                data[3][row] * m[3, col]
            }
        }
    }

    operator fun <N: Number> times(m: N) = Matrix4x4 {
        val n = m.toFloat()
        Array(4) { col ->
            FloatArray(4) { row ->
                data[col][row] * n
            }
        }
    }

    /**
     * A * **v**
     */
    operator fun times(v4: Vector4): Vector4 {
        return FloatArray(4) { row ->
            data[0][row] * v4.x + data[1][row] * v4.y +
            data[2][row] * v4.z + data[3][row] * v4.w
        }.let { Vector4(it[0], it[1], it[2], it[3]) }
    }

    operator fun unaryMinus() = Matrix4x4 {
        Array(4) { col ->
            FloatArray(4) { row ->
                -data[col][row]
            }
        }
    }

    override fun hashCode(): Int {
        return column(0).hashCode()
            .xor(column(1).hashCode().shl(2))
            .xor(column(2).hashCode().shr(2))
            .xor(column(3).hashCode().shr(1))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Matrix4x4
        for (i in 0..4) {
            if (column(i) != other.column(i)) {
                return false
            }
        }
        return true
    }

    fun toString(precision: Int): String {
        val strArray = data.map { fa ->
            val l = fa.maxOf { f -> "%.${precision}f".format(f).length }
            Array(4) { index -> "%-${l}.${precision}f".format(fa[index]) }
        }
        return "[" + intArrayOf(0, 1, 2, 3).map { row ->
            strArray.joinToString("  ") { it[row] }
        }.joinToString("\n") { " [ $it ] " }.trim() + "]"
    }

    override fun toString() = toString(2)

    companion object {
        fun scaleMatrix(v3: Vector3) = Matrix4x4 {
            val v4 = Vector4(v3, 1f)
            Array(4) { col ->
                FloatArray(4) { row ->
                    if (row == col) v4[col] else 0f
                }
            }
        }

        fun translateMatrix(v3: Vector3) = Matrix4x4 {
            Array(4) { col ->
                FloatArray(4) { row ->
                    when (col) {
                        row -> 1f
                        3 -> v3[row]
                        else -> 0f
                    }
                }
            }
        }

        fun rotateMatrix(q: Quaternion) = Matrix4x4 {
            val x = q.a * 2;    val y = q.b * 2;    val z = q.c * 2
            val xx = q.a * x;   val yy = q.b * y;   val zz = q.c * z
            val xy = q.a * y;   val xz = q.a * z;   val yz = q.b * z
            val wx = q.d * x;   val wy = q.d * y;   val wz = q.d * z
            arrayOf(
                floatArrayOf( 1 - yy - zz,    xy + wz,        xz - wy,        0f ),
                floatArrayOf( xy - wz,        1 - xx - zz,    yz + wx,        0f ),
                floatArrayOf( xz + wy,        yz - wx,        1 - xx - yy,    0f ),
                floatArrayOf( 0f,             0f,             0f,             1f )
            )
        }
    }
}
