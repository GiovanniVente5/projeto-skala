package org.example.projeto_skala.objetos;

import java.util.List;
import java.util.Map;

public class Empresas {
    private String nome;
    private int num;

    public String endereco;
    public String CNPJ;
    public String InscrCCM;
    public String InscrEST;
    private List<Servicos> servicos;

    public double[] valores(){
        double comDesconto = 0;
        double totalDoDesconto = 0;
        for (Servicos servicos1 : servicos){
            double temp = servicos1.getValor();
            if (servicos1.getValor() < 0) temp += totalDoDesconto;
            temp += comDesconto;
        }
        return new double[] {comDesconto,totalDoDesconto};
    }

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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCNPJ() {
        return CNPJ;
    }

    public void setCNPJ(String CNPJ) {
        this.CNPJ = CNPJ;
    }

    public String getInscrCCM() {
        return InscrCCM;
    }

    public void setInscrCCM(String inscrCCM) {
        InscrCCM = inscrCCM;
    }

    public String getInscrEST() {
        return InscrEST;
    }

    public void setInscrEST(String inscrEST) {
        InscrEST = inscrEST;
    }
}
