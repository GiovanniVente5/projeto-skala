param(
    [string]$AppName = "SkalaConvertor",
    [string]$Vendor = "Skala",
    [string]$Version = "1.0.0"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$TargetDir = Join-Path $ProjectRoot "target"
$DependencyDir = Join-Path $TargetDir "dependency"
$AppLibsDir = Join-Path $TargetDir "app-libs"
$InstallerDir = Join-Path $TargetDir "installer"
$AppImageRoot = Join-Path $TargetDir "app-image"
$PayloadDir = Join-Path $TargetDir "installer-payload"
$AppImageDir = Join-Path $AppImageRoot $AppName
$ZipPath = Join-Path $PayloadDir "$AppName-app.zip"
$InstallerPath = Join-Path $InstallerDir "$AppName-Setup.exe"
$MainClass = "org.example.projeto_skala.Launcher"

function Resolve-CommandPath {
    param(
        [string]$CommandName,
        [string[]]$Candidates
    )

    $command = Get-Command $CommandName -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    foreach ($candidate in $Candidates) {
        if ($candidate -and (Test-Path $candidate)) {
            return $candidate
        }
    }

    return $null
}

function Remove-DirectoryIfExists {
    param([string]$Path)

    if (Test-Path $Path) {
        Remove-Item -LiteralPath $Path -Recurse -Force
    }
}

function Write-TextFile {
    param(
        [string]$Path,
        [string]$Content
    )

    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}

$JPackage = $null
if ($env:JAVA_HOME) {
    $JPackage = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
}

if (-not ($JPackage -and (Test-Path $JPackage))) {
    $JPackage = Resolve-CommandPath "jpackage.exe" @(
        "C:\Program Files\Java\jdk-21.0.11\bin\jpackage.exe"
    )
}

if (-not $JPackage) {
    throw "jpackage.exe nao encontrado. Instale um JDK 21 e defina JAVA_HOME."
}

$JdkBin = Split-Path -Parent $JPackage
$env:JAVA_HOME = Split-Path -Parent $JdkBin
$env:Path = "$JdkBin;$env:Path"

$Maven = Resolve-CommandPath "mvn.cmd" @(
    (Join-Path $ProjectRoot "mvnw.cmd"),
    "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.1\plugins\maven\lib\maven3\bin\mvn.cmd",
    "C:\Users\giovanni\.m2\wrapper\dists\apache-maven-3.8.5-bin\5i5jha092a3i37g0paqnfr15e0\apache-maven-3.8.5\bin\mvn.cmd"
)

if (-not $Maven) {
    throw "Maven nao encontrado. Instale o Maven, adicione mvn.cmd ao PATH ou adicione mvnw.cmd ao projeto."
}

$IExpress = Resolve-CommandPath "iexpress.exe" @(
    "$env:WINDIR\System32\iexpress.exe",
    "$env:WINDIR\SysWOW64\iexpress.exe"
)

if (-not $IExpress) {
    throw "iexpress.exe nao encontrado. Sem ele, instale WiX/Inno Setup ou gere apenas app-image pelo jpackage."
}

Set-Location $ProjectRoot

Write-Host "Compilando projeto Maven..."
& $Maven "-DskipTests" "clean" "package" "dependency:copy-dependencies" "-DincludeScope=runtime" "-DoutputDirectory=$DependencyDir"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Remove-DirectoryIfExists $InstallerDir
Remove-DirectoryIfExists $AppImageRoot
Remove-DirectoryIfExists $PayloadDir
Remove-DirectoryIfExists $AppLibsDir
New-Item -ItemType Directory -Force -Path $InstallerDir, $AppImageRoot, $PayloadDir, $AppLibsDir | Out-Null

$MainJar = Get-ChildItem -LiteralPath $TargetDir -Filter "*.jar" |
    Where-Object { $_.Name -notmatch "sources|javadoc|original" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $MainJar) {
    throw "JAR principal nao encontrado em $TargetDir"
}

Copy-Item -LiteralPath $MainJar.FullName -Destination $AppLibsDir -Force
Copy-Item -Path (Join-Path $DependencyDir "*.jar") -Destination $AppLibsDir -Force

Write-Host "Gerando app-image com runtime Java embutido..."
& $JPackage `
    "--type" "app-image" `
    "--dest" $AppImageRoot `
    "--name" $AppName `
    "--app-version" $Version `
    "--vendor" $Vendor `
    "--input" $AppLibsDir `
    "--main-jar" $MainJar.Name `
    "--main-class" $MainClass `
    "--add-modules" "java.desktop,java.logging,java.naming,java.sql,java.xml,jdk.crypto.ec,jdk.localedata,jdk.unsupported" `
    "--java-options" "-Dfile.encoding=UTF-8"

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Compactando app-image..."
Compress-Archive -Path $AppImageDir -DestinationPath $ZipPath -Force

$InstallCmd = @"
@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0install.ps1"
exit /b %ERRORLEVEL%
"@

$InstallPs1 = @"
`$ErrorActionPreference = "Stop"

`$appName = "$AppName"
`$installParent = Join-Path `$env:LOCALAPPDATA "Programs"
`$installDir = Join-Path `$installParent `$appName
`$zipPath = Join-Path `$PSScriptRoot "$AppName-app.zip"

New-Item -ItemType Directory -Force -Path `$installParent | Out-Null

if (Test-Path `$installDir) {
    Remove-Item -LiteralPath `$installDir -Recurse -Force
}

Expand-Archive -LiteralPath `$zipPath -DestinationPath `$installParent -Force
New-Item -ItemType Directory -Force -Path (Join-Path `$installDir "data") | Out-Null

`$exePath = Join-Path `$installDir "$AppName.exe"
if (-not (Test-Path `$exePath)) {
    throw "Executavel nao encontrado apos instalacao: `$exePath"
}

`$uninstallPath = Join-Path `$installDir "Uninstall-$AppName.ps1"
@'
`$ErrorActionPreference = "Stop"
`$appName = "$AppName"
`$installDir = Join-Path `$env:LOCALAPPDATA "Programs\$AppName"
`$desktopShortcut = Join-Path ([Environment]::GetFolderPath("Desktop")) "$AppName.lnk"
`$startMenuDir = Join-Path `$env:APPDATA "Microsoft\Windows\Start Menu\Programs\$AppName"

if (Test-Path `$desktopShortcut) {
    Remove-Item -LiteralPath `$desktopShortcut -Force
}

if (Test-Path `$startMenuDir) {
    Remove-Item -LiteralPath `$startMenuDir -Recurse -Force
}

if (Test-Path `$installDir) {
    Remove-Item -LiteralPath `$installDir -Recurse -Force
}
'@ | Set-Content -LiteralPath `$uninstallPath -Encoding UTF8

`$shell = New-Object -ComObject WScript.Shell

`$desktopShortcut = Join-Path ([Environment]::GetFolderPath("Desktop")) "$AppName.lnk"
`$shortcut = `$shell.CreateShortcut(`$desktopShortcut)
`$shortcut.TargetPath = `$exePath
`$shortcut.WorkingDirectory = `$installDir
`$shortcut.IconLocation = `$exePath
`$shortcut.Save()

`$startMenuDir = Join-Path `$env:APPDATA "Microsoft\Windows\Start Menu\Programs\$AppName"
New-Item -ItemType Directory -Force -Path `$startMenuDir | Out-Null

`$startShortcut = Join-Path `$startMenuDir "$AppName.lnk"
`$shortcut = `$shell.CreateShortcut(`$startShortcut)
`$shortcut.TargetPath = `$exePath
`$shortcut.WorkingDirectory = `$installDir
`$shortcut.IconLocation = `$exePath
`$shortcut.Save()

`$uninstallShortcut = Join-Path `$startMenuDir "Desinstalar $AppName.lnk"
`$shortcut = `$shell.CreateShortcut(`$uninstallShortcut)
`$shortcut.TargetPath = "powershell.exe"
`$shortcut.Arguments = "-NoProfile -ExecutionPolicy Bypass -File ""`$uninstallPath"""
`$shortcut.WorkingDirectory = `$installDir
`$shortcut.IconLocation = "powershell.exe"
`$shortcut.Save()

Write-Host "$AppName instalado em `$installDir"
"@

Write-TextFile -Path (Join-Path $PayloadDir "install.cmd") -Content $InstallCmd
Write-TextFile -Path (Join-Path $PayloadDir "install.ps1") -Content $InstallPs1

$SedPath = Join-Path $PayloadDir "$AppName.sed"
$PayloadDirForSed = $PayloadDir.TrimEnd("\") + "\"
$InstallerPathForSed = $InstallerPath

$Sed = @"
[Version]
Class=IEXPRESS
SEDVersion=3

[Options]
PackagePurpose=InstallApp
ShowInstallProgramWindow=0
HideExtractAnimation=1
UseLongFileName=1
InsideCompressed=0
CAB_FixedSize=0
CAB_ResvCodeSigning=0
RebootMode=N
InstallPrompt=
DisplayLicense=
FinishMessage=$AppName instalado com sucesso.
TargetName=$InstallerPathForSed
FriendlyName=$AppName
AppLaunched=install.cmd
PostInstallCmd=<None>
AdminQuietInstCmd=
UserQuietInstCmd=
SourceFiles=SourceFiles

[Strings]
FILE0="$AppName-app.zip"
FILE1="install.cmd"
FILE2="install.ps1"

[SourceFiles]
SourceFiles0=$PayloadDirForSed

[SourceFiles0]
%FILE0%=
%FILE1%=
%FILE2%=
"@

Write-TextFile -Path $SedPath -Content $Sed

Write-Host "Gerando instalador EXE..."
& $IExpress "/N" "/Q" $SedPath
$IExpressExitCode = $LASTEXITCODE

$Deadline = (Get-Date).AddMinutes(10)
$LastInstallerSize = -1
$StableChecks = 0

while ((Get-Date) -lt $Deadline) {
    if (Test-Path $InstallerPath) {
        $CurrentInstallerSize = (Get-Item -LiteralPath $InstallerPath).Length

        if ($CurrentInstallerSize -gt 0 -and $CurrentInstallerSize -eq $LastInstallerSize) {
            $StableChecks++
        } else {
            $StableChecks = 0
            $LastInstallerSize = $CurrentInstallerSize
        }

        if ($StableChecks -ge 3) {
            break
        }
    }

    Start-Sleep -Seconds 1
}

if (-not (Test-Path $InstallerPath)) {
    throw "IExpress terminou com codigo $IExpressExitCode sem gerar o instalador esperado: $InstallerPath"
}

if ($IExpressExitCode -ne 0) {
    Write-Warning "IExpress retornou codigo $IExpressExitCode, mas o instalador foi gerado corretamente."
}

Get-ChildItem -LiteralPath $InstallerDir -Filter "~$AppName-Setup.*" -ErrorAction SilentlyContinue |
    Remove-Item -Force -ErrorAction SilentlyContinue

Write-Host "Instalador gerado em: $InstallerPath"
exit 0
