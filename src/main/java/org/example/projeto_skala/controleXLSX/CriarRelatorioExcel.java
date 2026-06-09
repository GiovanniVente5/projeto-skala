package org.example.projeto_skala.controleXLSX;

import org.apache.poi.ss.usermodel.*;
import org.example.projeto_skala.controlePDF.GerarPDF;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class CriarRelatorioExcel {
    public static void main(String[] args) throws IOException {

        List<Servicos> servicos = new ArrayList<>();
        servicos.add(new Servicos("teste1", 1, 1000.0));
        servicos.add(new Servicos("teste2", 2, -100.0));
        servicos.add(new Servicos("test3", 3, 67.0));

        List<Empresas> empresas = new ArrayList<>();
        Month mes = Month.NOVEMBER;
        LocalDate emissao = LocalDate.now();
        empresas.add(new Empresas(1,"BRASSCOM, ASSOCIACAO DAS EMPRESAS DE TECNOLOGIA DA INFORMACAO E COMUNICACAO (TIC) E DETECNOLOGIAS DIGITAIS", 1, 1, 10,mes,emissao,"enredeço", "123", "123", "123", servicos));
        empresas.add(new Empresas(2,"empresa2", 2, 2, 20,mes, emissao,"enredeço", "123", "123", "123", servicos));

        File path = new File("data/");
        GerarPDF.gerarRecibo(empresas.getFirst(),path);
//
//        criarRelatorio(empresas, "data/RecibosGerados/", data);
    }

    public static void criarRelatorio(List<Empresas> empresas, String destino, LocalDate data) throws IOException {
//
////      dados - formato do valor + periodo de vencimento
//        NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "BR"));
//        nf.setMinimumFractionDigits(2);
//        nf.setMaximumFractionDigits(2);
//
//        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        DateTimeFormatter formatoVence = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//        LocalDate hoje = LocalDate.now();
//        YearMonth mesAtual = YearMonth.from(hoje);
//
//        empresas.sort(Comparator.comparing(Empresas::getDiaVencimento));
//
//        int diaAjustado = Math.min(empresas.getFirst().getDiaVencimento(), mesAtual.lengthOfMonth());
//        LocalDate vencimentoDiaUm = mesAtual.atDay(diaAjustado);
//        LocalDate vencimentoDiaDois = mesAtual.atDay(mesAtual.lengthOfMonth());
//
////      cria arquivo excel + planilha + primeira linha
//        Workbook excel = new XSSFWorkbook();
//        Sheet planilha = excel.createSheet("Relatório");
//
////      fonte
//        Font negrito = excel.createFont();
//        negrito.setBold(true);
//        CellStyle estilo = excel.createCellStyle();
//        estilo.setFont(negrito);
//
////      cabeçalho
//        String[] cabecalho = {"Relatório Mensal", "Período de Vencimento:", vencimentoDiaUm.format(formato), vencimentoDiaDois.format(formato), "Skala Contabilidade", "Data:", hoje.format(formato)};
//        Row linha = planilha.createRow(0);
//        int y = 0;
//        for (int x = 0; x < 2; x++) {
//            System.out.println("X: "+ x );
//            for (int i = 0; i < 5; i++) {
//                System.out.println("i: "+ i );
//
//                Cell celula = linha.createCell(x);
//                celula.setCellStyle(estilo);
//
//                switch (x){
//                    case 0:
//                        if (i != 1){
//                            celula.setCellValue(cabecalho[y]);
//                            y++;
//                            System.out.println("Y + 1: " + y + "VALUE " + celula.getStringCellValue());
//
//                        }
//                        break;
//
//                    case 1:
//                        if (i==0) celula.setCellValue(cabecalho[4]);
//                        if (i==3) celula.setCellValue(cabecalho[5]);
//                        if (i==4) celula.setCellValue(cabecalho[6]);
//                        break;
//                }
//            }
//        }
//
//
//        FileOutputStream out = new FileOutputStream(destino + "Relatório-" + formatoVence.format(data) + ".xlsx");
//        excel.write(out);
    }
}
