# Cobertura unitaria Android de OYL Frutos Secos

Base funcional: `feature/admin-productos-fotos`, commit `8c5d8b4879fc0762bbe99d4eb31416292fd50e20`.
Backend contrastado en GitHub: `75d8c411fb268ba29f506d7c6843398c998a9c36`. No se modifica.

## Alcance y criterio

JaCoCo 0.8.14 mide las lÃ­neas ejecutables compiladas de todas las clases de estos paquetes, incluidas sus clases anidadas y corrutinas:

| Paquete | Motivo |
| --- | --- |
| `data/**` | Repositories, HTTP, imÃ¡genes, modelos, entidades, preferencias, inicializaciÃ³n y migraciones |
| `feature/**` | Reglas de negocio, coordinadores, validadores, geocodificaciÃ³n y adaptadores de plataforma |
| `ui/viewmodel/**` | Estados, sesiones, carrito, perfil, pedidos, productos, QR y mapa; incluye factories |
| `ui/validation/**` | Validaciones y adaptadores de compatibilidad |

No se seleccionan solamente clases probadas. `AppDatabase`, migraciones y rutas de error siguen en el denominador aunque tengan cobertura baja. No se excluyen modelos ni mÃ©todos por conveniencia. Se excluyen implementaciones `_Impl` generadas por Room, sin lÃ³gica escrita por el equipo. Los contadores de interfaces sin lÃ­neas ejecutables no aumentan el denominador.

El informe principal mide estas capas; excluye `MainActivity`, `ui/screens`, `ui/components` y `ui/theme`, por ser la capa de presentaciÃ³n Compose y su integraciÃ³n con el dispositivo. **No demuestra que todo callback o toda coordinaciÃ³n embebida en un composable estÃ© probado.** Parte de la comunicaciÃ³n remota y de la coordinaciÃ³n de formularios permanece en pantallas y requiere pruebas de interfaz o extracciÃ³n posterior, fuera de esta intervenciÃ³n para conservar su comportamiento. Para hacer visible esa limitaciÃ³n se genera tambiÃ©n `coberturaCompletaDebug`, que incluye todas las clases de la aplicaciÃ³n salvo cÃ³digo generado. No debe presentarse el porcentaje de las capas como porcentaje global de toda la aplicaciÃ³n.

FÃ³rmula: `LINE.covered / (LINE.covered + LINE.missed) * 100`, utilizando el contador raÃ­z XML de JaCoCo. No se promedian porcentajes de clases o paquetes. Los resultados de Android Studio/IntelliJ pueden tener otro denominador por las diferencias entre motores y filtros de bytecode Kotlin; se comparan antes/despuÃ©s con la misma herramienta y el mismo alcance.

## MediciÃ³n inicial

La copia intacta de la base funcional se ejecutÃ³ con el mismo `cobertura.init.gradle` para el informe de capas: **245 pruebas aprobadas, 0 fallidas, 0 errores y 0 omitidas**. Cobertura: **820/1424 lÃ­neas = 57,58 %**. Evidencias en `evidencias/cobertura-inicial.xml` y `evidencias/resumen-inicial.md`.

El 65,6 % provisional de las capturas correspondÃ­a a cuatro grupos y a otro motor. El 13 % de la aplicaciÃ³n completa tampoco es directamente comparable con este informe de capas.

## Resultado final verificado

EjecuciÃ³n `:app:verificarCoberturaLogicaDebug` con resultado `BUILD SUCCESSFUL` el 21 de noviembre de 2026. Se comprobaron los XML JUnit y JaCoCo generados en el equipo, no solo la salida de consola.

| MÃ©trica | Inicial | Final |
| --- | ---: | ---: |
| Pruebas unitarias | 245 | 281 |
| Aprobadas | 245 | 281 |
| Fallidas / errores / omitidas | 0 / 0 / 0 | 0 / 0 / 0 |
| LÃ­neas cubiertas / evaluadas | 820 / 1424 | 1330 / 1424 |
| Cobertura de lÃ­neas de las capas | 57,58 % | **93,40 %** |
| Cumple 80 % | No | **SÃ­** |
| Cumple 85 % | No | **SÃ­** |

Se aÃ±adieron 36 pruebas y se cubrieron 510 lÃ­neas adicionales, manteniendo el mismo denominador. La mejora es de 35,81 puntos porcentuales. Cobertura final de ramas: 493/566 = 87,10 %.

| Grupo final | LÃ­neas cubiertas / evaluadas | Cobertura |
| --- | ---: | ---: |
| ViewModels, estados y factories | 463 / 483 | 95,86 % |
| Repositories y clientes remotos | 253 / 272 | 93,01 % |
| Reglas de negocio (`feature/**/domain`) | 293 / 295 | 99,32 % |
| Validaciones de compatibilidad | 28 / 31 | 90,32 % |
| Persistencia e inicializaciÃ³n (`data.local`) | 159 / 182 | 87,36 % |

La tabla completa por paquete y el listado de clases bajo 85 % estÃ¡n en [evidencias/resumen-final.md](evidencias/resumen-final.md). El XML final estÃ¡ en [evidencias/cobertura-final.xml](evidencias/cobertura-final.xml).

**Contraste global:** al incluir Compose y `MainActivity`, la misma suite cubre **1330/6106 lÃ­neas = 21,78 %**. Es un alcance diferente; el 93,40 % no es cobertura de toda la aplicaciÃ³n. El informe completo permite revisar la integraciÃ³n que permanece dentro de los composables.

Pendientes que permanecen incluidos en el denominador:

- Apertura singleton de `AppDatabase` y migraciones 1â†’2 y 2â†’3: sin ejecutar; conviene comprobarlas con una base Room real y esquemas exportados.
- `GeocodificadorDespacho.buscar`: solo se verifica la configuraciÃ³n deshabilitada predeterminada. El cuerpo de su corrutina tiene 3/20 lÃ­neas; quedan cachÃ©, restricciones y limitaciÃ³n temporal del adaptador. La consulta Retrofit, respuestas y errores de Nominatim sÃ­ tienen pruebas existentes.
- `ValidadorCorreoAndroid`: 0/3 lÃ­neas; las pruebas del perfil utilizan un validador inyectado. No se oculta esta diferencia.
- `ClienteViewModel` y sus corrutinas de registro/carga todavÃ­a tienen rutas sin ejecutar; el detalle estÃ¡ en el HTML y en el resumen.
- DTO `PedidosRemotos.RespuestaPedido`: 12/22 lÃ­neas. Gson deserializa sin ejecutar todo su constructor; el mapeo a pedido y los contratos HTTP estÃ¡n comprobados. No se excluyen esas lÃ­neas.
- FotografÃ­as: aunque el conjunto supera 85 % en lÃ­neas, quedan combinaciones EXIF y fallos de compresiÃ³n sin cubrir. Las pruebas JVM usan imÃ¡genes reales y una URI de contenido con streams; la cÃ¡mara, la galerÃ­a del sistema y los grÃ¡ficos fÃ­sicos requieren pruebas de dispositivo.

No se repitieron pruebas manuales en el celular ni pruebas instrumentadas: este resultado certifica exclusivamente la suite unitaria y el umbral del alcance documentado. Se conservan todas las pruebas preexistentes. La compilaciÃ³n de las fuentes de producciÃ³n y de prueba finalizÃ³ correctamente.

## Detalle de las pruebas incorporadas

- Contratos reales de Retrofit con servidores HTTP efÃ­meros en loopback: catÃ¡logo y stock, altas multipart, fotos opcionales, ediciÃ³n, estados, movimientos de inventario, JWT, clave idempotente, perfil, registro, pedidos e historial.
- Rechazos HTTP, respuestas de autenticaciÃ³n invÃ¡lidas y conexiones interrumpidas.
- SesiÃ³n y vencimiento con reloj virtual; compra exitosa, solicitudes duplicadas, reintento tras pÃ©rdida de respuesta, cancelaciÃ³n y limpieza de datos.
- Repositories locales con DAOs simulados, inicializaciÃ³n de productos, validaciÃ³n de datos y factories.
- Robolectric para QR con JSON real de Android, preferencias y pedidos locales, y procesamiento/reescalado de fotografÃ­as en JVM.
- Filtros y selecciÃ³n del mapa. No se simula que renderizar un mapa o abrir la cÃ¡mara estÃ© cubierto por estas pruebas.

JUnit, MockK, Kotest y coroutines-test ya estaban configurados. Se aÃ±ade Robolectric para ejecutar comportamiento Android realista sin telÃ©fono; se conserva el servidor HTTP JDK ya usado en pruebas existentes, evitando otra dependencia MockWebServer.

PequeÃ±a refactorizaciÃ³n: `CatalogoRemoto` y `PedidosRemotos` admiten puertos opcionales. Los valores originales 8081/8082/8084 se conservan; las pruebas usan puertos efÃ­meros para no tocar los microservicios reales ni interferir con una demostraciÃ³n en ejecuciÃ³n. Las pruebas de contrato verifican las mismas rutas, cabeceras y cuerpos. No hay cambios visuales.

## Ejecutar y verificar

Requisitos: JDK compatible con Gradle (se comprobÃ³ con el JBR incluido en Android Studio), Android SDK 37 y `local.properties` configurado. La descarga inicial de dependencias Gradle/Robolectric necesita Internet; las peticiones de la aplicaciÃ³n durante las pruebas se resuelven mediante simulaciones o loopback. No se usan servicios externos ni credenciales reales.

En PowerShell, desde la raÃ­z del repositorio:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat --no-configuration-cache -I cobertura.init.gradle :app:coberturaLogicaDebug --console=plain
.\scripts\resumir-cobertura.ps1
.\gradlew.bat --no-configuration-cache -I cobertura.init.gradle :app:verificarCoberturaLogicaDebug --console=plain
```

La Ãºltima tarea exige **al menos 85 %** en el conjunto de capas y falla si no se alcanza. Para ejecutar todo y abrir el informe: `./scripts/ejecutar-cobertura.ps1 -Abrir`. El archivo init se aplica explÃ­citamente para no modificar las compilaciones normales ni activar cobertura en producciÃ³n. `--no-configuration-cache` es necesario porque este script configura tareas despuÃ©s de evaluar los proyectos.

Informes:

| Informe | Ruta relativa |
| --- | --- |
| Cobertura de capas HTML | `app/build/reports/jacoco/coberturaLogicaDebug/html/index.html` |
| Cobertura de capas XML | `app/build/reports/jacoco/coberturaLogicaDebug/coberturaLogicaDebug.xml` |
| AplicaciÃ³n completa HTML | `app/build/reports/jacoco/coberturaCompletaDebug/html/index.html` |
| AplicaciÃ³n completa XML | `app/build/reports/jacoco/coberturaCompletaDebug/coberturaCompletaDebug.xml` |
| Resultado JUnit HTML | `app/build/reports/tests/testDebugUnitTest/index.html` |
| Resultados JUnit XML | `app/build/test-results/testDebugUnitTest/TEST-*.xml` |
| Resumen de mÃ©tricas | `app/build/reports/resumen-cobertura.md` |

```powershell
Start-Process '.\app\build\reports\jacoco\coberturaLogicaDebug\html\index.html'
Start-Process '.\app\build\reports\jacoco\coberturaCompletaDebug\html\index.html'
Start-Process '.\app\build\reports\tests\testDebugUnitTest\index.html'
```

## SeparaciÃ³n de pruebas instrumentadas

`src/test` se ejecuta en JVM, tambiÃ©n cuando utiliza Robolectric. El informe no mezcla `.ec` de un dispositivo ni resultados de `src/androidTest`. Las pruebas manuales anteriores y las instrumentadas no se incluyen en el porcentaje unitario. No se requiere levantar el backend ni conectar un telÃ©fono para este procedimiento.

