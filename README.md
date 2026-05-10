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
