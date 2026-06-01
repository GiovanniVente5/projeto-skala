package org.example.projeto_skala.controlePDF;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.Canvas;
import com.itextpdf.kernel.geom.Rectangle;

import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.renderer.IRenderer;
import org.example.projeto_skala.controleTXT.gerarTXT;
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

import static com.itextpdf.layout.borders.Border.DOTTED;
import static com.itextpdf.layout.borders.Border.NO_BORDER;

public class GerarPDF {
    private static final String TEMPLATE_RESOURCE = "/org/example/projeto_skala/controlePDF/PdfTemplate/Recibo-Template.pdf";
    private static final String FONT_RESOURCE = "/org/example/projeto_skala/controlePDF/PdfTemplate/ARIAL.TTF";

    public static void gerarRecibo(Empresas empresa, File outDir) throws IOException {
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("Nao foi possivel criar a pasta de destino: " + outDir.getAbsolutePath());
        }


        String destino = outDir.getPath() + File.separator + "Empresa-" + empresa.getNum() + "-Fatura-" + empresa.getNumFatura() + ".pdf";

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

            escrever(canvas, fonte, new Paragraph(dataFormatada), 430, 737, 100);

//          Numero fatura
            escrever(canvas, fonte, new Paragraph(String.valueOf(empresa.getNumFatura())), 440, 724, 100);

//          Vencimento
            escrever(canvas, fonte, new Paragraph(empresa.calcularVencimento().format(formato)), 445, 710, 100);

//          Dados cliente
            escrever(canvas, fonte, new Paragraph(empresa.getNome()).setBold(), 77, 640, 600);
            escrever(canvas, fonte, new Paragraph(empresa.getEndereco()), 77, 630, 400);
            escrever(canvas, fonte, new Paragraph("CNPJ/MF: " + empresa.getCNPJ()), 77, 620, 150);
            escrever(canvas, fonte, new Paragraph("INSCR. EST: " + empresa.getInscrEST()), 207, 620, 150);
            escrever(canvas, fonte, new Paragraph("INSCR. CCM: " + empresa.getInscrCCM()), 357, 620, 150);

//          Numero empresa
            escrever(canvas, fonte, new Paragraph(String.valueOf(empresa.getNum())), 415, 594, 100);

//          Serviços a serem pagos
            Document servicosDoc = new Document(pdfDocument).setFont(fonte).setFontSize(8);
            servicosDoc.setMargins(248, 145, 20, 77);

            Document valoresDoc = new Document(pdfDocument).setFont(fonte).setFontSize(8).setTextAlignment(TextAlignment.RIGHT);
            valoresDoc.setMargins(248, 50, 10, 160);

            NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "BR"));
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);


//                for (Servicos servicos : empresa.getServicos()) {
//                    if (!(servicos.getValor() < 0)) {
//                        servicosDoc.add(new Paragraph(servicos.getNome()));
//                        valoresDoc.add(new Paragraph(nf.format(servicos.getValor())))
//                                .setFixedPosition(0, servicosDoc.getLeftMargin() + 257, y,20);
//                    }
//                    double valor = servicos.getValor();
//                    if (valor < 0) descontosSomados += valor;
//                    else subTotal += valor;
//                }

            double descontosSomados = 0;
            double subTotal = 0;

            int y = 565;
            for (Servicos servico : empresa.getServicos()) {
                if (servico.getValor() > 0) {
                    subTotal += servico.getValor();

                    Paragraph pServ = new Paragraph(servico.getNome())
                            .setTextAlignment(TextAlignment.LEFT);
//                                .setFixedPosition(1,77,y,370);

                    Paragraph pValor = new Paragraph(nf.format(servico.getValor()))
                            .setTextAlignment(TextAlignment.RIGHT);
//                                .setFixedPosition(1,445,y,100);

                    escrever(canvas, fonte, pValor, 490, y, 370,20, TextAlignment.RIGHT);
                    escrever(canvas, fonte, pServ, 77, y, 370,20, TextAlignment.RIGHT);
//                        servicosDoc.add(pServ).setFixedPosition(1,77,1,370);
//                        valoresDoc.add(pValor).setFixedPosition(1,445,y,100);

                    y -= 20;
                } else {
                    descontosSomados += servico.getValor();
                }

            }

//          todos os descontos
            valoresDoc.add(new Paragraph(nf.format(descontosSomados)).setFixedPosition(495, 408, 50));


//          SubTotal
            valoresDoc.add(new Paragraph(nf.format(subTotal)).setFixedPosition(495, 375, 50));

//          Descontos
            valoresDoc.add(new Paragraph(nf.format(descontosSomados)).setFixedPosition(495, 325, 50));

//          Liquido a receber
            valoresDoc.add(new Paragraph(nf.format(descontosSomados + subTotal)).setFixedPosition(495, 308, 50));

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
                                 Paragraph texto, float x, float y,
                                 float largura) throws IOException {
        Rectangle area = new Rectangle(x, y, largura, (float) 8 + 4);

        try (Canvas canvas = new Canvas(pdfCanvas, area)) {
            canvas.add(texto
                    .setFont(fonte)
                    .setFontSize((float) 8)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMargin(0));
        }
    }

    private static void escrever(PdfCanvas pdfCanvas,
                                 PdfFont fonte,
                                 Paragraph texto, float x, float y,
                                 float largura,
                                 float altura, // Novo parâmetro para permitir múltiplas linhas
                                 TextAlignment alinhamento) throws IOException {

        // O retângulo agora usa a altura passada por parâmetro
        Rectangle area = new Rectangle(x, y, largura, altura);

        try (Canvas canvas = new Canvas(pdfCanvas, area)) {
            canvas.add(texto
                    .setFont(fonte)
                    .setFontSize(8f)
                    .setTextAlignment(alinhamento)
                    .setMargin(0));
        }
    }
}
