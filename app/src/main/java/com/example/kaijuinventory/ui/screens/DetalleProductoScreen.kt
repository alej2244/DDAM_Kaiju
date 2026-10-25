package com.example.kaijuinventory.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kaijuinventory.ui.components.EstadoVacio
import com.example.kaijuinventory.ui.components.FilaDato
import com.example.kaijuinventory.ui.components.FilaMovimiento
import com.example.kaijuinventory.ui.components.FotoProducto
import com.example.kaijuinventory.ui.components.StockChip
import com.example.kaijuinventory.ui.components.colorStock
import com.example.kaijuinventory.util.Formato
import com.example.kaijuinventory.viewmodel.InventarioViewModel

/** Ficha del producto con su stock, datos y el historial de movimientos (trazabilidad). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoScreen(
    productoId: Long,
    viewModel: InventarioViewModel,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onRegistrarMovimiento: () -> Unit,
    onEliminado: (String) -> Unit
) {
    // Se observa la lista completa para que la ficha se actualice sola tras un movimiento o edición.
    val productos by viewModel.productos.collectAsStateWithLifecycle()
    val producto = productos.find { it.id == productoId }
    val todosMovimientos by viewModel.movimientos.collectAsStateWithLifecycle()
    val movimientos = remember(todosMovimientos, productoId) { todosMovimientos.filter { it.productoId == productoId } }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }
    var eliminando by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(producto?.nombre ?: "Producto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (producto != null) {
                        IconButton(onClick = onEditar) { Icon(Icons.Filled.Edit, contentDescription = "Editar") }
                        IconButton(onClick = { confirmarEliminar = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (producto == null) {
            if (!eliminando) {
                EstadoVacio(
                    icono = Icons.Filled.Warning,
                    titulo = "Producto no encontrado",
                    detalle = "Puede que haya sido eliminado.",
                    modifier = Modifier.padding(padding)
                )
            }
            return@Scaffold
        }

        // Barra de nivel de stock: llena al doble del mínimo, animada al cambiar.
        val referencia = (producto.stockMinimo * 2).coerceAtLeast(1)
        val nivel by animateFloatAsState(
            (producto.stock.toFloat() / referencia).coerceIn(0f, 1f), tween(700), label = "nivelStock"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    FotoProducto(producto.fotoPath, 180.dp, redondeo = 24.dp)
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Stock", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            StockChip(producto)
                        }
                        LinearProgressIndicator(
                            progress = { nivel },
                            color = colorStock(producto),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                        FilaDato("Stock mínimo", "${producto.stockMinimo} unidades")
                        FilaDato("Código", producto.codigo)
                        FilaDato("Categoría", producto.categoria)
                        FilaDato("Precio", Formato.pesos(producto.precio))
                        FilaDato("Valor en stock", Formato.pesos(producto.valorTotal))
                        if (producto.descripcion.isNotBlank()) {
                            Text(producto.descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = onRegistrarMovimiento,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.SwapVert, contentDescription = null)
                    Text("  Registrar entrada, venta o ajuste", fontWeight = FontWeight.Bold)
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.History, contentDescription = null)
                    Text(
                        "  Historial de movimientos (${movimientos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(movimientos, key = { it.id }) { movimiento ->
                FilaMovimiento(movimiento, mostrarProducto = false, modifier = Modifier.animateItem())
            }
        }

        if (confirmarEliminar) {
            AlertDialog(
                onDismissRequest = { confirmarEliminar = false },
                icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                title = { Text("¿Eliminar producto?") },
                text = { Text("Se borrará \"${producto.nombre}\" y todo su historial de movimientos. Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            confirmarEliminar = false
                            eliminando = true
                            viewModel.eliminarProducto(producto)
                            onEliminado(producto.nombre)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
                }
            )
        }
    }
}
