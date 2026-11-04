package com.example.oylfrutossecos.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeBienvenida = Color(0xFF176243)

@Composable
fun BienvenidaScreen(
    modifier: Modifier = Modifier,
    onComenzar: () -> Unit = {},
    onYaTengoCuenta: () -> Unit = onComenzar
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val opacidad by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "entradaBienvenida"
    )

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezado, foto y beneficios pueden desplazarse en telefonos pequeños.
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .graphicsLayer { alpha = opacidad },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(22.dp))
            Text("OYL", fontSize = 54.sp, fontWeight = FontWeight.ExtraBold,
                color = VerdeBienvenida, letterSpacing = 2.sp)
            Text("FRUTOS SECOS", fontSize = 15.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            Text("Productos naturales, más cerca de ti", textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(22.dp))

            // Fotografía auténtica de frutos secos; nunca usar emojis como imagen.
            Box(
                modifier = Modifier.fillMaxWidth().height(205.dp)
                    .clip(RoundedCornerShape(23.dp))
            ) {
                FotoProductoOYL(
                    nombre = "Selección natural de frutos secos",
                    imagenUri = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                BeneficioBienvenida("Catálogo actualizado")
                BeneficioBienvenida("Pedidos desde tu celular")
                BeneficioBienvenida("Tiendas y puntos de retiro")
                BeneficioBienvenida("Productos saludables")
            }
        }

        // Acciones fijas y legibles incluso si el contenido anterior hace scroll.
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 10.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Button(
                onClick = onComenzar,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeBienvenida,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("Comenzar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            OutlinedButton(
                onClick = onYaTengoCuenta,
                modifier = Modifier.fillMaxWidth().height(49.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("Ya tengo una cuenta", fontWeight = FontWeight.SemiBold,
                    color = VerdeBienvenida)
            }
        }
    }
}

@Composable
private fun BeneficioBienvenida(titulo: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(27.dp)
                .background(Color(0xFFE7F1E7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = VerdeBienvenida, fontWeight = FontWeight.Bold)
        }
        Text(titulo, color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium)
    }
}
