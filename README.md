# Projeto Skala

## Build Windows installer

Requirements:

- Windows
- JDK 21 or newer installed

To create the installer, run this from the project root:

```bat
build-installer.bat
```

The installer will be created at:

```text
dist\SkalaConvertor-Setup-1.0.0.exe
```

It installs the app for the current Windows user, creates Desktop and Start Menu shortcuts, and includes its own Java runtime.

## Build portable app on Windows

Requirements:

- JDK 21 installed
- `java` and `jpackage` available on PATH

To create the portable version, run this from the project root:

```bat
build-portable.bat
```

The executable will be created at:

```text
dist\SkalaConvertor\SkalaConvertor.exe
```

You can zip the `SkalaConvertor` folder and send it to another Windows machine. This is not an installer; open the `.exe` directly.
