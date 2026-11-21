package services

import config.FirebaseProvider
import models.Promocion

class PromocionService {

    private val db = FirebaseProvider.db

    suspend fun obtenerPromociones(): List<Promocion> {
        return try {
            val snapshot = db.collection("promociones").get()
            snapshot.documents.map { document ->
                Promocion(
                    id = document.get<Int?>("id") ?: 0,
                    nombre = document.get<String?>("nombre") ?: "",
                    descuento = document.get<Double?>("descuento") ?: 0.0,
                    productoId = document.get<String?>("productoId") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun buscarPorId(id: Int): Promocion? {
        return try {
            val snapshot = db.collection("promociones")
                .where { "id" equalTo id }
                .get()

            snapshot.documents.firstOrNull()?.let { document ->
                Promocion(
                    id = document.get<Int?>("id") ?: id,
                    nombre = document.get<String?>("nombre") ?: "",
                    descuento = document.get<Double?>("descuento") ?: 0.0,
                    productoId = document.get<String?>("productoId") ?: ""
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun agregarPromocion(nombre: String, descuento: Double, productoId: String): Promocion {
        // Obtenemos un nuevo ID autoincremental basado en las promociones existentes
        val actuales = obtenerPromociones()
        val nuevoId = (actuales.maxOfOrNull { it.id } ?: 0) + 1

        val nuevaPromocion = Promocion(
            id = nuevoId,
            nombre = nombre,
            descuento = descuento,
            productoId = productoId
        )

        // Guardamos en Firestore usando un id de documento automático pero guardando el campo "id" interno
        val docRef = db.collection("promociones").add(
            mapOf(
                "id" to nuevaPromocion.id,
                "nombre" to nuevaPromocion.nombre,
                "descuento" to nuevaPromocion.descuento,
                "productoId" to nuevaPromocion.productoId
            )
        )

        return nuevaPromocion
    }

    suspend fun actualizarPromocion(id: Int, nombre: String, descuento: Double, productoId: String): Boolean {
        return try {
            val snapshot = db.collection("promociones")
                .where { "id" equalTo id }
                .get()

            val document = snapshot.documents.firstOrNull() ?: return false

            // Actualizamos los campos en Firestore
            db.collection("promociones").document(document.id).update(
                mapOf(
                    "nombre" to nombre,
                    "descuento" to descuento,
                    "productoId" to productoId
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarPromocion(promocion: Promocion): Boolean {
        return try {
            val snapshot = db.collection("promociones")
                .where { "id" equalTo promocion.id }
                .get()

            val document = snapshot.documents.firstOrNull() ?: return false
            db.collection("promociones").document(document.id).delete()
            true
        } catch (e: Exception) {
            false
        }
    }
}