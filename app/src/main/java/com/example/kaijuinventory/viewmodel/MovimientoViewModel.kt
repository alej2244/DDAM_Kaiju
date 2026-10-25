package com.example.kaijuinventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaijuinventory.KaijuApp
import com.example.kaijuinventory.model.Movimiento
import com.example.kaijuinventory.model.Producto
import com.example.kaijuinventory.model.TipoMovimiento
import com.example.kaijuinventory.nativo.Notificaciones
import com.example.kaijuinventory.nativo.Vibracion
import com.example.kaijuinventory.validation.MovimientoValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovimientoFormState(
    val producto: Producto? = null,
    val tipo: TipoMovimiento = TipoMovimiento.ENTRADA,
    val cantidad: String = "",
    val nota: String = "",
    val cantidadError: String? = null,
    val notaError: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val intentosFallidos: Int = 0,
    val alertaNotificada: Boolean = false
) {
    /** Vista previa del stock que quedará, o null si la cantidad aún no es válida. */
    val stockResultante: Int?
        get() {
            val p = producto ?: return null
            val n = cantidad.trim().toIntOrNull() ?: return null
            if (cantidadError != null) return null
            return tipo.aplicar(p.stock, n)
        }
}

class MovimientoViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as KaijuApp).repository

    private val _estado = MutableStateFlow(MovimientoFormState())
    val estado: StateFlow<MovimientoFormState> = _estado.asStateFlow()

    fun cargar(productoId: Long) {
        if (_estado.value.producto?.id == productoId) return
        viewModelScope.launch {
            val p = repository.obtenerProducto(productoId)
            _estado.update { it.copy(producto = p) }
        }
    }

    fun onTipo(tipo: TipoMovimiento) = _estado.update { s ->
        s.copy(
            tipo = tipo,
            cantidadError = if (s.cantidad.isBlank()) null else validarCantidad(s.cantidad, tipo),
            notaError = if (s.nota.isBlank()) null else MovimientoValidator.nota(s.nota, tipo)
        )
    }

    fun onCantidad(v: String) = _estado.update { it.copy(cantidad = v, cantidadError = validarCantidad(v, it.tipo)) }

    fun onNota(v: String) = _estado.update { it.copy(nota = v, notaError = MovimientoValidator.nota(v, it.tipo)) }

    private fun validarCantidad(v: String, tipo: TipoMovimiento): String? =
        MovimientoValidator.cantidad(v, tipo, _estado.value.producto?.stock ?: 0)

    fun guardar() {
        val s = _estado.value
        val producto = s.producto ?: return
        if (s.guardando) return
        val errorCantidad = validarCantidad(s.cantidad, s.tipo)
        val errorNota = MovimientoValidator.nota(s.nota, s.tipo)
        if (errorCantidad != null || errorNota != null) {
            _estado.update {
                it.copy(cantidadError = errorCantidad, notaError = errorNota, intentosFallidos = it.intentosFallidos + 1)
            }
            return
        }
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val cantidad = s.cantidad.trim().toInt()
            val actualizado = repository.registrarMovimiento(
                Movimiento(
                    productoId = producto.id,
                    tipo = s.tipo,
                    cantidad = cantidad,
                    stockResultante = s.tipo.aplicar(producto.stock, cantidad),
                    nota = s.nota
                )
            )
            Vibracion.corta(getApplication<Application>())
            val notificada = actualizado != null && actualizado.stockBajo &&
                Notificaciones.notificarStockBajo(getApplication<Application>(), actualizado)
            _estado.update { it.copy(guardando = false, guardado = true, producto = actualizado, alertaNotificada = notificada) }
        }
    }
}
