@echo off
setlocal
:: Build jar
if not exist mvnw.cmd (
  echo Maven wrapper (mvnw.cmd) not found. Run 'mvn clean package' manually.
) else (
  mvnw.cmd clean package -DskipTests || goto :err
)
:: artifact name (from pom.xml)
set ARTIFACT=Projeto_Skala-1.0-SNAPSHOT.jar
set APP_NAME=SkalaConvertor
set OUT_DIR=dist\portable
:: cleanup
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"
:: Create app-image with jpackage (bundles JRE into image)
where jpackage >nul 2>&1
if errorlevel 1 (
  echo jpackage not found on PATH. Install a JDK that includes jpackage (JDK 17+), or use jlink approach.
  goto :err
)
jpackage --type app-image --input target --dest "%OUT_DIR%" --name "%APP_NAME%" --main-jar %ARTIFACT% --main-class org.example.projeto_skala.SkalaApplication --icon src\main\resources\icon.ico
if errorlevel 1 goto :err
:: Zip the app image (requires PowerShell)
powershell -Command "Remove-Item -LiteralPath '%OUT_DIR%\%APP_NAME%.zip' -ErrorAction SilentlyContinue; Compress-Archive -Path '%OUT_DIR\\%APP_NAME%\\*' -DestinationPath '%OUT_DIR%\%APP_NAME%.zip' -Force"
if errorlevel 1 goto :err
necho Portable package created: %OUT_DIR%\%APP_NAME%.zipnecho Instrucoes para usuarios: descompacte e execute \%APP_NAME%\%APP_NAME%.exe (ou o launcher correspondente). 
exit /b 0
:: error
:errnecho ERROR: build failed.
exit /b 1