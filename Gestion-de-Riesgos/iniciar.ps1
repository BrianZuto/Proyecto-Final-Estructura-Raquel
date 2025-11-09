# Script para iniciar la aplicación Spring Boot
# Este script asegura que se ejecute desde el directorio correcto

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Gestión de Riesgos - Spring Boot" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Directorio: $scriptPath" -ForegroundColor Gray
Write-Host ""

# Verificar que existe el pom.xml
if (-not (Test-Path "pom.xml")) {
    Write-Host "ERROR: No se encuentra pom.xml en el directorio actual" -ForegroundColor Red
    Write-Host "Directorio actual: $(Get-Location)" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Archivo pom.xml encontrado" -ForegroundColor Green
Write-Host ""

# Verificar puerto 8082
Write-Host "Verificando puerto 8082..." -ForegroundColor Yellow
$port = netstat -ano | findstr :8082
if ($port) {
    Write-Host "⚠️  Puerto 8082 está en uso. Deteniendo procesos..." -ForegroundColor Yellow
    netstat -ano | findstr :8082 | ForEach-Object {
        $parts = $_ -split '\s+'
        $pid = $parts[-1]
        if ($pid -match '^\d+$') {
            try {
                Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                Write-Host "   ✓ Proceso $pid detenido" -ForegroundColor Green
            } catch {
                Write-Host "   ✗ No se pudo detener el proceso $pid" -ForegroundColor Red
            }
        }
    }
    Start-Sleep -Seconds 2
} else {
    Write-Host "✓ Puerto 8082 está libre" -ForegroundColor Green
}

Write-Host ""
Write-Host "Iniciando aplicación Spring Boot en puerto 8082..." -ForegroundColor Cyan
Write-Host "Presiona Ctrl+C para detener la aplicación" -ForegroundColor Yellow
Write-Host ""
Write-Host "Una vez iniciada, accede a:" -ForegroundColor Cyan
Write-Host "  - API: http://localhost:8082/api/zonas" -ForegroundColor White
Write-Host "  - Frontend: http://localhost:8082/index.html" -ForegroundColor White
Write-Host ""

# Ejecutar la aplicación
mvn spring-boot:run

