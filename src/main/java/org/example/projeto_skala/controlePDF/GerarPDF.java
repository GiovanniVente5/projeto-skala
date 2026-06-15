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
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BorderCollapsePropertyValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.Canvas;
import com.itextpdf.kernel.geom.Rectangle;

import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;


import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.Servicos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.text.NumberFormat;

import java.time.format.DateTimeFormatter;

import java.util.Locale;

import static com.itextpdf.layout.borders.Border.NO_BORDER;

public class GerarPDF {
    private static final String TEMPLATE_RESOURCE = "/org/example/projeto_skala/controlePDF/PdfTemplate/Recibo-Template.pdf";
    private static final String FONT_RESOURCE = "/org/example/projeto_skala/controlePDF/PdfTemplate/ARIAL.TTF";

    public static void gerarRecibo(Empresas empresa, File outDir, String emissao) throws IOException {


        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("Nao foi possivel criar a pasta de destino: " + outDir.getAbsolutePath());
        }

        String destino = outDir.getPath() + File.separator + "Empresa-" + empresa.getNum() + "-Fatura-" + empresa.getNumFatura() + ".pdf";

        try (InputStream template = abrirRecurso(TEMPLATE_RESOURCE); PdfReader reader = new PdfReader(template); PdfWriter writer = new PdfWriter(destino); PdfDocument pdfDocument = new PdfDocument(reader, writer)) {

            PdfFont fonte = PdfFontFactory.createFont(carregarRecurso(), PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            PdfPage pagina = pdfDocument.getFirstPage();
            PdfCanvas canvas = new PdfCanvas(pagina);

//          Emissão

            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String emissaoFormato = "01/" + emissao.replaceAll("-","/");
            escrever(canvas, fonte, new Paragraph(emissaoFormato), 450, 737, 100);

//          Numero fatura
            escrever(canvas, fonte, new Paragraph(String.valueOf(empresa.getNumFatura())), 460, 724, 100);

//          Vencimento
            escrever(canvas, fonte, new Paragraph(empresa.calcularVencimento().format(formato)), 465, 710, 100);

//          Dados cliente
//          nome podendo ocupar duas linhas
            if (empresa.getNome().length() > 90) {
                String linha1 = null;
                String linha2 = null;
                char[] nomeChar = empresa.getNome().toCharArray();

                for (int i = 75; i < 91; i++) {
                    char tempChar = nomeChar[i];
                    if (tempChar == ' ') {
                        linha1 = empresa.getNome().substring(0, i);
                        linha2 = empresa.getNome().substring(i);
                    }
                }

                escrever(canvas, fonte, new Paragraph(linha1).setBold(), 68, 650, 490);
                escrever(canvas, fonte, new Paragraph(linha2).setBold(), 68, 640, 500);
            } else {
                escrever(canvas, fonte, new Paragraph(empresa.getNome()).setBold(), 68, 640, 500);
            }
            escrever(canvas, fonte, new Paragraph(empresa.getEndereco()), 68, 630, 400);
            escrever(canvas, fonte, new Paragraph("CNPJ/MF: " + empresa.getCNPJ()), 68, 620, 150);
            escrever(canvas, fonte, new Paragraph("INSCR. EST: " + empresa.getInscrEST()), 207, 620, 150);
            escrever(canvas, fonte, new Paragraph("INSCR. CCM: " + empresa.getInscrCCM()), 357, 620, 150);

//          Numero empresa
            escrever(canvas, fonte, new Paragraph(String.valueOf(empresa.getNum())), 415, 594, 100);

//          Serviços a serem pagos
            Document servicosDoc = new Document(pdfDocument).setFont(fonte).setFontSize(8);
            servicosDoc.setMargins(248, 10, 20, 68);

            Document valoresDoc = new Document(pdfDocument).setFont(fonte).setFontSize(8).setTextAlignment(TextAlignment.RIGHT);
            valoresDoc.setMargins(248, 44, 10, 160);

            NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "BR"));
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            double descontosSomados = 0;
            double subTotal = 0;

            //          Serviços
            Table tabela = new Table(UnitValue.createPointArray(new float[]{440f, 200f}));
            tabela.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
            tabela.setMarginRight(30);

            for (Servicos servico : empresa.getServicos()) {
                if (servico.getValor() > 0) {
                    tabela.addCell(celulaSemBorda(servico.getNome(), fonte, TextAlignment.LEFT));
                    tabela.addCell(celulaSemBorda(nf.format(servico.getValor()), fonte, TextAlignment.RIGHT));
                    subTotal += servico.getValor();
                } else {
                    descontosSomados += servico.getValor();
                }
            }
            servicosDoc.add(tabela);

//          todos os descontos
            valoresDoc.add(new Paragraph(nf.format(descontosSomados)).setFixedPosition(502, 408, 50));


//          SubTotal
            valoresDoc.add(new Paragraph(nf.format(subTotal)).setFixedPosition(502, 375, 50));

//          Descontos
            valoresDoc.add(new Paragraph(nf.format(descontosSomados)).setFixedPosition(502, 325, 50));

//          Liquido a receber
            valoresDoc.add(new Paragraph(nf.format(descontosSomados + subTotal)).setFixedPosition(502, 308, 50));


//          Texto de mensagem no final do recibo
            if (empresa.getTexto().length() > 90) {
                String linha3 = null;
                String linha4 = null;
                char[] nomeChar = empresa.getTexto().toCharArray();

                for (int i = 75; i < 91; i++) {
                    char tempChar = nomeChar[i];
                    if (tempChar == ' ') {
                        linha3 = empresa.getTexto().substring(0, i);
                        linha4 = empresa.getTexto().substring(i);
                    }
                }
                escrever(canvas, fonte, new Paragraph(linha3).setBold(), 68, 75, 550);
                escrever(canvas, fonte, new Paragraph(linha4).setBold(), 68, 65, 550);
            } else {
                escrever(canvas, fonte, new Paragraph(empresa.getTexto()).setBold(), 68, 65, 550);
            }

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

    public static void escrever(PdfCanvas pdfCanvas, PdfFont fonte, Paragraph texto, float x, float y, float largura) {
        Rectangle area = new Rectangle(x, y, largura, (float) 8 + 4);

        try (Canvas canvas = new Canvas(pdfCanvas, area)) {
            canvas.add(texto.setFont(fonte).setFontSize((float) 8).setTextAlignment(TextAlignment.LEFT).setMargin(0));
        }
    }

    static Cell celulaSemBorda(String texto, PdfFont fonte, TextAlignment alinhamento) {
        return new Cell().add(new Paragraph(texto).setFont(fonte).setFontSize(8)).setBorder(NO_BORDER).setTextAlignment(alinhamento).setVerticalAlignment(VerticalAlignment.MIDDLE).setPaddingTop(6f).setPaddingBottom(6f);
    }
}
