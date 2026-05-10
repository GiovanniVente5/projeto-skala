package org.example.projeto_skala.controleXLSX;

import org.apache.poi.ss.usermodel.*;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

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

            Map<Integer, String> servicosNome = new HashMap<>();
            for (Row row : sheet1) {
                if (getValorCell(row.getCell(0)) != "") {
                    System.out.println(getValorCell(row.getCell(0)));
                    int num = (int) Double.parseDouble(getValorCell(row.getCell(0)));
                    String nome = getValorCell(row.getCell(1));

                    servicosNome.put(num, nome);
                } else {continue;}
            }

            for (Row row : sheet0) {
                Map<Integer,Double> servicosNum = new HashMap<>();
                if (row.getRowNum() == 0) {
                    continue;
                } else if (row.getRowNum() == 1) {
                    boolean continuar = true;

                    while(continuar){
//                        RESOLVER ISSO
                        Integer celula = 2;
                        System.out.println("Dentro do while - Cell = " + celula);
                        if (!getValorCell(row.getCell(celula)).isEmpty()){
                            Row proxRow = sheet0.getRow(row.getRowNum() + 1);
                            servicosNum.put((int) Double.parseDouble(getValorCell(row.getCell(celula))), Double.parseDouble(getValorCell(proxRow.getCell(celula))));
                            celula += 1;
                            System.out.println("AAAAAAAAAAA");
                        } else {
                            continuar = false;
                        }

                    }
                    continue;
                }

                int num = (int) Double.parseDouble(getValorCell(row.getCell(0)));
                String nome = getValorCell(row.getCell(1));

                List<Servicos> servicos = new ArrayList<>();

                for (int chave : servicosNum.keySet()){
                    Servicos servicos1 = new Servicos(servicosNome.get(chave),chave, servicosNum.get(chave));
                    servicos.add(servicos1);
                }

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
