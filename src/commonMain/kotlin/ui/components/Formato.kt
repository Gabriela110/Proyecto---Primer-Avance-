package ui.components

import kotlin.math.abs
import kotlin.math.roundToInt

/** Formatea un monto con 2 decimales sin depender de String.format (que no es multiplataforma). */
fun dinero(valor: Double): String {
    val centavos = (abs(valor) * 100).roundToInt()
    val signo = if (valor < 0 && centavos > 0) "-" else ""
    return "$signo${centavos / 100}.${(centavos % 100).toString().padStart(2, '0')}"
}

/** Convierte "12,5" o "12.5" en Double; devuelve null si no es un número. */
fun parseDecimal(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

/** 20.0 -> "20", 12.5 -> "12.5" */
fun formatoDescuento(valor: Double): String =
    if (valor % 1.0 == 0.0) valor.toInt().toString() else valor.toString()
