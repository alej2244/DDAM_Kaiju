package com.example.kaijuinventory.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.kaijuinventory.model.Movimiento
import com.example.kaijuinventory.model.Producto
import com.example.kaijuinventory.model.TipoMovimiento

/**
 * Persistencia local con SQLite. Guarda productos y movimientos de inventario
 * en el almacenamiento interno del dispositivo, por lo que los datos sobreviven
 * al cierre de la aplicación.
 */
class KaijuDatabase(context: Context) : SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $T_PRODUCTOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL,
                descripcion TEXT NOT NULL DEFAULT '',
                categoria TEXT NOT NULL,
                precio INTEGER NOT NULL,
                stock INTEGER NOT NULL,
                stock_minimo INTEGER NOT NULL,
                foto_path TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $T_MOVIMIENTOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                producto_id INTEGER NOT NULL REFERENCES $T_PRODUCTOS(id) ON DELETE CASCADE,
                tipo TEXT NOT NULL,
                cantidad INTEGER NOT NULL,
                stock_resultante INTEGER NOT NULL,
                nota TEXT NOT NULL DEFAULT '',
                fecha INTEGER NOT NULL
            )
            """.trimIndent()
        )
        DatosIniciales.cargar(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $T_MOVIMIENTOS")
        db.execSQL("DROP TABLE IF EXISTS $T_PRODUCTOS")
        onCreate(db)
    }

    // ---------- Productos ----------

    fun obtenerProductos(): List<Producto> =
        readableDatabase.query(T_PRODUCTOS, null, null, null, null, null, "nombre COLLATE NOCASE ASC")
            .use { c -> buildList { while (c.moveToNext()) add(c.toProducto()) } }

    fun obtenerProducto(id: Long): Producto? =
        readableDatabase.query(T_PRODUCTOS, null, "id = ?", arrayOf(id.toString()), null, null, null)
            .use { c -> if (c.moveToFirst()) c.toProducto() else null }

    fun existeCodigo(codigo: String, excluirId: Long): Boolean =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $T_PRODUCTOS WHERE codigo = ? COLLATE NOCASE AND id <> ?",
            arrayOf(codigo.trim(), excluirId.toString())
        ).use { c -> c.moveToFirst() && c.getInt(0) > 0 }

    /** Inserta el producto y, si trae stock inicial, registra su movimiento de entrada. */
    fun insertarProducto(producto: Producto): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val id = db.insertOrThrow(T_PRODUCTOS, null, producto.toValues())
            if (producto.stock > 0) {
                db.insertOrThrow(
                    T_MOVIMIENTOS, null,
                    Movimiento(
                        productoId = id,
                        tipo = TipoMovimiento.ENTRADA,
                        cantidad = producto.stock,
                        stockResultante = producto.stock,
                        nota = "Stock inicial"
                    ).toValues()
                )
            }
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    /** Actualiza los datos descriptivos. El stock no se edita aquí: cambia solo con movimientos. */
    fun actualizarProducto(producto: Producto): Int {
        val valores = producto.toValues().apply { remove("stock") }
        return writableDatabase.update(T_PRODUCTOS, valores, "id = ?", arrayOf(producto.id.toString()))
    }

    fun eliminarProducto(id: Long): Int =
        writableDatabase.delete(T_PRODUCTOS, "id = ?", arrayOf(id.toString()))

    // ---------- Movimientos ----------

    /**
     * Registra un movimiento y actualiza el stock del producto en una sola transacción,
     * así el historial y el stock nunca quedan desincronizados.
     */
    fun registrarMovimiento(movimiento: Movimiento): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val id = db.insertOrThrow(T_MOVIMIENTOS, null, movimiento.toValues())
            db.update(
                T_PRODUCTOS,
                ContentValues().apply { put("stock", movimiento.stockResultante) },
                "id = ?",
                arrayOf(movimiento.productoId.toString())
            )
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerMovimientos(): List<Movimiento> =
        readableDatabase.rawQuery(
            """
            SELECT m.*, p.nombre AS nombre_producto
            FROM $T_MOVIMIENTOS m JOIN $T_PRODUCTOS p ON p.id = m.producto_id
            ORDER BY m.fecha DESC, m.id DESC
            """.trimIndent(),
            null
        ).use { c -> buildList { while (c.moveToNext()) add(c.toMovimiento()) } }

    // ---------- Mapeo ----------

    private fun Producto.toValues() = ContentValues().apply {
        put("codigo", codigo.trim().uppercase())
        put("nombre", nombre.trim())
        put("descripcion", descripcion.trim())
        put("categoria", categoria)
        put("precio", precio)
        put("stock", stock)
        put("stock_minimo", stockMinimo)
        put("foto_path", fotoPath)
    }

    private fun Movimiento.toValues() = ContentValues().apply {
        put("producto_id", productoId)
        put("tipo", tipo.name)
        put("cantidad", cantidad)
        put("stock_resultante", stockResultante)
        put("nota", nota.trim())
        put("fecha", fecha)
    }

    private fun Cursor.toProducto() = Producto(
        id = getLong(getColumnIndexOrThrow("id")),
        codigo = getString(getColumnIndexOrThrow("codigo")),
        nombre = getString(getColumnIndexOrThrow("nombre")),
        descripcion = getString(getColumnIndexOrThrow("descripcion")),
        categoria = getString(getColumnIndexOrThrow("categoria")),
        precio = getInt(getColumnIndexOrThrow("precio")),
        stock = getInt(getColumnIndexOrThrow("stock")),
        stockMinimo = getInt(getColumnIndexOrThrow("stock_minimo")),
        fotoPath = getColumnIndexOrThrow("foto_path").let { if (isNull(it)) null else getString(it) }
    )

    private fun Cursor.toMovimiento() = Movimiento(
        id = getLong(getColumnIndexOrThrow("id")),
        productoId = getLong(getColumnIndexOrThrow("producto_id")),
        nombreProducto = getString(getColumnIndexOrThrow("nombre_producto")),
        tipo = TipoMovimiento.valueOf(getString(getColumnIndexOrThrow("tipo"))),
        cantidad = getInt(getColumnIndexOrThrow("cantidad")),
        stockResultante = getInt(getColumnIndexOrThrow("stock_resultante")),
        nota = getString(getColumnIndexOrThrow("nota")),
        fecha = getLong(getColumnIndexOrThrow("fecha"))
    )

    companion object {
        const val NOMBRE_BD = "kaiju_inventario.db"
        const val VERSION = 1
        const val T_PRODUCTOS = "productos"
        const val T_MOVIMIENTOS = "movimientos"
    }
}
