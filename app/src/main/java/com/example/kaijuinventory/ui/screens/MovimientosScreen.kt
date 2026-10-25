package com.example.kaijuinventory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kaijuinventory.model.TipoMovimiento
import com.example.kaijuinventory.ui.components.EstadoVacio
import com.example.kaijuinventory.ui.components.FilaMovimiento
import com.example.kaijuinventory.ui.components.iconoMovimiento
import com.example.kaijuinventory.viewmodel.InventarioViewModel

/** Historial global de movimientos, filtrable por tipo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreen(viewModel: InventarioViewModel) {
    val movimientos by viewModel.movimientosFiltrados.collectAsStateWithLifecycle()
    val filtro by viewModel.filtroTipo.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TopAppBar(title = { Text("Movimientos", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TipoMovimiento.entries) { tipo ->
                    FilterChip(
                        selected = filtro == tipo,
                        onClick = { viewModel.onFiltroTipo(tipo) },
                        label = { Text(tipo.etiqueta) },
                        leadingIcon = { Icon(iconoMovimiento(tipo), contentDescription = null) }
                    )
                }
            }
            if (movimientos.isEmpty()) {
                EstadoVacio(
                    icono = Icons.Filled.History,
                    titulo = "Sin movimientos",
                    detalle = "Los ingresos, ventas y ajustes aparecerán aquí."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(movimientos, key = { it.id }) { movimiento ->
                        FilaMovimiento(movimiento, mostrarProducto = true, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}
