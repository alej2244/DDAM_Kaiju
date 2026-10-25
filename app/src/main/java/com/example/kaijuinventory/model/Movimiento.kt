package com.example.kaijuinventory.model

enum class TipoMovimiento(val etiqueta: String) {
    ENTRADA("Entrada"),
    VENTA("Venta"),
    SALIDA("Salida"),
    AJUSTE("Ajuste");

    /** Calcula el stock resultante de aplicar este movimiento. */
    fun aplicar(stockActual: Int, cantidad: Int): Int = when (this) {
        ENTRADA -> stockActual + cantidad
        VENTA, SALIDA -> stockActual - cantidad
        AJUSTE -> cantidad
    }

    val restaStock: Boolean get() = this == VENTA || this == SALIDA
}

/** Registro de un ingreso, venta, salida o ajuste de stock. */
data class Movimiento(
    val id: Long = 0,
    val productoId: Long,
    val nombreProducto: String = "",
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val stockResultante: Int,
    val nota: String = "",
    val fecha: Long = System.currentTimeMillis()
)
