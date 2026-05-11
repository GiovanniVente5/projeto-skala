param(
    [string]$AppName = "SkalaConvertor",
    [string]$AppVersion = "1.0.0",
    [string]$Vendor = "Skala"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Log = Join-Path $Root "build-installer.log"
$JarName = "Projeto_Skala-1.0-SNAPSHOT.jar"
$MainClass = "org.example.projeto_skala.Launcher"

$DistDir = Join-Path $Root "dist"
$BuildDir = Join-Path $Root "target\installer-build"
$InputDir = Join-Path $Root "target\installer-input"
$AppImageDest = Join-Path $BuildDir "app-image"
$AppImageDir = Join-Path $AppImageDest $AppName
$ZipPath = Join-Path $BuildDir "$AppName.zip"
$InstallerScriptPath = Join-Path $BuildDir "install-skala.ps1"
$SedPath = Join-Path $BuildDir "installer.sed"
$InstallerPath = Join-Path $DistDir "$AppName-Setup-$AppVersion.exe"

function Write-Log {
    param([string]$Message)
    $Message | Tee-Object -FilePath $Log -Append
}

function Invoke-Logged {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [string]$WorkingDirectory = $Root
    )

    Write-Log ("> " + $FilePath + " " + ($Arguments -join " "))

    Push-Location $WorkingDirectory
    try {
        $PreviousErrorActionPreference = $ErrorActionPreference
        $ErrorActionPreference = "Continue"
        try {
            & $FilePath @Arguments 2>&1 |
                ForEach-Object { $_.ToString() } |
                Tee-Object -FilePath $Log -Append
            $ExitCode = $LASTEXITCODE
        }
        finally {
            $ErrorActionPreference = $PreviousErrorActionPreference
        }

        if ($ExitCode -ne 0) {
            throw "Command failed with exit code ${ExitCode}: $FilePath"
        }
    }
    finally {
        Pop-Location
    }
}

function Test-Jdk {
    param([string]$Candidate)

    if ([string]::IsNullOrWhiteSpace($Candidate)) {
        return $null
    }

    $Java = Join-Path $Candidate "bin\java.exe"
    $JPackage = Join-Path $Candidate "bin\jpackage.exe"

    if ((Test-Path -LiteralPath $Java) -and (Test-Path -LiteralPath $JPackage)) {
        $Resolved = (Resolve-Path -LiteralPath $Candidate).Path
        return [pscustomobject]@{
            Home = $Resolved
            Java = Join-Path $Resolved "bin\java.exe"
            JPackage = Join-Path $Resolved "bin\jpackage.exe"
        }
    }

    return $null
}

function Find-Jdk {
    $Candidates = New-Object System.Collections.Generic.List[string]

    if ($env:JAVA_HOME) {
        $Candidates.Add($env:JAVA_HOME)
    }

    $JPackageCommand = Get-Command jpackage.exe -ErrorAction SilentlyContinue
    if ($JPackageCommand) {
        $BinDir = Split-Path -Parent $JPackageCommand.Source
        $Candidates.Add((Resolve-Path -LiteralPath (Join-Path $BinDir "..")).Path)
    }

    $Patterns = @(
        "$env:ProgramFiles\Java\jdk-*",
        "$env:ProgramFiles\Eclipse Adoptium\jdk-*",
        "$env:ProgramFiles\Microsoft\jdk-*",
        "$env:USERPROFILE\.jdks\*"
    )

    foreach ($Pattern in $Patterns) {
        Get-ChildItem -Directory -Path $Pattern -ErrorAction SilentlyContinue |
            ForEach-Object { $Candidates.Add($_.FullName) }
    }

    foreach ($Candidate in ($Candidates | Select-Object -Unique)) {
        $Jdk = Test-Jdk $Candidate
        if ($Jdk) {
            return $Jdk
        }
    }

    throw "A full JDK with jpackage was not found. Install JDK 21 or newer, or set JAVA_HOME to the JDK folder."
}

function Find-IExpress {
    $Candidates = @(
        (Join-Path $env:SystemRoot "System32\iexpress.exe"),
        (Join-Path $env:SystemRoot "SysWOW64\iexpress.exe")
    )

    foreach ($Candidate in $Candidates) {
        if (Test-Path -LiteralPath $Candidate) {
            return $Candidate
        }
    }

    throw "IExpress was not found. It is required to create the self-extracting installer on Windows."
}

function Write-InstallerScript {
    $Content = @"
`$ErrorActionPreference = 'Stop'
`$appName = '$AppName'
`$appVersion = '$AppVersion'
`$publisher = '$Vendor'
`$programsDir = Join-Path `$env:LOCALAPPDATA 'Programs'
`$installRoot = Join-Path `$programsDir `$appName
`$dataDir = Join-Path `$env:APPDATA `$appName
`$startMenuDir = Join-Path `$env:APPDATA 'Microsoft\Windows\Start Menu\Programs'
`$desktopDir = [Environment]::GetFolderPath('DesktopDirectory')
`$zipPath = Join-Path `$PSScriptRoot "`$appName.zip"
`$exePath = Join-Path `$installRoot "`$appName.exe"

function New-AppShortcut {
    param(
        [string]`$Path,
        [string]`$TargetPath,
        [string]`$WorkingDirectory,
        [string]`$IconLocation,
        [string]`$Arguments = ''
    )

    `$shell = New-Object -ComObject WScript.Shell
    `$shortcut = `$shell.CreateShortcut(`$Path)
    `$shortcut.TargetPath = `$TargetPath
    `$shortcut.WorkingDirectory = `$WorkingDirectory
    `$shortcut.IconLocation = `$IconLocation
    if (`$Arguments) {
        `$shortcut.Arguments = `$Arguments
    }
    `$shortcut.Save()
}

if (-not (Test-Path -LiteralPath `$zipPath)) {
    throw "Installer payload not found: `$zipPath"
}

Get-Process -Name `$appName -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

New-Item -ItemType Directory -Force -Path `$programsDir, `$installRoot, `$dataDir, `$startMenuDir | Out-Null
Get-ChildItem -LiteralPath `$installRoot -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force
Expand-Archive -LiteralPath `$zipPath -DestinationPath `$installRoot -Force

if (-not (Test-Path -LiteralPath `$exePath)) {
    throw "Application executable was not installed: `$exePath"
}

New-AppShortcut `
    -Path (Join-Path `$desktopDir "`$appName.lnk") `
    -TargetPath `$exePath `
    -WorkingDirectory `$dataDir `
    -IconLocation "`$exePath,0"

New-AppShortcut `
    -Path (Join-Path `$startMenuDir "`$appName.lnk") `
    -TargetPath `$exePath `
    -WorkingDirectory `$dataDir `
    -IconLocation "`$exePath,0"

`$uninstallPath = Join-Path `$installRoot 'uninstall-skala.ps1'
`$uninstallScript = @'
`$ErrorActionPreference = 'SilentlyContinue'
`$appName = 'SkalaConvertor'
`$installRoot = Join-Path `$env:LOCALAPPDATA "Programs\`$appName"
`$startMenuDir = Join-Path `$env:APPDATA 'Microsoft\Windows\Start Menu\Programs'
`$desktopDir = [Environment]::GetFolderPath('DesktopDirectory')

Remove-Item -LiteralPath (Join-Path `$desktopDir "`$appName.lnk") -Force
Remove-Item -LiteralPath (Join-Path `$startMenuDir "`$appName.lnk") -Force
Remove-Item -LiteralPath (Join-Path `$startMenuDir "Uninstall `$appName.lnk") -Force
Remove-Item -LiteralPath "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\`$appName" -Recurse -Force

Start-Process -FilePath `$env:ComSpec -ArgumentList '/c', "timeout /t 2 /nobreak >nul & rmdir /s /q ""`$installRoot""" -WindowStyle Hidden
'@
Set-Content -LiteralPath `$uninstallPath -Value `$uninstallScript -Encoding UTF8

New-AppShortcut `
    -Path (Join-Path `$startMenuDir "Uninstall `$appName.lnk") `
    -TargetPath (Join-Path `$env:SystemRoot 'System32\WindowsPowerShell\v1.0\powershell.exe') `
    -Arguments "-NoProfile -ExecutionPolicy Bypass -File ""`$uninstallPath""" `
    -WorkingDirectory `$dataDir `
    -IconLocation "`$exePath,0"

`$regPath = "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\`$appName"
New-Item -Path `$regPath -Force | Out-Null
Set-ItemProperty -Path `$regPath -Name DisplayName -Value `$appName
Set-ItemProperty -Path `$regPath -Name DisplayVersion -Value `$appVersion
Set-ItemProperty -Path `$regPath -Name Publisher -Value `$publisher
Set-ItemProperty -Path `$regPath -Name InstallLocation -Value `$installRoot
Set-ItemProperty -Path `$regPath -Name DisplayIcon -Value "`$exePath,0"
Set-ItemProperty -Path `$regPath -Name UninstallString -Value "powershell.exe -NoProfile -ExecutionPolicy Bypass -File ""`$uninstallPath"""
Set-ItemProperty -Path `$regPath -Name NoModify -Type DWord -Value 1
Set-ItemProperty -Path `$regPath -Name NoRepair -Type DWord -Value 1

`$estimatedSize = [int]((Get-ChildItem -LiteralPath `$installRoot -Recurse -File | Measure-Object -Property Length -Sum).Sum / 1KB)
Set-ItemProperty -Path `$regPath -Name EstimatedSize -Type DWord -Value `$estimatedSize

Write-Host "`$appName `$appVersion installed successfully."
Write-Host "Installed at: `$installRoot"
"@

    Set-Content -LiteralPath $InstallerScriptPath -Value $Content -Encoding UTF8
}

function Write-SedFile {
    $SourceDir = $BuildDir.TrimEnd('\') + '\'
    $Content = @"
[Version]
Class=IEXPRESS
SEDVersion=3
[Options]
PackagePurpose=InstallApp
ShowInstallProgramWindow=1
HideExtractAnimation=1
UseLongFileName=1
InsideCompressed=0
CAB_FixedSize=0
CAB_ResvCodeSigning=0
RebootMode=N
InstallPrompt=Install ${AppName}?
DisplayLicense=
FinishMessage=$AppName was installed successfully.
TargetName=$InstallerPath
FriendlyName=$AppName Setup
AppLaunched=powershell.exe -NoProfile -ExecutionPolicy Bypass -File install-skala.ps1
PostInstallCmd=<None>
AdminQuietInstCmd=
UserQuietInstCmd=
SourceFiles=SourceFiles
[Strings]
FILE0="install-skala.ps1"
FILE1="$AppName.zip"
[SourceFiles]
SourceFiles0=$SourceDir
[SourceFiles0]
%FILE0%=
%FILE1%=
"@

    Set-Content -LiteralPath $SedPath -Value $Content -Encoding ASCII
}

Remove-Item -LiteralPath $Log -Force -ErrorAction SilentlyContinue

Write-Log "=============================="
Write-Log "Building $AppName installer"
Write-Log "=============================="

$Jdk = Find-Jdk
$IExpress = Find-IExpress

Write-Log "Using JDK: $($Jdk.Home)"
Write-Log "Using IExpress: $IExpress"

$env:JAVA_HOME = $Jdk.Home
$env:PATH = (Join-Path $Jdk.Home "bin") + ";" + $env:PATH

Invoke-Logged -FilePath $Jdk.Java -Arguments @("-version")
Invoke-Logged -FilePath $Jdk.JPackage -Arguments @("--version")

New-Item -ItemType Directory -Force -Path $DistDir | Out-Null
Remove-Item -LiteralPath $BuildDir -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $InputDir -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $InstallerPath -Force -ErrorAction SilentlyContinue

Write-Log "Running Maven build..."
Invoke-Logged -FilePath (Join-Path $Root "mvnw.cmd") -Arguments @("clean", "package", "dependency:copy-dependencies", "-DincludeScope=runtime")

$JarPath = Join-Path $Root "target\$JarName"
if (-not (Test-Path -LiteralPath $JarPath)) {
    throw "Application jar was not created: $JarPath"
}

New-Item -ItemType Directory -Force -Path $BuildDir, $InputDir, $AppImageDest | Out-Null
Copy-Item -LiteralPath $JarPath -Destination $InputDir -Force
Get-ChildItem -Path (Join-Path $Root "target\dependency") -Filter "*.jar" |
    Copy-Item -Destination $InputDir -Force

Write-Log "Creating application image..."
Invoke-Logged -FilePath $Jdk.JPackage -Arguments @(
    "--type", "app-image",
    "--name", $AppName,
    "--app-version", $AppVersion,
    "--vendor", $Vendor,
    "--dest", $AppImageDest,
    "--input", $InputDir,
    "--main-jar", $JarName,
    "--main-class", $MainClass
)

if (-not (Test-Path -LiteralPath (Join-Path $AppImageDir "$AppName.exe"))) {
    throw "jpackage finished but the app executable was not created."
}

Write-Log "Compressing installer payload..."
Compress-Archive -Path (Join-Path $AppImageDir "*") -DestinationPath $ZipPath -Force

Write-InstallerScript
Write-SedFile

Write-Log "Creating self-extracting installer..."
Invoke-Logged -FilePath $IExpress -Arguments @("/N", "/Q", $SedPath)

if (-not (Test-Path -LiteralPath $InstallerPath)) {
    throw "IExpress finished but the installer was not created: $InstallerPath"
}

Write-Log ""
Write-Log "Installer created:"
Write-Log $InstallerPath
