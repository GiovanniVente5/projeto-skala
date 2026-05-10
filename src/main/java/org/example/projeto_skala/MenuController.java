package org.example.projeto_skala;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.example.projeto_skala.controleXLSX.LerExcel;
import org.example.projeto_skala.objetos.Empresas;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class MenuController {
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
                    selectSpreadsheetFile(spreadsheet);
                    success = true;
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

    private void selectSpreadsheetFile(File file) {
        selectedFileLabel.setText(file.getName());
        List<Empresas> linhas = LerExcel.lerExcel(file);
        JsonCriar.salvar(linhas);
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
