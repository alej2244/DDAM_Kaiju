package com.example.kaijuinventory.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kaijuinventory.model.TipoMovimiento
import com.example.kaijuinventory.ui.components.CampoValidado
import com.example.kaijuinventory.ui.components.FotoProducto
import com.example.kaijuinventory.ui.components.StockChip
import com.example.kaijuinventory.ui.components.iconoMovimiento
import com.example.kaijuinventory.ui.theme.StockAgotado
import com.example.kaijuinventory.ui.theme.StockBajo
import com.example.kaijuinventory.ui.theme.StockOk
import com.example.kaijuinventory.viewmodel.MovimientoViewModel
import kotlin.math.roundToInt

/** Registro de entradas, ventas, salidas y ajustes con vista previa del stock resultante. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoScreen(
    productoId: Long,
    onVolver: () -> Unit,
    onGuardado: (String) -> Unit,
    viewModel: MovimientoViewModel = viewModel()
) {
    LaunchedEffect(productoId) { viewModel.cargar(productoId) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    val sacudida = remember { Animatable(0f) }
    LaunchedEffect(estado.intentosFallidos) {
        if (estado.intentosFallidos > 0) {
            for (d in listOf(18f, -18f, 12f, -12f, 6f, 0f)) sacudida.animateTo(d, spring(stiffness = Spring.StiffnessHigh))
        }
    }
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            val p = estado.producto
            val mensaje = when {
                p != null && p.stockBajo && estado.alertaNotificada -> "Movimiento registrado. Se envió alerta de stock bajo"
                p != null && p.stockBajo -> "Movimiento registrado. ${p.nombre} quedó con stock bajo"
                else -> "Movimiento registrado"
            }
            onGuardado(mensaje)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Registrar movimiento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        val producto = estado.producto
        if (producto == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .offset { IntOffset(sacudida.value.roundToInt(), 0) },
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    FotoProducto(producto.fotoPath, 56.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(producto.nombre, fontWeight = FontWeight.SemiBold)
                        Text(producto.codigo, style = MaterialTheme.typography.bodySmall)
                    }
                    StockChip(producto)
                }
            }

            Text("Tipo de movimiento", style = MaterialTheme.typography.titleSmall)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoMovimiento.entries.forEach { tipo ->
                    FilterChip(
                        selected = estado.tipo == tipo,
                        onClick = { viewModel.onTipo(tipo) },
                        label = { Text(tipo.etiqueta) },
                        leadingIcon = { Icon(iconoMovimiento(tipo), contentDescription = null) }
                    )
                }
            }
            Text(
                when (estado.tipo) {
                    TipoMovimiento.ENTRADA -> "Ingreso de mercadería (compra o reposición)."
                    TipoMovimiento.VENTA -> "Venta a cliente. Descuenta stock."
                    TipoMovimiento.SALIDA -> "Consumo interno, merma o pérdida. Descuenta stock."
                    TipoMovimiento.AJUSTE -> "Conteo físico: ingresa el stock real contado."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CampoValidado(
                valor = estado.cantidad,
                onCambio = viewModel::onCantidad,
                etiqueta = if (estado.tipo == TipoMovimiento.AJUSTE) "Stock contado" else "Cantidad",
                icono = Icons.Filled.Numbers,
                error = estado.cantidadError,
                teclado = KeyboardType.Number
            )
            CampoValidado(
                valor = estado.nota,
                onCambio = viewModel::onNota,
                etiqueta = if (estado.tipo == TipoMovimiento.AJUSTE) "Motivo del ajuste" else "Nota (opcional)",
                icono = Icons.AutoMirrored.Filled.Notes,
                error = estado.notaError
            )

            VistaPreviaStock(actual = producto.stock, resultante = estado.stockResultante, minimo = producto.stockMinimo)

            BotonAnimado(texto = "Guardar movimiento", cargando = estado.guardando, onClick = viewModel::guardar)
        }
    }
}

/** Muestra "actual → resultante" y anima el número y su color cuando cambia la cantidad. */
@Composable
private fun VistaPreviaStock(actual: Int, resultante: Int?, minimo: Int) {
    val colorObjetivo = when {
        resultante == null -> MaterialTheme.colorScheme.onSurfaceVariant
        resultante == 0 -> StockAgotado
        resultante <= minimo -> StockBajo
        else -> StockOk
    }
    val color by animateColorAsState(colorObjetivo, label = "colorPrevia")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Stock actual", style = MaterialTheme.typography.labelMedium)
                Text("$actual", style = MaterialTheme.typography.headlineSmall)
            }
            Text("→", style = MaterialTheme.typography.headlineSmall)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Quedará en", style = MaterialTheme.typography.labelMedium)
                AnimatedContent(
                    targetState = resultante,
                    transitionSpec = {
                        (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                    },
                    label = "stockResultante"
                ) { valor ->
                    Text(
                        valor?.toString() ?: "—",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }
}
