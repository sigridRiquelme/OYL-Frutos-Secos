
package com.example.oylfrutossecos.data.local

import com.example.oylfrutossecos.data.local.entity.ProductoEntity

suspend fun insertarProductosIniciales(
    database: AppDatabase
): Boolean {

    val productoDao = database.productoDao()

    // Comprobar si ya existen productos
    if (productoDao.contarProductos() > 0) {
        return false
    }

    // Productos ficticios para pruebas
    val productos = listOf(

        ProductoEntity(
            id = 1,
            nombre = "Almendras",
            categoria = "Frutos secos",
            precio = 5990,
            stock = 25,
            descripcion = "Almendras naturales.",
            presentacion = "Bolsa de 250 g"
        ),

        ProductoEntity(
            id = 2,
            nombre = "Nueces",
            categoria = "Frutos secos",
            precio = 6490,
            stock = 18,
            descripcion = "Nueces naturales.",
            presentacion = "Bolsa de 250 g"
        ),

        ProductoEntity(
            id = 3,
            nombre = "Semillas de chía",
            categoria = "Semillas",
            precio = 2990,
            stock = 40,
            descripcion = "Semillas de chía naturales.",
            presentacion = "Bolsa de 250 g"
        ),

        ProductoEntity(
            id = 4,
            nombre = "Pasas",
            categoria = "Deshidratados",
            precio = 1990,
            stock = 30,
            descripcion = "Pasas deshidratadas.",
            presentacion = "Bolsa de 250 g"
        ),

        ProductoEntity(
            id = 5,
            nombre = "Cranberries",
            categoria = "Deshidratados",
            precio = 3990,
            stock = 12,
            descripcion = "Cranberries deshidratados.",
            presentacion = "Bolsa de 250 g"
        ),

        ProductoEntity(
            id = 6,
            nombre = "Semillas de maravilla",
            categoria = "Semillas",
            precio = 2490,
            stock = 35,
            descripcion = "Semillas de maravilla naturales.",
            presentacion = "Bolsa de 250 g"
        )
    )

    // Insertar los productos en SQLite
    productoDao.guardarProductos(productos)

    return true
}
