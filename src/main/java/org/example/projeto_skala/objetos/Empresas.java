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
    private Banco banco;
    private String texto = "";

    public Empresas(int id, String nome, int num, long numFatura, int diaVencimento, Month vencimentoMes, LocalDate emissao, String endereco, String CNPJ, String inscrCCM, String inscrEST, List<Servicos> servicos, String texto) {
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
        this.texto = texto;
    }

    public Empresas(int id, String nome, int num, long numFatura, int diaVencimento, Month vencimentoMes, LocalDate emissao, String endereco, String CNPJ, String inscrCCM, String inscrEST, List<Servicos> servicos, Banco banco, String texto) {
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
        this.banco = banco;
        this.texto = texto;
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
                ", banco=" + banco +
                ", texto='" + texto + '\'' +
                '}';
    }

    public LocalDate calcularVencimento() {

        int diaVencimento = this.diaVencimento;

        YearMonth mes = YearMonth.of(emissao.getYear(), vencimentoMes);
        LocalDate hoje = LocalDate.now();

        int diaAjustado = Math.min(diaVencimento, vencimentoMes.length(hoje.isLeapYear()));
        LocalDate vencimento = mes.atDay(diaAjustado);

        return vencimento;
    }

    public double getValorTotal() {
        double valorTotal = 0;
        for (Servicos serv : servicos) {
            valorTotal += serv.getValor();
        }
        return valorTotal;
    }

    public String getTexto() {
        return texto;
    }

    public int getDiaVencimento() {
        return diaVencimento;
    }

    public LocalDate getEmissao() {
        return emissao;
    }

    public Month getVencimentoMes() {
        return vencimentoMes;
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

    public Banco getBanco() {
        return banco;
    }
}
