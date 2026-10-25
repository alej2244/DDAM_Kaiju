package com.example.kaijuinventory.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formato {
    private val localeChile: Locale = Locale.forLanguageTag("es-CL")

    fun pesos(valor: Long): String =
        "$" + NumberFormat.getIntegerInstance(localeChile).format(valor)

    fun pesos(valor: Int): String = pesos(valor.toLong())

    fun numero(valor: Int): String = NumberFormat.getIntegerInstance(localeChile).format(valor)

    fun fecha(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", localeChile).format(Date(millis))
}
