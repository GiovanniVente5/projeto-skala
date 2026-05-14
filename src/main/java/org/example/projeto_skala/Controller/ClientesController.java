package org.example.projeto_skala.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import org.example.projeto_skala.Json.JsonCriar;
import org.example.projeto_skala.SkalaApplication;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

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

    @FXML
    private void adicionarCliente() {
        Empresas novaEmpresa = new Empresas("Novo Cliente", 0, "", "", "", "", new java.util.ArrayList<>());
        abrirEditDialog(novaEmpresa, true);
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

    private TitledPane criarCardCliente(Empresas empresa) {
        TitledPane card = new TitledPane();
        card.getStyleClass().add("cliente-dropdown");
        card.setAnimated(true);
        card.setCollapsible(true);
        card.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        card.setExpanded(false);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setText("");

        HBox cabecalho = new HBox(12);
        cabecalho.getStyleClass().add("cliente-header");
        cabecalho.setMaxWidth(Double.MAX_VALUE);
        cabecalho.prefWidthProperty().bind(card.widthProperty().subtract(56));

        Label nome = new Label(empresa.getNome());
        nome.getStyleClass().add("cliente-nome");
        nome.setWrapText(true);
        HBox.setHgrow(nome, Priority.ALWAYS);

        Label numero = new Label("No " + empresa.getNum());
        numero.getStyleClass().add("cliente-numero");

        Button editarBtn = new Button("Editar");
        editarBtn.setStyle("-fx-padding: 2 6; -fx-font-size: 11;");
        editarBtn.setOnAction(e -> abrirEditDialog(empresa, false));

        Button excluirBtn = new Button("Excluir");
        excluirBtn.setStyle("-fx-padding: 2 6; -fx-font-size: 11; -fx-text-fill: #d32f2f;");
        excluirBtn.setOnAction(e -> excluirCliente(empresa));

        cabecalho.getChildren().addAll(nome, numero, editarBtn, excluirBtn);

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

        card.setGraphic(cabecalho);
        card.setContent(servicosBox);
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

    private void abrirEditDialog(Empresas empresa, boolean isNovoCliente) {
        try {
            Dialog<javafx.scene.control.ButtonType> dialog = new Dialog<>();
            dialog.setTitle(isNovoCliente ? "Novo Cliente" : "Editar Cliente");
            
            VBox content = new VBox(12);
            content.setStyle("-fx-padding: 15;");
            
            HBox nomeBox = new HBox(10);
            Label nomeLbl = new Label("Nome:");
            nomeLbl.setPrefWidth(80);
            TextField nomeField = new TextField();
            nomeField.setText(empresa.getNome());
            nomeField.setPrefWidth(300);
            nomeBox.getChildren().addAll(nomeLbl, nomeField);
            
            HBox numBox = new HBox(10);
            Label numLbl = new Label("Número:");
            numLbl.setPrefWidth(80);
            TextField numField = new TextField();
            numField.setText(String.valueOf(empresa.getNum()));
            numField.setPrefWidth(100);
            numBox.getChildren().addAll(numLbl, numField);
            
            Label servicosLbl = new Label("Serviços:");
            servicosLbl.setStyle("-fx-font-weight: bold;");
            
            VBox servicosContainer = new VBox(8);
            servicosContainer.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-padding: 8;");
            
            java.util.List<ClienteEditItem> servicoItems = new ArrayList<>();
            
            if (empresa.getServicos() != null) {
                for (Servicos servico : empresa.getServicos()) {
                    HBox servicoRow = criarLinhaServicoEdit(servico, servicosContainer, servicoItems);
                    servicosContainer.getChildren().add(servicoRow);
                }
            }
            
            Button adicionarServicoBtn = new Button("+ Adicionar Serviço");
            adicionarServicoBtn.setStyle("-fx-padding: 6;");
            adicionarServicoBtn.setOnAction(e -> {
                HBox servicoRow = criarLinhaServicoEdit(new Servicos("", 0, 0.0), servicosContainer, servicoItems);
                servicosContainer.getChildren().add(servicoRow);
            });
            
            content.getChildren().addAll(
                nomeBox, numBox, new Separator(),
                servicosLbl, servicosContainer, adicionarServicoBtn
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
            
            Optional<javafx.scene.control.ButtonType> resultado = dialog.showAndWait();
            if (resultado.isPresent() && resultado.get() == javafx.scene.control.ButtonType.OK) {
                String nome = nomeField.getText();
                int numero;
                try {
                    numero = Integer.parseInt(numField.getText());
                } catch (NumberFormatException e) {
                    numero = empresa.getNum();
                }
                
                List<Servicos> servicos = new ArrayList<>();
                for (ClienteEditItem item : servicoItems) {
                    try {
                        String nomeServ = item.nomeField.getText();
                        int numServ = Integer.parseInt(item.numField.getText());
                        Double valorServ = Double.parseDouble(item.valorField.getText());
                        servicos.add(new Servicos(nomeServ, numServ, valorServ));
                    } catch (NumberFormatException e) {
                        // Skip invalid
                    }
                }
                
                Empresas empresaAtualizada = new Empresas(
                    nome, numero, empresa.endereco, empresa.CNPJ, 
                    empresa.InscrCCM, empresa.InscrEST, servicos
                );
                
                LocalDate hoje = LocalDate.now();
                JsonCriar.salvar(java.util.Arrays.asList(empresaAtualizada), hoje.getMonthValue(), hoje.getYear());
                
                carregarClientes();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setContentText("Erro ao abrir diálogo de edição: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private HBox criarLinhaServicoEdit(Servicos servico, VBox container, java.util.List<ClienteEditItem> items) {
        HBox linha = new HBox(8);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setStyle("-fx-border-color: #f0f0f0; -fx-border-radius: 4; -fx-padding: 6;");
        
        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome");
        nomeField.setText(servico.getNome() == null ? "" : servico.getNome());
        nomeField.setPrefWidth(180);
        
        TextField numField = new TextField();
        numField.setPromptText("Num");
        numField.setText(String.valueOf(servico.getNum()));
        numField.setPrefWidth(60);
        
        TextField valorField = new TextField();
        valorField.setPromptText("Valor");
        valorField.setText(String.valueOf(servico.getValor()));
        valorField.setPrefWidth(80);
        
        ClienteEditItem item = new ClienteEditItem(nomeField, numField, valorField);
        items.add(item);
        
        Button removerBtn = new Button("X");
        removerBtn.setStyle("-fx-padding: 2 6; -fx-font-size: 11;");
        removerBtn.setOnAction(e -> {
            container.getChildren().remove(linha);
            items.remove(item);
        });
        
        linha.getChildren().addAll(nomeField, numField, valorField, removerBtn);
        return linha;
    }
    
    private static class ClienteEditItem {
        TextField nomeField;
        TextField numField;
        TextField valorField;
        
        ClienteEditItem(TextField nomeField, TextField numField, TextField valorField) {
            this.nomeField = nomeField;
            this.numField = numField;
            this.valorField = valorField;
        }
    }

    private void excluirCliente(Empresas empresa) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir Cliente");
        confirmacao.setContentText("Tem certeza que deseja excluir o cliente '" + empresa.getNome() + "'?");

        Optional<javafx.scene.control.ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == javafx.scene.control.ButtonType.OK) {
            JsonCriar.excluirCliente(empresa.getNum());
            carregarClientes();
        }
    }
}
