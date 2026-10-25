package com.example.kaijuinventory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kaijuinventory.ui.theme.StockOk

/**
 * Campo de texto reutilizable con retroalimentación visual:
 * ícono del campo, ícono de error o de válido, borde rojo y mensaje animado.
 * No valida por sí mismo: muestra el error que le entrega el ViewModel.
 */
@Composable
fun CampoValidado(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    habilitado: Boolean = true,
    unaLinea: Boolean = true,
    ayuda: String? = null,
    prefijo: String? = null
) {
    val valido = error == null && valor.isNotBlank()
    Column(modifier) {
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = when {
                error != null -> {
                    { Icon(Icons.Filled.Error, contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error) }
                }
                valido && habilitado -> {
                    { Icon(Icons.Filled.CheckCircle, contentDescription = "Campo válido", tint = StockOk) }
                }
                else -> null
            },
            prefix = if (prefijo != null) { { Text(prefijo) } } else null,
            isError = error != null,
            enabled = habilitado,
            singleLine = unaLinea,
            minLines = if (unaLinea) 1 else 2,
            keyboardOptions = KeyboardOptions(keyboardType = teclado, imeAction = ImeAction.Next),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (error == null && ayuda != null) {
            Text(
                text = ayuda,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
