package com.example.kaijuinventory.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kaijuinventory.ui.screens.DetalleProductoScreen
import com.example.kaijuinventory.ui.screens.FormularioProductoScreen
import com.example.kaijuinventory.ui.screens.InicioScreen
import com.example.kaijuinventory.ui.screens.MovimientoScreen
import com.example.kaijuinventory.ui.screens.MovimientosScreen
import com.example.kaijuinventory.ui.screens.ProductosScreen
import com.example.kaijuinventory.viewmodel.InventarioViewModel
import kotlinx.coroutines.launch

/** Rutas de navegación de la app. */
object Rutas {
    const val INICIO = "inicio"
    const val PRODUCTOS = "productos"
    const val MOVIMIENTOS = "movimientos"
    const val FORMULARIO = "producto/formulario?id={id}"
    const val DETALLE = "producto/{id}"
    const val MOVIMIENTO = "producto/{id}/movimiento"

    fun formulario(id: Long = -1L) = "producto/formulario?id=$id"
    fun detalle(id: Long) = "producto/$id"
    fun movimiento(id: Long) = "producto/$id/movimiento"
}

private data class Pestana(val ruta: String, val titulo: String, val icono: ImageVector)

private val pestanas = listOf(
    Pestana(Rutas.INICIO, "Inicio", Icons.Filled.Home),
    Pestana(Rutas.PRODUCTOS, "Productos", Icons.Filled.Inventory2),
    Pestana(Rutas.MOVIMIENTOS, "Movimientos", Icons.Filled.History)
)

@Composable
fun AppNavigation(inventarioViewModel: InventarioViewModel) {
    val navController = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val stockBajo by inventarioViewModel.cantidadStockBajo.collectAsStateWithLifecycle()

    val avisar: (String) -> Unit = { texto -> scope.launch { snackbar.showSnackbar(texto) } }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            // La barra inferior solo aparece en las pantallas principales, con animación.
            AnimatedVisibility(
                visible = pestanas.any { it.ruta == rutaActual },
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                NavigationBar {
                    pestanas.forEach { pestana ->
                        NavigationBarItem(
                            selected = rutaActual == pestana.ruta,
                            onClick = { navegarPestana(navController, pestana.ruta) },
                            icon = {
                                if (pestana.ruta == Rutas.PRODUCTOS && stockBajo > 0) {
                                    BadgedBox(badge = { Badge { Text("$stockBajo") } }) {
                                        Icon(pestana.icono, contentDescription = pestana.titulo)
                                    }
                                } else {
                                    Icon(pestana.icono, contentDescription = pestana.titulo)
                                }
                            },
                            label = { Text(pestana.titulo) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.INICIO,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 6 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 6 } }
        ) {
            composable(Rutas.INICIO) {
                InicioScreen(
                    viewModel = inventarioViewModel,
                    onVerProducto = { navController.navigate(Rutas.detalle(it)) },
                    onNuevoProducto = { navController.navigate(Rutas.formulario()) },
                    onVerProductos = { navegarPestana(navController, Rutas.PRODUCTOS) }
                )
            }
            composable(Rutas.PRODUCTOS) {
                ProductosScreen(
                    viewModel = inventarioViewModel,
                    onVerProducto = { navController.navigate(Rutas.detalle(it)) },
                    onNuevoProducto = { navController.navigate(Rutas.formulario()) }
                )
            }
            composable(Rutas.MOVIMIENTOS) {
                MovimientosScreen(viewModel = inventarioViewModel)
            }
            composable(
                Rutas.FORMULARIO,
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
            ) { backStack ->
                val id = backStack.arguments?.getLong("id") ?: -1L
                FormularioProductoScreen(
                    productoId = id,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { mensaje ->
                        avisar(mensaje)
                        navController.popBackStack()
                    }
                )
            }
            composable(
                Rutas.DETALLE,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong("id") ?: return@composable
                DetalleProductoScreen(
                    productoId = id,
                    viewModel = inventarioViewModel,
                    onVolver = { navController.popBackStack() },
                    onEditar = { navController.navigate(Rutas.formulario(id)) },
                    onRegistrarMovimiento = { navController.navigate(Rutas.movimiento(id)) },
                    onEliminado = { nombre ->
                        avisar("Se eliminó \"$nombre\"")
                        navController.popBackStack()
                    }
                )
            }
            composable(
                Rutas.MOVIMIENTO,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong("id") ?: return@composable
                MovimientoScreen(
                    productoId = id,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { mensaje ->
                        avisar(mensaje)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/** Navega entre pestañas sin apilar pantallas repetidas y conservando su estado. */
private fun navegarPestana(navController: NavHostController, ruta: String) {
    navController.navigate(ruta) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
