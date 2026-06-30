package org.example.projeto_skala.objetos;

import java.util.Arrays;
import java.util.List;

public class Banco {
    private String NomeBanco;
    private String NomeConta;
    private String CNPJ;
    private String agencia;
    private String CC;
    private List<String> info;

    private void info(String[] infoArr){
        this.info = Arrays.stream(infoArr).toList();
    }

    public Banco(String nomeBanco, String nomeConta, String CNPJ, String agencia, String CC) {
        NomeBanco = nomeBanco;
        NomeConta = nomeConta;
        this.CNPJ = CNPJ;
        this.agencia = agencia;
        this.CC = CC;
        String[] a = {nomeBanco,nomeConta,CNPJ,agencia,CC};
        info(a);
    }

    public Banco(String nomeConta, String CNPJ) {
        NomeConta = nomeConta;
        this.CNPJ = CNPJ;
        String[] a = {nomeConta,CNPJ};
        info(a);
    }

    @Override
    public String toString() {
        return "Banco{" +
                "NomeBanco='" + NomeBanco + '\'' +
                ", NomeConta='" + NomeConta + '\'' +
                ", CNPJ='" + CNPJ + '\'' +
                ", agencia='" + agencia + '\'' +
                ", CC='" + CC + '\'' +
                '}';
    }

    public List<String> getInfo() {
        return info;
    }
}
