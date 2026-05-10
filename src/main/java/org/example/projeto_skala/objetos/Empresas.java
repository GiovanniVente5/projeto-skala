package org.example.projeto_skala.objetos;

import java.util.List;
import java.util.Map;

public class Empresas {
    String nome;
    int num;
    List<Servicos> servicos;

    @Override
    public String toString() {
        return "Empresas{" +
               "nome='" + nome + '\'' +
               ", num=" + num +
               ", servicos=" + servicos +
               '}';
    }

    public Empresas(String nome, int num) {
        this.nome = nome;
        this.num = num;
    }

    public Empresas(String nome, int num, List<Servicos> servicos) {
        this.nome = nome;
        this.num = num;
        this.servicos = servicos;
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

    public List<Servicos> getServicos() {
        return servicos;
    }

    public void setServicos(List<Servicos> servicos) {
        this.servicos = servicos;
    }
}
