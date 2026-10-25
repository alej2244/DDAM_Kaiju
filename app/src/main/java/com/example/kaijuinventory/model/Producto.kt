package com.example.kaijuinventory.model

/**
 * Producto del inventario de Kaiju.
 * El stock solo cambia a través de movimientos para mantener la trazabilidad.
 */
data class Producto(
    val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val descripcion: String = "",
    val categoria: String,
    val precio: Int,
    val stock: Int,
    val stockMinimo: Int,
    val fotoPath: String? = null
) {
    val stockBajo: Boolean get() = stock <= stockMinimo
    val sinStock: Boolean get() = stock == 0
    val valorTotal: Long get() = precio.toLong() * stock
}

object Categorias {
    val todas = listOf("Alimentos", "Bebidas", "Limpieza", "Ropa", "Accesorios", "Insumos", "Otros")
}
