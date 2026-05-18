# Projeto Skala

Instruções para distribuir e instalar a aplicação (forma simples via ZIP para usuários finais).

Distribuição para usuários (ZIP portátil)

1. Gerar o pacote portátil (desenvolvedor):
   - No Windows, a partir da raiz do projeto execute:
     ```bat
     build-portable.bat
     ```
   - Pré-requisitos: JDK 17+ com jpackage no PATH, PowerShell disponível, mvnw.cmd presente.
   - O artefato gerado ficará em `dist\portable\SkalaConvertor.zip`.

2. Instalar/Executar (usuário final):
   - Baixe e descompacte `SkalaConvertor.zip`.
   - Abra a pasta extraída e execute o launcher (ex.: `SkalaConvertor\SkalaConvertor.exe`) ou o arquivo `.exe` gerado.
   - Para conveniência, crie um atalho na Área de Trabalho.

Gerar instalador Windows (opcional)

1. Existem scripts no repositório para criar instaladores:
   - `create-installer.bat` — empacotamento via `--main-jar` (modo não modular).
   - `create-installer-module.bat` — para aplicações modulares / JavaFX (ajuste `JAVA_HOME` e `JAVAFX_SDK`).

2. Pré-requisitos: JDK 14+ com `jpackage` no PATH e Maven (`mvn`) no PATH. Ajuste `JAVA_HOME` no topo dos scripts se necessário.

3. Uso:
   - Abra um Prompt de Comando na raiz do projeto e execute `create-installer.bat` ou `create-installer-module.bat` conforme o caso.
   - O instalador (arquivo `.exe`) será gerado em `target\installer\`.

Dica: se o JAR não for "fat" (sem dependências embutidas), prefira o modo modular ou coloque dependências na pasta `target` usada como `--input` para o `jpackage`.

Gerar manualmente (alternativa)

- Se preferir não usar os scripts, gere o JAR e use jpackage manualmente:
  1. mvn clean package (ou mvnw.cmd clean package)
  2. jpackage (modo não modular):
     jpackage --type exe --input target --dest target\installer --name Projeto_Skala --main-jar <ARTIFACT_JAR> --main-class org.example.projeto_skala.Launcher --win-shortcut --win-menu --icon src\main\resources\icon.ico
  3. Exemplo modular (JavaFX):
     jpackage --type exe --module org.example.projeto_skala/org.example.projeto_skala.Launcher --module-path "C:\\path\\to\\javafx-sdk-21\\lib;target\\mods" --add-modules javafx.controls,javafx.fxml --dest target\installer --name Projeto_Skala --win-shortcut --win-menu --icon src\main\resources\icon.ico

  Exemplo PowerShell (Windows PowerShell / PowerShell Core):

  # Ajuste JAVA_HOME se necessário
  $env:JAVA_HOME = 'C:\\Program Files\\Java\\jdk-21'
  $env:Path = $env:JAVA_HOME + '\\bin;' + $env:Path

  # Build
  mvn clean package -DskipTests

  # Encontrar JAR (exemplo)
  $jar = Get-ChildItem -Path .\\target -Filter *.jar -Recurse | Where-Object { $_.Name -notmatch 'sources|javadoc|original' } | Select-Object -First 1

  # jpackage (modo não modular)
  jpackage --type exe --input .\\target --dest .\\target\\installer --name Projeto_Skala --main-jar $jar.Name --main-class org.example.projeto_skala.Launcher --win-shortcut --win-menu --icon src\\main\\resources\\icon.ico

  # jpackage (modular, JavaFX)
  jpackage --type exe --module org.example.projeto_skala/org.example.projeto_skala.Launcher --module-path "C:\\path\\to\\javafx-sdk-21\\lib;target\\mods" --add-modules javafx.controls,javafx.fxml --dest target\\installer --name Projeto_Skala --win-shortcut --win-menu --icon src\\main\\resources\\icon.ico

Observações importantes

- A pasta `data/` contém os JSONs importados e é necessária para o funcionamento. A pasta `data/RecibosGerados/` é gerada e NÃO deve ser comitada.
- Garanta que os templates e fontes (ex.: `ARIAL.TTF` e `PdfTemplate/Recibo-Template.pdf`) estejam presentes no repositório ou incluídos no pacote.
- Teste o pacote final em uma máquina sem JDK para verificar que o runtime foi empacotado corretamente.

Compartilhar no GitHub (opcional)

1. Inicializar git e enviar para um repositório remoto:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin git@github.com:SEU_USUARIO/SEU_REPO.git
   git branch -M main
   git push -u origin main
   ```

2. Para automatizar builds e publicar artefatos, crie um workflow GitHub Actions que rode `mvn package` e `jpackage`, e publique o ZIP/em installador em Releases.

Se quiser, eu:
- Crio o workflow GitHub Actions que gera o ZIP e o instalador automaticamente em cada release.
- Faço um commit com os scripts e um exemplo de release workflow.

