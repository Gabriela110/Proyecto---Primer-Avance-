package utils

import kotlin.math.abs
import kotlin.math.roundToInt

//Clase Utils para funciones adicionales o utilidades prácticas para el desarrollo de la app
object Utils {
    //Función para convertir un texto en un slug (ej: Hola mundo! -> hola-mundo)
    fun slugify(texto: String): String {
        val nonLatin = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
        val regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()

        return regex.replace(nonLatin, "")
            .lowercase()
            .trim()
            .replace(Regex("[^a-z0-9\\s-]"), "") // Elimina caracteres especiales
            .replace(Regex("\\s+"), "-")         // Reemplaza espacios por guiones
            .replace(Regex("-+"), "-")           // Evita guiones múltiples consecutivos
    }
    /* Formatea un monto con 2 decimales sin depender de String.format (que no es multiplataforma). */
    fun dinero(valor: Double): String {
        val centavos = (abs(valor) * 100).roundToInt()
        val signo = if (valor < 0 && centavos > 0) "-" else ""
        return "$signo${centavos / 100}.${(centavos % 100).toString().padStart(2, '0')}"
    }

    /* Convierte "12,5" o "12.5" en Double; devuelve null si no es un número. */
    fun parseDecimal(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

    /* 20.0 -> "20", 12.5 -> "12.5" */
    fun formatoDescuento(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toInt().toString() else valor.toString()
}