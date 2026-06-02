@echo off
title PisPax Backend

:: Auto-elevacion a Administrador
net session > nul 2>&1
if %errorLevel% neq 0 (
    echo Solicitando permisos de administrador...
    powershell -Command "Start-Process '%~f0' -Verb RunAs"
    exit /b
)

echo.
echo  ==========================================
echo   PisPax Backend v1.0
echo   Spring Boot 3.2  |  MySQL 8  |  :8080
echo  ==========================================
echo.

:: 1. MySQL
echo [1/2] Arrancando MySQL...
net start MYSQL80 > nul 2>&1
if %errorLevel% equ 0 (
    echo       OK - MySQL arrancado.
) else (
    echo       MySQL ya estaba corriendo.
)
timeout /t 2 /nobreak > nul

:: 2. Spring Boot
echo [2/2] Levantando Spring Boot en http://localhost:8080 ...
echo.

cd /d "C:\Users\adria\Documents\ClaudeIA\Code-Pispax\pispax-backend"

:: Abre el navegador 15 segundos despues del arranque
start /b cmd /c "timeout /t 15 /nobreak > nul && start http://localhost:8080"

mvn spring-boot:run

:: Si Spring Boot se cierra
echo.
echo  El servidor se ha detenido.
pause
