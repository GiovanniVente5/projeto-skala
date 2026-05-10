@echo off
setlocal

cd /d "%~dp0"

echo ==============================
echo Building SkalaConvertor portable
echo ==============================
echo.

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java was not found on PATH.
    echo Install JDK 21 and make sure java works in the terminal.
    echo.
    pause
    exit /b 1
)

where jpackage >nul 2>nul
if errorlevel 1 (
    echo ERROR: jpackage was not found on PATH.
    echo jpackage comes with the JDK, not the JRE.
    echo Install JDK 21 and add its bin folder to PATH.
    echo Example: C:\Program Files\Java\jdk-21\bin
    echo.
    pause
    exit /b 1
)

if not exist dist mkdir dist

echo Running Maven build...
call mvnw.cmd clean package dependency:copy-dependencies -DincludeScope=runtime
if errorlevel 1 (
    echo.
    echo ERROR: Maven build failed.
    pause
    exit /b %errorlevel%
)

if exist dist\SkalaConvertor rmdir /s /q dist\SkalaConvertor

echo Creating portable app...
jpackage ^
  --type app-image ^
  --name SkalaConvertor ^
  --dest dist ^
  --module-path "target\classes;target\dependency" ^
  --module org.example.projeto_skala/org.example.projeto_skala.Launcher

if errorlevel 1 (
    echo.
    echo ERROR: jpackage failed.
    pause
    exit /b %errorlevel%
)

echo.
echo Portable app created at:
echo dist\SkalaConvertor\SkalaConvertor.exe
echo.
pause
endlocal
