package org.example.projeto_skala.controleXLSX;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.projeto_skala.objetos.Empresas;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CriarRelatorio {

    public static void criarRelatorio(List<Empresas> empresas, String destino,String folderData) throws IOException {
        empresas.sort(Comparator.comparing(Empresas::getDiaVencimento));

//      dados - formato do valor + periodo de vencimento
        NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "BR"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate hoje = empresas.getFirst().getEmissao();
        YearMonth mesAtual = YearMonth.from(hoje);


        int diaAjustado = Math.min(empresas.getFirst().getDiaVencimento(), mesAtual.lengthOfMonth());
        LocalDate vencimentoDiaUm = mesAtual.atDay(diaAjustado);
        LocalDate vencimentoDiaDois = mesAtual.atDay(mesAtual.lengthOfMonth());

//      cria arquivo excel + planilha + primeira linha
        Workbook excel = new XSSFWorkbook();
        Sheet planilha = excel.createSheet("Relatório");

//      fonte
        Font negrito = excel.createFont();
        negrito.setBold(true);
        CellStyle estilo = excel.createCellStyle();
        estilo.setFont(negrito);

//      cabeçalho
        CellStyle cabecalhoEstilo = excel.createCellStyle();
        cabecalhoEstilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        cabecalhoEstilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        cabecalhoEstilo.setBorderBottom(BorderStyle.THIN);
        cabecalhoEstilo.setBorderLeft(BorderStyle.THIN);
        cabecalhoEstilo.setBorderRight(BorderStyle.THIN);
        cabecalhoEstilo.setBorderTop(BorderStyle.THIN);

        CellStyle cabecalhoEstilo2 = excel.createCellStyle();
        cabecalhoEstilo2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        cabecalhoEstilo2.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        cabecalhoEstilo2.setBorderBottom(BorderStyle.THICK);
        cabecalhoEstilo2.setBorderLeft(BorderStyle.THIN);
        cabecalhoEstilo2.setBorderRight(BorderStyle.THIN);
        cabecalhoEstilo2.setBorderTop(BorderStyle.THICK);

        CellStyle dinheiroEstilo = excel.createCellStyle();
        dinheiroEstilo.setDataFormat((short) 7);
        dinheiroEstilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dinheiroEstilo.setAlignment(HorizontalAlignment.LEFT);
        dinheiroEstilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        dinheiroEstilo.setBorderBottom(BorderStyle.THIN);
        dinheiroEstilo.setBorderLeft(BorderStyle.THIN);
        dinheiroEstilo.setBorderRight(BorderStyle.THICK);
        dinheiroEstilo.setBorderTop(BorderStyle.THIN);


        String[] cabecalho = {"Relatório Mensal", "Período de Vencimento:", vencimentoDiaUm.format(formato), vencimentoDiaDois.format(formato), "Skala Contabilidade", "Data do relatório:", LocalDate.now().format(formato)};

        Row cabecalho0 = planilha.createRow(0);
        Row cabecalho1 = planilha.createRow(1);


        Cell titulo = cabecalho0.createCell(0);
        titulo.setCellStyle(cabecalhoEstilo);
        titulo.setCellValue(cabecalho[0]);

        Cell vazio = cabecalho0.createCell(1);
        vazio.setCellStyle(cabecalhoEstilo);

        Cell vencimentoPeriodo = cabecalho0.createCell(2);
        vencimentoPeriodo.setCellStyle(cabecalhoEstilo);
        vencimentoPeriodo.setCellValue(cabecalho[1]);

        Cell vencimentoData1 = cabecalho0.createCell(3);
        vencimentoData1.setCellStyle(cabecalhoEstilo);
        vencimentoData1.setCellValue(cabecalho[2] + " a " + cabecalho[3]);

        Cell vencimentoData2 = cabecalho0.createCell(4);
        vencimentoData2.setCellStyle(cabecalhoEstilo);
//      --------------------------------------------------------------------------
        Cell skalaNome = cabecalho1.createCell(0);
        skalaNome.setCellStyle(cabecalhoEstilo);
        skalaNome.setCellValue(cabecalho[4]);

        Cell vazio1 = cabecalho1.createCell(1);
        Cell vazio2 = cabecalho1.createCell(2);
        vazio1.setCellStyle(cabecalhoEstilo);
        vazio2.setCellStyle(cabecalhoEstilo);

        Cell criacaoDataText = cabecalho1.createCell(3);
        criacaoDataText.setCellStyle(cabecalhoEstilo);
        criacaoDataText.setCellValue(cabecalho[5]);

        Cell criacaoData = cabecalho1.createCell(4);
        criacaoData.setCellStyle(cabecalhoEstilo);
        criacaoData.setCellValue(cabecalho[6]);

        String[] colunas = {"Número", "Empresa", "Vencimento", "Número Fatura", "Valor"};

        Row colunasNome = planilha.createRow(2);

        Cell numColuna = colunasNome.createCell(0);
        numColuna.setCellStyle(cabecalhoEstilo2);
        numColuna.setCellValue(colunas[0]);

        Cell nomeColuna = colunasNome.createCell(1);
        nomeColuna.setCellStyle(cabecalhoEstilo2);
        nomeColuna.setCellValue(colunas[1]);

        Cell vencimentoColuna = colunasNome.createCell(2);
        vencimentoColuna.setCellStyle(cabecalhoEstilo2);
        vencimentoColuna.setCellValue(colunas[2]);

        Cell numFaturaColuna = colunasNome.createCell(3);
        numFaturaColuna.setCellStyle(cabecalhoEstilo2);
        numFaturaColuna.setCellValue(colunas[3]);

        Cell valorColuna = colunasNome.createCell(4);
        valorColuna.setCellStyle(cabecalhoEstilo2);
        valorColuna.setCellValue(colunas[4]);

//      listagem de empresas
        int coluna = 3;

        StringBuilder sb = new StringBuilder();

        empresas.sort(Comparator.comparing(Empresas::getDiaVencimento));

        for (Empresas emp : empresas) {
            empresaCelulas(emp, planilha, coluna, estilo,dinheiroEstilo);
            planilha.autoSizeColumn(coluna);
            sb.append(planilha.getRow(coluna).getCell(4).getAddress()).append("+");
            coluna++;
        }
        sb.deleteCharAt(sb.length() - 1);

        CellStyle resultadoEstilo = excel.createCellStyle();
        resultadoEstilo.setDataFormat((short) 7);
        resultadoEstilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        resultadoEstilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        resultadoEstilo.setBorderBottom(BorderStyle.THICK);
        resultadoEstilo.setBorderTop(BorderStyle.THICK);
        resultadoEstilo.setBorderRight(BorderStyle.THICK);

//      Coluna Resultados totais
        Row resultado = planilha.createRow(coluna);

        Cell vaziaResultado = resultado.createCell(0);
        vaziaResultado.setCellStyle(resultadoEstilo);

        Cell vaziaResultado1 = resultado.createCell(1);
        vaziaResultado1.setCellStyle(resultadoEstilo);

        Cell vaziaResultado2 = resultado.createCell(2);
        vaziaResultado2.setCellStyle(resultadoEstilo);

        Cell resultadoString = resultado.createCell(3);
        resultadoString.setCellValue("Valor Total:");
        resultadoString.setCellStyle(resultadoEstilo);

        Cell resultadoTotal = resultado.createCell(4);
        resultadoTotal.setCellFormula(sb.toString());
        resultadoTotal.setCellStyle(resultadoEstilo);

        planilha.autoSizeColumn(0);
        planilha.autoSizeColumn(1);
        planilha.autoSizeColumn(2);

        folderData = "1-" + folderData;
        FileOutputStream out = new FileOutputStream(destino + "/Relatório-" + folderData + ".xlsx");
        excel.write(out);
        excel.close();
    }

    public static void empresaCelulas(Empresas emp, Sheet planilha, int rowComeco, CellStyle cs,CellStyle valores) {
        Row row = planilha.createRow(rowComeco);

        Cell cellNum = row.createCell(0);
        cellNum.setCellStyle(cs);
        cellNum.setCellValue(emp.getNum());

        Cell cellNome = row.createCell(1);
        cellNome.setCellValue(emp.getNome());

        Cell cellVencimento = row.createCell(2);
        cellVencimento.setCellStyle(cs);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        cellVencimento.setCellValue(emp.calcularVencimento().format(df));

        Cell cellFaturaNum = row.createCell(3);
        cellFaturaNum.setCellValue(emp.getNumFatura());

        Cell cellValor = row.createCell(4);
        cellValor.setCellStyle(valores);
        cellValor.setCellValue(emp.getValorTotal());
    }
}