package com.example.kaijuinventory.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaijuinventory.KaijuApp
import com.example.kaijuinventory.model.Producto
import com.example.kaijuinventory.nativo.ImagenStorage
import com.example.kaijuinventory.nativo.Notificaciones
import com.example.kaijuinventory.validation.ProductoValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado completo del formulario: valores, error por campo y estado de guardado. */
data class ProductoFormState(
    val id: Long = 0,
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val precio: String = "",
    val stock: String = "",
    val stockMinimo: String = "",
    val fotoPath: String? = null,
    val codigoError: String? = null,
    val nombreError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null,
    val stockMinimoError: String? = null,
    val procesandoFoto: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val intentosFallidos: Int = 0,
    val mensaje: String? = null
) {
    val esEdicion: Boolean get() = id != 0L
    val errores: List<String?>
        get() = listOf(
            codigoError, nombreError, descripcionError, categoriaError,
            precioError, stockError, stockMinimoError
        )
}

class ProductoFormViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as KaijuApp).repository

    private val _estado = MutableStateFlow(ProductoFormState())
    val estado: StateFlow<ProductoFormState> = _estado.asStateFlow()

    private var fotoOriginal: String? = null
    private var idCargado: Long? = null

    /** Carga el producto a editar una sola vez (id = -1 significa producto nuevo). */
    fun cargar(id: Long) {
        if (idCargado == id) return
        idCargado = id
        if (id <= 0) return
        viewModelScope.launch {
            val p = repository.obtenerProducto(id) ?: return@launch
            fotoOriginal = p.fotoPath
            _estado.value = ProductoFormState(
                id = p.id,
                codigo = p.codigo,
                nombre = p.nombre,
                descripcion = p.descripcion,
                categoria = p.categoria,
                precio = p.precio.toString(),
                stock = p.stock.toString(),
                stockMinimo = p.stockMinimo.toString(),
                fotoPath = p.fotoPath
            )
        }
    }

    // Cada cambio valida su campo al instante: el error aparece y desaparece en tiempo real.
    fun onCodigo(v: String) = _estado.update { it.copy(codigo = v.uppercase(), codigoError = ProductoValidator.codigo(v)) }
    fun onNombre(v: String) = _estado.update { it.copy(nombre = v, nombreError = ProductoValidator.nombre(v)) }
    fun onDescripcion(v: String) = _estado.update { it.copy(descripcion = v, descripcionError = ProductoValidator.descripcion(v)) }
    fun onCategoria(v: String) = _estado.update { it.copy(categoria = v, categoriaError = ProductoValidator.categoria(v)) }
    fun onPrecio(v: String) {
        val limpio = v.filterNot { c -> c == '.' || c == ' ' } // acepta "1.490" escrito con separador de miles
        _estado.update { it.copy(precio = limpio, precioError = ProductoValidator.precio(limpio)) }
    }
    fun onStock(v: String) = _estado.update { it.copy(stock = v, stockError = ProductoValidator.stock(v)) }
    fun onStockMinimo(v: String) = _estado.update { it.copy(stockMinimo = v, stockMinimoError = ProductoValidator.stockMinimo(v)) }

    fun onFotoCapturada(bitmap: Bitmap) = procesarFoto { ImagenStorage.guardarBitmap(getApplication<Application>(), bitmap) }

    fun onFotoSeleccionada(uri: Uri) = procesarFoto { ImagenStorage.guardarDesdeUri(getApplication<Application>(), uri) }

    fun quitarFoto() {
        reemplazarFoto(null)
    }

    fun mostrarMensaje(texto: String) = _estado.update { it.copy(mensaje = texto) }

    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

    private fun procesarFoto(guardar: () -> String?) {
        _estado.update { it.copy(procesandoFoto = true) }
        viewModelScope.launch {
            val ruta = withContext(Dispatchers.IO) { runCatching(guardar).getOrNull() }
            if (ruta == null) {
                _estado.update { it.copy(procesandoFoto = false, mensaje = "No se pudo cargar la imagen") }
            } else {
                reemplazarFoto(ruta)
                _estado.update { it.copy(procesandoFoto = false) }
            }
        }
    }

    /** Borra la foto temporal anterior (nunca la original hasta guardar). */
    private fun reemplazarFoto(nueva: String?) {
        val anterior = _estado.value.fotoPath
        if (anterior != null && anterior != fotoOriginal) ImagenStorage.eliminar(anterior)
        _estado.update { it.copy(fotoPath = nueva) }
    }

    private fun validarTodo(): Boolean {
        val s = _estado.value
        val validado = s.copy(
            codigoError = ProductoValidator.codigo(s.codigo),
            nombreError = ProductoValidator.nombre(s.nombre),
            descripcionError = ProductoValidator.descripcion(s.descripcion),
            categoriaError = ProductoValidator.categoria(s.categoria),
            precioError = ProductoValidator.precio(s.precio),
            stockError = ProductoValidator.stock(s.stock),
            stockMinimoError = ProductoValidator.stockMinimo(s.stockMinimo)
        )
        _estado.value = validado
        return validado.errores.all { it == null }
    }

    fun guardar() {
        if (_estado.value.guardando) return
        if (!validarTodo()) {
            _estado.update { it.copy(intentosFallidos = it.intentosFallidos + 1, mensaje = "Revisa los campos marcados en rojo") }
            return
        }
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val s = _estado.value
            if (repository.existeCodigo(s.codigo, s.id)) {
                _estado.update {
                    it.copy(
                        guardando = false,
                        codigoError = "Ya existe un producto con este código",
                        intentosFallidos = it.intentosFallidos + 1
                    )
                }
                return@launch
            }
            val producto = Producto(
                id = s.id,
                codigo = s.codigo.trim(),
                nombre = s.nombre.trim(),
                descripcion = s.descripcion.trim(),
                categoria = s.categoria,
                precio = s.precio.trim().toInt(),
                stock = s.stock.trim().toInt(),
                stockMinimo = s.stockMinimo.trim().toInt(),
                fotoPath = s.fotoPath
            )
            val id = repository.guardarProducto(producto)
            if (fotoOriginal != null && fotoOriginal != s.fotoPath) ImagenStorage.eliminar(fotoOriginal)
            if (!s.esEdicion && producto.stockBajo) {
                Notificaciones.notificarStockBajo(getApplication<Application>(), producto.copy(id = id))
            }
            _estado.update { it.copy(guardando = false, guardado = true) }
        }
    }
}
