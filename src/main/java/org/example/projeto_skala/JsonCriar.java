package org.example.projeto_skala;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.projeto_skala.objetos.Empresas;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonCriar {
    private static final String JSON_PATH = "data/Excel-Importado.json";

    public static void salvar(List<Empresas> empresasNovas) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        File dataFolder = new File("data");
        dataFolder.mkdirs();

        List<Empresas> empresasAntigas = carregarEmpresasExistentes(gson);

        Map<Integer, Empresas> empresasPorNumero = new LinkedHashMap<>();

        for (Empresas empresa : empresasAntigas) {
            empresasPorNumero.put(empresa.getNum(), empresa);
        }

        for (Empresas empresa : empresasNovas) {
            empresasPorNumero.putIfAbsent(empresa.getNum(), empresa);
        }

        try (FileWriter writer = new FileWriter(JSON_PATH)) {
            gson.toJson(new ArrayList<>(empresasPorNumero.values()), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Empresas> carregarEmpresasExistentes(Gson gson) {
        File jsonFile = new File(JSON_PATH);

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
}
//como criar um botao na primeira tela que leva a outra tela que mostra todas as empresas salvas no json de forma de lista,