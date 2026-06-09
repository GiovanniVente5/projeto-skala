package org.example.projeto_skala.controlePDF;

import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class testepdf {
    public static void main(String[] args) throws IOException {
        Servicos serv = new Servicos("PROCESSAMENTO DE ARQUIVOS XML PARA TRANSMISSÃO VIA SISTEMA AUDESP - ATOS DE PESSOAL DO TRIBUNAL DE CONTAS DO ESTADO DE SÃO PAULO",1,10.00);
        Servicos serv1 = new Servicos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",1,-10.00);
        Servicos serv2 = new Servicos("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",1,18360.00);

        ArrayList<Servicos> servs = new ArrayList<>();
        servs.add(serv);
        servs.add(serv1);
        servs.add(serv2);

        Empresas emp = new Empresas(
                1,
                "Giovanni",
                23,
                123,
                1,
                "RUA",
                "123",
                "123",
                "123",
                servs
        );
        File outDir = new File("data/");
        ArrayList<Empresas> Lista = new ArrayList<>();

        Lista.add(emp);
        Lista.add(emp);

        GerarPDF.gerarRecibo(emp,outDir);
    }
}
