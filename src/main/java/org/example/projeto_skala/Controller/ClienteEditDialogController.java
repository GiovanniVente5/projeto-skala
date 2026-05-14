package org.example.projeto_skala.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import org.example.projeto_skala.SkalaApplication;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClienteEditDialogController {
    @FXML
    private TextField nomeField;
    @FXML
    private TextField numeroField;
    @FXML
    private VBox servicosContainer;

    private Empresas empresa;
    private List<ServicoEditItem> servicoItems = new ArrayList<>();

    public void setEmpresa(Empresas empresa) {
        this.empresa = empresa;
        nomeField.setText(empresa.getNome());
        numeroField.setText(String.valueOf(empresa.getNum()));
        
        servicosContainer.getChildren().clear();
        servicoItems.clear();

        if (empresa.getServicos() != null) {
            for (Servicos servico : empresa.getServicos()) {
                adicionarServico(servico);
            }
        }
    }

    @FXML
    private void adicionarServico() {
        adicionarServico(new Servicos("", 0, 0.0));
    }

    private void adicionarServico(Servicos servico) {
        HBox servicoRow = new HBox(10);
        servicoRow.setAlignment(Pos.CENTER_LEFT);
        servicoRow.setStyle("-fx-border-color: #f0f0f0; -fx-border-radius: 4; -fx-padding: 8;");

        TextField nomeServico = new TextField();
        nomeServico.setPromptText("Nome do serviço");
        nomeServico.setText(servico.getNome());
        nomeServico.setPrefWidth(200);

        TextField numServico = new TextField();
        numServico.setPromptText("Num");
        numServico.setText(String.valueOf(servico.getNum()));
        numServico.setPrefWidth(60);

        TextField valorServico = new TextField();
        valorServico.setPromptText("Valor");
        valorServico.setText(String.valueOf(servico.getValor()));
        valorServico.setPrefWidth(100);

        ServicoEditItem item = new ServicoEditItem(nomeServico, numServico, valorServico);

        Button removerBtn = new Button("Remover");
        removerBtn.setStyle("-fx-padding: 6;");
        removerBtn.setOnAction(e -> {
            servicosContainer.getChildren().remove(servicoRow);
            servicoItems.remove(item);
        });

        servicoRow.getChildren().addAll(
            new Label("Nome:"), nomeServico,
            new Label("Num:"), numServico,
            new Label("Valor:"), valorServico,
            removerBtn
        );

        servicosContainer.getChildren().add(servicoRow);
        servicoItems.add(item);
    }

    public Empresas getEmpresa() {
        String nome = nomeField.getText();
        int numero;
        try {
            numero = Integer.parseInt(numeroField.getText());
        } catch (NumberFormatException e) {
            numero = empresa.getNum();
        }

        List<Servicos> servicos = new ArrayList<>();
        for (ServicoEditItem item : servicoItems) {
            try {
                String nomeServ = item.nome.getText();
                int numServ = Integer.parseInt(item.numero.getText());
                Double valorServ = Double.parseDouble(item.valor.getText());
                servicos.add(new Servicos(nomeServ, numServ, valorServ));
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }

        return new Empresas(
            nome,
            numero,
            empresa.endereco,
            empresa.CNPJ,
            empresa.InscrCCM,
            empresa.InscrEST,
            servicos
        );
    }

    private static class ServicoEditItem {
        TextField nome;
        TextField numero;
        TextField valor;

        ServicoEditItem(TextField nome, TextField numero, TextField valor) {
            this.nome = nome;
            this.numero = numero;
            this.valor = valor;
        }
    }
}
