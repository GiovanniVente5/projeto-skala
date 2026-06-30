package org.example.projeto_skala.controleXLSX;

import org.apache.poi.ss.usermodel.*;
import org.example.projeto_skala.objetos.Banco;
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
            Sheet sheet2 = workbook.getSheetAt(2);

            coordenadasCell cord = new coordenadasCell(sheet0);
            Map<Integer, String> servicosNome = new HashMap<>();

//          LENDO BANCOS
            LinkedHashMap<Integer, Banco> bancos = new LinkedHashMap<>();
            List<String> bancoInfo = new ArrayList<>();
            List<Banco> bancoList = new ArrayList<>();
            int rowNum = 1;
            double bancoNumSheet = 1.0;
            boolean test = true;

            while (test) {
                Row row = sheet2.getRow(rowNum);
                if (row != null) {
                    if (getValorCell(row.getCell(0)).equals(String.valueOf(bancoNumSheet)) || getValorCell(row.getCell(0)).equals("")) {
                        bancoInfo.add((getValorCell(row.getCell(1))));
                        rowNum++;
                    } else {
                        bancoNumSheet += 1;
                        if (bancoInfo.size() > 2) {
                            bancoList.add(new Banco(bancoInfo.get(0), bancoInfo.get(1), bancoInfo.get(2), bancoInfo.get(3), bancoInfo.get(4)));
                        } else {
                            bancoList.add(new Banco(bancoInfo.getFirst(), bancoInfo.getLast()));
                        }
                        bancoInfo = new ArrayList<>();
                    }
                } else {
                    if (bancoInfo.size() > 2) {
                        bancoList.add(new Banco(bancoInfo.get(0), bancoInfo.get(1), bancoInfo.get(2), bancoInfo.get(3), bancoInfo.get(4)));
                    } else {
                        bancoList.add(new Banco(bancoInfo.getFirst(), bancoInfo.getLast()));
                    }
                    test = false;
                }
            }

            for (int i = 0; i < bancoList.size(); i++) {
                bancos.put(i, bancoList.get(i));
            }

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
            int celula = 8;

            while (!getValorCell(rowServicos.getCell(celula)).isEmpty()) {
                int numServico = (int) Double.parseDouble(getValorCell(rowServicos.getCell(celula)));
                servicosPorColuna.put(celula, numServico);
                celula++;
            }

//          LENDO EMPRESAS
            int id = 1;
            long numFatura = 0;
            int bancoNUM = 0;
            for (Row row : sheet0) {
                if (row.getRowNum() == 0) {
                    numFatura = (long) Double.parseDouble(getValorCell(row.getCell(cord.getCoordenada())));
                }
                if (row.getRowNum() <= 1 || getValorCell(row.getCell(0)).isEmpty()) {
                    continue;
                }

                int num = (int) Double.parseDouble(getValorCell(row.getCell(0)));
                String nome = getValorCell(row.getCell(1));
                int diaVencimento = (int) Double.parseDouble(getValorCell(row.getCell(2)));

                if (!row.getCell(3).getCellType().equals(CellType.BLANK)){
                    bancoNUM = (int) Double.parseDouble(getValorCell(row.getCell(3)))-1;
                } else {
                    bancoNUM = 0;
                }

                String endereco = getValorCell(row.getCell(4));
                String CNPJ = getValorCell(row.getCell(5));

                DataFormatter format = new DataFormatter();
                String InscrCCM = format.formatCellValue(row.getCell(6));
                String InscrEST = format.formatCellValue(row.getCell(7));


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
                    Empresas emp = new Empresas(id, nome, num, numFatura, diaVencimento, cord.mesVencimento, cord.getDataEmissao(), endereco, CNPJ, InscrCCM, InscrEST, servicos, bancos.get(bancoNUM), cord.getTexto());
                    linhas.add(emp);
                    numFatura++;
                    id++;
                }
            }
            sheet0.getRow(0).getCell(cord.getCoordenada()).setCellValue(numFatura);
            try (FileOutputStream fos = new FileOutputStream(file)) {
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
                return "";
            default:
                return "";
        }
    }
}

