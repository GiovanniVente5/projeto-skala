package org.example.projeto_skala.controlePDF;

import com.itextpdf.io.font.PdfEncodings;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
                    carregarRecurso(FONT_RESOURCE), PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
            PdfPage pagina = pdfDocument.getFirstPage();
            PdfCanvas canvas = new PdfCanvas(pagina);

//          Emissão
            LocalDate hoje = LocalDate.now();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataFormatada = hoje.format(formato);

            escrever(canvas, fonte, dataFormatada, 430, 738, 100);

//          Numero fatura
            escrever(canvas, fonte, String.valueOf(empresa.getNum() + hoje.getDayOfMonth()), 440, 724, 100);

//          Vencimento
            LocalDate mes = hoje.plusMonths(1);
            String dataFormatada1 = mes.format(formato);
            escrever(canvas, fonte, dataFormatada1, 445, 711, 100);

//          Dados cliente
            escrever(canvas, fonte, empresa.getNome(), 77, 640, 400);
            escrever(canvas, fonte, empresa.getEndereco(), 77, 630, 400);
            escrever(canvas, fonte, "CNPJ/MF: " + empresa.getCNPJ(), 77, 620, 150);
            escrever(canvas, fonte, "INSCR. EST: : " + empresa.getInscrEST(), 207, 620, 150);
            escrever(canvas, fonte, "INSCR. CCM: : " + empresa.getInscrCCM(), 337, 620, 150);

//          Serviços a serem pagos
            double descontosSomados = 0;
            double subTotal = 0;
            int y = 580;
            for (Servicos servicos : empresa.getServicos()) {
                escrever(canvas, fonte, servicos.getNome(), 77, y, 250);
                if (servicos.getValor() < 0) {
                    escrever(canvas, fonte, String.valueOf(servicos.getValor()), 487, y, 250);
                    descontosSomados += servicos.getValor();
                } else {
                    escrever(canvas, fonte, String.valueOf(servicos.getValor()), 490, y, 250);
                    subTotal += servicos.getValor();
                }
                y = y - 10;
            }
//          todos os descontos
            escrever(canvas, fonte, String.valueOf(descontosSomados), 487, 409, 100);

//          SubTotal
            escrever(canvas, fonte, String.valueOf(subTotal), 487, 375, 100);

//          Descontos
            escrever(canvas, fonte, String.valueOf(descontosSomados), 487, 325, 100);

//          Liquido a receber
            escrever(canvas, fonte, String.valueOf(descontosSomados + subTotal), 487, 308, 100);

        }
    }

    private static InputStream abrirRecurso(String caminho) throws IOException {
        InputStream inputStream = GerarPDF.class.getResourceAsStream(caminho);

        if (inputStream == null) {
            throw new FileNotFoundException("Recurso nao encontrado no pacote: " + caminho);
        }

        return inputStream;
    }

    private static byte[] carregarRecurso(String caminho) throws IOException {
        try (InputStream inputStream = abrirRecurso(caminho)) {
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
