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

Gerar manualmente (alternativa)

- Se preferir não usar o script, gere o JAR e use jpackage manualmente:
  1. `mvnw.cmd clean package` (ou `mvn clean package`)
  2. `jpackage --type app-image --input target --dest dist\portable --name SkalaConvertor --main-jar <ARTIFACT_JAR> --main-class org.example.projeto_skala.SkalaApplication --icon src\main\resources\icon.ico`

Notas importantes

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

2. Para automatizar builds e publicar artefatos, crie um workflow GitHub Actions que rode `mvn package` e `jpackage`, e publique o ZIP em Releases.

Se quiser, eu:
- Adiciono instruções de uso passo-a-passo no README em português com prints rápidos.
- Crio um workflow GitHub Actions que gera o ZIP automaticamente em cada release.

