package org.example.projeto_skala.Controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.projeto_skala.Json.JsonReciboConfig;
import org.example.projeto_skala.SkalaApplication;
import org.example.projeto_skala.objetos.ReciboCampoConfig;
import org.example.projeto_skala.objetos.ReciboConfig;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ReciboTemplateController {
    private static final float PREVIEW_DPI = 120f;

    @FXML
    private ImageView pdfImageView;

    @FXML
    private Pane overlayPane;

    @FXML
    private StackPane pageStack;

    @FXML
    private ComboBox<JsonReciboConfig.CampoModelo> campoCombo;

    @FXML
    private TextField xField;

    @FXML
    private TextField yField;

    @FXML
    private TextField larguraField;

    @FXML
    private TextField alturaField;

    @FXML
    private Label templateLabel;

    @FXML
    private Label statusLabel;

    private ReciboConfig config;
    private final Map<String, Rectangle> retangulos = new LinkedHashMap<>();
    private double pageWidth = 595;
    private double pageHeight = 842;
    private double displayWidth = 595;
    private double displayHeight = 842;
    private double dragStartX;
    private double dragStartY;
    private Rectangle draftRectangle;
    private String movingCampoId;
    private double moveStartSceneX;
    private double moveStartSceneY;
    private ReciboCampoConfig moveStartConfig;
    private boolean atualizandoCampos;

    @FXML
    private void initialize() {
        config = JsonReciboConfig.carregarOuCriarPadrao();

        campoCombo.getItems().setAll(JsonReciboConfig.listarCampos());
        campoCombo.getSelectionModel().selectedItemProperty().addListener((obs, antigo, atual) -> selecionarCampo(atual));

        configurarCampoNumerico(xField);
        configurarCampoNumerico(yField);
        configurarCampoNumerico(larguraField);
        configurarCampoNumerico(alturaField);
        configurarOverlay();

        renderizarTemplate();
        desenharRetangulos();

        if (!campoCombo.getItems().isEmpty()) {
            campoCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void voltarMenu() throws IOException {
        Parent root = FXMLLoader.load(SkalaApplication.class.getResource("MenuView.fxml"));
        pageStack.getScene().setRoot(root);
    }

    @FXML
    private void selecionarTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar PDF do recibo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File atual = getTemplateFile();
        if (atual != null && atual.getParentFile() != null && atual.getParentFile().isDirectory()) {
            fileChooser.setInitialDirectory(atual.getParentFile());
        }

        Window window = pageStack.getScene() == null ? null : pageStack.getScene().getWindow();
        File escolhido = fileChooser.showOpenDialog(window);

        if (escolhido == null) {
            return;
        }

        config.setTemplatePath(escolhido.getAbsolutePath());
        renderizarTemplate();
        desenharRetangulos();
        atualizarTemplateLabel();
        statusLabel.setText("Template selecionado. Salve para manter a alteracao.");
    }

    @FXML
    private void restaurarPadrao() {
        config = JsonReciboConfig.criarPadrao();
        renderizarTemplate();
        desenharRetangulos();
        selecionarCampo(campoCombo.getSelectionModel().getSelectedItem());
        statusLabel.setText("Configuracao padrao restaurada. Salve para confirmar.");
    }

    @FXML
    private void salvarConfiguracao() {
        aplicarCamposManuais();
        JsonReciboConfig.salvar(config);
        statusLabel.setText("Configuracao salva em " + JsonReciboConfig.getArquivoConfig().getPath());
    }

    private void configurarCampoNumerico(TextField field) {
        field.setOnAction(event -> aplicarCamposManuais());
        field.focusedProperty().addListener((obs, estavaFocado, estaFocado) -> {
            if (!estaFocado) {
                aplicarCamposManuais();
            }
        });
    }

    private void configurarOverlay() {
        overlayPane.setPadding(Insets.EMPTY);

        overlayPane.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() != MouseButton.PRIMARY || event.getTarget() instanceof Rectangle) {
                return;
            }

            dragStartX = limitar(event.getX(), 0, displayWidth);
            dragStartY = limitar(event.getY(), 0, displayHeight);
            draftRectangle = criarRetanguloVisual(true);
            draftRectangle.setX(dragStartX);
            draftRectangle.setY(dragStartY);
            draftRectangle.setWidth(0);
            draftRectangle.setHeight(0);
            overlayPane.getChildren().add(draftRectangle);
            event.consume();
        });

        overlayPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (draftRectangle == null) {
                return;
            }

            double atualX = limitar(event.getX(), 0, displayWidth);
            double atualY = limitar(event.getY(), 0, displayHeight);
            double x = Math.min(dragStartX, atualX);
            double y = Math.min(dragStartY, atualY);

            draftRectangle.setX(x);
            draftRectangle.setY(y);
            draftRectangle.setWidth(Math.abs(atualX - dragStartX));
            draftRectangle.setHeight(Math.abs(atualY - dragStartY));
            event.consume();
        });

        overlayPane.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (draftRectangle == null) {
                return;
            }

            JsonReciboConfig.CampoModelo campo = campoCombo.getSelectionModel().getSelectedItem();
            if (campo != null && draftRectangle.getWidth() > 2 && draftRectangle.getHeight() > 2) {
                ReciboCampoConfig novoCampo = converterParaPdf(
                        draftRectangle.getX(),
                        draftRectangle.getY(),
                        draftRectangle.getWidth(),
                        draftRectangle.getHeight()
                );
                config.setCampo(campo.id(), novoCampo);
                preencherCampos(novoCampo);
            }

            overlayPane.getChildren().remove(draftRectangle);
            draftRectangle = null;
            desenharRetangulos();
            statusLabel.setText("Retangulo atualizado. Salve para manter a alteracao.");
            event.consume();
        });
    }

    private void renderizarTemplate() {
        try (InputStream inputStream = abrirTemplate(); PDDocument document = PDDocument.load(inputStream)) {
            PDRectangle mediaBox = document.getPage(0).getMediaBox();
            pageWidth = mediaBox.getWidth();
            pageHeight = mediaBox.getHeight();

            BufferedImage bufferedImage = new PDFRenderer(document).renderImageWithDPI(0, PREVIEW_DPI);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);

            displayWidth = image.getWidth();
            displayHeight = image.getHeight();
            pdfImageView.setImage(image);
            pdfImageView.setFitWidth(displayWidth);
            pdfImageView.setFitHeight(displayHeight);
            overlayPane.setMinSize(displayWidth, displayHeight);
            overlayPane.setPrefSize(displayWidth, displayHeight);
            overlayPane.setMaxSize(displayWidth, displayHeight);
            pageStack.setMinSize(displayWidth, displayHeight);
            pageStack.setPrefSize(displayWidth, displayHeight);
            pageStack.setMaxSize(displayWidth, displayHeight);
            atualizarTemplateLabel();
        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarErro("Nao foi possivel abrir o PDF do template: " + ex.getMessage());
        }
    }

    private InputStream abrirTemplate() throws IOException {
        File templateFile = getTemplateFile();

        if (templateFile != null && templateFile.isFile()) {
            return new FileInputStream(templateFile);
        }

        InputStream inputStream = ReciboTemplateController.class.getResourceAsStream(JsonReciboConfig.DEFAULT_TEMPLATE_RESOURCE);
        if (inputStream == null) {
            throw new FileNotFoundException("Template padrao nao encontrado.");
        }

        return inputStream;
    }

    private File getTemplateFile() {
        String caminho = config.getTemplatePath();
        if (caminho == null || caminho.isBlank()) {
            return null;
        }

        return new File(caminho);
    }

    private void desenharRetangulos() {
        overlayPane.getChildren().clear();
        retangulos.clear();

        JsonReciboConfig.CampoModelo selecionado = campoCombo.getSelectionModel().getSelectedItem();

        for (JsonReciboConfig.CampoModelo campo : JsonReciboConfig.listarCampos()) {
            ReciboCampoConfig campoConfig = config.getCampo(campo.id());
            if (campoConfig == null) {
                continue;
            }

            Rectangle rectangle = criarRetanguloVisual(selecionado != null && selecionado.id().equals(campo.id()));
            atualizarRetangulo(rectangle, campoConfig);
            rectangle.setOnMouseClicked(event -> {
                campoCombo.getSelectionModel().select(campo);
                event.consume();
            });
            rectangle.setOnMousePressed(event -> iniciarMovimento(campo, event));
            rectangle.setOnMouseDragged(this::moverRetangulo);
            rectangle.setOnMouseReleased(event -> finalizarMovimento(event));
            Tooltip.install(rectangle, new Tooltip(campo.label()));

            retangulos.put(campo.id(), rectangle);
            overlayPane.getChildren().add(rectangle);
        }
    }

    private Rectangle criarRetanguloVisual(boolean ativo) {
        Rectangle rectangle = new Rectangle();
        rectangle.setFill(ativo ? Color.rgb(100, 199, 132, 0.26) : Color.rgb(92, 197, 250, 0.16));
        rectangle.setStroke(ativo ? Color.rgb(100, 199, 132) : Color.rgb(92, 197, 250));
        rectangle.setStrokeWidth(ativo ? 2.2 : 1.4);
        rectangle.setArcWidth(2);
        rectangle.setArcHeight(2);
        return rectangle;
    }

    private void iniciarMovimento(JsonReciboConfig.CampoModelo campo, MouseEvent event) {
        campoCombo.getSelectionModel().select(campo);
        movingCampoId = campo.id();
        moveStartSceneX = event.getSceneX();
        moveStartSceneY = event.getSceneY();
        ReciboCampoConfig atual = config.getCampo(campo.id());
        moveStartConfig = atual == null ? JsonReciboConfig.getCampoPadrao(campo.id()) : atual.copiar();
        event.consume();
    }

    private void moverRetangulo(MouseEvent event) {
        if (movingCampoId == null || moveStartConfig == null) {
            return;
        }

        double dx = (event.getSceneX() - moveStartSceneX) / getScaleX();
        double dy = (event.getSceneY() - moveStartSceneY) / getScaleY();
        ReciboCampoConfig movido = new ReciboCampoConfig(
                moveStartConfig.getX() + dx,
                moveStartConfig.getY() - dy,
                moveStartConfig.getLargura(),
                moveStartConfig.getAltura()
        );

        movido = limitarAoPdf(movido);
        config.setCampo(movingCampoId, movido);

        Rectangle rectangle = retangulos.get(movingCampoId);
        if (rectangle != null) {
            atualizarRetangulo(rectangle, movido);
        }
        preencherCampos(movido);
        event.consume();
    }

    private void finalizarMovimento(MouseEvent event) {
        if (movingCampoId != null) {
            statusLabel.setText("Retangulo atualizado. Salve para manter a alteracao.");
        }

        movingCampoId = null;
        moveStartConfig = null;
        event.consume();
    }

    private void selecionarCampo(JsonReciboConfig.CampoModelo campo) {
        if (campo == null || config == null) {
            return;
        }

        ReciboCampoConfig campoConfig = config.getCampo(campo.id());
        if (campoConfig == null) {
            campoConfig = JsonReciboConfig.getCampoPadrao(campo.id());
            config.setCampo(campo.id(), campoConfig);
        }

        preencherCampos(campoConfig);
        desenharRetangulos();
    }

    private void preencherCampos(ReciboCampoConfig campoConfig) {
        atualizandoCampos = true;
        xField.setText(formatarNumero(campoConfig.getX()));
        yField.setText(formatarNumero(campoConfig.getY()));
        larguraField.setText(formatarNumero(campoConfig.getLargura()));
        alturaField.setText(formatarNumero(campoConfig.getAltura()));
        atualizandoCampos = false;
    }

    private void aplicarCamposManuais() {
        if (atualizandoCampos || config == null) {
            return;
        }

        JsonReciboConfig.CampoModelo campo = campoCombo.getSelectionModel().getSelectedItem();
        if (campo == null) {
            return;
        }

        try {
            ReciboCampoConfig novoCampo = new ReciboCampoConfig(
                    parseNumero(xField.getText()),
                    parseNumero(yField.getText()),
                    parseNumero(larguraField.getText()),
                    parseNumero(alturaField.getText())
            );
            novoCampo = limitarAoPdf(novoCampo);
            config.setCampo(campo.id(), novoCampo);
            preencherCampos(novoCampo);
            desenharRetangulos();
        } catch (NumberFormatException ex) {
            statusLabel.setText("Coordenadas invalidas.");
        }
    }

    private ReciboCampoConfig converterParaPdf(double x, double y, double largura, double altura) {
        double pdfX = x / getScaleX();
        double pdfLargura = largura / getScaleX();
        double pdfAltura = altura / getScaleY();
        double pdfY = pageHeight - ((y + altura) / getScaleY());

        return limitarAoPdf(new ReciboCampoConfig(pdfX, pdfY, pdfLargura, pdfAltura));
    }

    private void atualizarRetangulo(Rectangle rectangle, ReciboCampoConfig campo) {
        double scaleX = getScaleX();
        double scaleY = getScaleY();

        rectangle.setX(campo.getX() * scaleX);
        rectangle.setY((pageHeight - campo.getY() - campo.getAltura()) * scaleY);
        rectangle.setWidth(campo.getLargura() * scaleX);
        rectangle.setHeight(campo.getAltura() * scaleY);
    }

    private ReciboCampoConfig limitarAoPdf(ReciboCampoConfig campo) {
        double largura = limitar(campo.getLargura(), 1, pageWidth);
        double altura = limitar(campo.getAltura(), 1, pageHeight);
        double x = limitar(campo.getX(), 0, pageWidth - largura);
        double y = limitar(campo.getY(), 0, pageHeight - altura);

        return new ReciboCampoConfig(x, y, largura, altura);
    }

    private double getScaleX() {
        return displayWidth / pageWidth;
    }

    private double getScaleY() {
        return displayHeight / pageHeight;
    }

    private double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    private double parseNumero(String texto) {
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }

    private String formatarNumero(double valor) {
        return String.format(Locale.US, "%.1f", valor);
    }

    private void atualizarTemplateLabel() {
        File templateFile = getTemplateFile();
        if (templateFile == null) {
            templateLabel.setText("Template: padrao interno");
        } else {
            templateLabel.setText("Template: " + templateFile.getAbsolutePath());
        }
    }

    private void mostrarErro(String mensagem) {
        statusLabel.setText(mensagem);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Template do recibo");
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
