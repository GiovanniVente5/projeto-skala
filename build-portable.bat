@echo off
setlocal

cd /d "%~dp0"
set LOG=build-portable.log

if exist "%LOG%" del "%LOG%"

echo ==============================
echo Building SkalaConvertor portable
echo ==============================
echo.

echo Checking Java... >> "%LOG%"
where java >> "%LOG%" 2>&1
if errorlevel 1 (
    echo ERROR: Java was not found on PATH.
    echo Install JDK 21 and make sure java works in the terminal.
    echo See details in %LOG%
    echo.
    pause
    exit /b 1
)

echo Checking jpackage... >> "%LOG%"
where jpackage >> "%LOG%" 2>&1
if errorlevel 1 (
    echo ERROR: jpackage was not found on PATH.
    echo jpackage comes with the JDK, not the JRE.
    echo Install JDK 21 and add its bin folder to PATH.
    echo Example: C:\Program Files\Java\jdk-21\bin
    echo See details in %LOG%
    echo.
    pause
    exit /b 1
)

echo Running Maven build...
echo Running Maven build... >> "%LOG%"
call mvnw.cmd clean package dependency:copy-dependencies -DincludeScope=runtime >> "%LOG%" 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Maven build failed.
    echo Open %LOG% and look at the last error lines.
    pause
    exit /b 1
)

if exist dist\SkalaConvertor rmdir /s /q dist\SkalaConvertor
if not exist dist mkdir dist

echo Creating portable app...
echo Creating portable app... >> "%LOG%"
jpackage ^
  --type app-image ^
  --name SkalaConvertor ^
  --dest dist ^
  --module-path "target\classes;target\dependency" ^
  --module org.example.projeto_skala/org.example.projeto_skala.Launcher >> "%LOG%" 2>&1

if errorlevel 1 (
    echo.
    echo ERROR: jpackage failed.
    echo Open %LOG% and look at the last error lines.
    pause
    exit /b 1
)

if not exist dist\SkalaConvertor\SkalaConvertor.exe (
    echo.
    echo ERROR: jpackage finished but the exe was not created.
    echo Open %LOG% for details.
    pause
    exit /b 1
)

echo.
echo Portable app created at:
echo dist\SkalaConvertor\SkalaConvertor.exe
echo.
pause
endlocal
