package org.example.projeto_skala.objetos;

import java.util.Map;

public class Empresas {
    String nome;
    int num;
    Map<Integer,String> servicosMap;

    @Override
    public String toString() {
        return "Empresas{" +
               "nome='" + nome + '\'' +
               ", num=" + num +
               ", servicosMap=" + servicosMap +
               '}';
    }

    public Empresas(String nome, int num) {
        this.nome = nome;
        this.num = num;
    }

    public Empresas(String nome, int num, Map<Integer, String> servicosMap) {
        this.nome = nome;
        this.num = num;
        this.servicosMap = servicosMap;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Map<Integer, String> getServicosMap() {
        return servicosMap;
    }

    public void setServicosMap(Map<Integer, String> servicosMap) {
        this.servicosMap = servicosMap;
    }
}
