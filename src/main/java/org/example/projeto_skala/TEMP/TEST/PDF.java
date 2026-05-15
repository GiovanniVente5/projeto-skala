package org.example.projeto_skala.TEMP.TEST;

import org.example.projeto_skala.controlePDF.GerarPDF;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDF {
    public static void main(String[] args) {
        List<Servicos> servicos = new ArrayList<>();
        servicos.add(new Servicos ("Servico1",1,10.0));
        servicos.add(new Servicos ("Servico2",2,20.0));
        servicos.add(new Servicos ("Servico3",3,30.0));
        servicos.add(new Servicos ("Servico4",4,-10.0));
        Empresas empresaTest = new Empresas(
                "ASSOCIACAO DOS LAB FARMACEUTICOS NACIONAIS - ALANAC",
                67,
                "RUA BONIFACIO CUBAS  N°2289 - SÃO PAULO - SP - BRASIL",
                "12.123.123/1234-12",
                "1.123.123-1",
                "ISENTO AAAAAAA",
                servicos
        );

        try {
            GerarPDF.gerarRecibo(empresaTest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
