package org.example.projeto_skala.controleXLSX;

import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;

import java.time.Month;

import java.time.format.DateTimeFormatter;



public class coordenadasCell {
    long fatura;
    LocalDate dataEmissao;
    Month mesVencimento;
    int coordenada;

    public coordenadasCell(Sheet sheet) {
        long fatura = 0;
        int coord = 0;

        for (Cell cell : sheet.getRow(0)) {
            if (cell.getCellType() == CellType.NUMERIC) {
                fatura = (long) cell.getNumericCellValue();
                coord = cell.getColumnIndex();
            }
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String data = LerExcel.getValorCell(sheet.getRow(1).getCell(coord));
        setDataEmissao(LocalDate.parse(data,df));
        this.mesVencimento = mesString(LerExcel.getValorCell(sheet.getRow(2).getCell(coord)));
        this.fatura = fatura;
        this.coordenada = coord;
    }

    public static Month mesString(String mes) {
        String formatada = mes.toLowerCase().trim().replace(" ", "");
        return switch (formatada) {
            case "janeiro" -> Month.JANUARY;
            case "fevereiro" -> Month.FEBRUARY;
            case "março" -> Month.MARCH;
            case "abril" -> Month.APRIL;
            case "maio" -> Month.MAY;
            case "junho" -> Month.JUNE;
            case "julho" -> Month.JULY;
            case "agosto" -> Month.AUGUST;
            case "setembro" -> Month.SEPTEMBER;
            case "outubro" -> Month.OCTOBER;
            case "novembro" -> Month.NOVEMBER;
            case "dezembro" -> Month.DECEMBER;
            default -> null;
        };
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public int getCoordenada() {
        return coordenada;
    }

    public long getFatura() {
        return fatura;
    }
}
