package services

class EstadisticaService(
    private val authService: AuthService,
    private val productoService: ProductoService,
    private val promocionService: PromocionService,
    private val categoriaService: CategoriaService,
    private val listaCompraService: ListaCompraService
) {
    // ==========================================================
    // ESTADÍSTICAS ADMINISTRACIÓN
    // ==========================================================

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

    // ==========================================================
    // ESTADÍSTICAS CLIENTES
    // ==========================================================

    fun obtenerProductosDistintosCliente(): Int {
        return listaCompraService.obtenerElementos().size
    }

    fun obtenerUnidadesTotalesCliente(): Int {
        return listaCompraService.obtenerElementos().values.sum()
    }

    fun obtenerTotalCompraCliente(): Double {
        return listaCompraService.calcularTotalLista()
    }

    fun obtenerSaldoDisponibleCliente(): Double {
        return listaCompraService.calcularSaldoDisponible()
    }
}