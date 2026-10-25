package com.example.kaijuinventory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kaijuinventory.model.Categorias
import com.example.kaijuinventory.ui.components.CampoValidado
import com.example.kaijuinventory.ui.components.FotoProducto
import com.example.kaijuinventory.viewmodel.ProductoFormViewModel
import kotlin.math.roundToInt

/**
 * Formulario para crear o editar un producto. La pantalla solo dibuja el estado
 * y envía eventos; toda la validación vive en ProductoFormViewModel/ProductoValidator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProductoScreen(
    productoId: Long,
    onVolver: () -> Unit,
    onGuardado: (String) -> Unit,
    viewModel: ProductoFormViewModel = viewModel()
) {
    LaunchedEffect(productoId) { viewModel.cargar(productoId) }
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    // --- Recursos nativos: cámara (con permiso) y galería (selector del sistema, sin permiso) ---
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) viewModel.onFotoCapturada(bitmap)
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) camara.launch(null)
        else viewModel.mostrarMensaje("Sin permiso de cámara no se puede tomar la foto")
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onFotoSeleccionada(uri)
    }
    val abrirCamara = {
        val concedido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (concedido) camara.launch(null) else permisoCamara.launch(Manifest.permission.CAMERA)
    }

    // Animación de "sacudida" del formulario cuando se intenta guardar con errores.
    val sacudida = remember { Animatable(0f) }
    LaunchedEffect(estado.intentosFallidos) {
        if (estado.intentosFallidos > 0) {
            for (desplazamiento in listOf(18f, -18f, 12f, -12f, 6f, 0f)) {
                sacudida.animateTo(desplazamiento, spring(stiffness = Spring.StiffnessHigh))
            }
        }
    }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) onGuardado(if (estado.esEdicion) "Producto actualizado" else "Producto registrado")
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(if (estado.esEdicion) "Editar producto" else "Nuevo producto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .offset { IntOffset(sacudida.value.roundToInt(), 0) },
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto del producto
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        FotoProducto(estado.fotoPath, 96.dp, redondeo = 20.dp)
                        if (estado.procesandoFoto) CircularProgressIndicator(Modifier.size(32.dp))
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = abrirCamara, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                            Text("  Tomar foto")
                        }
                        OutlinedButton(
                            onClick = {
                                galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                            Text("  Galería")
                        }
                        if (estado.fotoPath != null) {
                            TextButton(onClick = viewModel::quitarFoto) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                                Text(" Quitar foto")
                            }
                        }
                    }
                }
            }

            CampoValidado(
                valor = estado.codigo,
                onCambio = viewModel::onCodigo,
                etiqueta = "Código del producto",
                icono = Icons.Filled.QrCode,
                error = estado.codigoError,
                ayuda = "Ejemplo: BEB-002"
            )
            CampoValidado(
                valor = estado.nombre,
                onCambio = viewModel::onNombre,
                etiqueta = "Nombre",
                icono = Icons.Filled.Sell,
                error = estado.nombreError
            )
            CampoValidado(
                valor = estado.descripcion,
                onCambio = viewModel::onDescripcion,
                etiqueta = "Descripción (opcional)",
                icono = Icons.Filled.Description,
                error = estado.descripcionError,
                unaLinea = false,
                ayuda = "${estado.descripcion.length}/120"
            )

            // Categoría: selección con chips para evitar errores de escritura
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Category,
                    contentDescription = null,
                    tint = if (estado.categoriaError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("  Categoría", style = MaterialTheme.typography.titleSmall)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(0.dp)) {
                items(Categorias.todas) { nombre ->
                    FilterChip(
                        selected = estado.categoria == nombre,
                        onClick = { viewModel.onCategoria(nombre) },
                        label = { Text(nombre) }
                    )
                }
            }
            estado.categoriaError?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Text(" $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            CampoValidado(
                valor = estado.precio,
                onCambio = viewModel::onPrecio,
                etiqueta = "Precio de venta",
                icono = Icons.Filled.AttachMoney,
                error = estado.precioError,
                teclado = KeyboardType.Number,
                prefijo = "$ "
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoValidado(
                    valor = estado.stock,
                    onCambio = viewModel::onStock,
                    etiqueta = if (estado.esEdicion) "Stock actual" else "Stock inicial",
                    icono = Icons.Filled.Inventory,
                    error = estado.stockError,
                    teclado = KeyboardType.Number,
                    habilitado = !estado.esEdicion,
                    ayuda = if (estado.esEdicion) "Se cambia con movimientos" else null,
                    modifier = Modifier.weight(1f)
                )
                CampoValidado(
                    valor = estado.stockMinimo,
                    onCambio = viewModel::onStockMinimo,
                    etiqueta = "Stock mínimo",
                    icono = Icons.Filled.Warning,
                    error = estado.stockMinimoError,
                    teclado = KeyboardType.Number,
                    ayuda = "Alerta de reposición",
                    modifier = Modifier.weight(1f)
                )
            }

            BotonAnimado(
                texto = if (estado.esEdicion) "Guardar cambios" else "Registrar producto",
                cargando = estado.guardando,
                onClick = viewModel::guardar
            )
        }
    }
}

/** Botón que se encoge al presionarlo y muestra un indicador de carga mientras guarda. */
@Composable
fun BotonAnimado(texto: String, cargando: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.95f else 1f, label = "escalaBoton")
    Button(
        onClick = onClick,
        enabled = !cargando,
        interactionSource = interaccion,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(escala)
    ) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        } else {
            Icon(Icons.Filled.Save, contentDescription = null)
            Text("  $texto", fontWeight = FontWeight.Bold)
        }
    }
}
