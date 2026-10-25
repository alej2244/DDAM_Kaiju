package com.example.kaijuinventory.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kaijuinventory.model.Movimiento
import com.example.kaijuinventory.model.Producto
import com.example.kaijuinventory.model.TipoMovimiento
import com.example.kaijuinventory.nativo.ImagenStorage
import com.example.kaijuinventory.ui.theme.StockAgotado
import com.example.kaijuinventory.ui.theme.StockBajo
import com.example.kaijuinventory.ui.theme.StockOk
import com.example.kaijuinventory.util.Formato
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun colorStock(producto: Producto): Color = when {
    producto.sinStock -> StockAgotado
    producto.stockBajo -> StockBajo
    else -> StockOk
}

/** Etiqueta de stock cuyo color y número cambian con animación al variar el stock. */
@Composable
fun StockChip(producto: Producto, modifier: Modifier = Modifier) {
    val color by animateColorAsState(colorStock(producto), tween(500), label = "colorStock")
    val cantidad by animateIntAsState(producto.stock, tween(500), label = "cantidadStock")
    val texto = when {
        producto.sinStock -> "Agotado"
        producto.stockBajo -> "$cantidad · Stock bajo"
        else -> "$cantidad unid."
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Text(
            text = texto,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Muestra la foto del producto con esquinas redondeadas y recorte centrado.
 * La imagen se decodifica en segundo plano para no bloquear la interfaz.
 */
@Composable
fun FotoProducto(ruta: String?, tamano: Dp, modifier: Modifier = Modifier, redondeo: Dp = 12.dp) {
    val imagen by produceState<ImageBitmap?>(initialValue = null, ruta) {
        value = withContext(Dispatchers.IO) { ImagenStorage.cargar(ruta)?.asImageBitmap() }
    }
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(redondeo))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = imagen, label = "fotoProducto") { bitmap ->
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Foto del producto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(tamano / 2.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaIndicador(
    titulo: String,
    valor: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icono, contentDescription = null)
            Spacer(Modifier.height(8.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(titulo, style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun iconoMovimiento(tipo: TipoMovimiento): ImageVector = when (tipo) {
    TipoMovimiento.ENTRADA -> Icons.Filled.ArrowDownward
    TipoMovimiento.VENTA -> Icons.Filled.PointOfSale
    TipoMovimiento.SALIDA -> Icons.Filled.ArrowUpward
    TipoMovimiento.AJUSTE -> Icons.Filled.Tune
}

@Composable
fun colorMovimiento(tipo: TipoMovimiento): Color = when (tipo) {
    TipoMovimiento.ENTRADA -> StockOk
    TipoMovimiento.VENTA -> MaterialTheme.colorScheme.primary
    TipoMovimiento.SALIDA -> StockBajo
    TipoMovimiento.AJUSTE -> MaterialTheme.colorScheme.tertiary
}

@Composable
fun FilaMovimiento(movimiento: Movimiento, mostrarProducto: Boolean, modifier: Modifier = Modifier) {
    val color = colorMovimiento(movimiento.tipo)
    val signo = when (movimiento.tipo) {
        TipoMovimiento.ENTRADA -> "+"
        TipoMovimiento.VENTA, TipoMovimiento.SALIDA -> "-"
        TipoMovimiento.AJUSTE -> "="
    }
    ListItem(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        leadingContent = {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(iconoMovimiento(movimiento.tipo), contentDescription = movimiento.tipo.etiqueta, tint = color) }
        },
        headlineContent = {
            Text(if (mostrarProducto) movimiento.nombreProducto else movimiento.tipo.etiqueta, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            val detalle = buildString {
                if (mostrarProducto) append(movimiento.tipo.etiqueta).append(" · ")
                append(Formato.fecha(movimiento.fecha))
                if (movimiento.nota.isNotBlank()) append("\n").append(movimiento.nota)
            }
            Text(detalle, style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text("$signo${movimiento.cantidad}", color = color, fontWeight = FontWeight.Bold)
                Text("Stock: ${movimiento.stockResultante}", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

@Composable
fun EstadoVacio(icono: ImageVector, titulo: String, detalle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FilaDato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, fontWeight = FontWeight.Medium)
    }
}
