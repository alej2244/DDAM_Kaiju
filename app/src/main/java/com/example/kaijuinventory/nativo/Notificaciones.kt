package com.example.kaijuinventory.nativo

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.kaijuinventory.R
import com.example.kaijuinventory.model.Producto

/** Notificaciones del sistema para avisar cuando un producto llega a su stock mínimo. */
object Notificaciones {

    private const val CANAL_STOCK = "stock_bajo"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_STOCK, "Alertas de stock bajo", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Avisa cuando un producto necesita reposición" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** Devuelve true si la notificación se mostró (requiere permiso concedido). */
    @SuppressLint("MissingPermission")
    fun notificarStockBajo(context: Context, producto: Producto): Boolean {
        if (!tienePermiso(context)) return false
        val texto = if (producto.sinStock) "Sin stock. Repón cuanto antes."
        else "Quedan ${producto.stock} unidades (mínimo ${producto.stockMinimo})."
        val notificacion = NotificationCompat.Builder(context, CANAL_STOCK)
            .setSmallIcon(R.drawable.ic_stock_alerta)
            .setContentTitle("Stock bajo: ${producto.nombre}")
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(producto.id.toInt(), notificacion)
        return true
    }
}
