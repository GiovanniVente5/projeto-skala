package org.example.projeto_skala.Json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonServicos {
    private static final String DATA_FOLDER = "data";
    private static final String JSON_PATH = DATA_FOLDER + "/Servicos.json";

    public static List<Servicos> carregar() {
        File jsonFile = new File(JSON_PATH);

        if (!jsonFile.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            Type listType = new TypeToken<List<Servicos>>() {
            }.getType();
            List<Servicos> servicos = new Gson().fromJson(reader, listType);

            if (servicos == null) {
                return new ArrayList<>();
            }

            return servicos;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void salvar(List<Servicos> servicos) {
        File dataFolder = new File(DATA_FOLDER);
        dataFolder.mkdirs();

        try (FileWriter writer = new FileWriter(JSON_PATH)) {
            new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .setPrettyPrinting()
                    .create().
                    toJson(servicos, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void adicionar(Servicos servico) {
        Map<Integer, Servicos> servicosPorNumero = carregarComoMapa();
        servicosPorNumero.put(servico.getNum(), servico);
        salvar(new ArrayList<>(servicosPorNumero.values()));
    }

    public static void remover(int num) {
        Map<Integer, Servicos> servicosPorNumero = carregarComoMapa();
        servicosPorNumero.remove(num);
        salvar(new ArrayList<>(servicosPorNumero.values()));
    }

    public static void sincronizarComEmpresas(List<Empresas> empresas) {
        Map<Integer, Servicos> servicosPorNumero = carregarComoMapa();

        for (Empresas empresa : empresas) {
            if (empresa.getServicos() == null) {
                continue;
            }

            for (Servicos servico : empresa.getServicos()) {
                servicosPorNumero.putIfAbsent(servico.getNum(), servico);
            }
        }

        salvar(new ArrayList<>(servicosPorNumero.values()));
    }

    private static Map<Integer, Servicos> carregarComoMapa() {
        Map<Integer, Servicos> servicosPorNumero = new LinkedHashMap<>();

        for (Servicos servico : carregar()) {
            servicosPorNumero.put(servico.getNum(), servico);
        }

        return servicosPorNumero;
    }
}
