package utils

class ConsoleUtils {
    // ==========================================================
    // VALIDACIONES
    // ==========================================================

    fun leerTexto(mensaje: String): String {

        while (true) {

            print(mensaje)

            val texto = readLine()?.trim() ?: ""

            if (texto.isNotEmpty()) {
                return texto
            }

            println("El texto no puede estar vacío.")
        }
    }
    fun leerDoublePositivo(mensaje: String): Double {

        while (true) {

            print(mensaje)

            val numero = readLine()?.toDoubleOrNull()

            if (numero != null && numero > 0) {
                return numero
            }

            println("Ingrese un número mayor que 0.")
        }
    }
    fun leerEntero(mensaje: String): Int {
        while (true) {
            print(mensaje)
            val numero = readLine()?.toIntOrNull()
            if (numero != null) {
                return numero
            }
            println("Ingrese un número entero válido.")
        }
    }
    fun leerEnteroNoNegativo(mensaje: String): Int {

        while (true) {

            print(mensaje)

            val numero = readLine()?.toIntOrNull()

            if (numero != null && numero >= 0) {
                return numero
            }

            println("Ingrese un número entero mayor o igual a 0.")
        }
    }
    fun leerDescuento(): Double {

        while (true) {

            print("Descuento (%): ")

            val descuento = readLine()?.toDoubleOrNull()

            if (descuento != null && descuento > 0 && descuento <= 100) {
                return descuento
            }

            println("El descuento debe estar entre 0 y 100.")
        }
    }
}