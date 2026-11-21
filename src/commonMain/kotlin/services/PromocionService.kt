package services

import config.FirebaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import models.Promocion

class PromocionService {
    private val _promociones = MutableStateFlow<List<Promocion>>(emptyList())
    val promociones: StateFlow<List<Promocion>> = _promociones.asStateFlow()

    private val db = FirebaseProvider.db

    suspend fun obtenerPromociones(): List<Promocion> {
        return try {
            val snapshot = db.collection("promociones").get()
            val lista = snapshot.documents.map { document ->
                val idCampo = document.get<String?>("id")?.takeIf { it.isNotBlank() }
                Promocion(
                    id = idCampo ?: document.id,
                    nombre = document.get<String?>("nombre") ?: "",
                    descuento = document.get<Double?>("descuento") ?: 0.0,
                    productoId = document.get<String?>("productoId") ?: ""
                )
            }
            _promociones.value = lista
            lista
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarPorId(id: String): Promocion? {
        return try {
            val docDirecto = db.collection("promociones").document(id).get()
            val document = if (docDirecto.exists) {
                docDirecto
            } else {
                db.collection("promociones")
                    .where { "id" equalTo id }
                    .get()
                    .documents
                    .firstOrNull()
            } ?: return null

            Promocion(
                id = document.get<String?>("id")?.takeIf { it.isNotBlank() } ?: document.id,
                nombre = document.get<String?>("nombre") ?: "",
                descuento = document.get<Double?>("descuento") ?: 0.0,
                productoId = document.get<String?>("productoId") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun agregarPromocion(nombre: String, descuento: Double, productoId: String): String {
        val promocionData = mapOf(
            "nombre" to nombre,
            "descuento" to descuento,
            "productoId" to productoId,
            "activa" to true
        )
        val promocionAgregada = db.collection("promociones").add(promocionData)
        return promocionAgregada.id
    }

    suspend fun actualizarPromocion(id: String, nombre: String, descuento: Double, productoId: String): Boolean {
        return try {
            val promocion = db.collection("promociones").document(id).get()
            val promocionId = if (promocion.exists) {
                promocion.id
            } else {
                db.collection("promociones")
                    .where { "id" equalTo id }
                    .get()
                    .documents
                    .firstOrNull()
                    ?.id
            } ?: return false

            db.collection("promociones").document(promocionId).update(
                mapOf(
                    "nombre" to nombre,
                    "descuento" to descuento,
                    "productoId" to productoId
                )
            )
            obtenerPromociones()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarPromocion(promocion: Promocion): Boolean {
        return try {
            db.collection("promociones").document(promocion.id).delete()
            obtenerPromociones()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarPorProductoId(productoId: String) {
        try {
            val snapshot = db.collection("promociones")
                .where { "productoId" equalTo productoId }
                .get()

            for (document in snapshot.documents) {
                db.collection("promociones").document(document.id).delete()
            }
            obtenerPromociones()
        } catch (_: Exception) {
        }
    }

    fun cambiarEstadoPromocion(id: String, activa: Boolean) {
        _promociones.value = _promociones.value.map {
            if (it.id == id) it.copy(activa = activa) else it
        }
    }
}