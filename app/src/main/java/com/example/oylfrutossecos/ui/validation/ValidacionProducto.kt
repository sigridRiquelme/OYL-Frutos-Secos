
package com.example.oylfrutossecos.ui.validation

data class DatosNuevoProducto(
    val nombre: String,
    val categoria: String,
    val precio: String,
    val stock: String,
    val descripcion: String,
    val presentacion: String
)

object ValidadorProducto {

    fun validar(
        datos: DatosNuevoProducto
    ): Map<String, String> {

        val errores = mutableMapOf<String, String>()

        if (datos.nombre.trim().length < 3) {
            errores["nombre"] =
                "El nombre debe tener al menos 3 caracteres"
        }

        if (datos.categoria.isBlank()) {
            errores["categoria"] =
                "Selecciona una categoría"
        }

        val precio = datos.precio.toIntOrNull()

        if (precio == null || precio <= 0) {
            errores["precio"] =
                "Ingresa un precio mayor a cero"
        }

        val stock = datos.stock.toIntOrNull()

        if (stock == null || stock < 0) {
            errores["stock"] =
                "Ingresa un stock válido (0 o más)"
        }

        if (datos.descripcion.isBlank()) {
            errores["descripcion"] =
                "Ingresa una descripción"
        }

        if (datos.presentacion.isBlank()) {
            errores["presentacion"] =
                "Ingresa una presentación"
        }

        return errores
    }
}

