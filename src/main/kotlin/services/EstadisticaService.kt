package services

class EstadisticaService(
    private val authService: AuthService,
    private val productoService: ProductoService,
    private val promocionService: PromocionService,
    private val categoriaService: CategoriaService
) {

    fun obtenerTotalCategorias(): Int {
        return categoriaService.obtenerCategorias().size
    }

    fun obtenerTotalProductos(): Int {
        return productoService.obtenerProductos().size
    }

    fun obtenerTotalPromociones(): Int {
        return promocionService.obtenerPromociones().size
    }

    fun obtenerTotalUsuarios(): Int {
        return authService.obtenerUsuarios().size
    }
}