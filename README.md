# OYL Frutos Secos — Android

Aplicación móvil para la comercialización y administración de productos de OYL Frutos Secos, desarrollada para la asignatura Desarrollo de Aplicaciones Móviles (DSY1105).

## Integrantes:
- Sigrid Riquelme 004D
- Diego Carrasco 003D
- Sebastian Valenzuela 004D

## Tecnologías

- Kotlin y Jetpack Compose.
- MVVM y Room.
- Retrofit.
- Spring Boot.
- JWT.
- Nominatim y MapLibre.
- JUnit, MockK, Kotest, Robolectric y JaCoCo.

## Funcionalidades

**Cliente:** registro, autenticación, catálogo, códigos QR, carrito, compras, historial de pedidos y perfil.

**Administrador:** gestión de productos, fotografías, inventario y pedidos.

## Backend

https://github.com/Sevalenzuela/OYL-Frutos-Secos-Backend

| Microservicio | Puerto | Ruta principal |
|---|---:|---|
| Productos | 8081 | /api/productos |
| Inventario | 8082 | /api/inventario |
| Clientes | 8083 | /api/clientes y /api/auth |
| Pedidos | 8084 | /api/pedidos |

API externa: https://nominatim.openstreetmap.org/search

## Ejecución

1. Iniciar los cuatro microservicios.
2. Conectar un dispositivo Android mediante USB y habilitar la depuración USB.
3. Redireccionar los puertos 8081, 8082, 8083 y 8084 mediante ADB reverse.
4. Instalar y ejecutar el APK firmado de entrega.

La versión release está configurada para una demostración académica mediante 127.0.0.1.

Para Nominatim, utilizar un único dispositivo de demostración y exclusivamente los lugares públicos predefinidos. No utilizar domicilios reales.

## Pruebas unitarias

- 281 pruebas aprobadas.
- 0 pruebas fallidas.
- 93,40 % de cobertura de las capas lógicas.
- 95,86 % de cobertura de ViewModels.
- 93,01 % de cobertura de Repositories.

La metodología, los comandos de ejecución y las evidencias están disponibles en [docs/COBERTURA.md](docs/COBERTURA.md).

## Documentación

La documentación técnica completa, los integrantes y las evidencias del APK firmado se incluyen en el documento README externo de la entrega académica.