@echo off
setlocal

cd /d "%~dp0"

if not exist dist mkdir dist

call mvnw.cmd clean package dependency:copy-dependencies -DincludeScope=runtime
if errorlevel 1 exit /b %errorlevel%

if exist dist\SkalaConvertor rmdir /s /q dist\SkalaConvertor

jpackage ^
  --type app-image ^
  --name SkalaConvertor ^
  --dest dist ^
  --module-path "target\classes;target\dependency" ^
  --module org.example.projeto_skala/org.example.projeto_skala.Launcher

if errorlevel 1 exit /b %errorlevel%

echo.
echo Portable app created at: dist\SkalaConvertor\SkalaConvertor.exe
endlocal
