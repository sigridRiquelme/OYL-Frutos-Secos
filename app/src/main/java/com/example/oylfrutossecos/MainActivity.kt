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
import com.example.oylfrutossecos.data.repository.ClienteRepository
// ViewModel
import com.example.oylfrutossecos.ui.viewmodel.ProductoViewModel
import com.example.oylfrutossecos.ui.viewmodel.ProductoViewModelFactory
import com.example.oylfrutossecos.ui.viewmodel.ClienteViewModel
import com.example.oylfrutossecos.ui.viewmodel.ClienteViewModelFactory
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

        // Repository para gestionar los clientes
        val clienteRepository = ClienteRepository(
            database.clienteDao()
        )

        // ViewModel para registrar clientes
        val clienteViewModel = ViewModelProvider(
            this,
            ClienteViewModelFactory(clienteRepository)
        )[ClienteViewModel::class.java]

        // 4. MOSTRAR NUESTRA APLICACION
        setContent {

            OYLFrutosSecosTheme {

                var pantallaActual by remember {
                    mutableStateOf("bienvenida")
                }

                var rolActual by remember {
                    mutableStateOf("Cliente")
                }

                var clienteIdActual by remember {
                    mutableStateOf<Int?>(null)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    when (pantallaActual) {

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


                        "login" -> {
                            LoginScreen(
                                clienteViewModel = clienteViewModel,
                                modifier = Modifier.padding(innerPadding),

                                onRegistrarse = {
                                    pantallaActual = "registro"
                                },

                                // Acceso temporal para el administrador
                                onIngresar = { rol, usuario, clave ->

                                    val correcto = rol == "Administrador" &&
                                            usuario == "admin" &&
                                            clave == "admin123"

                                    if (correcto) {
                                        rolActual = "Administrador"
                                        clienteIdActual = null
                                        pantallaActual = "dashboard"
                                    }

                                    correcto
                                },

                                // Acceso real de clientes mediante Room
                                onClienteAutenticado = { clienteId ->
                                    clienteIdActual = clienteId
                                    rolActual = "Cliente"
                                    pantallaActual = "dashboard"
                                }
                            )
                        }


                        "registro" -> {
                            RegistroScreen(
                                clienteViewModel = clienteViewModel,
                                modifier = Modifier.padding(innerPadding),

                                onVolverLogin = {
                                    pantallaActual = "login"
                                }
                            )
                        }


                        "dashboard" -> {
                            DashboardScreen(
                                rol = rolActual,
                                modifier = Modifier.padding(innerPadding),

                                productoViewModel = productoViewModel,

                                // Datos del cliente que inicio sesion
                                clienteViewModel = clienteViewModel,
                                clienteId = clienteIdActual,
                                onCerrarSesion = {
                                    clienteIdActual = null
                                    clienteViewModel.limpiarEstadoLogin()
                                    rolActual = "Cliente"
                                    pantallaActual = "login"
                                }
                            )
                        }

                    }
                }
            }
        }

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
                    "Productos insertados ahora: $seInsertaron. Total guardados: $cantidad"
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
