# Script para ejecutar la aplicación Spring Boot
# Detiene cualquier proceso que esté usando el puerto 8081

Write-Host "=== Gestión de Riesgos - Iniciando Aplicación ===" -ForegroundColor Cyan
Write-Host ""

# Verificar y detener procesos en el puerto 8081
Write-Host "Verificando puerto 8081..." -ForegroundColor Yellow
$port = netstat -ano | findstr :8081
if ($port) {
    Write-Host "⚠️  Puerto 8081 está en uso. Deteniendo procesos..." -ForegroundColor Yellow
    $processes = netstat -ano | findstr :8081 | ForEach-Object {
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
}

# Verificar que el puerto esté libre
$portCheck = netstat -ano | findstr :8081
if ($portCheck) {
    Write-Host "❌ El puerto 8081 aún está en uso. Por favor, detén el proceso manualmente." -ForegroundColor Red
    Write-Host "   Usa: netstat -ano | findstr :8081" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "✓ Puerto 8081 está libre" -ForegroundColor Green
}

Write-Host ""
Write-Host "Iniciando aplicación Spring Boot..." -ForegroundColor Cyan
Write-Host "Presiona Ctrl+C para detener la aplicación" -ForegroundColor Yellow
Write-Host ""

# Ejecutar la aplicación
mvn spring-boot:run

