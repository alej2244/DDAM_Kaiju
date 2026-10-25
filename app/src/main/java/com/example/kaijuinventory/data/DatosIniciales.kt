package com.example.kaijuinventory.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

/** Catálogo ficticio para que la app tenga datos de ejemplo la primera vez que se abre. */
object DatosIniciales {

    private data class Semilla(
        val codigo: String,
        val nombre: String,
        val descripcion: String,
        val categoria: String,
        val precio: Int,
        val stock: Int,
        val stockMinimo: Int,
        val vendidos: Int
    )

    private val catalogo = listOf(
        Semilla("ALI-001", "Galletas de avena", "Paquete 300 g", "Alimentos", 1490, 40, 10, 18),
        Semilla("BEB-001", "Agua mineral 1,5 L", "Sin gas", "Bebidas", 990, 6, 12, 30),
        Semilla("LIM-001", "Detergente líquido", "Botella 3 L", "Limpieza", 6990, 15, 5, 7),
        Semilla("ROP-001", "Polera Kaiju talla M", "Algodón, color negro", "Ropa", 9990, 3, 4, 9),
        Semilla("ACC-001", "Llavero Kaiju", "Metálico", "Accesorios", 2490, 25, 5, 12),
        Semilla("INS-001", "Bolsas de papel", "Pack 100 unidades", "Insumos", 3990, 0, 2, 4)
    )

    fun cargar(db: SQLiteDatabase) {
        val ahora = System.currentTimeMillis()
        val dia = 24L * 60 * 60 * 1000
        catalogo.forEachIndexed { i, s ->
            val stockInicial = s.stock + s.vendidos
            val id = db.insert(KaijuDatabase.T_PRODUCTOS, null, ContentValues().apply {
                put("codigo", s.codigo)
                put("nombre", s.nombre)
                put("descripcion", s.descripcion)
                put("categoria", s.categoria)
                put("precio", s.precio)
                put("stock", s.stock)
                put("stock_minimo", s.stockMinimo)
            })
            movimiento(db, id, "ENTRADA", stockInicial, stockInicial, "Stock inicial", ahora - (7 - i) * dia)
            movimiento(db, id, "VENTA", s.vendidos, s.stock, "Ventas de la semana", ahora - (3 - i % 3) * dia)
        }
    }

    private fun movimiento(
        db: SQLiteDatabase, productoId: Long, tipo: String,
        cantidad: Int, stockResultante: Int, nota: String, fecha: Long
    ) {
        db.insert(KaijuDatabase.T_MOVIMIENTOS, null, ContentValues().apply {
            put("producto_id", productoId)
            put("tipo", tipo)
            put("cantidad", cantidad)
            put("stock_resultante", stockResultante)
            put("nota", nota)
            put("fecha", fecha)
        })
    }
}
