
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeOYL = Color(0xFF1B5E20)
private val FondoOYL = Color(0xFFF8F9F3)
private val TextoOYL = Color(0xFF26332A)

@Composable
fun BienvenidaScreen(
    modifier: Modifier = Modifier,
    onComenzar: () -> Unit = {},
    onYaTengoCuenta: () -> Unit = onComenzar
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoOYL)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "OYL",
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = VerdeOYL
        )

        Text(
            text = "FRUTOS SECOS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOYL
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Productos naturales\nmás cerca de ti",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = TextoOYL
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Espacio para la fotografía
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(
                    Color(0xFFE5EDD9),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🥜 🌰 🥜",
                fontSize = 40.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BeneficioOYL("Catálogo actualizado")
            BeneficioOYL("Pedidos online")
            BeneficioOYL("Retiros y despachos")
            BeneficioOYL("Productos saludables")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onComenzar,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeOYL
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Comenzar", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onYaTengoCuenta,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "Ya tengo una cuenta",
                color = VerdeOYL
            )
        }
    }
}

@Composable
private fun BeneficioOYL(texto: String) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Box(
            modifier = Modifier
                .size(28.dp)
                .background(VerdeOYL, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = Color.White
            )
        }

        Text(
            text = texto,
            fontSize = 15.sp,
            color = TextoOYL
        )
    }
}
