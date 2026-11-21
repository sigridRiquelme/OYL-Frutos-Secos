# Ejecución visible y acotada de Gradle: reintenta solamente cuando cambia el código.
# Termina al aprobar pruebas y 85 %, o con Ctrl+C.
$ErrorActionPreference = 'Continue'
Set-Location $PSScriptRoot
if (!$env:JAVA_HOME) { $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr' }
function Marca-Codigo {
    $archivos = @(Get-ChildItem app/src -Recurse -File) + @(Get-Item app/build.gradle.kts, cobertura.init.gradle)
    ($archivos | Sort-Object FullName | ForEach-Object { '{0}|{1}|{2}' -f $_.FullName, $_.LastWriteTimeUtc.Ticks, $_.Length }) -join "`n"
}
while ($true) {
    $marca = Marca-Codigo
    Write-Host 'Ejecutando suite y umbral 85 %. Log: ejecucion-verificacion.log'
    & .\gradlew.bat --no-configuration-cache -I cobertura.init.gradle :app:verificarCoberturaLogicaDebug --console=plain *> ejecucion-verificacion.log
    $codigo = $LASTEXITCODE
    Write-Host "Código de salida: $codigo"
    if ($codigo -eq 0) { Write-Host 'Pruebas y cobertura verificadas.'; break }
    Write-Host 'Esperando correcciones de código. Ctrl+C para detener.'
    do { Start-Sleep -Seconds 3; $actual = Marca-Codigo } while ($actual -eq $marca)
    # Permitir terminar una edición antes de iniciar una compilación nueva.
    Start-Sleep -Seconds 3
}
