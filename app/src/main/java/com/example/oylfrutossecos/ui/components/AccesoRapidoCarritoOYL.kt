
package com.example.oylfrutossecos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.oylfrutossecos.feature.navegacion.domain.ReglasNavegacionDashboard
import com.example.oylfrutossecos.ui.theme.VerdeOyl
import com.example.oylfrutossecos.ui.theme.VerdeOylOscuro

@Composable
fun AccesoRapidoCarritoOYL(
    unidadesEnCarrito: Int,
    onAbrirCarrito: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 4.dp),

        horizontalArrangement = Arrangement.End
    ) {

        Surface(
            onClick = onAbrirCarrito,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                VerdeOyl.copy(alpha = 0.25f)
            ),
            shadowElevation = 2.dp
        ) {

            Row(
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .padding(horizontal = 14.dp),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {

                // Icono del carrito
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    tint = VerdeOyl,
                    modifier = Modifier.size(22.dp)
                )

                // Nombre del acceso
                Text(
                    text = "Mi carrito",
                    color = VerdeOylOscuro,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                // Indicador de unidades
                Box(
                    modifier = Modifier
                        .defaultMinSize(
                            minWidth = 23.dp,
                            minHeight = 23.dp
                        )
                        .background(
                            if (unidadesEnCarrito > 0) {
                                VerdeOyl
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            CircleShape
                        )
                        .padding(horizontal = 5.dp),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = ReglasNavegacionDashboard
                            .etiquetaCantidadCarrito(
                                unidadesEnCarrito
                            ),

                        color = if (unidadesEnCarrito > 0) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },

                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
