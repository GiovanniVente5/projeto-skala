package org.example.projeto_skala.Controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;

import org.example.projeto_skala.Json.JsonCriar;
import org.example.projeto_skala.SkalaApplication;
import org.example.projeto_skala.controlePDF.GerarPDF;
import org.example.projeto_skala.controleXLSX.CriarRelatorio;
import org.example.projeto_skala.objetos.Empresas;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class RelatoriosController {
    @FXML
    private Button abrirPastaBtn;

    @FXML
    private Button gerarBtn;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox relatoriosContainer;

    @FXML
    private void initialize() {
        abrirPastaBtn.setOnAction(e -> abrirPastaRecibos());
        gerarBtn.setOnAction(e -> {
            try {
                gerarRecibos();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        carregarRelatoriosGerados();
    }

    private void carregarRelatoriosGerados() {
        relatoriosContainer.getChildren().clear();

        File baseDir = new File("data" + File.separator + "RecibosGerados");
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            statusLabel.setText("Nenhum relatorio gerado ainda.");
            Label vazio = new Label("Nenhum relatorio gerado ainda.");
            vazio.getStyleClass().add("empty-state");
            relatoriosContainer.getChildren().add(vazio);
            return;
        }

        File[] periodos = baseDir.listFiles(File::isDirectory);
        if (periodos == null || periodos.length == 0) {
            statusLabel.setText("Nenhum relatorio gerado ainda.");
            Label vazio = new Label("Nenhum relatorio gerado ainda.");
            vazio.getStyleClass().add("empty-state");
            relatoriosContainer.getChildren().add(vazio);
            return;
        }

        Arrays.sort(periodos, Comparator.comparing(File::getName));
        int totalRelatorios = 0;

        for (File periodoDir : periodos) {
            File[] relatorios = periodoDir.listFiles((dir, nome) -> nome.toLowerCase().endsWith(".xlsx"));
            if (relatorios == null || relatorios.length == 0) {
                continue;
            }

            Arrays.sort(relatorios, Comparator.comparing(File::getName));
            totalRelatorios += relatorios.length;
            relatoriosContainer.getChildren().add(criarDropdownRelatorio(periodoDir.getName(), relatorios));
        }

        if (totalRelatorios == 0) {
            statusLabel.setText("Nenhum relatorio gerado ainda.");
            Label vazio = new Label("Nenhum relatorio gerado ainda.");
            vazio.getStyleClass().add("empty-state");
            relatoriosContainer.getChildren().add(vazio);
            return;
        }

        statusLabel.setText(totalRelatorios + " relatorio(s) gerado(s).");
    }

    private TitledPane criarDropdownRelatorio(String periodoPasta, File[] relatorios) {
        TitledPane dropdown = new TitledPane();
        dropdown.getStyleClass().add("cliente-dropdown");
        dropdown.setAnimated(true);
        dropdown.setCollapsible(true);
        dropdown.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        dropdown.setExpanded(false);
        dropdown.setMaxWidth(Double.MAX_VALUE);
        dropdown.setText("");

        HBox cabecalho = new HBox(12);
        cabecalho.getStyleClass().add("cliente-header");
        cabecalho.setMaxWidth(Double.MAX_VALUE);
        cabecalho.prefWidthProperty().bind(dropdown.widthProperty().subtract(56));

        Label periodo = new Label("Periodo: " + formatarPeriodo(periodoPasta));
        periodo.getStyleClass().add("cliente-nome");
        periodo.setWrapText(true);
        HBox.setHgrow(periodo, Priority.ALWAYS);

        Label quantidade = new Label(relatorios.length + " arquivo(s)");
        quantidade.getStyleClass().add("cliente-numero");

        cabecalho.getChildren().addAll(periodo, quantidade);

        VBox arquivosBox = new VBox(6);
        arquivosBox.getStyleClass().add("servicos-lista");

        for (File relatorio : relatorios) {
            arquivosBox.getChildren().add(criarLinhaRelatorio(relatorio));
        }


        dropdown.setGraphic(cabecalho);
        dropdown.setContent(arquivosBox);
        return dropdown;
    }

    private HBox criarLinhaRelatorio(File relatorio) {
        HBox linha = new HBox(10);
        linha.getStyleClass().add("servico-linha");

        Label nome = new Label(relatorio.getName());
        nome.getStyleClass().add("servico-nome");
        nome.setWrapText(true);
        HBox.setHgrow(nome, Priority.ALWAYS);

        Button abrirBtn = new Button("Relatório");
        abrirBtn.setOnAction(e -> abrirArquivo(relatorio));

        Button abrirBtnRecibo = new Button("Recibos");
        File recibos = new File(relatorio.getPath().replace(relatorio.getName(), ""));
        abrirBtnRecibo.setOnAction(e -> abrirArquivo(recibos));

        linha.getChildren().addAll(nome, abrirBtn,abrirBtnRecibo);
        return linha;
    }

    private String formatarPeriodo(String periodoPasta) {
        if (periodoPasta == null || periodoPasta.isBlank()) {
            return "Sem referencia";
        }

        String sufixoAntigo = "";
        String periodoBase = periodoPasta;
        int indiceVelho = periodoPasta.indexOf("-Velho");
        if (indiceVelho >= 0) {
            periodoBase = periodoPasta.substring(0, indiceVelho);
            String versao = periodoPasta.substring(indiceVelho + "-Velho".length()).replace("-", " ").trim();
            sufixoAntigo = versao.isBlank() ? " - Velho" : " - Velho " + versao;
        }

        String periodoFormatado = periodoBase.equalsIgnoreCase("sem-referencia")
                ? "Sem referencia"
                : periodoBase.replace('-', '/');

        return periodoFormatado + sufixoAntigo;
    }

    private String criarNomePastaPeriodo(String periodoLabel) {
        if (periodoLabel == null || periodoLabel.isBlank() || periodoLabel.equalsIgnoreCase("Sem referencia")) {
            return "sem-referencia";
        }

        return periodoLabel.replace('/', '-');
    }

    private boolean pastaContemArquivosGerados(File pasta) {
        if (!pasta.exists() || !pasta.isDirectory()) {
            return false;
        }

        File[] arquivos = pasta.listFiles((dir, nome) -> {
            String lower = nome.toLowerCase();
            return lower.endsWith(".pdf") || lower.endsWith(".xlsx");
        });

        return arquivos != null && arquivos.length > 0;
    }

    private File criarDestinoPastaVelha(File pastaAtual) {
        File parent = pastaAtual.getParentFile();
        String nomeBase = pastaAtual.getName() + "-Velho";
        File destino = new File(parent, nomeBase);
        int versao = 2;

        while (destino.exists()) {
            destino = new File(parent, nomeBase + "-" + versao);
            versao++;
        }

        return destino;
    }

    private boolean arquivarPastasAntigas(List<File> pastasParaArquivar) {
        for (File pastaAtual : pastasParaArquivar) {
            if (!pastaAtual.exists()) {
                continue;
            }

            File pastaVelha = criarDestinoPastaVelha(pastaAtual);
            try {
                Files.move(pastaAtual.toPath(), pastaVelha.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText("Nao foi possivel arquivar a pasta antiga.");
                alert.setContentText("Erro ao renomear " + pastaAtual.getPath() + " para " + pastaVelha.getPath() + ": " + ex.getMessage());
                alert.showAndWait();
                return false;
            }
        }

        return true;
    }

    private void abrirArquivo(File arquivo) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(arquivo);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Abrir relatorio");
                alert.setContentText("Nao e possivel abrir o relatorio neste sistema.");
                alert.showAndWait();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setContentText("Erro ao abrir o relatorio: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void abrirPastaRecibos() {
        try {
            File dir = new File("data/RecibosGerados");
            if (!dir.exists()) dir.mkdirs();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(dir);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Abrir pasta");
                alert.setContentText("Não é possível abrir a pasta neste sistema.");
                alert.showAndWait();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setContentText("Erro ao abrir a pasta: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void voltarMenu() throws IOException {
        Parent root = FXMLLoader.load(SkalaApplication.class.getResource("MenuView.fxml"));
        gerarBtn.getScene().setRoot(root);
    }

    private void gerarRecibos() throws IOException {
        Map<String, List<Empresas>> empresasPorPeriodo = JsonCriar.carregarPorPeriodo();
        if (empresasPorPeriodo.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Nenhuma empresa");
            alert.setContentText("Nenhuma empresa encontrada nos JSONs.");
            alert.showAndWait();
            return;
        }

        // ask user which period to generate
        java.util.List<String> periodos = new java.util.ArrayList<>(empresasPorPeriodo.keySet());
        java.util.Collections.sort(periodos);
        final String ALL = "Todos os períodos";
        periodos.add(0, ALL);

        ChoiceDialog<String> choice = new ChoiceDialog<>(ALL, periodos);
        choice.setTitle("Selecionar período");
        choice.setHeaderText("Escolha o período para gerar os recibos");
        choice.setContentText("Período:");

        java.util.Optional<String> escolha = choice.showAndWait();
        if (!escolha.isPresent()) {
            return; // cancelled
        }

        String selecionado = escolha.get();
        Map<String, List<Empresas>> toProcess = new java.util.LinkedHashMap<>();
        if (ALL.equals(selecionado)) {
            toProcess.putAll(empresasPorPeriodo);
        } else {
            List<Empresas> lista = empresasPorPeriodo.get(selecionado);
            if (lista == null || lista.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Nenhuma empresa");
                alert.setContentText("Nenhuma empresa encontrada para o período selecionado.");
                alert.showAndWait();
                return;
            }
            toProcess.put(selecionado, lista);
        }

        // before generating, confirm for periods that already have files
        Map<String, List<Empresas>> finalToProcess = new java.util.LinkedHashMap<>();
        List<File> pastasParaArquivar = new java.util.ArrayList<>();
        for (Map.Entry<String, List<Empresas>> entry : toProcess.entrySet()) {
            String periodoLabel = entry.getKey();
            String folderName = criarNomePastaPeriodo(periodoLabel);
            File outDir = new File("data" + File.separator + "RecibosGerados" + File.separator + folderName);
            boolean hasExisting = pastaContemArquivosGerados(outDir);

            if (hasExisting) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Relatorios ja existem");
                confirm.setHeaderText("Ja existem recibos ou relatorios para o periodo: " + periodoLabel);
                confirm.setContentText("Deseja recriar este periodo?\nA pasta atual sera renomeada para " + folderName + "-Velho e uma nova pasta sera criada com os relatorios atualizados.");
                ButtonType sim = new ButtonType("Recriar");
                ButtonType nao = new ButtonType("Não");
                ButtonType cancelar = new ButtonType("Cancelar");
                confirm.getButtonTypes().setAll(sim, nao, cancelar);

                java.util.Optional<ButtonType> resposta = confirm.showAndWait();
                if (resposta.isEmpty() || resposta.get() == cancelar) {
                    // user cancelled entire operation
                    return;
                } else if (resposta.get() == nao) {
                    // skip this period
                    continue;
                }
                pastasParaArquivar.add(outDir);
            }

            finalToProcess.put(entry.getKey(), entry.getValue());
        }

        int totalFinal = finalToProcess.values().stream().mapToInt(List::size).sum();
        if (totalFinal == 0) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Nenhum recibo a gerar");
            info.setContentText("Nenhum recibo será gerado (todos os períodos foram pulados).");
            info.showAndWait();
            return;
        }

        if (!arquivarPastasAntigas(pastasParaArquivar)) {
            return;
        }

        ProgressBar progressBar = new ProgressBar(0);
        ProgressIndicator spinner = new ProgressIndicator();
        Label message = new Label("Iniciando...");

        VBox content = new VBox(12);
        content.setPadding(new Insets(12));
        HBox top = new HBox(12, spinner, progressBar);
        content.getChildren().addAll(top, message);

        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        Stage owner = (Stage) gerarBtn.getScene().getWindow();
        dialog.initOwner(owner);
        dialog.setTitle("Gerando recibos");
        dialog.setScene(new Scene(content, 460, 160));
        dialog.setResizable(false);
        dialog.show();

        // use a thread pool to parallelize generation
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CompletionService<Empresas> completionService = new ExecutorCompletionService<>(executor);

        // submit tasks per selected period; create folder per period
        for (Map.Entry<String, List<Empresas>> entry : finalToProcess.entrySet()) {
            String periodoLabel = entry.getKey(); // expected format MM/YYYY or 'Sem referencia'
            String folderName = criarNomePastaPeriodo(periodoLabel);
            File outDir = new File("data" + File.separator + "RecibosGerados" + File.separator + folderName);
            if (!outDir.exists()) outDir.mkdirs();
            for (Empresas emp : entry.getValue()) {

                    completionService.submit(() -> {
                        try {
                            GerarPDF.gerarRecibo(emp, outDir,folderName);
                            return emp;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return emp;
                        }
                    });
            }
            System.out.println(outDir.getPath());

            CriarRelatorio.criarRelatorio(entry.getValue(), outDir.getPath(), folderName);
        }

        // monitor completion
        new Thread(() -> {
            int done = 0;
            for (int i = 0; i < totalFinal; i++) {
                try {
                    Future<Empresas> future = completionService.take(); // blocks until one finishes
                    Empresas emp = null;
                    try {
                        emp = future.get();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    done++;
                    final int current = done;
                    final String nome = emp != null ? emp.getNome() : "(sem nome)";
                    Platform.runLater(() -> {
                        progressBar.setProgress((double) current / totalFinal);
                        message.setText(String.format("Gerando %d de %d: %s", current, totalFinal, nome));
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            executor.shutdown();

            Platform.runLater(() -> {
                dialog.close();
                carregarRelatoriosGerados();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Concluído");
                if (ALL.equals(selecionado)) {
                    alert.setContentText("Recibos gerados com sucesso (pastas por período criadas em data/RecibosGerados).");
                } else {
                    String folderName = criarNomePastaPeriodo(selecionado);
                    alert.setContentText("Recibos gerados com sucesso em: data/RecibosGerados/" + folderName);
                }
                alert.showAndWait();
            });
        }).start();
    }
}
