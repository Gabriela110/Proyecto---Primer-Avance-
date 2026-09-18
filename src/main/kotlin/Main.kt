import services.AuthService

fun main() {

    val authService = AuthService()

    var continuar = true

    while (continuar) {

        println()
        println("================================")
        println("         SUPER SELECTOS")
        println("================================")
        println("1. Registrarse")
        println("2. Iniciar sesión")
        println("3. Salir")
        print("Seleccione una opción: ")

        when (readLine()) {

            "1" -> registrarCliente(authService)

            "2" -> iniciarSesion(authService)

            "3" -> {
                println()
                println("Gracias por utilizar Super Selectos.")
                continuar = false
            }

            else -> {
                println()
                println("Opción inválida.")
            }
        }
    }
}

fun registrarCliente(authService: AuthService) {

    println()
    println("===== REGISTRO DE CLIENTE =====")

    print("Nombre: ")
    val nombre = readLine() ?: ""

    print("Correo: ")
    val correo = readLine() ?: ""

    print("Contraseña: ")
    val contrasena = readLine() ?: ""

    authService.registrarCliente(
        nombre = nombre,
        correo = correo,
        contrasena = contrasena
    )
}

fun iniciarSesion(authService: AuthService) {

    println()
    println("===== INICIAR SESIÓN =====")

    print("Correo: ")
    val correo = readLine() ?: ""

    print("Contraseña: ")
    val contrasena = readLine() ?: ""

    val inicioExitoso = authService.iniciarSesion(
        correo = correo,
        contrasena = contrasena
    )

    if (inicioExitoso) {
        mostrarMenuUsuario(authService)
    }
}

fun mostrarMenuUsuario(authService: AuthService) {

    var cerrar = false

    while (!cerrar) {

        val usuario = authService.obtenerUsuarioActual()

        if (usuario == null) {
            cerrar = true
            continue
        }

        usuario.mostrarMenu()

        print("Seleccione una opción: ")
        val opcion = readLine()

        when (usuario.rol) {

            "CLIENTE" -> {

                when (opcion) {

                    "1" -> {
                        println("Módulo de listas de compras.")
                    }

                    "2" -> {
                        println("Módulo de productos.")
                    }

                    "3" -> {
                        println("Módulo de promociones.")
                    }

                    "4" -> {
                        println("Módulo de presupuesto.")
                    }

                    "5" -> {
                        println("Módulo de estadísticas.")
                    }

                    "6" -> {
                        authService.cerrarSesion()
                        cerrar = true
                    }

                    else -> {
                        println("Opción inválida.")
                    }
                }
            }

            "ADMINISTRADOR" -> {

                when (opcion) {

                    "1" -> {
                        println("Gestión de productos.")
                    }

                    "2" -> {
                        println("Gestión de promociones.")
                    }

                    "3" -> {
                        println("Estadísticas del sistema.")
                    }

                    "4" -> {
                        println()
                        println("Mostrando usuarios registrados...")
                        authService.listarUsuarios()
                    }

                    "5" -> {
                        authService.cerrarSesion()
                        cerrar = true
                    }

                    else -> {
                        println("Opción inválida.")
                    }
                }
            }
        }
    }
}