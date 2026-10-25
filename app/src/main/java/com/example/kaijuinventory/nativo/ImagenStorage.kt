package com.example.kaijuinventory.nativo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Guarda las fotos de productos (cámara o galería) en el almacenamiento interno
 * de la app. Solo se persiste la ruta en SQLite, no la imagen.
 */
object ImagenStorage {

    private const val LADO_MAXIMO = 1024

    fun guardarBitmap(context: Context, bitmap: Bitmap): String {
        val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
        val archivo = File(carpeta, "producto_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivo).use { salida ->
            escalar(bitmap).compress(Bitmap.CompressFormat.JPEG, 85, salida)
        }
        return archivo.absolutePath
    }

    /** Copia una imagen elegida en la galería, reduciéndola para no ocupar memoria de más. */
    fun guardarDesdeUri(context: Context, uri: Uri): String? {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: return null
        return guardarBitmap(context, bitmap)
    }

    fun cargar(ruta: String?): Bitmap? =
        ruta?.takeIf { File(it).exists() }?.let { BitmapFactory.decodeFile(it) }

    fun eliminar(ruta: String?) {
        ruta?.let { File(it).delete() }
    }

    private fun escalar(bitmap: Bitmap): Bitmap {
        val mayor = maxOf(bitmap.width, bitmap.height)
        if (mayor <= LADO_MAXIMO) return bitmap
        val factor = LADO_MAXIMO.toFloat() / mayor
        return Bitmap.createScaledBitmap(
            bitmap, (bitmap.width * factor).toInt(), (bitmap.height * factor).toInt(), true
        )
    }
}
