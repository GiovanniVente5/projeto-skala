package org.example.projeto_skala.objetos;

import java.util.List;
import java.util.Map;

public class Empresas {
    String nome;
    int num;
    public String endereco;
    public String CNPJ;
    public String InscrCCM;
    public String InscrEST;
    List<Servicos> servicos;

    public Empresas(String nome, int num, String endereco, String CNPJ, String inscrCCM, String inscrEST, List<Servicos> servicos) {
        this.nome = nome;
        this.num = num;
        this.endereco = endereco;
        this.CNPJ = CNPJ;
        this.InscrCCM = inscrCCM;
        this.InscrEST = inscrEST;
        this.servicos = servicos;
    }

    @Override
    public String toString() {
        return "Empresas{" +
               "nome='" + nome + '\'' +
               ", num=" + num +
               ", endereco='" + endereco + '\'' +
               ", CNPJ='" + CNPJ + '\'' +
               ", InscrCCM='" + InscrCCM + '\'' +
               ", InscrEST='" + InscrEST + '\'' +
               ", servicos=" + servicos +
               '}';
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
