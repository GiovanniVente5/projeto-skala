package org.example.projeto_skala.controleTXT;

import org.example.projeto_skala.objetos.Empresas;
import java.io.IOException;
import java.nio.file.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class gerarTXT {
    public static void gerarRelatorio(List<Empresas> empresas, String destino, String nomeArquivo) throws IOException {
        Path caminho;

        NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "BR"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);

        empresas.sort(Comparator.comparing(Empresas::getDiaVencimento));

        int diaAjustado = Math.min(empresas.getFirst().getDiaVencimento(), mesAtual.lengthOfMonth());
        LocalDate vencimentoDiaUm = mesAtual.atDay(diaAjustado);
        LocalDate vencimentoDiaDois = mesAtual.atDay(mesAtual.lengthOfMonth());

        String cabecalho = String.format("%-4s | %-70s | %-10s | %-7s | %-6s",
                "N°","Nome","Vencimento","N° Fatura","Valor");
        caminho = Files.writeString(Path.of(destino + nomeArquivo + ".txt"),
                "Relatório Faturas | Relatório feito dia: " + formato.format(hoje) +
                        " | Periodo de vencimento: " + formato.format(vencimentoDiaUm) + " a " + formato.format(vencimentoDiaDois), StandardOpenOption.CREATE);
        Files.writeString(caminho,"\n"+cabecalho,StandardOpenOption.APPEND);
        Files.writeString(caminho, "\n----------------------------------------------------------------------------------", StandardOpenOption.APPEND);

        int diaAnterior = 1;
        for (Empresas emp : empresas) {
            if (diaAnterior != emp.getDiaVencimento()){
                Files.writeString(caminho,"\n",StandardOpenOption.APPEND);
                diaAnterior = emp.getDiaVencimento();
            }
            String empresasTXT = formatarLinha(emp.getNum(),emp.getNome(),formato.format(emp.calcularVencimento()),emp.getNumFatura(),nf.format(emp.getValorTotal()));
            Files.writeString(caminho,
                    "\n" + empresasTXT, StandardOpenOption.APPEND);
        }
    }

    private static String formatarLinha(int numEmp, String nomeEmp, String vencimento, long numFatura, String valorTotal) {
        return String.format("%-4s | %-70s | %-10s | %-5s | %-6s",
                numEmp,
                nomeEmp,
                vencimento,
                numFatura,
                valorTotal);
    }
}

