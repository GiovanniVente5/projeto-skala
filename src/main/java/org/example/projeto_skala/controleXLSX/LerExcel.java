package org.example.projeto_skala.controleXLSX;

import org.apache.poi.ss.usermodel.*;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

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
            int celula = 7;

            while (!getValorCell(rowServicos.getCell(celula)).isEmpty()) {
                int numServico = (int) Double.parseDouble(getValorCell(rowServicos.getCell(celula)));
                servicosPorColuna.put(celula, numServico);
                celula++;
            }

//          LENDO EMPRESAS
            int id = 1;
            long numFatura = 1;
            for (Row row : sheet0) {
                if (row.getRowNum() == 0){
                    numFatura = (long) Double.parseDouble(getValorCell(row.getCell(3)));
                }
                if (row.getRowNum() <= 1 || getValorCell(row.getCell(0)).isEmpty()) {
                    continue;
                }

                int num = (int) Double.parseDouble(getValorCell(row.getCell(0)));
                String nome = getValorCell(row.getCell(1));
                int diaVencimento = (int) Double.parseDouble(getValorCell(row.getCell(2)));
                String endereco = getValorCell(row.getCell(3));
                String CNPJ = getValorCell(row.getCell(4));

                DataFormatter format = new DataFormatter();
                String InscrCCM = format.formatCellValue(row.getCell(5));
                String InscrEST = format.formatCellValue(row.getCell(6));


                List<Servicos> servicos = new ArrayList<>();

                for (Map.Entry<Integer, Integer> servicoPorColuna : servicosPorColuna.entrySet()) {
                    String valorServico = getValorCell(row.getCell(servicoPorColuna.getKey()));

                    if (valorServico.isEmpty()) {
                        continue;
                    }

                    int chave = servicoPorColuna.getValue();
                    if (Double.parseDouble(valorServico) != 0) {
                        servicos.add(new Servicos(servicosNome.get(chave), chave, Double.parseDouble(valorServico)));
                    }
                }
                if (!servicos.isEmpty()) {
                    linhas.add(new Empresas(id,nome, num, numFatura, diaVencimento, endereco, CNPJ, InscrCCM, InscrEST, servicos));
                    numFatura++;
                    id++;
                }
            }
            sheet0.getRow(0).getCell(3).setCellValue(String.valueOf(numFatura));
            try (FileOutputStream fos = new FileOutputStream(file)){
                workbook.write(fos);
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
