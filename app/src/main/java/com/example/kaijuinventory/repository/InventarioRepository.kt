package com.example.kaijuinventory.repository

import com.example.kaijuinventory.data.KaijuDatabase
import com.example.kaijuinventory.model.Movimiento
import com.example.kaijuinventory.model.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Única fuente de verdad del inventario. Los ViewModels no conocen SQLite:
 * piden datos aquí y observan los StateFlow, que se refrescan tras cada escritura.
 */
class InventarioRepository(private val db: KaijuDatabase) {

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _movimientos = MutableStateFlow<List<Movimiento>>(emptyList())
    val movimientos: StateFlow<List<Movimiento>> = _movimientos.asStateFlow()

    private var cargado = false

    suspend fun cargarSiHaceFalta() {
        if (!cargado) refrescar()
    }

    suspend fun refrescar() = withContext(Dispatchers.IO) {
        _productos.value = db.obtenerProductos()
        _movimientos.value = db.obtenerMovimientos()
        cargado = true
    }

    suspend fun obtenerProducto(id: Long): Producto? = withContext(Dispatchers.IO) {
        db.obtenerProducto(id)
    }

    suspend fun existeCodigo(codigo: String, excluirId: Long): Boolean = withContext(Dispatchers.IO) {
        db.existeCodigo(codigo, excluirId)
    }

    /** Inserta si el producto es nuevo (id = 0) o actualiza si ya existe. Devuelve el id. */
    suspend fun guardarProducto(producto: Producto): Long {
        val id = withContext(Dispatchers.IO) {
            if (producto.id == 0L) db.insertarProducto(producto)
            else {
                db.actualizarProducto(producto)
                producto.id
            }
        }
        refrescar()
        return id
    }

    suspend fun eliminarProducto(producto: Producto) {
        withContext(Dispatchers.IO) {
            db.eliminarProducto(producto.id)
            producto.fotoPath?.let { File(it).delete() }
        }
        refrescar()
    }

    /** Guarda el movimiento y devuelve el producto con su stock actualizado. */
    suspend fun registrarMovimiento(movimiento: Movimiento): Producto? {
        val actualizado = withContext(Dispatchers.IO) {
            db.registrarMovimiento(movimiento)
            db.obtenerProducto(movimiento.productoId)
        }
        refrescar()
        return actualizado
    }
}
