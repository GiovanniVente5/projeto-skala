package org.example.projeto_skala.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.projeto_skala.Json.JsonServicos;
import org.example.projeto_skala.SkalaApplication;
import org.example.projeto_skala.objetos.Servicos;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServicosController {
    @FXML
    private VBox servicosContainer;

    @FXML
    private TextField numeroField;

    @FXML
    private TextField nomeField;

    @FXML
    private TextField valorField;

    @FXML
    private Label mensagemLabel;

    @FXML
    private void initialize() {
        carregarServicos();
    }

    @FXML
    private void voltarMenu() throws IOException {
        Parent root = FXMLLoader.load(SkalaApplication.class.getResource("MenuView.fxml"));
        servicosContainer.getScene().setRoot(root);
    }

    @FXML
    private void atualizarServicos() {
        carregarServicos();
    }

    @FXML
    private void adicionarServico() {
        Integer numero = lerNumero();
        String nome = nomeField.getText().trim();
        Double valor = lerValor();

        if (numero == null || nome.isEmpty() || valor == null) {
            mensagemLabel.setText("Preencha numero, nome e valor corretamente.");
            return;
        }

        JsonServicos.adicionar(new Servicos(nome, numero, valor));
        numeroField.clear();
        nomeField.clear();
        valorField.clear();
        mensagemLabel.setText("Servico salvo.");
        carregarServicos();
    }

    private void carregarServicos() {
        servicosContainer.getChildren().clear();

        List<Servicos> servicos = JsonServicos.carregar();

        if (servicos.isEmpty()) {
            Label vazio = new Label("Nenhum servico cadastrado.");
            vazio.getStyleClass().add("empty-state");
            servicosContainer.getChildren().add(vazio);
            return;
        }

        for (Servicos servico : servicos) {
            servicosContainer.getChildren().add(criarLinhaServico(servico));
        }
    }

    private HBox criarLinhaServico(Servicos servico) {
        HBox linha = new HBox(10);
        linha.getStyleClass().add("servico-cadastro-linha");

        Label numero = new Label(String.valueOf(servico.getNum()));
        numero.getStyleClass().add("servico-numero");

        Label nome = new Label(servico.getNome() == null ? "Servico sem nome" : servico.getNome());
        nome.getStyleClass().add("servico-nome");
        nome.setWrapText(true);
        HBox.setHgrow(nome, Priority.ALWAYS);

        Label valor = new Label(formatarValor(servico.getValor()));
        valor.getStyleClass().add("servico-valor");

        javafx.scene.control.Button remover = new javafx.scene.control.Button("Remover");
        remover.getStyleClass().add("danger-button");
        remover.setOnAction(event -> removerServico(servico.getNum()));

        linha.getChildren().addAll(numero, nome, valor, remover);
        return linha;
    }

    private void removerServico(int numero) {
        JsonServicos.remover(numero);
        mensagemLabel.setText("Servico removido.");
        carregarServicos();
    }

    private Integer lerNumero() {
        try {
            return Integer.parseInt(numeroField.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double lerValor() {
        try {
            String texto = valorField.getText().trim().replace(",", ".");
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatarValor(Double valor) {
        if (valor == null) {
            return "-";
        }

        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valor);
    }
}
