package org.example.projeto_skala.Json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.projeto_skala.objetos.ReciboCampoConfig;
import org.example.projeto_skala.objetos.ReciboConfig;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonReciboConfig {
    public static final String DATA_FOLDER = "data";
    public static final String CONFIG_FILE_NAME = "CONFIGURA\u00c7\u00d5ES.json";
    public static final String DEFAULT_TEMPLATE_RESOURCE = "/org/example/projeto_skala/controlePDF/PdfTemplate/Recibo-Template.pdf";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(DATA_FOLDER, CONFIG_FILE_NAME);

    private static final List<CampoModelo> CAMPOS = List.of(
            new CampoModelo("emissao", "Emissao", new ReciboCampoConfig(450, 737, 100, 12)),
            new CampoModelo("numeroFatura", "Numero da fatura", new ReciboCampoConfig(460, 724, 100, 12)),
            new CampoModelo("vencimento", "Vencimento", new ReciboCampoConfig(465, 710, 100, 12)),
            new CampoModelo("nomeCliente", "Nome do cliente", new ReciboCampoConfig(68, 638, 500, 24)),
            new CampoModelo("endereco", "Endereco", new ReciboCampoConfig(68, 630, 400, 12)),
            new CampoModelo("cnpj", "CNPJ", new ReciboCampoConfig(68, 620, 150, 12)),
            new CampoModelo("inscrEst", "Inscr. EST", new ReciboCampoConfig(207, 620, 150, 12)),
            new CampoModelo("inscrCcm", "Inscr. CCM", new ReciboCampoConfig(357, 620, 150, 12)),
            new CampoModelo("numeroEmpresa", "Numero da empresa", new ReciboCampoConfig(415, 594, 100, 12)),
            new CampoModelo("servicos", "Servicos", new ReciboCampoConfig(68, 430, 490, 145)),
            new CampoModelo("descontosSomados", "Descontos somados", new ReciboCampoConfig(502, 408, 50, 12)),
            new CampoModelo("subtotal", "Subtotal", new ReciboCampoConfig(502, 375, 50, 12)),
            new CampoModelo("descontos", "Descontos", new ReciboCampoConfig(502, 325, 50, 12)),
            new CampoModelo("liquidoReceber", "Liquido a receber", new ReciboCampoConfig(502, 308, 50, 12)),
            new CampoModelo("banco", "Banco / PIX", new ReciboCampoConfig(68, 285, 500, 45)),
            new CampoModelo("mensagem", "Mensagem", new ReciboCampoConfig(68, 65, 550, 24))
    );

    private JsonReciboConfig() {
    }

    public static synchronized ReciboConfig carregarOuCriarPadrao() {
        ReciboConfig config = null;

        if (CONFIG_FILE.exists()) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE.toPath(), StandardCharsets.UTF_8)) {
                config = GSON.fromJson(reader, ReciboConfig.class);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (config == null) {
            config = criarPadrao();
        }

        normalizar(config);
        salvar(config);
        return config;
    }

    public static synchronized void salvar(ReciboConfig config) {
        normalizar(config);

        File dataFolder = new File(DATA_FOLDER);
        dataFolder.mkdirs();

        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE.toPath(), StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ReciboConfig criarPadrao() {
        ReciboConfig config = new ReciboConfig();
        config.setTemplatePath("");

        Map<String, ReciboCampoConfig> campos = new LinkedHashMap<>();
        for (CampoModelo campo : CAMPOS) {
            campos.put(campo.id(), campo.padrao().copiar());
        }

        config.setCampos(campos);
        return config;
    }

    public static List<CampoModelo> listarCampos() {
        return CAMPOS;
    }

    public static File getArquivoConfig() {
        return CONFIG_FILE;
    }

    public static ReciboCampoConfig getCampoPadrao(String id) {
        for (CampoModelo campo : CAMPOS) {
            if (campo.id().equals(id)) {
                return campo.padrao().copiar();
            }
        }

        return new ReciboCampoConfig(0, 0, 100, 12);
    }

    private static void normalizar(ReciboConfig config) {
        if (config.getTemplatePath() == null) {
            config.setTemplatePath("");
        }

        Map<String, ReciboCampoConfig> camposAtuais = config.getCampos();
        Map<String, ReciboCampoConfig> camposNormalizados = new LinkedHashMap<>();

        for (CampoModelo campo : CAMPOS) {
            ReciboCampoConfig atual = camposAtuais == null ? null : camposAtuais.get(campo.id());
            camposNormalizados.put(campo.id(), sanitizar(atual, campo.padrao()));
        }

        config.setCampos(camposNormalizados);
    }

    private static ReciboCampoConfig sanitizar(ReciboCampoConfig atual, ReciboCampoConfig padrao) {
        if (atual == null) {
            return padrao.copiar();
        }

        double x = valorFinito(atual.getX(), padrao.getX());
        double y = valorFinito(atual.getY(), padrao.getY());
        double largura = Math.max(1, valorFinito(atual.getLargura(), padrao.getLargura()));
        double altura = Math.max(1, valorFinito(atual.getAltura(), padrao.getAltura()));

        return new ReciboCampoConfig(x, y, largura, altura);
    }

    private static double valorFinito(double valor, double padrao) {
        return Double.isFinite(valor) ? valor : padrao;
    }

    public record CampoModelo(String id, String label, ReciboCampoConfig padrao) {
        @Override
        public String toString() {
            return label;
        }
    }
}
