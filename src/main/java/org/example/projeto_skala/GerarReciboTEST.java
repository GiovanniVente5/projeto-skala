package org.example.projeto_skala;

import org.example.projeto_skala.Controller.RelatoriosController;
import org.example.projeto_skala.controlePDF.GerarPDF;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.itextpdf.kernel.pdf.PdfName.List;

public class GerarReciboTEST {
    public static void main(String[] args) throws IOException {
        Empresas empresaTest = new Empresas(
                "ADMINISTRADORA DE CARTAO DE DESCONTOS FREGUESIA DO O LTDA ME",
                524,
                "RUA RAULINO GALDINO DA SILVA, 1020 COMP - BLOCO 1 APT 75 - JARDIM MARISTELA - SÃO PAULO/SP",
                "12.123.123/1234-23",
                "1.123.123-1",
                "148091860110",
                java.util.List.of(new Servicos[]{
                        new Servicos("PROCESSAMENTO DE ARQUIVOS XML PARA TRANSMISSÃO VIA SISTEMA AUDESP - ATOS DE PESSOAL DO TRIBUNAL DE CONTAS DO ESTADO DE SÃO PAULO", 1, 1.0),
                        new Servicos("STDA - DECLARAÇÃO DO SN RELATIVA À SUBSTITUIÇÃO TRIBUTÁRIA E DIFERENCIAL DE ALÍQUOTA - 2026 ANUAL", 2, 12.0),
                        new Servicos("PREPARAÇÃO E ENTREGA DIMOB - DECLARAÇÃO DE OPERAÇÕES IMOBILIÁRIAS - RECEITA FEDERAL 2026", 3, -10.0)
                })
        );

        GerarPDF.gerarRecibo(empresaTest);
    }
}
