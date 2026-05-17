package org.example.projeto_skala.controlePDF;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.Canvas;
import com.itextpdf.kernel.geom.Rectangle;

import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class GerarPDF {
    //    Formato temporario, depois criar pdf pelo codigo usando o itex
//    para permitir edição de imagens e formatação do recibo
    public static void gerarRecibo(Empresas empresa) throws IOException {
        final String origem = "src/main/java/org/example/projeto_skala/controlePDF/PdfTemplate/Recibo-Template.pdf";
        String destino = "data/RecibosGerados/Empresa-Num-" + empresa.getNum() + ".pdf";

        try (PdfReader reader = new PdfReader(origem);
             PdfWriter writer = new PdfWriter(destino);
             PdfDocument pdfDocument = new PdfDocument(reader, writer)) {

            final String fontPath = "src/main/java/org/example/projeto_skala/controlePDF/PdfTemplate/ARIAL.TTF";
            PdfFont fonte = PdfFontFactory.createFont(
                    fontPath, PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
            PdfPage pagina = pdfDocument.getFirstPage();
            PdfCanvas canvas = new PdfCanvas(pagina);

//          Emissão
            LocalDate hoje = LocalDate.now();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataFormatada = hoje.format(formato);

            escrever(canvas, pdfDocument, fonte, 8, dataFormatada, 430, 738, 100);

//          Numero fatura
            escrever(canvas, pdfDocument, fonte, 8, String.valueOf(empresa.getNum() + hoje.getDayOfMonth()), 440, 724, 100);

//          Vencimento
            LocalDate mes = hoje.plusMonths(1);
            String dataFormatada1 = mes.format(formato);
            escrever(canvas, pdfDocument, fonte, 8, dataFormatada1, 445, 711, 100);

//          Dados cliente
            escrever(canvas, pdfDocument, fonte, 8, empresa.getNome(), 77, 640, 400);
            escrever(canvas, pdfDocument, fonte, 8, empresa.getEndereco(), 77, 630, 400);
            escrever(canvas, pdfDocument, fonte, 8, "CNPJ/MF: " + empresa.getCNPJ(), 77, 620, 150);
            escrever(canvas, pdfDocument, fonte, 8, "INSCR. EST: : " + empresa.getInscrEST(), 207, 620, 150);
            escrever(canvas, pdfDocument, fonte, 8, "INSCR. CCM: : " + empresa.getInscrCCM(), 337, 620, 150);

//          Serviços a serem pagos
            double descontosSomados = 0;
            double subTotal = 0;
            int y = 580;
            for (Servicos servicos : empresa.getServicos()) {
                escrever(canvas, pdfDocument, fonte, 8, servicos.getNome(), 77, y, 250);
                if (servicos.getValor() < 0) {
                    escrever(canvas, pdfDocument, fonte, 8, String.valueOf(servicos.getValor()), 487, y, 250);
                    descontosSomados += servicos.getValor();
                } else {
                    escrever(canvas, pdfDocument, fonte, 8, String.valueOf(servicos.getValor()), 490, y, 250);
                    subTotal += servicos.getValor();
                }
                y = y - 10;
            }
//          todos os descontos
            escrever(canvas, pdfDocument, fonte, 8, String.valueOf(descontosSomados), 487, 409, 100);

//          SubTotal
            escrever(canvas, pdfDocument, fonte, 8, String.valueOf(subTotal), 487, 375, 100);

//          Descontos
            escrever(canvas, pdfDocument, fonte, 8, String.valueOf(descontosSomados), 487, 325, 100);

//          Liquido a receber
            escrever(canvas, pdfDocument, fonte, 8, String.valueOf(descontosSomados+subTotal), 487, 308, 100);

        }
        System.out.println("Foi?");

    }

    private static void escrever(PdfCanvas pdfCanvas, PdfDocument pdfDocument,
                                 PdfFont fonte, float tamanho,
                                 String texto, float x, float y,
                                 float largura) throws IOException {
        Rectangle area = new Rectangle(x, y, largura, tamanho + 4);

        try (Canvas canvas = new Canvas(pdfCanvas, area)) {
            canvas.add(new Paragraph(texto)
                    .setFont(fonte)
                    .setFontSize(tamanho)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMargin(0));
        }
    }
}
