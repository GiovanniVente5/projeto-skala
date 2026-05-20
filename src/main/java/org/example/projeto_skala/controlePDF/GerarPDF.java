package org.example.projeto_skala.controlePDF;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.Canvas;
import com.itextpdf.kernel.geom.Rectangle;

import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.Locale;

public class GerarPDF {
    private static final String TEMPLATE_RESOURCE = "/org/example/projeto_skala/controlePDF/PdfTemplate/Recibo-Template.pdf";
    private static final String FONT_RESOURCE = "/org/example/projeto_skala/controlePDF/PdfTemplate/ARIAL.TTF";

        public static void gerarRecibo(Empresas empresa, File outDir) throws IOException {
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("Nao foi possivel criar a pasta de destino: " + outDir.getAbsolutePath());
        }

        String destino = outDir.getPath() + File.separator + "Empresa-Num-" + empresa.getNum() + ".pdf";

        try (InputStream template = abrirRecurso(TEMPLATE_RESOURCE);
             PdfReader reader = new PdfReader(template);
             PdfWriter writer = new PdfWriter(destino);
             PdfDocument pdfDocument = new PdfDocument(reader, writer)) {

            PdfFont fonte = PdfFontFactory.createFont(
                    carregarRecurso(), PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
            PdfPage pagina = pdfDocument.getFirstPage();
            PdfCanvas canvas = new PdfCanvas(pagina);

//          Emissão
            LocalDate hoje = LocalDate.now();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataFormatada = hoje.format(formato);

            escrever(canvas, fonte, dataFormatada, 450, 738, 100);

//          Numero fatura
            escrever(canvas, fonte, String.valueOf(empresa.getNumFatura()), 460, 724, 100);

//          Vencimento
            escrever(canvas, fonte, empresa.calcularVencimento().format(formato), 465, 711, 100);

//          Dados cliente
            escrever(canvas, fonte, empresa.getNome(), 68, 640, 400);
            escrever(canvas, fonte, empresa.getEndereco(), 68, 630, 400);
            escrever(canvas, fonte, "CNPJ/MF: " + empresa.getCNPJ(), 68, 620, 150);
            escrever(canvas, fonte, "INSCR. EST: " + empresa.getInscrEST(), 207, 620, 150);
            escrever(canvas, fonte, "INSCR. CCM: " + empresa.getInscrCCM(), 357, 620, 150);

//          Numero empresa
            escrever(canvas, fonte, String.valueOf(empresa.getNum()), 415, 594, 100);

//          Serviços a serem pagos
            Document servicosDoc = new Document(pdfDocument).setFont(fonte).setFontSize(8);
            servicosDoc.setMargins(248, 128, 20, 68);

            Document valoresDoc = new Document(pdfDocument).setFont(fonte).setFontSize(8).setTextAlignment(TextAlignment.RIGHT);
            valoresDoc.setMargins(252, 44, 10, 160);

            NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "BR"));
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            double descontosSomados = 0;
            double subTotal = 0;

            for (Servicos servicos : empresa.getServicos()) {

                servicosDoc.add(new Paragraph(servicos.getNome()));
                valoresDoc.add(new Paragraph(nf.format(servicos.getValor())));
                valoresDoc.add(new Paragraph(""));

                double valor = servicos.getValor();
                if (valor < 0) descontosSomados += valor;
                else subTotal += valor;
            }

//          todos os descontos
            valoresDoc.add(new Paragraph(nf.format(descontosSomados)).setFixedPosition(502, 408, 50));


//          SubTotal
            valoresDoc.add(new Paragraph(nf.format(subTotal)).setFixedPosition(502, 375, 50));

//          Descontos
            valoresDoc.add(new Paragraph(nf.format(descontosSomados)).setFixedPosition(502, 325, 50));

//          Liquido a receber
            valoresDoc.add(new Paragraph(nf.format(descontosSomados + subTotal)).setFixedPosition(502, 308, 50));


        }
    }

    private static InputStream abrirRecurso(String caminho) throws IOException {
        InputStream inputStream = GerarPDF.class.getResourceAsStream(caminho);

        if (inputStream == null) {
            throw new FileNotFoundException("Recurso nao encontrado no pacote: " + caminho);
        }

        return inputStream;
    }

    private static byte[] carregarRecurso() throws IOException {
        try (InputStream inputStream = abrirRecurso(GerarPDF.FONT_RESOURCE)) {
            return inputStream.readAllBytes();
        }
    }

    private static void escrever(PdfCanvas pdfCanvas,
                                 PdfFont fonte,
                                 String texto, float x, float y,
                                 float largura) throws IOException {
        Rectangle area = new Rectangle(x, y, largura, (float) 8 + 4);

        try (Canvas canvas = new Canvas(pdfCanvas, area)) {
            canvas.add(new Paragraph(texto)
                    .setFont(fonte)
                    .setFontSize((float) 8)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMargin(0));
        }
    }
}
