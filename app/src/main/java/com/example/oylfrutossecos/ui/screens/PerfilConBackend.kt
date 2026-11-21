package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.data.repository.AutenticacionRemota
import com.example.oylfrutossecos.data.repository.DatosPerfil
import com.example.oylfrutossecos.data.repository.SesionRemota
import com.example.oylfrutossecos.feature.autenticacion.domain.ValidadorDatosCliente
import com.example.oylfrutossecos.feature.autenticacion.platform.ValidadorCorreoAndroid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
internal fun PerfilConBackend(
    host: String,
    sesion: SesionRemota,
    onPerfilActualizado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val repositorio = remember(host) { AutenticacionRemota(host) }
    val alcance = rememberCoroutineScope()
    var perfil by remember(sesion) { mutableStateOf<DatosPerfil?>(null) }
    var borrador by remember(sesion) { mutableStateOf<DatosPerfil?>(null) }
    var error by remember(sesion) { mutableStateOf<String?>(null) }
    var intento by remember(sesion) { mutableIntStateOf(0) }
    var guardando by remember(sesion) { mutableStateOf(false) }
    var mostrarErrores by remember(sesion) { mutableStateOf(false) }
    var actualizado by remember(sesion) { mutableStateOf(false) }

    LaunchedEffect(repositorio, sesion, intento) {
        try {
            perfil = repositorio.consultarPerfil(sesion)
            perfil?.let { onPerfilActualizado(it.nombre) }
        } catch (cancelada: CancellationException) {
            throw cancelada
        } catch (fallo: Exception) {
            error = mensajePerfil(fallo, "No pudimos cargar tu perfil.")
        }
    }

    val datos = perfil
    val edicion = borrador
    val mensaje = error
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineMedium)
        when {
            datos == null && mensaje == null -> CircularProgressIndicator()
            datos == null -> {
                Text(mensaje.orEmpty(), color = MaterialTheme.colorScheme.error)
                Button(onClick = {
                    error = null
                    intento++
                }) { Text("Reintentar") }
            }
            edicion == null -> {
                Text(datos.nombre, style = MaterialTheme.typography.titleLarge)
                Text("Usuario: ${datos.nombreUsuario}")
                Text("Correo: ${datos.correo}")
                Text("Teléfono: ${datos.telefono}")
                Text("Dirección: ${datos.direccion.orEmpty().ifBlank { "Sin dirección" }}")
                if (actualizado) Text("Perfil actualizado.")
                Button(onClick = {
                    actualizado = false
                    mostrarErrores = false
                    error = null
                    borrador = datos
                }) { Text("Editar perfil") }
            }
            else -> {
                val nombreValido = ValidadorDatosCliente.nombreValido(edicion.nombre)
                val usuarioValido = ValidadorDatosCliente.usuarioValido(edicion.nombreUsuario)
                val correoValido = edicion.correo.trim().length <= 150 &&
                        ValidadorCorreoAndroid.esValido(edicion.correo.trim())
                val telefonoValido = ValidadorDatosCliente.telefonoValido(edicion.telefono)
                val direccionValida = ValidadorDatosCliente.direccionValida(edicion.direccion.orEmpty())

                CampoPerfil("Nombre completo", edicion.nombre, !guardando, mostrarErrores && !nombreValido,
                    "Usa entre 3 y 120 caracteres") { borrador = edicion.copy(nombre = it) }
                CampoPerfil("Nombre de usuario", edicion.nombreUsuario, !guardando, mostrarErrores && !usuarioValido,
                    "Usa de 3 a 80 letras, números, puntos, guiones o guiones bajos") { borrador = edicion.copy(nombreUsuario = it) }
                CampoPerfil("Correo electrónico", edicion.correo, !guardando, mostrarErrores && !correoValido,
                    "Ingresa un correo válido de hasta 150 caracteres") { borrador = edicion.copy(correo = it) }
                CampoPerfil("Teléfono", edicion.telefono, !guardando, mostrarErrores && !telefonoValido,
                    "Ingresa al menos 9 dígitos, con un máximo de 30 caracteres") { borrador = edicion.copy(telefono = it) }
                CampoPerfil("Dirección (opcional)", edicion.direccion.orEmpty(), !guardando, mostrarErrores && !direccionValida,
                    "Usa hasta 250 caracteres") { borrador = edicion.copy(direccion = it) }

                if (mensaje != null) Text(mensaje, color = MaterialTheme.colorScheme.error)
                Button(
                    enabled = !guardando,
                    onClick = {
                        mostrarErrores = true
                        error = null
                        if (!guardando && nombreValido && usuarioValido && correoValido && telefonoValido && direccionValida) {
                            val solicitud = DatosPerfil(
                                edicion.nombre.trim(), edicion.nombreUsuario.trim(), edicion.correo.trim(),
                                edicion.telefono.trim(), edicion.direccion?.trim()?.takeIf { it.isNotEmpty() }
                            )
                            guardando = true
                            alcance.launch {
                                try {
                                    val respuesta = repositorio.actualizarPerfil(sesion, solicitud)
                                    perfil = respuesta
                                    borrador = null
                                    actualizado = true
                                    onPerfilActualizado(respuesta.nombre)
                                } catch (cancelada: CancellationException) {
                                    throw cancelada
                                } catch (fallo: Exception) {
                                    error = mensajePerfil(fallo, "No pudimos confirmar el guardado. Reintenta o vuelve a abrir tu perfil.")
                                } finally {
                                    guardando = false
                                }
                            }
                        }
                    }
                ) { Text(if (guardando) "Guardando…" else "Guardar cambios") }
                TextButton(enabled = !guardando, onClick = {
                    borrador = null
                    error = null
                }) { Text("Cancelar edición") }
            }
        }
    }
}

@Composable
private fun CampoPerfil(
    etiqueta: String,
    valor: String,
    habilitado: Boolean,
    invalido: Boolean,
    ayuda: String,
    onCambiar: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambiar,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = habilitado,
        isError = invalido,
        supportingText = if (invalido) ({ Text(ayuda) }) else null
    )
}

private fun mensajePerfil(fallo: Exception, mensajeGeneral: String): String =
    when ((fallo as? HttpException)?.code()) {
        400 -> "Revisa los datos ingresados."
        401, 403 -> "Tu sesión ya no permite acceder al perfil. Vuelve a iniciar sesión."
        409 -> "El nombre de usuario o el correo ya está registrado."
        else -> mensajeGeneral
    }
