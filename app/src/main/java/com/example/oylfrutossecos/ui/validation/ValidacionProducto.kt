
package com.example.oylfrutossecos.ui.validation

// Alias temporal para mantener compatibles
// los archivos que utilizan el modelo antiguo.
typealias DatosNuevoProducto =
        com.example.oylfrutossecos.feature.productos.domain.DatosNuevoProducto

// Delegar al validador del nuevo módulo.
object ValidadorProducto {

    fun validar(
        datos: DatosNuevoProducto
    ): Map<String, String> {

        return com.example.oylfrutossecos.feature.productos.domain
            .ValidadorProducto.validar(datos)
    }
}
