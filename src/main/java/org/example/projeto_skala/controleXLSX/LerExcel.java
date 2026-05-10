package org.example.projeto_skala.controleXLSX;

import org.apache.poi.ss.usermodel.*;
import org.example.projeto_skala.objetos.Empresas;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LerExcel {
    public static List<Empresas> lerExcel(File file) {
        List<Empresas> linhas = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet0 = workbook.getSheetAt(0);
            Sheet sheet1 = workbook.getSheetAt(1);

            Map<Integer, String> servicos = new HashMap<>();
            for (Row row : sheet1) {
                if (getValorCell(row.getCell(0)) != "") {
                    System.out.println(getValorCell(row.getCell(0)));
                    int num = (int) Double.parseDouble(getValorCell(row.getCell(0)));
                    String nome = getValorCell(row.getCell(1));

                    servicos.put(num, nome);
                } else {continue;}
            }

            for (Row row : sheet0) {
                if (row.getRowNum() <= 1) {
                    continue;
                }
                int num = (int) Double.parseDouble(getValorCell(row.getCell(0)));
                String nome = getValorCell(row.getCell(1));

                linhas.add(new Empresas(nome, num, servicos));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return linhas;
    }

    public static String getValorCell(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                return cell.getCellFormula();

            case BLANK:
            default:
                return "";
        }
    }
}
