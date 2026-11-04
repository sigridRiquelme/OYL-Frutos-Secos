package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

// Pantalla inicial renovada: conserva los callbacks del Dashboard existente.
@Composable
fun InicioDashboardOYL(
    rol: String,
    nombreCliente: String,
    productosDestacados: List<ProductoCatalogo>,
    onAbrirMapa: () -> Unit,
    onAbrirQr: () -> Unit,
    onAbrirCatalogo: () -> Unit,
    onAbrirCarrito: () -> Unit,
    onVerMisPedidos: () -> Unit,
    onGestionProductos: () -> Unit,
    onGestionInventario: () -> Unit,
    productosStockBajo: Int,
    productosAgotados: Int,
    onVerAlertasStock: () -> Unit,
    pedidosPendientes: Int,
    onVerPedidosPendientes: () -> Unit,
    totalAvisos: Int,
    onAbrirNotificaciones: () -> Unit
) {
    val administrador = rol == "Administrador"
    val nombreCorto = nombreCliente.trim().substringBefore(' ').ifBlank { "" }
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(Modifier.padding(horizontal = 19.dp)) {
            Text(
                if (administrador) "Hola, administrador" else if (nombreCorto.isNotEmpty())
                    "¡Hola, $nombreCorto!" else "¡Hola!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                if (administrador) "Así va tu tienda hoy" else "Qué bueno verte de nuevo",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Fotografia real y encabezado corto; no un mosaico gigante de emojis.
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF176243))
        ) {
            Row(Modifier.height(151.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1.2f).padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text("OYL FRUTOS SECOS", fontWeight = FontWeight.ExtraBold,
                        color = Color.White, fontSize = 13.sp, letterSpacing = 1.sp)
                    Text("Naturalmente delicioso", color = Color.White,
                        fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 20.sp)
                    Text("Descubre nuestra selección",
                        color = Color(0xFFE0F0E5), fontSize = 11.sp)
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    FotoProductoOYL(
                        nombre = "frutos secos variados",
                        imagenUri = "https://images.pexels.com/photos/4753637/pexels-photo-4753637.jpeg?auto=compress&cs=tinysrgb&w=700",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (administrador) {
            Text("Resumen de la tienda", modifier = Modifier.padding(horizontal = 19.dp),
                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                AccesoOYL("Pedidos pendientes", "$pedidosPendientes por revisar", Icons.Filled.ReceiptLong,
                    onVerPedidosPendientes, Modifier.weight(1f))
                AccesoOYL("Alertas de stock", "${productosStockBajo + productosAgotados} alertas", Icons.Filled.WarningAmber,
                    onVerAlertasStock, Modifier.weight(1f))
            }
            Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                AccesoOYL("Productos", "Gestionar catálogo", Icons.Filled.GridView,
                    onGestionProductos, Modifier.weight(1f))
                AccesoOYL("Inventario", "Revisar existencias", Icons.Filled.Inventory2,
                    onGestionInventario, Modifier.weight(1f))
            }
            if (totalAvisos > 0) {
                OutlinedButton(
                    onClick = onAbrirNotificaciones,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)
                ) { Text("Ver notificaciones ($totalAvisos)") }
            }
        } else {
            Text("Explora y compra", modifier = Modifier.padding(horizontal = 19.dp),
                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                AccesoOYL("Catálogo", "Ver productos", Icons.Filled.ShoppingBag, onAbrirCatalogo, Modifier.weight(1f))
                AccesoOYL("Mi carrito", "Revisar compra", Icons.Filled.ShoppingCart, onAbrirCarrito, Modifier.weight(1f))
            }
            Row(Modifier.padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                AccesoOYL("Mapa", "Puntos de retiro", Icons.Filled.Place,
                    onAbrirMapa, Modifier.weight(1f))
                AccesoOYL("Mis pedidos", "Tu historial", Icons.Filled.ReceiptLong,
                    onVerMisPedidos, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 19.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Productos destacados", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onAbrirCatalogo) { Text("Ver todos") }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                productosDestacados.filter { it.activo }.take(5).forEach { producto ->
                    Card(
                        modifier = Modifier.width(151.dp).clickable { onAbrirCatalogo() },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        FotoProductoOYL(
                            nombre = producto.nombre,
                            imagenUri = producto.imagenUri,
                            modifier = Modifier.fillMaxWidth().height(104.dp)
                        )
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(producto.nombre, fontWeight = FontWeight.SemiBold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(precioOYL(producto.precio),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = onAbrirQr,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(9.dp))
                Text("Escanear código QR")
            }
        }
    }
}

@Composable
private fun AccesoOYL(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(
                modifier = Modifier.size(37.dp)
                    .clip(RoundedCornerShape(11.dp)).background(Color(0xFFE4F1E6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = Color(0xFF176243),
                    modifier = Modifier.size(21.dp))
            }
            Text(titulo, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2)
        }
    }
}
