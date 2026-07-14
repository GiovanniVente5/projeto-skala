package org.example.projeto_skala.objetos;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReciboConfig {
    private String templatePath = "";
    private Map<String, ReciboCampoConfig> campos = new LinkedHashMap<>();

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public Map<String, ReciboCampoConfig> getCampos() {
        return campos;
    }

    public void setCampos(Map<String, ReciboCampoConfig> campos) {
        this.campos = campos;
    }

    public ReciboCampoConfig getCampo(String id) {
        return campos.get(id);
    }

    public void setCampo(String id, ReciboCampoConfig campo) {
        campos.put(id, campo);
    }
}
