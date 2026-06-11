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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import org.example.projeto_skala.Json.JsonCriar;
import org.example.projeto_skala.SkalaApplication;
import org.example.projeto_skala.controlePDF.GerarPDF;
import org.example.projeto_skala.controleXLSX.CriarRelatorio;
import org.example.projeto_skala.objetos.Empresas;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
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
    private void initialize() {
        abrirPastaBtn.setOnAction(e -> abrirPastaRecibos());
        gerarBtn.setOnAction(e -> {
            try {
                gerarRecibos();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
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
        for (Map.Entry<String, List<Empresas>> entry : toProcess.entrySet()) {
            String periodoLabel = entry.getKey();
            String folderName;
            if (periodoLabel == null || periodoLabel.isBlank() || periodoLabel.equalsIgnoreCase("Sem referencia")) {
                folderName = "sem-referencia";
            } else {
                folderName = periodoLabel.replace('/', '-');
            }
            File outDir = new File("data" + File.separator + "RecibosGerados" + File.separator + folderName);
            boolean hasExisting = false;
            if (outDir.exists() && outDir.isDirectory()) {
                File[] existing = outDir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
                hasExisting = existing != null && existing.length > 0;
            }

            if (hasExisting) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Recibos já existem");
                confirm.setHeaderText("Já existem recibos para o período: " + periodoLabel);
                confirm.setContentText("Deseja gerar novamente e sobrescrever os recibos existentes?\nEscolha 'Sim' para sobrescrever, 'Não' para pular este período, ou 'Cancelar' para abortar.");
                ButtonType sim = new ButtonType("Sim");
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
                // else 'sim' -> proceed and will overwrite
            }

            // ensure directory exists and add to final list
            if (!outDir.exists()) outDir.mkdirs();
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
            String folderName;
            if (periodoLabel == null || periodoLabel.isBlank() || periodoLabel.equalsIgnoreCase("Sem referencia")) {
                folderName = "sem-referencia";
            } else {
                folderName = periodoLabel.replace('/', '-');
            }
            File outDir = new File("data" + File.separator + "RecibosGerados" + File.separator + folderName);
            if (!outDir.exists()) outDir.mkdirs();
            for (Empresas emp : entry.getValue()) {

                    completionService.submit(() -> {
                        try {
                            GerarPDF.gerarRecibo(emp, outDir);
                            return emp;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return emp;
                        }
                    });
            }
            System.out.println(outDir.getPath());
//            gerarTXT.gerarRelatorio(entry.getValue(), outDir.getPath(),"Relatório");
            CriarRelatorio.criarRelatorio(entry.getValue(), outDir.getPath());
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
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Concluído");
                if (ALL.equals(selecionado)) {
                    alert.setContentText("Recibos gerados com sucesso (pastas por período criadas em data/RecibosGerados).");
                } else {
                    String folderName = selecionado.replace('/', '-');
                    alert.setContentText("Recibos gerados com sucesso em: data/RecibosGerados/" + folderName);
                }
                alert.showAndWait();
            });
        }).start();
    }
}
