package org.example.projeto_skala.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.util.Pair;
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
    private void initialize() {
        configureFileDropZone();
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
        // show modal dialog to select month and year when a spreadsheet is dropped
        Pair<Integer, Integer> resultado = promptForMonthYear(file);
        if (resultado == null) {
            // user cancelled
            selectedFileLabel.setText("Import cancelled.");
            return false;
        }

        int mes = resultado.getKey();
        int ano = resultado.getValue();

        selectedFileLabel.setText(file.getName() + " - " + String.format("%02d/%04d", mes, ano));
        List<Empresas> linhas = LerExcel.lerExcel(file);
        JsonCriar.salvar(linhas, mes, ano);
        JsonServicos.sincronizarComEmpresas(linhas);

        return true;
    }

    private Pair<Integer,Integer> promptForMonthYear(File file) {
        Dialog<Pair<Integer,Integer>> dialog = new Dialog<>();
        dialog.setTitle("Selecionar referência");
        dialog.setHeaderText("Selecione mês e ano para: " + file.getName());

        ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        ComboBox<String> mesBox = new ComboBox<>();
        mesBox.getItems().setAll(MESES);
        mesBox.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);

        Spinner<Integer> anoBox = new Spinner<>();
        anoBox.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                LocalDate.now().getYear() - 20,
                LocalDate.now().getYear() + 20,
                LocalDate.now().getYear()
        ));
        anoBox.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Mês:"), 0, 0);
        grid.add(mesBox, 1, 0);
        grid.add(new Label("Ano:"), 0, 1);
        grid.add(anoBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node okButton = dialog.getDialogPane().lookupButton(okType);
        okButton.setDisable(false);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okType) {
                try {
                    int ano = Integer.parseInt(anoBox.getEditor().getText());
                    return new Pair<>(mesBox.getSelectionModel().getSelectedIndex() + 1, ano);
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }


    @FXML
    private void abrirTelaClientes() throws IOException {
        Parent root = FXMLLoader.load(SkalaApplication.class.getResource("ClientesView.fxml"));
        dropZone.getScene().setRoot(root);
    }

    @FXML
    private void abrirTelaRelatorios() throws IOException {
        Parent root = FXMLLoader.load(SkalaApplication.class.getResource("RelatoriosView.fxml"));
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
