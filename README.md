# OYL Frutos Secos para Android

## API REST externa · Demostración DSY1105

En **Administración → Pedidos → Ver detalles**, un pedido de **Despacho** con dirección muestra **Ver ubicación del despacho**. Al confirmar **Consultar ubicación**, Retrofit llama a **GET `https://nominatim.openstreetmap.org/search`** con `q` (lugar público, ciudad y país), `format=jsonv2`, `limit=1`, `countrycodes=cl` y `accept-language=es`. Las direcciones de prueba incluyen Santiago porque corresponden a esa ciudad. Nominatim devuelve la dirección de OpenStreetMap y las coordenadas que MapLibre utiliza para centrar el marcador de entrega. El resultado es **aproximado**. Un solo candidato no permite determinar si existen otros. La app avisa si la respuesta es demasiado general o no contiene una coincidencia y permite volver al pedido.

### Condiciones de la demo

- Usa **un solo teléfono** con la demo habilitada. El máximo del [servidor público de Nominatim](https://operations.osmfoundation.org/policies/nominatim/) es de **una solicitud por segundo sumando a todos los usuarios**. La app serializa las consultas en ese teléfono y espera al menos 1,1 segundos entre el fin de una y el inicio de la siguiente. Usa la búsqueda ocasionalmente y detén la demo si el servicio responde 403 o 429.
- El cliente identifica la app en `User-Agent`, conserva coincidencias y búsquedas vacías en una caché local persistente por lugar y servidor, y muestra la atribución de OpenStreetMap. Solo consulta tras confirmación. No hay autocompletado, consultas masivas ni reintentos automáticos.
- La app solo envía uno de dos lugares públicos predefinidos. Nunca envía campos del cliente ni texto libre al proveedor. Usa además datos de cliente ficticios en el backend de evaluación. Los domicilios reales, incluso con consentimiento, quedan fuera del alcance del servicio público.
- La consulta solo se habilita al compilar la variante debug con `-PoylNominatimDemo=true`. Un administrador de dispositivos Android puede establecer la configuración gestionada `nominatim_endpoint` a una URL base HTTPS compatible, o a una cadena vacía para detener las consultas, sin instalar otra versión. Para una distribución en varios dispositivos, configura antes un proveedor con capacidad y condiciones adecuadas, o un servicio controlado con caché y límite global. La app por sí sola no coordina el tráfico entre instalaciones.

### Probar en un teléfono por USB

1. Inicia Productos, Inventario, Clientes y Pedidos siguiendo el [README del backend](https://github.com/Sevalenzuela/OYL-Frutos-Secos-Backend). Comprueba que escuchan en los puertos 8081, 8082, 8083 y 8084, respectivamente.
2. Activa la depuración USB, autoriza el equipo en el teléfono y ejecuta esto desde la raíz de Android en PowerShell. Usa el `adb.exe` de `platform-tools` del SDK de Android si `adb` no está en tu PATH.

   ```powershell
   adb devices
   8081..8084 | ForEach-Object { adb reverse "tcp:$_" "tcp:$_" }
   adb reverse --list
   .\gradlew.bat -PoylPruebaUsb=true -PoylNominatimDemo=true :app:installDebug
   ```

3. Abre la instalación de prueba, que usa el identificador con sufijo `.pruebausb` y conecta el backend mediante `127.0.0.1`. El teléfono necesita Internet para Nominatim y OpenFreeMap.
4. Como cliente, crea un pedido de **Despacho** con la dirección **Palacio de La Moneda, Santiago, Chile**. También puedes usar **Museo Nacional de Bellas Artes, Santiago, Chile**. Como administrador, abre el pedido y confirma la consulta de ubicación. Comprueba la dirección devuelta, el aviso, el marcador, la atribución y el botón **Volver al pedido**. Abre de nuevo la misma ubicación para usar la caché. En un pedido de **Retiro** comprueba que la acción no aparezca.

### Pruebas

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat -PoylPruebaUsb=true -PoylNominatimDemo=true :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.oylfrutossecos.UbicacionDespachoUiTest"
```

Las pruebas unitarias de Retrofit usan un servidor en la propia máquina. Las pruebas de interfaz cubren carga, fallos, privacidad y visibilidad. La prueba de red real `demoRealMuestraMarcadorDeEntrega` requiere habilitación explícita mediante el argumento de instrumentación `oylProbarNominatim=true`. Ejecútala en el único teléfono de la demo.
