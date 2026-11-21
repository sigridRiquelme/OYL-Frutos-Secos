
package com.example.oylfrutossecos.feature.navegacion.domain

// Acciones principales del flujo de compra.
enum class AccionCliente {
    ABRIR_CATALOGO,
    VER_DETALLE,
    VOLVER_A_CATALOGO,
    ABRIR_CARRITO,
    CONTINUAR_A_CONFIRMACION,
    VOLVER_A_CARRITO,
    PEDIDO_REGISTRADO,
    VOLVER_A_INICIO
}

object NavegacionCliente {

    // Determina la pantalla siguiente sin depender
    // de Android ni de componentes de Compose.
    fun siguientePantalla(
        pantallaActual: String,
        accion: AccionCliente
    ): String {

        return when (accion) {

            AccionCliente.ABRIR_CATALOGO,
            AccionCliente.VOLVER_A_CATALOGO -> "Catalogo"

            AccionCliente.ABRIR_CARRITO,
            AccionCliente.VOLVER_A_CARRITO -> "Carrito"

            AccionCliente.VER_DETALLE -> {

                if (
                    pantallaActual == "Catalogo" ||
                    pantallaActual == "QR"
                ) {
                    "Detalle"
                } else {
                    pantallaActual
                }
            }

            AccionCliente.CONTINUAR_A_CONFIRMACION -> {

                if (pantallaActual == "Carrito") {
                    "Confirmacion"
                } else {
                    pantallaActual
                }
            }

            AccionCliente.PEDIDO_REGISTRADO -> {

                if (pantallaActual == "Confirmacion") {
                    "PedidoExito"
                } else {
                    pantallaActual
                }
            }

            AccionCliente.VOLVER_A_INICIO -> "Inicio"
        }
    }
}
