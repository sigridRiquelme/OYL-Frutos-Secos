
package com.example.oylfrutossecos.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

import androidx.compose.foundation.layout.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PedidoExitoScreen(
    numeroPedido: String,
    modalidad: String,
    total: Int,
    onVolverInicio: () -> Unit,
    modifier: Modifier = Modifier
) {

    // Estado que inicia la animación
    var animacionIniciada by remember(numeroPedido) {
        mutableStateOf(false)
    }

    // Iniciar cuando aparece la confirmación
    LaunchedEffect(numeroPedido) {
        animacionIniciada = true
    }

    // Animación de escala del símbolo de éxito
    val escalaIcono by animateFloatAsState(
        targetValue = if (animacionIniciada) 1f else 0.55f,

        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),

        label = "Escala de confirmación"
    )

    // Animación de transparencia
    val opacidad by animateFloatAsState(
        targetValue = if (animacionIniciada) 1f else 0f,

        animationSpec = tween(
            durationMillis = 650,
            easing = FastOutSlowInEasing
        ),

        label = "Opacidad de confirmación"
    )

    // Desplazamiento suave del contenido
    val desplazamiento by animateFloatAsState(
        targetValue = if (animacionIniciada) 0f else 28f,

        animationSpec = tween(
            durationMillis = 650,
            easing = FastOutSlowInEasing
        ),

        label = "Desplazamiento de confirmación"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // SÍMBOLO DE CONFIRMACIÓN ANIMADO
        Text(
            text = "✓",

            fontSize = 90.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),

            modifier = Modifier.graphicsLayer {
                scaleX = escalaIcono
                scaleY = escalaIcono
                alpha = opacidad
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // CONTENIDO QUE APARECE SUAVEMENTE
        Column(
            modifier = Modifier.graphicsLayer {
                alpha = opacidad
                translationY = desplazamiento
            },

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "¡Solicitud registrada!",

                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Tu pedido fue guardado correctamente.",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),

                    horizontalAlignment = Alignment.CenterHorizontally,

                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text("Número de pedido")

                    Text(
                        text = numeroPedido,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text("Estado: Pendiente")

                    Text("Modalidad: $modalidad")

                    Text("Total: $${total}")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pedido guardado localmente en este dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onVolverInicio,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Volver al inicio")
            }
        }
    }
}
