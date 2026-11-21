package utils

//Clase Utils para funciones adicionales o utilidades prácticas para el desarrollo de la app
class Utils {
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
}