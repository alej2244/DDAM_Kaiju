package com.example.kaijuinventory.validation

import com.example.kaijuinventory.model.Categorias
import com.example.kaijuinventory.model.TipoMovimiento

/**
 * Reglas de validación separadas de la interfaz. Cada función devuelve el mensaje
 * de error o null si el valor es válido, por lo que se pueden probar con tests unitarios.
 */
object ProductoValidator {

    private val formatoCodigo = Regex("^[A-Za-z0-9-]{3,15}$")

    fun codigo(valor: String): String? = when {
        valor.isBlank() -> "El código es obligatorio"
        !formatoCodigo.matches(valor.trim()) -> "Usa de 3 a 15 letras, números o guiones"
        else -> null
    }

    fun nombre(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        valor.trim().length > 40 -> "Máximo 40 caracteres"
        else -> null
    }

    fun descripcion(valor: String): String? =
        if (valor.length > 120) "Máximo 120 caracteres" else null

    fun categoria(valor: String): String? = when {
        valor.isBlank() -> "Selecciona una categoría"
        valor !in Categorias.todas -> "Categoría no válida"
        else -> null
    }

    fun precio(valor: String): String? {
        if (valor.isBlank()) return "El precio es obligatorio"
        val numero = valor.trim().toIntOrNull() ?: return "Ingresa solo números enteros (CLP)"
        return when {
            numero <= 0 -> "El precio debe ser mayor a 0"
            numero > 10_000_000 -> "El precio no puede superar $10.000.000"
            else -> null
        }
    }

    fun stock(valor: String): String? {
        if (valor.isBlank()) return "El stock inicial es obligatorio"
        val numero = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
        return when {
            numero < 0 -> "El stock no puede ser negativo"
            numero > 100_000 -> "Máximo 100.000 unidades"
            else -> null
        }
    }

    fun stockMinimo(valor: String): String? {
        if (valor.isBlank()) return "El stock mínimo es obligatorio"
        val numero = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
        return when {
            numero < 0 -> "No puede ser negativo"
            numero > 10_000 -> "Máximo 10.000 unidades"
            else -> null
        }
    }
}

object MovimientoValidator {

    fun cantidad(valor: String, tipo: TipoMovimiento, stockActual: Int): String? {
        if (valor.isBlank()) return "La cantidad es obligatoria"
        val numero = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
        return when {
            tipo == TipoMovimiento.AJUSTE && numero < 0 -> "El stock contado no puede ser negativo"
            tipo != TipoMovimiento.AJUSTE && numero <= 0 -> "La cantidad debe ser mayor a 0"
            tipo == TipoMovimiento.AJUSTE && numero == stockActual -> "El stock ya es $stockActual, no hay nada que ajustar"
            tipo.restaStock && numero > stockActual -> "Stock insuficiente (disponible: $stockActual)"
            numero > 100_000 -> "Máximo 100.000 unidades"
            else -> null
        }
    }

    fun nota(valor: String, tipo: TipoMovimiento): String? = when {
        tipo == TipoMovimiento.AJUSTE && valor.isBlank() -> "Indica el motivo del ajuste"
        valor.length > 80 -> "Máximo 80 caracteres"
        else -> null
    }
}
