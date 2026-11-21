param(
    [string]$Proyecto = $PSScriptRoot,
    [string]$Salida = 'app/build/reports/resumen-cobertura.md'
)
$ErrorActionPreference = 'Stop'
$xmlPath = Join-Path $Proyecto 'app/build/reports/jacoco/coberturaLogicaDebug/coberturaLogicaDebug.xml'
if (!(Test-Path -LiteralPath $xmlPath)) { throw "No existe $xmlPath. Genera primero coberturaLogicaDebug." }
[xml]$reporte = Get-Content -LiteralPath $xmlPath -Raw
function Obtener-Lineas($nodo) {
    $contador = @($nodo.counter) | Where-Object { $_.type -eq 'LINE' }
    $cubiertas = [long]$contador.covered
    $total = $cubiertas + [long]$contador.missed
    [pscustomobject]@{ Cubiertas = $cubiertas; Total = $total; Porcentaje = $(if ($total -gt 0) { 100.0 * $cubiertas / $total } else { 0.0 }) }
}
$total = Obtener-Lineas $reporte.report
$pruebas = 0L; $fallidas = 0L; $errores = 0L; $omitidas = 0L
$resultados = @(Get-ChildItem -LiteralPath (Join-Path $Proyecto 'app/build/test-results/testDebugUnitTest') -Filter 'TEST-*.xml')
if ($resultados.Count -eq 0) { throw 'No hay resultados JUnit para validar el total de pruebas.' }
foreach ($resultado in $resultados) {
    [xml]$suite = Get-Content -LiteralPath $resultado.FullName -Raw
    $pruebas += [long]$suite.testsuite.tests
    $fallidas += [long]$suite.testsuite.failures
    $errores += [long]$suite.testsuite.errors
    $omitidas += [long]$suite.testsuite.skipped
}
$aprobadas = $pruebas - $fallidas - $errores - $omitidas
$texto = [System.Collections.Generic.List[string]]::new()
$texto.Add('# Resultado de cobertura unitaria Android')
$texto.Add('')
$texto.Add(('Generado: {0:yyyy-MM-dd HH:mm:ss zzz}' -f (Get-Date)))
$texto.Add('')
$texto.Add(('Líneas: **{0}/{1} ({2:N2} %)**. Cálculo ponderado con el contador LINE del informe JaCoCo.' -f $total.Cubiertas, $total.Total, $total.Porcentaje))
$texto.Add(('Pruebas: {0}; aprobadas: {1}; fallidas: {2}; errores: {3}; omitidas: {4}.' -f $pruebas, $aprobadas, $fallidas, $errores, $omitidas))
$texto.Add(('Mínimo 80 %: {0}. Objetivo 85 %: {1}.' -f $(if ($total.Porcentaje -ge 80) {'CUMPLE'} else {'NO CUMPLE'}), $(if ($total.Porcentaje -ge 85) {'CUMPLE'} else {'NO CUMPLE'})))
$texto.Add('')
$texto.Add('Solo pruebas JVM de src/test; no incluye resultados instrumentados de src/androidTest.')
$texto.Add('')
$texto.Add('| Paquete | Cubiertas | Evaluadas | Cobertura |')
$texto.Add('| --- | ---: | ---: | ---: |')
foreach ($paquete in @($reporte.report.package) | Sort-Object name) {
    $lineas = Obtener-Lineas $paquete
    $texto.Add(('| {0} | {1} | {2} | {3:N2} % |' -f $paquete.name.Replace('/','.'), $lineas.Cubiertas, $lineas.Total, $lineas.Porcentaje))
}
$texto.Add('')
$texto.Add('## Clases bajo 85 %')
$texto.Add('')
$texto.Add('| Clase | Cubiertas | Evaluadas | Cobertura |')
$texto.Add('| --- | ---: | ---: | ---: |')
foreach ($paquete in @($reporte.report.package) | Sort-Object name) {
    foreach ($clase in @($paquete.class) | Sort-Object name) {
        $lineas = Obtener-Lineas $clase
        if ($lineas.Total -gt 0 -and $lineas.Porcentaje -lt 85) {
            $texto.Add(('| {0} | {1} | {2} | {3:N2} % |' -f $clase.name.Replace('/','.'), $lineas.Cubiertas, $lineas.Total, $lineas.Porcentaje))
        }
    }
}
$destino = if ([IO.Path]::IsPathRooted($Salida)) { $Salida } else { Join-Path $Proyecto $Salida }
New-Item -ItemType Directory -Force -Path (Split-Path $destino) | Out-Null
$texto | Set-Content -LiteralPath $destino -Encoding utf8
Write-Output ($texto -join [Environment]::NewLine)
if ($fallidas + $errores -gt 0) { throw 'Hay pruebas fallidas: no se puede certificar una ejecución correcta.' }
