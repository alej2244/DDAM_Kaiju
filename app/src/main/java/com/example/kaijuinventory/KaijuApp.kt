package com.example.kaijuinventory

import android.app.Application
import com.example.kaijuinventory.data.KaijuDatabase
import com.example.kaijuinventory.nativo.Notificaciones
import com.example.kaijuinventory.repository.InventarioRepository

/** Punto de entrada de la app: crea una sola instancia del repositorio (inyección manual). */
class KaijuApp : Application() {

    val repository: InventarioRepository by lazy { InventarioRepository(KaijuDatabase(this)) }

    override fun onCreate() {
        super.onCreate()
        Notificaciones.crearCanal(this)
    }
}
