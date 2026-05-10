# Projeto Skala

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

## Build Windows installer

Requirements:

- JDK 21 installed
- `java` and `jpackage` available on PATH
- WiX Toolset installed
- `wix` available on PATH, or WiX 3 tools `candle` and `light` available on PATH

To create the Windows installer, run this from the project root:

```bat
build-installer.bat
```

The installer will be created in:

```text
dist\
```

Look for a file like:

```text
SkalaConvertor-1.0.0.exe
```

If the build fails, open:

```text
build-installer.log
```

The installer build copies the app jar and all runtime dependencies into `target\installer-input` before running `jpackage`.
