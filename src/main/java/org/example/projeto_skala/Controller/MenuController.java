package org.example.projeto_skala.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.example.projeto_skala.Json.JsonCriar;
import org.example.projeto_skala.Json.JsonServicos;
import org.example.projeto_skala.SkalaApplication;
import org.example.projeto_skala.controleXLSX.LerExcel;
import org.example.projeto_skala.objetos.Empresas;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class MenuController {
    private static final List<String> MESES = List.of(
            "01 - Janeiro",
            "02 - Fevereiro",
            "03 - Marco",
            "04 - Abril",
            "05 - Maio",
            "06 - Junho",
            "07 - Julho",
            "08 - Agosto",
            "09 - Setembro",
            "10 - Outubro",
            "11 - Novembro",
            "12 - Dezembro"
    );

    @FXML
    private VBox dropZone;

    @FXML
    private Label selectedFileLabel;

    @FXML
    private ComboBox<String> mesComboBox;

    @FXML
    private Spinner<Integer> anoSpinner;

    @FXML
    private void initialize() {
        configureReferenciaImportacao();
        configureFileDropZone();
    }

    private void configureReferenciaImportacao() {
        LocalDate hoje = LocalDate.now();

        mesComboBox.getItems().setAll(MESES);
        mesComboBox.getSelectionModel().select(hoje.getMonthValue() - 1);

        anoSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                hoje.getYear() - 20,
                hoje.getYear() + 20,
                hoje.getYear()
        ));
        anoSpinner.setEditable(true);
    }

    private void configureFileDropZone() {
        dropZone.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasFiles() && hasSpreadsheetFile(dragboard.getFiles())) {
                event.acceptTransferModes(TransferMode.COPY);
                setDropZoneState("drop-zone-active");
            } else if (dragboard.hasFiles()) {
                setDropZoneState("drop-zone-invalid");
            }

            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            clearDropZoneState();
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasFiles()) {
                File spreadsheet = findFirstSpreadsheetFile(dragboard.getFiles());
                if (spreadsheet != null) {
                    success = selectSpreadsheetFile(spreadsheet);
                } else {
                    selectedFileLabel.setText("Please choose an Excel or CSV file.");
                }
            }

            clearDropZoneState();
            event.setDropCompleted(success);
            event.consume();
        });

        dropZone.setOnMouseClicked(event -> openFileChooser());
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose spreadsheet file");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Spreadsheet files", "*.xlsx", "*.xls", "*.xlsm", "*.csv")
        );

        Window window = dropZone.getScene() == null ? null : dropZone.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            selectSpreadsheetFile(file);
        }
    }

    private boolean selectSpreadsheetFile(File file) {
        int mes = mesComboBox.getSelectionModel().getSelectedIndex() + 1;
        Integer ano = getAnoSelecionado();

        if (mes < 1 || ano == null) {
            selectedFileLabel.setText("Selecione um mes e ano validos.");
            return false;
        }

        selectedFileLabel.setText(file.getName() + " - " + String.format("%02d/%04d", mes, ano));
        List<Empresas> linhas = LerExcel.lerExcel(file);
        JsonCriar.salvar(linhas, mes, ano);
        JsonServicos.sincronizarComEmpresas(linhas);
        return true;
    }

    private Integer getAnoSelecionado() {
        try {
            int ano = Integer.parseInt(anoSpinner.getEditor().getText());
            anoSpinner.getValueFactory().setValue(ano);
            return ano;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML
    private void abrirTelaClientes() throws IOException {
        Parent root = FXMLLoader.load(SkalaApplication.class.getResource("ClientesView.fxml"));
        dropZone.getScene().setRoot(root);
    }

    @FXML
    private void abrirTelaServicos() throws IOException {
        Parent root = FXMLLoader.load(SkalaApplication.class.getResource("ServicosView.fxml"));
        dropZone.getScene().setRoot(root);
    }

    private boolean hasSpreadsheetFile(List<File> files) {
        return findFirstSpreadsheetFile(files) != null;
    }

    private File findFirstSpreadsheetFile(List<File> files) {
        for (File file : files) {
            if (isSpreadsheetFile(file)) {
                return file;
            }
        }

        return null;
    }

    private boolean isSpreadsheetFile(File file) {
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        return file.isFile()
                && (fileName.endsWith(".xlsx")
                || fileName.endsWith(".xls")
                || fileName.endsWith(".xlsm")
                || fileName.endsWith(".csv"));
    }

    private void setDropZoneState(String styleClass) {
        clearDropZoneState();
        dropZone.getStyleClass().add(styleClass);
    }

    private void clearDropZoneState() {
        dropZone.getStyleClass().removeAll("drop-zone-active", "drop-zone-invalid");
    }
}
