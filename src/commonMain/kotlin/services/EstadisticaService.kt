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
        //return categoriaService.obtenerCategorias().size
        return 1
    }

    fun obtenerTotalProductos(): Int {
        //return productoService.obtenerProductos().size
        return 1
    }

    fun obtenerTotalPromociones(): Int {
        //return promocionService.obtenerPromociones().size
        return 1
    }

    fun obtenerTotalUsuarios(): Int {
        return authService.obtenerUsuarios().size
    }

    // ==========================================================
    // ESTADÍSTICAS CLIENTES
    // ==========================================================

    fun obtenerProductosDistintosCliente(): Int {
        //return listaCompraService.obtenerElementos().size
        return 1
    }

    fun obtenerUnidadesTotalesCliente(): Int {
        //return listaCompraService.obtenerElementos().values.sum()
        return 1
    }

    fun obtenerTotalCompraCliente(): Double {
        return listaCompraService.calcularTotalLista()
    }

    fun obtenerSaldoDisponibleCliente() {
        //return listaCompraService.calcularSaldoDisponible()
    }
}