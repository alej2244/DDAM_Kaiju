package com.example.kaijuinventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaijuinventory.KaijuApp
import com.example.kaijuinventory.model.Movimiento
import com.example.kaijuinventory.model.Producto
import com.example.kaijuinventory.model.TipoMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResumenInventario(
    val totalProductos: Int = 0,
    val totalUnidades: Int = 0,
    val valorInventario: Long = 0,
    val stockBajo: List<Producto> = emptyList(),
    val masVendidos: List<Pair<String, Int>> = emptyList()
)

/**
 * Estado compartido por las pantallas principales (Inicio, Productos, Movimientos).
 * Expone StateFlow que la UI observa con collectAsStateWithLifecycle.
 */
class InventarioViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as KaijuApp).repository

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _categoria = MutableStateFlow<String?>(null)
    val categoria: StateFlow<String?> = _categoria.asStateFlow()

    private val _soloStockBajo = MutableStateFlow(false)
    val soloStockBajo: StateFlow<Boolean> = _soloStockBajo.asStateFlow()

    private val _filtroTipo = MutableStateFlow<TipoMovimiento?>(null)
    val filtroTipo: StateFlow<TipoMovimiento?> = _filtroTipo.asStateFlow()

    val productos: StateFlow<List<Producto>> = repository.productos

    val movimientos: StateFlow<List<Movimiento>> = repository.movimientos

    val productosFiltrados: StateFlow<List<Producto>> = combine(
        repository.productos, _busqueda, _categoria, _soloStockBajo
    ) { lista, texto, categoria, soloBajo ->
        lista.filter { p ->
            (texto.isBlank() || p.nombre.contains(texto, true) || p.codigo.contains(texto, true)) &&
                (categoria == null || p.categoria == categoria) &&
                (!soloBajo || p.stockBajo)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val movimientosFiltrados: StateFlow<List<Movimiento>> = combine(
        repository.movimientos, _filtroTipo
    ) { lista, tipo ->
        if (tipo == null) lista else lista.filter { it.tipo == tipo }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val resumen: StateFlow<ResumenInventario> = combine(
        repository.productos, repository.movimientos
    ) { productos, movimientos ->
        ResumenInventario(
            totalProductos = productos.size,
            totalUnidades = productos.sumOf { it.stock },
            valorInventario = productos.sumOf { it.valorTotal },
            stockBajo = productos.filter { it.stockBajo }.sortedBy { it.stock },
            masVendidos = movimientos
                .filter { it.tipo == TipoMovimiento.VENTA }
                .groupBy { it.nombreProducto }
                .map { (nombre, ventas) -> nombre to ventas.sumOf { it.cantidad } }
                .sortedByDescending { it.second }
                .take(3)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumenInventario())

    val cantidadStockBajo: StateFlow<Int> = repository.productos
        .map { lista -> lista.count { it.stockBajo } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            repository.cargarSiHaceFalta()
            _cargando.value = false
        }
    }

    fun onBusqueda(texto: String) { _busqueda.value = texto }

    fun onCategoria(categoria: String?) {
        _categoria.value = if (_categoria.value == categoria) null else categoria
    }

    fun onSoloStockBajo(activo: Boolean) { _soloStockBajo.value = activo }

    fun onFiltroTipo(tipo: TipoMovimiento?) {
        _filtroTipo.value = if (_filtroTipo.value == tipo) null else tipo
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch { repository.eliminarProducto(producto) }
    }
}
