
package com.example.oylfrutossecos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.example.oylfrutossecos.data.model.ProductoCatalogo
import com.example.oylfrutossecos.ui.screens.CatalogoScreen
import com.example.oylfrutossecos.ui.screens.DetalleProductoScreen

@Composable
fun PantallasExploracionCliente(
    pantalla: String,
    productos: List<ProductoCatalogo>,
    productoSeleccionado: ProductoCatalogo?,
    rol: String,
    cantidadesCarrito: Map<Int, Int>,
    onVerDetalle: (ProductoCatalogo) -> Unit,
    onVolverCatalogo: () -> Unit,
    onAgregar: (ProductoCatalogo, Int) -> Unit,
    modifier: Modifier = Modifier
) {

    when (pantalla) {

        // Mostrar catálogo de productos
        "Catalogo" -> {

            CatalogoScreen(
                productos = productos,
                modifier = modifier,
                onVerDetalle = onVerDetalle
            )
        }

        // Mostrar detalle del producto seleccionado
        "Detalle" -> {

            val producto = productoSeleccionado

            if (producto != null) {

                DetalleProductoScreen(
                    producto = producto,
                    rol = rol,

                    cantidadYaAgregada =
                        cantidadesCarrito[producto.id] ?: 0,

                    onVolver = onVolverCatalogo,

                    onAgregar = { cantidad ->
                        onAgregar(producto, cantidad)
                    },

                    modifier = modifier
                )
            }
        }
    }
}
