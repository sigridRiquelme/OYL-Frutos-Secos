package com.example.oylfrutossecos.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.oylfrutossecos.ui.screens.ProductoCatalogo
import org.json.JSONObject

// El contenido del QR ES un JSON, por ejemplo:
// {"codigo":"OYL-1","idProducto":1,"nombre":"Almendras"}
// El precio y el stock se consultan en Room, NO se confia en datos del QR.
data class QrUiState(
    val codigoLeido: String = "",
    val idProducto: Int? = null,
    val mensaje: String = "Escanea el QR de un producto.",
    val nombreReferencia: String? = null
)

class QrViewModel : ViewModel() {

    var estado by mutableStateOf(QrUiState())
        private set

    fun iniciando() {
        estado = QrUiState(mensaje = "Abriendo escáner...")
    }

    fun procesar(contenidoQr: String, productos: List<ProductoCatalogo>) {
        val texto = contenidoQr.trim()

        if (texto.length > 2048 || !texto.startsWith("{")) {
            estado = QrUiState(mensaje = "Código no reconocido. Escanea un producto de OYL.")
            return
        }

        // Leer directamente el texto del QR. No hacen falta clases ni archivos JSON adicionales.
        try {
            val json = JSONObject(texto)
            // Si el JSON trae solo {"codigo":"OYL-1"}, tambien funciona.
            val codigoOriginal = json.optString("codigo").trim()
            val idDesdeCodigo = Regex("^OYL-([1-9][0-9]*)$", RegexOption.IGNORE_CASE)
                .matchEntire(codigoOriginal)?.groupValues?.get(1)?.toIntOrNull()
            val id = json.optInt("idProducto", json.optInt("id", idDesdeCodigo ?: -1))
            val codigo = codigoOriginal.ifBlank { "OYL-$id" }
            val nombre = json.optString("nombre", json.optString("nombreReferencia", ""))

            if (id <= 0 || !codigo.equals("OYL-$id", ignoreCase = true)) {
                estado = QrUiState(mensaje = "Este código no corresponde a un producto válido.")
                return
            }

            val producto = productos.firstOrNull { it.id == id }
            if (producto == null) {
                estado = QrUiState(codigoLeido = codigo,
                    mensaje = "No encontramos este producto en el catálogo.")
                return
            }

            estado = QrUiState(
                codigoLeido = codigo,
                idProducto = id,
                mensaje = "Producto identificado correctamente.",
                nombreReferencia = nombre.ifBlank { producto.nombre }
            )
        } catch (_: Exception) {
            estado = QrUiState(mensaje = "No se pudo leer el código QR. Intenta nuevamente.")
        }
    }

    fun error(mensaje: String) {
        estado = estado.copy(mensaje = mensaje)
    }
}
