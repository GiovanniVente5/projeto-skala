@echo off
setlocal

cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-installer.ps1"
set EXIT_CODE=%ERRORLEVEL%

if not "%EXIT_CODE%"=="0" (
    echo.
    echo ERROR: Installer build failed. See build-installer.log for details.
    pause
    exit /b %EXIT_CODE%
)

echo.
echo Installer created at:
echo dist\SkalaConvertor-Setup-1.0.0.exe
echo.
pause
endlocal
