
package com.example.oylfrutossecos

import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Base de datos y Repository
import com.example.oylfrutossecos.data.local.AppDatabase
import com.example.oylfrutossecos.data.local.insertarProductosIniciales
import com.example.oylfrutossecos.data.repository.ProductoRepository

// ViewModel
import com.example.oylfrutossecos.ui.viewmodel.ProductoViewModel
import com.example.oylfrutossecos.ui.viewmodel.ProductoViewModelFactory

// Pantallas
import com.example.oylfrutossecos.ui.screens.BienvenidaScreen
import com.example.oylfrutossecos.ui.screens.LoginScreen
import com.example.oylfrutossecos.ui.screens.RegistroScreen
import com.example.oylfrutossecos.ui.screens.DashboardScreen

import com.example.oylfrutossecos.ui.theme.OYLFrutosSecosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // 1. CONECTAR LA BASE DE DATOS
        val database = AppDatabase.getDatabase(
            applicationContext
        )

        // 2. CREAR EL REPOSITORY
        val repository = ProductoRepository(
            database.productoDao()
        )

        // 3. CREAR EL VIEWMODEL
        val productoViewModel = ViewModelProvider(
            this,
            ProductoViewModelFactory(repository)
        )[ProductoViewModel::class.java]

        // 4. MOSTRAR NUESTRA APLICACION
        setContent {

            OYLFrutosSecosTheme {

                var pantallaActual by remember {
                    mutableStateOf("bienvenida")
                }

                var rolActual by remember {
                    mutableStateOf("Cliente")
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    when (pantallaActual) {

                        // PANTALLA DE BIENVENIDA
                        "bienvenida" -> {

                            BienvenidaScreen(
                                modifier = Modifier.padding(innerPadding),

                                onComenzar = {
                                    pantallaActual = "login"
                                },

                                onYaTengoCuenta = {
                                    pantallaActual = "login"
                                }
                            )
                        }

                        // INICIO DE SESION
                        "login" -> {

                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),

                                onRegistrarse = {
                                    pantallaActual = "registro"
                                },

                                onIngresar = { rol, usuario, clave ->

                                    val correcto = when (rol) {

                                        "Administrador" ->
                                            usuario == "admin" &&
                                                    clave == "admin123"

                                        "Cliente" ->
                                            usuario == "cliente" &&
                                                    clave == "cliente123"

                                        else -> false
                                    }

                                    if (correcto) {

                                        rolActual = rol
                                        pantallaActual = "dashboard"
                                    }

                                    correcto
                                }
                            )
                        }

                        // REGISTRO
                        "registro" -> {

                            RegistroScreen(
                                modifier = Modifier.padding(innerPadding),

                                onVolverLogin = {
                                    pantallaActual = "login"
                                }
                            )
                        }

                        // DASHBOARD
                        "dashboard" -> {

                            DashboardScreen(
                                rol = rolActual,

                                modifier = Modifier.padding(innerPadding),

                                productoViewModel = productoViewModel,

                                // NUEVO: CIERRE DE SESION
                                onCerrarSesion = {

                                    // Restablecer rol
                                    rolActual = "Cliente"

                                    // Regresar al login
                                    pantallaActual = "login"
                                }
                            )
                        }

                    } // Cierra when

                } // Cierra Scaffold

            } // Cierra OYLFrutosSecosTheme

        } // Cierra setContent

        // 5. INSERTAR PRODUCTOS INICIALES EN ROOM
        lifecycleScope.launch(Dispatchers.IO) {

            try {

                val seInsertaron = insertarProductosIniciales(
                    database
                )

                val cantidad = database.productoDao()
                    .contarProductos()

                Log.d(
                    "OYL_ROOM",
                    "Productos insertados ahora: " +
                            "$seInsertaron. Total guardados: $cantidad"
                )

            } catch (error: Exception) {

                Log.e(
                    "OYL_ROOM",
                    "Error al utilizar Room",
                    error
                )
            }
        }
    }
}
