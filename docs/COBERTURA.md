# Cobertura unitaria Android de OYL Frutos Secos

Base funcional: `feature/admin-productos-fotos`, commit `8c5d8b4879fc0762bbe99d4eb31416292fd50e20`.
Backend contrastado en GitHub: `75d8c411fb268ba29f506d7c6843398c998a9c36`. No se modifica.

## Alcance y criterio

JaCoCo 0.8.14 mide las líneas ejecutables compiladas de todas las clases de estos paquetes, incluidas sus clases anidadas y corrutinas:

| Paquete | Motivo |
| --- | --- |
| `data/**` | Repositories, HTTP, imágenes, modelos, entidades, preferencias, inicialización y migraciones |
| `feature/**` | Reglas de negocio, coordinadores, validadores, geocodificación y adaptadores de plataforma |
| `ui/viewmodel/**` | Estados, sesiones, carrito, perfil, pedidos, productos, QR y mapa; incluye factories |
| `ui/validation/**` | Validaciones y adaptadores de compatibilidad |

No se seleccionan solamente clases probadas. `AppDatabase`, migraciones y rutas de error siguen en el denominador aunque tengan cobertura baja. No se excluyen modelos ni métodos por conveniencia. Se excluyen implementaciones `_Impl` generadas por Room, sin lógica escrita por el equipo. Los contadores de interfaces sin líneas ejecutables no aumentan el denominador.

El informe principal mide estas capas; excluye `MainActivity`, `ui/screens`, `ui/components` y `ui/theme`, por ser la capa de presentación Compose y su integración con el dispositivo. **No demuestra que todo callback o toda coordinación embebida en un composable esté probado.** Parte de la comunicación remota y de la coordinación de formularios permanece en pantallas y requiere pruebas de interfaz o extracción posterior, fuera de esta intervención para conservar su comportamiento. Para hacer visible esa limitación se genera también `coberturaCompletaDebug`, que incluye todas las clases de la aplicación salvo código generado. No debe presentarse el porcentaje de las capas como porcentaje global de toda la aplicación.

Fórmula: `LINE.covered / (LINE.covered + LINE.missed) * 100`, utilizando el contador raíz XML de JaCoCo. No se promedian porcentajes de clases o paquetes. Los resultados de Android Studio/IntelliJ pueden tener otro denominador por las diferencias entre motores y filtros de bytecode Kotlin; se comparan antes/después con la misma herramienta y el mismo alcance.

## Medición inicial

La copia intacta de la base funcional se ejecutó con el mismo `cobertura.init.gradle` para el informe de capas: **245 pruebas aprobadas, 0 fallidas, 0 errores y 0 omitidas**. Cobertura: **820/1424 líneas = 57,58 %**. Evidencias en `evidencias/cobertura-inicial.xml` y `evidencias/resumen-inicial.md`.

El 65,6 % provisional de las capturas correspondía a cuatro grupos y a otro motor. El 13 % de la aplicación completa tampoco es directamente comparable con este informe de capas.

## Pruebas incorporadas

- Contratos reales de Retrofit con servidores HTTP efímeros en loopback: catálogo y stock, altas multipart, fotos opcionales, edición, estados, movimientos de inventario, JWT, clave idempotente, perfil, registro, pedidos e historial.
- Rechazos HTTP, respuestas de autenticación inválidas y conexiones interrumpidas.
- Sesión y vencimiento con reloj virtual; compra exitosa, solicitudes duplicadas, reintento tras pérdida de respuesta, cancelación y limpieza de datos.
- Repositories locales con DAOs simulados, inicialización de productos, validación de datos y factories.
- Robolectric para QR con JSON real de Android, preferencias y pedidos locales, y procesamiento/reescalado de fotografías en JVM.
- Filtros y selección del mapa. No se simula que renderizar un mapa o abrir la cámara esté cubierto por estas pruebas.

JUnit, MockK, Kotest y coroutines-test ya estaban configurados. Se añade Robolectric para ejecutar comportamiento Android realista sin teléfono; se conserva el servidor HTTP JDK ya usado en pruebas existentes, evitando otra dependencia MockWebServer.

Pequeña refactorización: `CatalogoRemoto` y `PedidosRemotos` admiten puertos opcionales. Los valores originales 8081/8082/8084 se conservan; las pruebas usan puertos efímeros para no tocar los microservicios reales ni interferir con una demostración en ejecución. Las pruebas de contrato verifican las mismas rutas, cabeceras y cuerpos. No hay cambios visuales.

## Ejecutar y verificar

Requisitos: JDK compatible con Gradle (se comprobó con el JBR incluido en Android Studio), Android SDK 37 y `local.properties` configurado. La descarga inicial de dependencias Gradle/Robolectric necesita Internet; las peticiones de la aplicación durante las pruebas se resuelven mediante simulaciones o loopback. No se usan servicios externos ni credenciales reales.

En PowerShell, desde la raíz del repositorio:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat --no-configuration-cache -I cobertura.init.gradle :app:coberturaLogicaDebug --console=plain
.\resumir-cobertura.ps1
.\gradlew.bat --no-configuration-cache -I cobertura.init.gradle :app:verificarCoberturaLogicaDebug --console=plain
```

La última tarea exige **al menos 85 %** en el conjunto de capas y falla si no se alcanza. Para ejecutar todo y abrir el informe: `./ejecutar-cobertura.ps1 -Abrir`. El archivo init se aplica explícitamente para no modificar las compilaciones normales ni activar cobertura en producción. `--no-configuration-cache` es necesario porque este script configura tareas después de evaluar los proyectos.

Informes:

| Informe | Ruta relativa |
| --- | --- |
| Cobertura de capas HTML | `app/build/reports/jacoco/coberturaLogicaDebug/html/index.html` |
| Cobertura de capas XML | `app/build/reports/jacoco/coberturaLogicaDebug/coberturaLogicaDebug.xml` |
| Aplicación completa HTML | `app/build/reports/jacoco/coberturaCompletaDebug/html/index.html` |
| Aplicación completa XML | `app/build/reports/jacoco/coberturaCompletaDebug/coberturaCompletaDebug.xml` |
| Resultado JUnit HTML | `app/build/reports/tests/testDebugUnitTest/index.html` |
| Resultados JUnit XML | `app/build/test-results/testDebugUnitTest/TEST-*.xml` |
| Resumen de métricas | `app/build/reports/resumen-cobertura.md` |

```powershell
Start-Process '.\app\build\reports\jacoco\coberturaLogicaDebug\html\index.html'
Start-Process '.\app\build\reports\jacoco\coberturaCompletaDebug\html\index.html'
Start-Process '.\app\build\reports\tests\testDebugUnitTest\index.html'
```

## Separación de pruebas instrumentadas

`src/test` se ejecuta en JVM, también cuando utiliza Robolectric. El informe no mezcla `.ec` de un dispositivo ni resultados de `src/androidTest`. Las pruebas manuales anteriores y las instrumentadas no se incluyen en el porcentaje unitario. No se requiere levantar el backend ni conectar un teléfono para este procedimiento.
