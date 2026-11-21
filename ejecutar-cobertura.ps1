param([switch]$Abrir)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if (!$env:JAVA_HOME -and (Test-Path 'C:\Program Files\Android\Android Studio\jbr')) {
        $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
    }
    & .\gradlew.bat --no-configuration-cache -I cobertura.init.gradle :app:coberturaLogicaDebug --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Las pruebas o el informe fallaron. Revisar app/build/reports/tests/testDebugUnitTest/index.html.' }
    & .\resumir-cobertura.ps1
    & .\gradlew.bat --no-configuration-cache -I cobertura.init.gradle :app:verificarCoberturaLogicaDebug --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'La verificación del 85 % no fue satisfactoria.' }
    if ($Abrir) { Start-Process (Join-Path $PSScriptRoot 'app/build/reports/jacoco/coberturaLogicaDebug/html/index.html') -WindowStyle Hidden }
} finally { Pop-Location }
