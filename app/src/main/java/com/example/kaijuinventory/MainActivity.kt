package com.example.kaijuinventory

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.kaijuinventory.nativo.Notificaciones
import com.example.kaijuinventory.ui.navigation.AppNavigation
import com.example.kaijuinventory.ui.theme.KaijuInventoryTheme
import com.example.kaijuinventory.viewmodel.InventarioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: InventarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaijuInventoryTheme {
                SolicitarPermisoNotificaciones()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(inventarioViewModel = viewModel)
                }
            }
        }
    }
}

/** Desde Android 13 las notificaciones requieren permiso en tiempo de ejecución. */
@Composable
private fun SolicitarPermisoNotificaciones() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notificaciones.tienePermiso(context)) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
