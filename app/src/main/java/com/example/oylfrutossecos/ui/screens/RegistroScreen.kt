
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistroScreen(
    modifier: Modifier = Modifier,
    onVolverLogin: () -> Unit = {}
) {

    var nombre by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    var mostrarErrores by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    val nombreInvalido = nombre.trim().length < 3
    val aliasInvalido = alias.trim().length < 3
    val correoInvalido = !android.util.Patterns
        .EMAIL_ADDRESS.matcher(correo).matches()
    val telefonoInvalido = telefono.count { it.isDigit() } < 9
    val contrasenaInvalida = contrasena.length < 6

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Crear Cuenta",
            fontSize = 28.sp
        )

        Text(
            text = "Regístrate en OYL Frutos Secos"
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && nombreInvalido,
            supportingText = {
                if (mostrarErrores && nombreInvalido)
                    Text("Ingresa al menos 3 caracteres")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text("Alias") },
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && aliasInvalido,
            supportingText = {
                if (mostrarErrores && aliasInvalido)
                    Text("Ingresa al menos 3 caracteres")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && correoInvalido,
            supportingText = {
                if (mostrarErrores && correoInvalido)
                    Text("Ingresa un correo válido")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && telefonoInvalido,
            supportingText = {
                if (mostrarErrores && telefonoInvalido)
                    Text("Ingresa un teléfono válido")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = mostrarErrores && contrasenaInvalida,
            supportingText = {
                if (mostrarErrores && contrasenaInvalida)
                    Text("Mínimo 6 caracteres")
            },
            singleLine = true
        )

        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                mostrarErrores = true

                mensaje = if (
                    !nombreInvalido &&
                    !aliasInvalido &&
                    !correoInvalido &&
                    !telefonoInvalido &&
                    !contrasenaInvalida
                ) {
                    "Datos válidos. Pendiente guardar la cuenta."
                } else {
                    "Corrige los campos marcados."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Cuenta")
        }

        if (mensaje.isNotEmpty()) {
            Text(text = mensaje)
        }

        TextButton(
            onClick = onVolverLogin
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
