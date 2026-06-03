package org.example.projeto_skala.objetos;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class Empresas {
    private int id;
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

    public Empresas(int id, String nome, int num, long numFatura, int diaVencimento, String endereco, String CNPJ, String inscrCCM, String inscrEST, List<Servicos> servicos) {
        this.id = id;
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

        if (diaAjustado > mesAtual.plusMonths(1).lengthOfMonth()){
            diaAjustado = mesAtual.plusMonths(1).lengthOfMonth();
        }

        return vencimento;
    }

    public double getValorTotal(){
        double valorTotal = 0;
        for (Servicos serv : servicos){
            valorTotal += serv.getValor();
        }
        return valorTotal;
    }

    public int getDiaVencimento() {
        return diaVencimento;
    }



    public long getNumFatura() {
        return numFatura;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Servicos> getServicos() {
        return servicos;
    }



    public String getEndereco() {
        return endereco;
    }



    public String getCNPJ() {
        return CNPJ;
    }



    public String getInscrCCM() {
        return InscrCCM;
    }



    public String getInscrEST() {
        return InscrEST;
    }


}
