
package com.example.oylfrutossecos.feature.productos.domain

object ValidadorProducto {

    fun validar(
        datos: DatosNuevoProducto
    ): Map<String, String> {

        val errores = mutableMapOf<String, String>()

        // Nombre: mínimo 3 caracteres
        if (datos.nombre.trim().length < 3) {
            errores["nombre"] =
                "El nombre debe tener al menos 3 caracteres"
        }

        // Categoría obligatoria
        if (datos.categoria.isBlank()) {
            errores["categoria"] =
                "Selecciona una categoría"
        }

        // Precio: número entero mayor a cero
        val precio = datos.precio.toIntOrNull()

        if (precio == null || precio <= 0) {
            errores["precio"] =
                "Ingresa un precio mayor a cero"
        }

        // Stock: número entero no negativo
        val stock = datos.stock.toIntOrNull()

        if (stock == null || stock < 0) {
            errores["stock"] =
                "Ingresa un stock válido (0 o más)"
        }

        // Descripción obligatoria
        if (datos.descripcion.isBlank()) {
            errores["descripcion"] =
                "Ingresa una descripción"
        }

        // Presentación obligatoria
        if (datos.presentacion.isBlank()) {
            errores["presentacion"] =
                "Ingresa una presentación"
        }

        return errores
    }
}
