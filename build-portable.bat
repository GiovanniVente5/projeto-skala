@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd /d "%~dp0"
set LOG=build-portable.log
set JDK_HOME=
set JAVA_CMD=
set JPACKAGE_CMD=

if exist "%LOG%" del "%LOG%"

echo ==============================
echo Building SkalaConvertor portable
echo ==============================
echo.

call :find_jdk
if not defined JDK_HOME (
    echo ERROR: A full JDK with jpackage was not found.
    echo Install JDK 21 or newer and set JAVA_HOME to the JDK folder.
    echo Example: C:\Program Files\Java\jdk-21
    echo.
    echo Details were written to %LOG%
    pause
    exit /b 1
)

set "JAVA_HOME=%JDK_HOME%"
set "JAVA_CMD=%JDK_HOME%\bin\java.exe"
set "JPACKAGE_CMD=%JDK_HOME%\bin\jpackage.exe"
set "PATH=%JDK_HOME%\bin;%PATH%"

echo Using JDK: %JDK_HOME%
echo Using JDK: %JDK_HOME% >> "%LOG%"
echo. >> "%LOG%"
echo Java version: >> "%LOG%"
"%JAVA_CMD%" -version >> "%LOG%" 2>&1
echo. >> "%LOG%"
echo jpackage version: >> "%LOG%"
"%JPACKAGE_CMD%" --version >> "%LOG%" 2>&1

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

echo Creating portable app...
echo Creating portable app... >> "%LOG%"
"%JPACKAGE_CMD%" ^
  --type app-image ^
  --name SkalaConvertor ^
  --dest dist ^
  --input target\installer-input ^
  --main-jar Projeto_Skala-1.0-SNAPSHOT.jar ^
  --main-class org.example.projeto_skala.Launcher >> "%LOG%" 2>&1

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
exit /b 0

:find_jdk
echo Searching for a JDK with jpackage... >> "%LOG%"

if defined JAVA_HOME (
    call :try_jdk "%JAVA_HOME%"
    if defined JDK_HOME exit /b 0
)

for /f "delims=" %%J in ('where jpackage 2^>nul') do (
    if not defined JDK_HOME (
        for %%P in ("%%~dpJ..") do call :try_jdk "%%~fP"
    )
)
if defined JDK_HOME exit /b 0

for /d %%J in ("%ProgramFiles%\Java\jdk-*") do (
    if not defined JDK_HOME call :try_jdk "%%~fJ"
)
if defined JDK_HOME exit /b 0

for /d %%J in ("%ProgramFiles%\Eclipse Adoptium\jdk-*") do (
    if not defined JDK_HOME call :try_jdk "%%~fJ"
)
if defined JDK_HOME exit /b 0

for /d %%J in ("%ProgramFiles%\Microsoft\jdk-*") do (
    if not defined JDK_HOME call :try_jdk "%%~fJ"
)
if defined JDK_HOME exit /b 0

for /d %%J in ("%USERPROFILE%\.jdks\*") do (
    if not defined JDK_HOME call :try_jdk "%%~fJ"
)
exit /b 0

:try_jdk
set "CANDIDATE=%~1"
echo Checking %CANDIDATE% >> "%LOG%"
if exist "%CANDIDATE%\bin\java.exe" if exist "%CANDIDATE%\bin\jpackage.exe" (
    set "JDK_HOME=%CANDIDATE%"
)
exit /b 0
