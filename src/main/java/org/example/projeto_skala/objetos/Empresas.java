package org.example.projeto_skala.objetos;

import com.google.gson.Gson;
import org.example.projeto_skala.Json.JsonCriar;

import java.io.File;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class Empresas {
    private String nome;
    private int num;
    private long numFatura;
    private int diaVencimento;
    public String endereco;
    public String CNPJ;
    public String InscrCCM;
    public String InscrEST;
    private List<Servicos> servicos;

    public Empresas(String nome, int num, long numFatura, int diaVencimento, String endereco, String CNPJ, String inscrCCM, String inscrEST, List<Servicos> servicos) {
        this.nome = nome;
        this.num = num;
        this.numFatura = numFatura;
        this.diaVencimento = diaVencimento;
        this.endereco = endereco;
        this.CNPJ = CNPJ;
        InscrCCM = inscrCCM;
        InscrEST = inscrEST;
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

    public LocalDate calcularVencimento(){
        int diaVencimento = this.diaVencimento;
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);

        int diaAjustado = Math.min(diaVencimento, mesAtual.lengthOfMonth());
        LocalDate vencimento = mesAtual.atDay(diaAjustado);

        if (!vencimento.isAfter(hoje)){
            YearMonth proximoMes = mesAtual.plusMonths(1);
            diaAjustado = Math.min(diaVencimento, proximoMes.lengthOfMonth());
            vencimento = proximoMes.atDay(diaAjustado);
        }
        return vencimento;
    }

    public int getDiaVencimento() {
        return diaVencimento;
    }

    public void setDiaVencimento(int diaVencimento) {
        this.diaVencimento = diaVencimento;
    }

    public long getNumFatura() {
        return numFatura;
    }

    public void setNumFatura(long numFatura) {
        this.numFatura = numFatura;
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
