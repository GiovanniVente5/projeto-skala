package org.example.projeto_skala.controleXLSX;

import org.apache.poi.ss.usermodel.*;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
//          LENDO SERVIÇOS
            for (Row row : sheet1) {
                if (!getValorCell(row.getCell(0)).isEmpty() && row.getRowNum() != 0) {
                    int num = (int) Double.parseDouble(getValorCell(row.getCell(0)));
                    String nome = getValorCell(row.getCell(1));

                    servicosNome.put(num, nome);
                }
            }

            Map<Integer, Integer> servicosPorColuna = new LinkedHashMap<>();
            Row rowServicos = sheet0.getRow(1);
            int celula = 6;

            while (!getValorCell(rowServicos.getCell(celula)).isEmpty()) {
                int numServico = (int) Double.parseDouble(getValorCell(rowServicos.getCell(celula)));
                servicosPorColuna.put(celula, numServico);
                celula++;
            }

//          LENDO EMPRESAS
            for (Row row : sheet0) {
                if (row.getRowNum() <= 1 || getValorCell(row.getCell(0)).isEmpty()) {
                    continue;
                }

                int num = (int) Double.parseDouble(getValorCell(row.getCell(0)));
                String nome = getValorCell(row.getCell(1));
                String endereco = getValorCell(row.getCell(2));
                String CNPJ = getValorCell(row.getCell(3));
                String InscrCCM = getValorCell(row.getCell(4));
                String InscrEST = getValorCell(row.getCell(5));

                List<Servicos> servicos = new ArrayList<>();

                for (Map.Entry<Integer, Integer> servicoPorColuna : servicosPorColuna.entrySet()) {
                    String valorServico = getValorCell(row.getCell(servicoPorColuna.getKey()));

                    if (valorServico.isEmpty()) {
                        continue;
                    }

                    int chave = servicoPorColuna.getValue();
                    servicos.add(new Servicos(servicosNome.get(chave), chave, Double.parseDouble(valorServico)));
                }

                linhas.add(new Empresas(nome, num, endereco, CNPJ, InscrCCM, InscrEST, servicos));

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
