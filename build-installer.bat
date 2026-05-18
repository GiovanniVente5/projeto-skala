@echo off
setlocal
:: Ensure mvnw exists and build the projectnif not exist mvnw.cmd (
  echo Maven wrapper (mvnw.cmd) not found. Run 'mvn clean package' manually.
) else (
  mvnw.cmd clean package -DskipTests || goto :err
)
:: artifact and names (from pom.xml)
set ARTIFACT=Projeto_Skala-1.0-SNAPSHOT.jar
set APP_NAME=SkalaConvertor
set OUT_DIR=dist\installer
set MAIN_CLASS=org.example.projeto_skala.Launcher
set APP_VERSION=1.0.0
:: cleanup
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"
:: Check jpackage
where jpackage >nul 2>&1
if errorlevel 1 (
  echo jpackage not found on PATH. Install a JDK that includes jpackage (JDK 17+), or add it to PATH.
  goto :err
)
:: icon (optional)
set ICON_PATH=src\main\resources\icon.ico
set ICON_ARG=
if exist "%ICON_PATH%" set ICON_ARG=--icon "%ICON_PATH%"
:: Run jpackage to create a Windows installer (.exe)
jpackage --type exe --input target --dest "%OUT_DIR%" --name "%APP_NAME%" --main-jar %ARTIFACT% --main-class %MAIN_CLASS% --app-version %APP_VERSION% --win-shortcut --win-menu %ICON_ARG%
if errorlevel 1 goto :err
necho Installer created in: %OUT_DIR%necho You can distribute the generated .exe to users.
exit /b 0
:: error
:errnecho ERROR: build failed.
exit /b 1