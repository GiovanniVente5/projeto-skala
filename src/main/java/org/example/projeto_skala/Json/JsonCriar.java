package org.example.projeto_skala.Json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.projeto_skala.objetos.Empresas;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonCriar {
    private static final String DATA_FOLDER = "data";
    private static final String JSON_PREFIX = "Excel-Importado";
    private static final String JSON_EXTENSION = ".json";
    private static final String DEFAULT_JSON_NAME = JSON_PREFIX + JSON_EXTENSION;

    public static void salvar(List<Empresas> empresasNovas) {
        LocalDate hoje = LocalDate.now();
        salvar(empresasNovas, hoje.getMonthValue(), hoje.getYear());
    }

    public static void salvar(List<Empresas> empresasNovas, int mes, int ano) {
        validarReferencia(mes, ano);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        File dataFolder = new File(DATA_FOLDER);
        dataFolder.mkdirs();

        File jsonFile = new File(dataFolder, criarNomeArquivo(mes, ano));
        List<Empresas> empresasAntigas = carregarEmpresasExistentes(gson, jsonFile);

        Map<Integer, Empresas> empresasPorNumero = new LinkedHashMap<>();

        for (Empresas empresa : empresasAntigas) {
            empresasPorNumero.put(empresa.getNum(), empresa);
        }

        for (Empresas empresa : empresasNovas) {
            empresasPorNumero.put(empresa.getNum(), empresa);
        }

        try (FileWriter writer = new FileWriter(jsonFile)) {
            gson.toJson(new ArrayList<>(empresasPorNumero.values()), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Empresas> carregar() {
        List<Empresas> empresas = new ArrayList<>();

        for (List<Empresas> empresasDoPeriodo : carregarPorPeriodo().values()) {
            empresas.addAll(empresasDoPeriodo);
        }

        return empresas;
    }

    public static Map<String, List<Empresas>> carregarPorPeriodo() {
        Gson gson = new Gson();
        Map<String, List<Empresas>> empresasPorPeriodo = new LinkedHashMap<>();

        for (File jsonFile : listarArquivosJson()) {
            empresasPorPeriodo.put(criarRotuloPeriodo(jsonFile), carregarEmpresasExistentes(gson, jsonFile));
        }

        return empresasPorPeriodo;
    }

    private static List<Empresas> carregarEmpresasExistentes(Gson gson, File jsonFile) {
        if (!jsonFile.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            Type listType = new TypeToken<List<Empresas>>() {}.getType();
            List<Empresas> empresas = gson.fromJson(reader, listType);

            if (empresas == null) {
                return new ArrayList<>();
            }

            return empresas;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static File[] listarArquivosJson() {
        File dataFolder = new File(DATA_FOLDER);

        if (!dataFolder.exists()) {
            return new File[0];
        }

        File[] jsonFiles = dataFolder.listFiles(file -> file.isFile() && isArquivoJsonImportado(file.getName()));

        if (jsonFiles == null) {
            return new File[0];
        }

        Arrays.sort(jsonFiles, (primeiro, segundo) -> primeiro.getName().compareToIgnoreCase(segundo.getName()));
        return jsonFiles;
    }

    private static boolean isArquivoJsonImportado(String nomeArquivo) {
        return nomeArquivo.equals(DEFAULT_JSON_NAME)
                || (nomeArquivo.startsWith(JSON_PREFIX + "-") && nomeArquivo.endsWith(JSON_EXTENSION));
    }

    private static String criarNomeArquivo(int mes, int ano) {
        return String.format("%s-%02d-%04d%s", JSON_PREFIX, mes, ano, JSON_EXTENSION);
    }

    private static String criarRotuloPeriodo(File jsonFile) {
        String nomeArquivo = jsonFile.getName();
        String prefixoComSeparador = JSON_PREFIX + "-";

        if (nomeArquivo.startsWith(prefixoComSeparador) && nomeArquivo.endsWith(JSON_EXTENSION)) {
            String referencia = nomeArquivo.substring(
                    prefixoComSeparador.length(),
                    nomeArquivo.length() - JSON_EXTENSION.length()
            );

            String[] partes = referencia.split("-");

            if (partes.length == 2) {
                return partes[0] + "/" + partes[1];
            }
        }

        return "Sem referencia";
    }

    private static void validarReferencia(int mes, int ano) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mes invalido: " + mes);
        }

        if (ano < 1900 || ano > 9999) {
            throw new IllegalArgumentException("Ano invalido: " + ano);
        }
    }

    public static void excluirCliente(int numeroCliente) {
        File dataFolder = new File(DATA_FOLDER);
        if (!dataFolder.exists()) {
            return;
        }

        File[] jsonFiles = listarArquivosJson();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for (File jsonFile : jsonFiles) {
            List<Empresas> empresas = carregarEmpresasExistentes(gson, jsonFile);
            empresas.removeIf(e -> e.getNum() == numeroCliente);

            try (FileWriter writer = new FileWriter(jsonFile)) {
                gson.toJson(empresas, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
