package ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Campo de búsqueda redondeado con lupa y botón para borrar (se reutiliza en varias pantallas). */
@Composable
fun CampoBusqueda(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { onCambio("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda")
                }
            }
        }
    )
}
