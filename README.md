# Projeto Skala

## Construa o portable app no Windows

Requerimentos:

- JDK 21 Instalado
- `java` e `jpackage` Disponivel no PATH

Para criar a versão portavel, rode isso na raiz do projeto:

```bat
build-portable.bat
```

O executavel será criado no:

```text
dist\SkalaConvertor\SkalaConvertor.exe
```

Você pode zippar o `SkalaConvertor` pasta e mandar para outra maquina windows. Isso não é um instalador, abra o `.exe ` diretamente
