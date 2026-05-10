@echo off
setlocal

cd /d "%~dp0"
set LOG=build-installer.log

if exist "%LOG%" del "%LOG%"

echo ==============================
echo Building SkalaConvertor installer
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

echo Checking WiX... >> "%LOG%"
where wix >> "%LOG%" 2>&1
if errorlevel 1 (
    where candle >> "%LOG%" 2>&1
    if errorlevel 1 (
        echo ERROR: WiX was not found on PATH.
        echo Install WiX Toolset to create Windows .exe installers.
        echo WiX 4 provides wix.exe. WiX 3 provides candle.exe and light.exe.
        echo See details in %LOG%
        echo.
        pause
        exit /b 1
    )

    where light >> "%LOG%" 2>&1
    if errorlevel 1 (
        echo ERROR: light.exe from WiX was not found on PATH.
        echo Install WiX Toolset to create Windows .exe installers.
        echo See details in %LOG%
        echo.
        pause
        exit /b 1
    )
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

if not exist dist mkdir dist
del /q dist\SkalaConvertor*.exe >> "%LOG%" 2>&1
if exist target\installer-input rmdir /s /q target\installer-input
mkdir target\installer-input

copy /Y target\Projeto_Skala-1.0-SNAPSHOT.jar target\installer-input\ >> "%LOG%" 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Could not copy the application jar.
    echo Open %LOG% and look at the last error lines.
    pause
    exit /b 1
)

xcopy /Y /I target\dependency\*.jar target\installer-input\ >> "%LOG%" 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Could not copy runtime dependencies.
    echo Open %LOG% and look at the last error lines.
    pause
    exit /b 1
)

echo Creating Windows installer...
echo Creating Windows installer... >> "%LOG%"
jpackage ^
  --type exe ^
  --name SkalaConvertor ^
  --app-version 1.0.0 ^
  --vendor "Skala" ^
  --dest dist ^
  --input target\installer-input ^
  --main-jar Projeto_Skala-1.0-SNAPSHOT.jar ^
  --main-class org.example.projeto_skala.Launcher ^
  --win-per-user-install ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut >> "%LOG%" 2>&1

if errorlevel 1 (
    echo.
    echo ERROR: jpackage failed.
    echo Open %LOG% and look at the last error lines.
    pause
    exit /b 1
)

if not exist dist\SkalaConvertor*.exe (
    echo.
    echo ERROR: jpackage finished but the installer exe was not created.
    echo Open %LOG% for details.
    pause
    exit /b 1
)

echo.
echo Installer created in the dist folder.
echo Look for a file like SkalaConvertor-1.0.0.exe
echo.
pause
endlocal
