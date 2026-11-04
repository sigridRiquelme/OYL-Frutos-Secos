package com.example.oylfrutossecos.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

// Seguimos utilizando datos de OpenStreetMap, pero con un proveedor de mapas
// que admite aplicaciones gratuitas sin API key: OpenFreeMap.
// Esto evita solicitar teselas al servidor OSM que devolvia el aviso 403.
private const val ESTILO_MAPA_OYL = "https://tiles.openfreemap.org/styles/liberty"

private data class TiendaDemoOYL(
    val id: Int,
    val nombre: String,
    val direccion: String,
    val horario: String,
    val latitud: Double,
    val longitud: Double
)

// Tiendas referenciales para la demostracion academica. NO son sucursales reales.
// Un mismo listado alimenta los tres marcadores y sus tarjetas.
private val tiendasDemoOYL = listOf(
    TiendaDemoOYL(1, "Tienda demo 1 · Santiago Centro", "Plaza de Armas, Santiago",
        "Lun a vie · 10:00–18:00", -33.4373, -70.6506),
    TiendaDemoOYL(2, "Tienda demo 2 · Providencia", "Parque Bustamante, Providencia",
        "Lun a sáb · 10:00–19:00", -33.4425, -70.6320),
    TiendaDemoOYL(3, "Tienda demo 3 · Quinta Normal", "Parque Quinta Normal, Santiago",
        "Lun a vie · 11:00–18:00", -33.4411, -70.6836)
)

@Composable
fun MapaScreen(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    var tiendaSeleccionadaId by rememberSaveable { mutableIntStateOf(1) }
    var mapaControl by remember { mutableStateOf<MapLibreMap?>(null) }

    // Se crea el mapa una sola vez mientras esta pantalla permanece abierta.
    val mapaVista = remember(contexto) {
        MapLibre.getInstance(contexto.applicationContext)
        MapView(contexto).apply {
            onCreate(null)
            setOnTouchListener { vista, evento ->
                when (evento.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                        vista.parent?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        vista.parent?.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
        }
    }

    // Sincronizar el mapa con el ciclo de vida de Android para evitar fugas.
    val duenoCicloVida = contexto as? LifecycleOwner
    DisposableEffect(mapaVista, duenoCicloVida) {
        var iniciada = false
        var reanudada = false

        fun iniciar() {
            if (!iniciada) { mapaVista.onStart(); iniciada = true }
        }
        fun reanudar() {
            iniciar()
            if (!reanudada) { mapaVista.onResume(); reanudada = true }
        }
        fun pausar() {
            if (reanudada) { mapaVista.onPause(); reanudada = false }
        }
        fun detener() {
            pausar()
            if (iniciada) { mapaVista.onStop(); iniciada = false }
        }

        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_START -> iniciar()
                Lifecycle.Event.ON_RESUME -> reanudar()
                Lifecycle.Event.ON_PAUSE -> pausar()
                Lifecycle.Event.ON_STOP -> detener()
                else -> Unit
            }
        }
        if (duenoCicloVida != null) {
            duenoCicloVida.lifecycle.addObserver(observador)
            if (duenoCicloVida.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) iniciar()
            if (duenoCicloVida.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) reanudar()
        } else {
            reanudar()
        }

        mapaVista.getMapAsync { mapa ->
            mapa.setStyle(ESTILO_MAPA_OYL) {
                // Vista general que permite ver las tres tiendas de Santiago.
                mapa.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(-33.4402, -70.6575))
                    .zoom(11.9)
                    .build()

                tiendasDemoOYL.forEach { tienda ->
                    mapa.addMarker(
                        MarkerOptions()
                            .position(LatLng(tienda.latitud, tienda.longitud))
                            .title(tienda.nombre)
                            .snippet(tienda.direccion)
                    )
                }

                mapa.setOnMarkerClickListener { marcador ->
                    val tienda = tiendasDemoOYL.firstOrNull { it.nombre == marcador.title }
                    if (tienda != null) {
                        tiendaSeleccionadaId = tienda.id
                        mapa.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(tienda.latitud, tienda.longitud), 14.0
                            ), 450
                        )
                    }
                    true
                }
                mapaControl = mapa
            }
        }

        onDispose {
            duenoCicloVida?.lifecycle?.removeObserver(observador)
            mapaControl = null
            detener()
            mapaVista.onDestroy()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 15.dp)) {
            Text(
                text = "Nuestras tiendas demo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "3 ubicaciones de prueba en Santiago · Toca un marcador o una tarjeta",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(280.dp)) {
                AndroidView(
                    factory = { mapaVista },
                    modifier = Modifier.fillMaxSize()
                )
                // Atribucion obligatoria visible para los datos y el estilo utilizados.
                Text(
                    text = "© OpenStreetMap contributors · OpenFreeMap",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF34413B),
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(7.dp)
                        .background(Color.White.copy(alpha = 0.93f), RoundedCornerShape(5.dp))
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                )
            }
        }

        tiendasDemoOYL.forEach { tienda ->
            val seleccionada = tienda.id == tiendaSeleccionadaId
            Card(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clickable {
                        tiendaSeleccionadaId = tienda.id
                        mapaControl?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(tienda.latitud, tienda.longitud), 14.0
                            ), 450
                        )
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (seleccionada) Color(0xFFE8F3E8) else Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    Modifier.padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "TIENDA DEMO ${tienda.id}" + if (seleccionada) " · SELECCIONADA" else "",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tienda.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(tienda.direccion, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Horario demo: ${tienda.horario}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Button(
                            onClick = {
                                val url = "https://www.google.com/maps/dir/?api=1&destination=${tienda.latitud},${tienda.longitud}"
                                contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Cómo llegar") }
                        OutlinedButton(
                            onClick = {
                                val url = "https://www.openstreetmap.org/?mlat=${tienda.latitud}&mlon=${tienda.longitud}#map=16/${tienda.latitud}/${tienda.longitud}"
                                contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Abrir en OSM") }
                    }
                }
            }
        }
        Text(
            text = "Las tres tiendas y sus horarios son ficticios. Las ubicaciones son referenciales para la demostración académica.",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
