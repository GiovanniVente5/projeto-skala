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

import java.io.IOException;

public class GerarPDF {
//    Formato temporario, depois criar pdf pelo codigo usando o itex
//    para permitir edição de imagens e formatação do recibo
    public static void gerarRecibo (Empresas empresa) throws IOException{
        final String origem = "src/main/java/org/example/projeto_skala/controlePDF/PdfTemplate/Recibo-Template.pdf";
        String destino = "data/RecibosGerados/Empresa-Num-"+ empresa.getNum() +".pdf";

        try (PdfReader reader = new PdfReader(origem);
             PdfWriter writer = new PdfWriter(destino);
             PdfDocument pdfDocument = new PdfDocument(reader ,writer)){

            final String fontPath = "src/main/java/org/example/projeto_skala/controlePDF/PdfTemplate/ARIAL.TTF";
            PdfFont fonte = PdfFontFactory.createFont(
                fontPath, PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
            PdfPage pagina = pdfDocument.getFirstPage();
            PdfCanvas canvas = new PdfCanvas(pagina);

            escrever(canvas,pdfDocument,fonte,8,empresa.getNome(),77,640,400);
            escrever(canvas,pdfDocument,fonte,8,empresa.getEndereco(),77,630,400);
            escrever(canvas,pdfDocument,fonte,8,"CNPJ/MF: "+empresa.getCNPJ(),77,620,150);
            escrever(canvas,pdfDocument,fonte,8,"INSCR. EST: : "+empresa.getInscrEST(),207,620,150);
        }
        System.out.println("Foi?");

    }

    private static void escrever(PdfCanvas pdfCanvas, PdfDocument pdfDocument,
                                PdfFont fonte, float tamanho,
                                String texto, float x, float y,
                                float largura) throws IOException {
        Rectangle area = new Rectangle(x,y,largura,tamanho + 4);

        try (Canvas canvas = new Canvas(pdfCanvas, area)){
            canvas.add(new Paragraph(texto)
                    .setFont(fonte)
                    .setFontSize(tamanho)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMargin(0));
        }
    }
}
