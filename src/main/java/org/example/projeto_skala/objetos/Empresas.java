package org.example.projeto_skala.objetos;

import com.google.gson.annotations.JsonAdapter;
import org.example.projeto_skala.Json.LocalDateAdapter;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

public class Empresas {
    private int id;
    private String nome;
    private final int num;
    private final long numFatura;
    private final int diaVencimento;
    private Month vencimentoMes;

    @JsonAdapter(LocalDateAdapter.class)
    private LocalDate emissao;

    public String endereco;
    public String CNPJ;
    public String InscrCCM;
    public String InscrEST;
    private List<Servicos> servicos;

    public Empresas(int id, String nome, int num, long numFatura, int diaVencimento, Month vencimentoMes, LocalDate emissao, String endereco, String CNPJ, String inscrCCM, String inscrEST, List<Servicos> servicos) {
        this.id = id;
        this.nome = nome;
        this.num = num;
        this.numFatura = numFatura;
        this.diaVencimento = diaVencimento;
        this.vencimentoMes = vencimentoMes;
        this.emissao = emissao;
        this.endereco = endereco;
        this.CNPJ = CNPJ;
        InscrCCM = inscrCCM;
        InscrEST = inscrEST;
        this.servicos = servicos;
    }

    @Override
    public String toString() {
        return "Empresas{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", num=" + num +
                ", numFatura=" + numFatura +
                ", diaVencimento=" + diaVencimento +
                ", vencimentoMes=" + vencimentoMes +
                ", emissao=" + emissao +
                ", endereco='" + endereco + '\'' +
                ", CNPJ='" + CNPJ + '\'' +
                ", InscrCCM='" + InscrCCM + '\'' +
                ", InscrEST='" + InscrEST + '\'' +
                ", servicos=" + servicos +
                '}';
    }

    public LocalDate calcularVencimento(){

        int diaVencimento = this.diaVencimento;

        YearMonth mes = YearMonth.of(emissao.plusMonths(1).getYear(),vencimentoMes);
        LocalDate hoje = LocalDate.now();

        int diaAjustado = Math.min(diaVencimento, vencimentoMes.length(hoje.isLeapYear()));
        LocalDate vencimento = mes.atDay(diaAjustado);

        if (!vencimento.isAfter(hoje)){
            YearMonth proximoMes = mes.plusMonths(1);
            diaAjustado = Math.min(diaVencimento, proximoMes.lengthOfMonth());
            vencimento = proximoMes.atDay(diaAjustado);
        }

        if (diaAjustado > mes.plusMonths(1).lengthOfMonth()){
            diaAjustado = mes.plusMonths(1).lengthOfMonth();
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

    public LocalDate getEmissao() {
        return emissao;
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
