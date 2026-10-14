
package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onRegistrarse: () -> Unit = {},
    onIngresar: (String, String, String) -> Boolean = { _, _, _ -> false }
) {

    var alias by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("Cliente") }
    var mensajeError by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "OYL Frutos Secos",
            style = MaterialTheme.typography.headlineMedium
        )


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selecciona tu tipo de usuario",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = rol == "Cliente",
                    onClick = { rol = "Cliente" }
                )

                Text("Cliente")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = rol == "Administrador",
                    onClick = { rol = "Administrador" }
                )

                Text("Administrador")
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text("Alias") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val accesoCorrecto = onIngresar(
                    rol,
                    alias.trim(),
                    contrasena
                )

                mensajeError = if (accesoCorrecto) {
                    ""
                } else {
                    "Alias o contraseña incorrectos"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        TextButton(
            onClick = onRegistrarse
        ) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}
