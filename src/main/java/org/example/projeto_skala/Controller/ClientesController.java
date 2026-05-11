package org.example.projeto_skala.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.projeto_skala.Json.JsonCriar;
import org.example.projeto_skala.SkalaApplication;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClientesController {
    @FXML
    private VBox clientesContainer;

    @FXML
    private void initialize() {
        carregarClientes();
    }

    @FXML
    private void voltarMenu() throws IOException {
        Parent root = FXMLLoader.load(SkalaApplication.class.getResource("MenuView.fxml"));
        clientesContainer.getScene().setRoot(root);
    }

    @FXML
    private void atualizarClientes() {
        carregarClientes();
    }

    private void carregarClientes() {
        clientesContainer.getChildren().clear();

        Map<String, List<Empresas>> empresasPorPeriodo = JsonCriar.carregarPorPeriodo();

        if (empresasPorPeriodo.isEmpty()) {
            Label vazio = new Label("Nenhum cliente salvo no JSON.");
            vazio.getStyleClass().add("empty-state");
            clientesContainer.getChildren().add(vazio);
            return;
        }

        for (Map.Entry<String, List<Empresas>> periodo : empresasPorPeriodo.entrySet()) {
            Label periodoLabel = new Label("Referencia: " + periodo.getKey());
            periodoLabel.getStyleClass().add("periodo-title");
            clientesContainer.getChildren().add(periodoLabel);

            for (Empresas empresa : periodo.getValue()) {
                clientesContainer.getChildren().add(criarCardCliente(empresa));
            }
        }
    }

    private VBox criarCardCliente(Empresas empresa) {
        VBox card = new VBox(10);
        card.getStyleClass().add("cliente-card");

        HBox cabecalho = new HBox(12);
        cabecalho.getStyleClass().add("cliente-header");

        Label nome = new Label(empresa.getNome());
        nome.getStyleClass().add("cliente-nome");
        nome.setWrapText(true);
        HBox.setHgrow(nome, Priority.ALWAYS);

        Label numero = new Label("No " + empresa.getNum());
        numero.getStyleClass().add("cliente-numero");

        cabecalho.getChildren().addAll(nome, numero);

        VBox servicosBox = new VBox(6);
        servicosBox.getStyleClass().add("servicos-lista");

        List<Servicos> servicos = empresa.getServicos();

        if (servicos == null || servicos.isEmpty()) {
            Label semServicos = new Label("Nenhum servico aplicado.");
            semServicos.getStyleClass().add("servico-vazio");
            servicosBox.getChildren().add(semServicos);
        } else {
            for (Servicos servico : servicos) {
                servicosBox.getChildren().add(criarLinhaServico(servico));
            }
        }

        card.getChildren().addAll(cabecalho, servicosBox);
        return card;
    }

    private HBox criarLinhaServico(Servicos servico) {
        HBox linha = new HBox(10);
        linha.getStyleClass().add("servico-linha");

        Label nome = new Label(servico.getNome() == null ? "Servico sem nome" : servico.getNome());
        nome.getStyleClass().add("servico-nome");
        nome.setWrapText(true);
        HBox.setHgrow(nome, Priority.ALWAYS);

        Label numero = new Label(String.valueOf(servico.getNum()));
        numero.getStyleClass().add("servico-numero");

        Label valor = new Label(formatarValor(servico.getValor()));
        valor.getStyleClass().add("servico-valor");

        linha.getChildren().addAll(nome, numero, valor);
        return linha;
    }

    private String formatarValor(Double valor) {
        if (valor == null) {
            return "-";
        }

        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valor);
    }
}
