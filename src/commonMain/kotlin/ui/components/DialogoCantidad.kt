package ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Diálogo para pedir una cantidad entera mayor a 0.
 *
 * [onConfirmar] recibe la cantidad ya validada y devuelve un mensaje de error si la operación
 * falla (por ejemplo, presupuesto excedido o stock insuficiente), o null si todo salió bien.
 * Quien lo llama es responsable de cerrar el diálogo cuando devuelve null.
 */
@Composable
fun DialogoCantidad(
    titulo: String,
    detalle: String,
    cantidadInicial: Int,
    textoConfirmar: String,
    onConfirmar: (Int) -> String?,
    onCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf(cantidadInicial.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(detalle, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = texto,
                    onValueChange = {
                        texto = it
                        error = null
                    },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cantidad = texto.trim().toIntOrNull()
                error = when {
                    texto.isBlank() -> "La cantidad es obligatoria"
                    cantidad == null -> "Ingresá un número entero"
                    cantidad <= 0 -> "Debe ser mayor a 0"
                    else -> onConfirmar(cantidad)
                }
            }) { Text(textoConfirmar) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
