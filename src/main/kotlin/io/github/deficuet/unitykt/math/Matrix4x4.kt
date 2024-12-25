package io.github.deficuet.unitykt.math

/**
 * Column-major matrix
 * ```
 *                     column no.
 *                0     1     2     3
 *              -----------------------
 *           0  [   ]|[   ]|[   ]|[   ]
 * row no.   1  [   ]|[   ]|[   ]|[   ]
 *           2  [   ]|[   ]|[   ]|[   ]
 *           3  [   ]|[   ]|[   ]|[   ]
 * ```
 * @param data `float[4][4]` Array of 4 **column** `float[4]` array
 */
class Matrix4x4(private val data: Array<FloatArray>) {
    private constructor(dataBlock: () -> Array<FloatArray>): this(dataBlock())

    operator fun get(column: Int, row: Int) = data[column][row]

    /**
     * Count by column
     */
    operator fun get(index: Int) = data[index / 4][index % 4]

    operator fun set(column: Int, row: Int, value: Float) { data[column][row] = value }

    /**
     * Count by column
     */
    operator fun set(index: Int, value: Float) { data[index / 4][index % 4] = value }

    /**
     * Column vector
     */
    fun column(index: Int) = with(data[index]) { Vector4(get(0), get(1), get(2), get(3)) }

    /**
     * Row vector
     */
    fun row(index: Int) = Vector4(data[0][index], data[1][index], data[2][index], data[3][index])

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
                data[col][row] + m[col, row]
            }
        }
    }

    operator fun minus(m: Matrix4x4) = Matrix4x4 {
        Array(4) { col ->
            FloatArray(4) { row ->
                data[col][row] - m[col, row]
            }
        }
    }

    /**
     * Matrix multiplication
     */
    operator fun times(m: Matrix4x4) = Matrix4x4 {
        Array(4) { col ->
            FloatArray(4) { row ->
                data[0][row] * m[col, 0] +
                data[1][row] * m[col, 1] +
                data[2][row] * m[col, 2] +
                data[3][row] * m[col, 3]
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

    override fun toString(): String {
        return "[\n" + data.mapIndexed { i, column ->
            "\tcolumn[$i] = ${column.contentToString()}\n"
        }.joinToString("") + "]"
    }

    companion object {
        val I = Matrix4x4 {
            Array(4) { col ->
                FloatArray(4) { row ->
                    if (col == row) 1f else 0f
                }
            }
        }

        fun scaleMatrix(v3: Vector3) = Matrix4x4 {
            Array(4) { col ->
                FloatArray(4) { row ->
                    if (row == col) {
                        when (col) {
                            3 -> 1f
                            else -> v3[col]
                        }
                    } else {
                        0f
                    }
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
            val x = q.x * 2;    val y = q.y * 2;    val z = q.z * 2
            val xx = q.x * x;   val yy = q.y * y;   val zz = q.z * z
            val xy = q.x * y;   val xz = q.x * z;   val yz = q.y * z
            val wx = q.w * x;   val wy = q.w * y;   val wz = q.w * z
            arrayOf(
                floatArrayOf( 1 - yy - zz,    xy + wz,        xz - wy,        0f ),
                floatArrayOf( xy - wz,        1 - xx - zz,    yz + wx,        0f ),
                floatArrayOf( xz + wy,        yz - wx,        1 - xx - yy,    0f ),
                floatArrayOf( 0f,             0f,             0f,             1f )
            )
        }
    }
}
