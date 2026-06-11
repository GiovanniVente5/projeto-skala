package org.example.projeto_skala.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.util.StringConverter;
import org.example.projeto_skala.Json.JsonCriar;
import org.example.projeto_skala.SkalaApplication;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

public class ClientesController {
    @FXML
    private VBox clientesContainer;

    @FXML
    private ComboBox<String> periodoComboBox;

    @FXML
    private void initialize() {
        carregarPeriodos();
        carregarClientes();
        periodoComboBox.setOnAction(e -> carregarClientes());
    }

    @FXML
    private void voltarMenu() throws IOException {
        Parent root = FXMLLoader.load(SkalaApplication.class.getResource("MenuView.fxml"));
        clientesContainer.getScene().setRoot(root);
    }

    @FXML
    private void atualizarClientes() {
        carregarPeriodos();
        carregarClientes();
    }

    private void carregarPeriodos() {
        Map<String, List<Empresas>> empresasPorPeriodo = JsonCriar.carregarPorPeriodo();
        java.util.List<String> periodos = new java.util.ArrayList<>(empresasPorPeriodo.keySet());
        java.util.Collections.sort(periodos);
        periodos.add(0, "Todos os períodos");
        periodoComboBox.getItems().setAll(periodos);
        periodoComboBox.getSelectionModel().selectFirst();
    }


    private void carregarClientes() {
        clientesContainer.getChildren().clear();

        Map<String, List<Empresas>> empresasPorPeriodo = JsonCriar.carregarPorPeriodo();

        if (empresasPorPeriodo.isEmpty()) {
            Label vazio = new Label("Nenhum cliente salvo no histórico.");
            vazio.getStyleClass().add("empty-state");
            clientesContainer.getChildren().add(vazio);
            return;
        }

        String selecionado = periodoComboBox == null ? "Todos os períodos" : periodoComboBox.getSelectionModel().getSelectedItem();

        if (selecionado == null || selecionado.equals("Todos os períodos")) {
            for (Map.Entry<String, List<Empresas>> periodo : empresasPorPeriodo.entrySet()) {
                Label periodoLabel = new Label("Referencia: " + periodo.getKey());
                periodoLabel.getStyleClass().add("periodo-title");
                clientesContainer.getChildren().add(periodoLabel);

                for (Empresas empresa : periodo.getValue()) {
                    clientesContainer.getChildren().add(criarCardCliente(empresa, periodo.getKey()));
                }
            }
        } else {
            List<Empresas> lista = empresasPorPeriodo.get(selecionado);
            if (lista == null || lista.isEmpty()) {
                Label vazio = new Label("Nenhum cliente salvo para o período selecionado.");
                vazio.getStyleClass().add("empty-state");
                clientesContainer.getChildren().add(vazio);
                return;
            }

            Label periodoLabel = new Label("Referencia: " + selecionado);
            periodoLabel.getStyleClass().add("periodo-title");
            clientesContainer.getChildren().add(periodoLabel);

            for (Empresas empresa : lista) {
                clientesContainer.getChildren().add(criarCardCliente(empresa, selecionado));
            }
        }
    }

    private TitledPane criarCardCliente(Empresas empresa, String periodoReferencia) {
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
        editarBtn.setOnAction(e -> abrirEditDialog(empresa, periodoReferencia, false));

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

    private void abrirEditDialog(Empresas empresa, String periodoReferencia, boolean isNovoCliente) {
        try {
            Dialog<javafx.scene.control.ButtonType> dialog = new Dialog<>();
            dialog.setTitle(isNovoCliente ? "Novo Cliente" : "Editar Cliente");
            dialog.setResizable(true);

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

            TextField numFaturaField = criarCampoTexto(String.valueOf(empresa.getNumFatura()), 140);
            TextField diaVencimentoField = criarCampoTexto(String.valueOf(empresa.getDiaVencimento()), 140);
            ComboBox<Month> vencimentoMesField = criarCampoMes(empresa.getVencimentoMes());
            DatePicker emissaoField = new DatePicker(empresa.getEmissao() == null ? LocalDate.now() : empresa.getEmissao());
            emissaoField.setPrefWidth(180);
            TextField enderecoField = criarCampoTexto(empresa.getEndereco(), 300);
            TextField cnpjField = criarCampoTexto(empresa.getCNPJ(), 220);
            TextField inscrCCMField = criarCampoTexto(empresa.getInscrCCM(), 220);
            TextField inscrESTField = criarCampoTexto(empresa.getInscrEST(), 220);
            
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
                HBox servicoRow = criarLinhaServicoEdit(new Servicos("", 0, null), servicosContainer, servicoItems);
                servicosContainer.getChildren().add(servicoRow);
            });
            
            content.getChildren().addAll(
                nomeBox,
                numBox,
                criarLinhaCampo("Numero da fatura:", numFaturaField),
                criarLinhaCampo("Dia vencimento:", diaVencimentoField),
                criarLinhaCampo("Mes vencimento:", vencimentoMesField),
                criarLinhaCampo("Emissao:", emissaoField),
                criarLinhaCampo("Endereco:", enderecoField),
                criarLinhaCampo("CNPJ:", cnpjField),
                criarLinhaCampo("Inscr. CCM:", inscrCCMField),
                criarLinhaCampo("Inscr. EST:", inscrESTField),
                new Separator(),
                servicosLbl, servicosContainer, adicionarServicoBtn
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
            
            Button salvarBtn = (Button) dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
            salvarBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                try {
                    Empresas empresaAtualizada = new Empresas(
                            empresa.getId(),
                            obterTexto(nomeField),
                            parseIntCampo(numField, "Numero"),
                            parseLongCampo(numFaturaField, "Numero da fatura"),
                            parseDiaVencimento(diaVencimentoField),
                            obterMesVencimento(vencimentoMesField),
                            obterEmissao(emissaoField),
                            obterTexto(enderecoField),
                            obterTexto(cnpjField),
                            obterTexto(inscrCCMField),
                            obterTexto(inscrESTField),
                            coletarServicos(servicoItems)
                    );

                    salvarEmpresaAtualizada(empresaAtualizada, periodoReferencia);
                    carregarClientes();
                } catch (IllegalArgumentException e) {
                    event.consume();
                    mostrarErroValidacao(e.getMessage());
                }
            });

            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setContentText("Erro ao abrir diálogo de edição: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private TextField criarCampoTexto(String valor, double largura) {
        TextField campo = new TextField(valor == null ? "" : valor);
        campo.setPrefWidth(largura);
        return campo;
    }

    private ComboBox<Month> criarCampoMes(Month mesSelecionado) {
        ComboBox<Month> campo = new ComboBox<>();
        campo.getItems().setAll(Month.values());
        campo.setPrefWidth(180);
        campo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Month mes) {
                return mes == null ? "" : formatarMes(mes);
            }

            @Override
            public Month fromString(String texto) {
                return null;
            }
        });
        campo.getSelectionModel().select(mesSelecionado == null ? LocalDate.now().getMonth() : mesSelecionado);
        return campo;
    }

    private String formatarMes(Month mes) {
        return String.format("%02d - %s", mes.getValue(), mes.getDisplayName(TextStyle.FULL, Locale.of("pt", "BR")));
    }

    private HBox criarLinhaCampo(String rotulo, Node campo) {
        HBox linha = new HBox(10);
        linha.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(rotulo);
        label.setPrefWidth(120);
        linha.getChildren().addAll(label, campo);
        return linha;
    }

    private int parseIntCampo(TextField campo, String nomeCampo) {
        String valor = obterTexto(campo);

        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe um numero inteiro valido para " + nomeCampo + ".");
        }
    }

    private long parseLongCampo(TextField campo, String nomeCampo) {
        String valor = obterTexto(campo);

        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe um numero valido para " + nomeCampo + ".");
        }
    }

    private double parseDoubleCampo(TextField campo, String nomeCampo) {
        String valor = obterTexto(campo).replace(",", ".");

        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe um valor valido para " + nomeCampo + ".");
        }
    }

    private int parseDiaVencimento(TextField campo) {
        int dia = parseIntCampo(campo, "Dia vencimento");

        if (dia < 1 || dia > 31) {
            throw new IllegalArgumentException("Dia vencimento deve estar entre 1 e 31.");
        }

        return dia;
    }

    private Month obterMesVencimento(ComboBox<Month> campo) {
        Month mes = campo.getValue();

        if (mes == null) {
            throw new IllegalArgumentException("Selecione o mes de vencimento.");
        }

        return mes;
    }

    private LocalDate obterEmissao(DatePicker campo) {
        LocalDate emissao = campo.getValue();

        if (emissao == null) {
            throw new IllegalArgumentException("Informe a data de emissao.");
        }

        return emissao;
    }

    private List<Servicos> coletarServicos(List<ClienteEditItem> servicoItems) {
        List<Servicos> servicos = new ArrayList<>();

        for (ClienteEditItem item : servicoItems) {
            String nomeServ = obterTexto(item.nomeField);
            String numServ = obterTexto(item.numField);
            String valorServ = obterTexto(item.valorField);

            if (nomeServ.isBlank() && numServ.isBlank() && valorServ.isBlank()) {
                continue;
            }

            servicos.add(new Servicos(
                    nomeServ,
                    parseIntCampo(item.numField, "Numero do servico"),
                    parseDoubleCampo(item.valorField, "Valor do servico")
            ));
        }

        return servicos;
    }

    private void salvarEmpresaAtualizada(Empresas empresaAtualizada, String periodoReferencia) {
        LocalDate referencia = LocalDate.now();
        int mes = referencia.getMonthValue();
        int ano = referencia.getYear();

        if (periodoReferencia != null) {
            String[] partes = periodoReferencia.split("/");

            if (partes.length == 2) {
                try {
                    mes = Integer.parseInt(partes[0]);
                    ano = Integer.parseInt(partes[1]);
                } catch (NumberFormatException ignored) {
                    mes = referencia.getMonthValue();
                    ano = referencia.getYear();
                }
            }
        }

        JsonCriar.salvar(Collections.singletonList(empresaAtualizada), mes, ano);
    }

    private String obterTexto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private void mostrarErroValidacao(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Dados invalidos");
        alert.setHeaderText("Nao foi possivel salvar");
        alert.setContentText(mensagem);
        alert.showAndWait();
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
        numField.setText(servico.getNum() == 0 ? "" : String.valueOf(servico.getNum()));
        numField.setPrefWidth(60);
        
        TextField valorField = new TextField();
        valorField.setPromptText("Valor");
        valorField.setText(servico.getValor() == null ? "" : String.valueOf(servico.getValor()));
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
