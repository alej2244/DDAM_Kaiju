package com.example.kaijuinventory.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kaijuinventory.ui.components.FotoProducto
import com.example.kaijuinventory.ui.components.StockChip
import com.example.kaijuinventory.ui.components.TarjetaIndicador
import com.example.kaijuinventory.ui.theme.StockOk
import com.example.kaijuinventory.util.Formato
import com.example.kaijuinventory.viewmodel.InventarioViewModel

/** Panel principal: indicadores del inventario, productos por reponer y más vendidos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    viewModel: InventarioViewModel,
    onVerProducto: (Long) -> Unit,
    onNuevoProducto: () -> Unit,
    onVerProductos: () -> Unit
) {
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Contadores animados: suben desde 0 al abrir el panel.
    val totalProductos by animateIntAsState(resumen.totalProductos, tween(800), label = "totalProductos")
    val totalUnidades by animateIntAsState(resumen.totalUnidades, tween(800), label = "totalUnidades")

    var mostrarSecciones by remember { mutableStateOf(false) }
    LaunchedEffect(cargando) { if (!cargando) mostrarSecciones = true }

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Inventario Kaiju", fontWeight = FontWeight.Bold)
                        Text(
                            "Control y trazabilidad de stock",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                scrollBehavior = scroll
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoProducto,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo producto") }
            )
        }
    ) { padding ->
        if (cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaIndicador(
                        "Productos", Formato.numero(totalProductos), Icons.Filled.Inventory2,
                        Modifier.weight(1f)
                    )
                    TarjetaIndicador(
                        "Unidades en stock", Formato.numero(totalUnidades), Icons.Filled.Numbers,
                        Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
            item {
                TarjetaIndicador(
                    "Valor total del inventario", Formato.pesos(resumen.valorInventario), Icons.Filled.AttachMoney,
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }

            item {
                AnimatedVisibility(mostrarSecciones, enter = fadeIn(tween(400)) + slideInVertically { it / 3 }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            "  Requieren reposición (${resumen.stockBajo.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onVerProductos) { Text("Ver todos") }
                    }
                }
            }
            if (resumen.stockBajo.isEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StockOk)
                        Text("  Todo el inventario está sobre su stock mínimo")
                    }
                }
            }
            items(resumen.stockBajo, key = { it.id }) { producto ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .clickable { onVerProducto(producto.id) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        FotoProducto(producto.fotoPath, 48.dp)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(producto.nombre, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Mínimo: ${producto.stockMinimo} · ${producto.codigo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StockChip(producto)
                    }
                }
            }

            if (resumen.masVendidos.isNotEmpty()) {
                item {
                    Text(
                        "Más vendidos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                val maximo = resumen.masVendidos.first().second.coerceAtLeast(1)
                items(resumen.masVendidos, key = { it.first }) { (nombre, cantidad) ->
                    Column(Modifier.fillMaxWidth().animateItem()) {
                        Row(Modifier.fillMaxWidth()) {
                            Text(nombre, modifier = Modifier.weight(1f))
                            Text("$cantidad vendidos", fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { cantidad / maximo.toFloat() },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
