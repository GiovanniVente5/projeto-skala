package org.example.projeto_skala.controlePDF;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BorderCollapsePropertyValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.example.projeto_skala.Json.JsonReciboConfig;
import org.example.projeto_skala.objetos.Empresas;
import org.example.projeto_skala.objetos.ReciboCampoConfig;
import org.example.projeto_skala.objetos.ReciboConfig;
import org.example.projeto_skala.objetos.Servicos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static com.itextpdf.layout.borders.Border.NO_BORDER;

public class GerarPDF {
    private static final String TEMPLATE_RESOURCE = JsonReciboConfig.DEFAULT_TEMPLATE_RESOURCE;
    private static final String FONT_RESOURCE = "/org/example/projeto_skala/controlePDF/PdfTemplate/ARIAL.TTF";

    public static void gerarRecibo(Empresas empresa, File outDir, String emissao) throws IOException {
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("Nao foi possivel criar a pasta de destino: " + outDir.getAbsolutePath());
        }

        String destino = outDir.getPath() + File.separator + "Empresa-" + empresa.getNum() + "-Fatura-" + empresa.getNumFatura() + ".pdf";
        ReciboConfig config = JsonReciboConfig.carregarOuCriarPadrao();

        try (InputStream template = abrirTemplate(config);
             PdfReader reader = new PdfReader(template);
             PdfWriter writer = new PdfWriter(destino);
             PdfDocument pdfDocument = new PdfDocument(reader, writer)) {

            PdfFont fonte = PdfFontFactory.createFont(carregarRecurso(), PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            PdfPage pagina = pdfDocument.getFirstPage();
            PdfCanvas canvas = new PdfCanvas(pagina);

            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String emissaoFormato = "01/" + emissao.replace("-", "/");
            escreverCampo(canvas, fonte, config, "emissao", emissaoFormato, false, TextAlignment.LEFT);
            escreverCampo(canvas, fonte, config, "numeroFatura", String.valueOf(empresa.getNumFatura()), false, TextAlignment.LEFT);
            escreverCampo(canvas, fonte, config, "vencimento", empresa.calcularVencimento().format(formato), false, TextAlignment.LEFT);

            escreverCampo(canvas, fonte, config, "nomeCliente", empresa.getNome(), true, TextAlignment.LEFT);
            escreverCampo(canvas, fonte, config, "endereco", empresa.getEndereco(), false, TextAlignment.LEFT);
            escreverCampo(canvas, fonte, config, "cnpj", "CNPJ/MF: " + textoSeguro(empresa.getCNPJ()), false, TextAlignment.LEFT);
            escreverCampo(canvas, fonte, config, "inscrEst", "INSCR. EST: " + textoSeguro(empresa.getInscrEST()), false, TextAlignment.LEFT);
            escreverCampo(canvas, fonte, config, "inscrCcm", "INSCR. CCM: " + textoSeguro(empresa.getInscrCCM()), false, TextAlignment.LEFT);
            escreverCampo(canvas, fonte, config, "numeroEmpresa", String.valueOf(empresa.getNum()), false, TextAlignment.LEFT);

            NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "BR"));
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            List<Servicos> servicos = empresa.getServicos() == null ? List.of() : empresa.getServicos();
            double descontosSomados = 0;
            double subTotal = 0;

            for (Servicos servico : servicos) {
                Double valor = servico.getValor();
                if (valor == null) {
                    continue;
                }

                if (valor > 0) {
                    subTotal += valor;
                } else {
                    descontosSomados += valor;
                }
            }

            escreverServicos(canvas, fonte, config, servicos, nf);
            escreverCampo(canvas, fonte, config, "descontosSomados", nf.format(descontosSomados), false, TextAlignment.RIGHT);
            escreverCampo(canvas, fonte, config, "subtotal", nf.format(subTotal), false, TextAlignment.RIGHT);
            escreverCampo(canvas, fonte, config, "descontos", nf.format(descontosSomados), false, TextAlignment.RIGHT);
            escreverCampo(canvas, fonte, config, "liquidoReceber", nf.format(descontosSomados + subTotal), false, TextAlignment.RIGHT);

            List<String> banco = empresa.getBanco() == null || empresa.getBanco().getInfo() == null
                    ? List.of()
                    : empresa.getBanco().getInfo();
            escreverCampo(canvas, fonte, config, "banco", String.join("\n", banco), true, TextAlignment.LEFT);
            escreverCampo(canvas, fonte, config, "mensagem", empresa.getTexto(), true, TextAlignment.LEFT);
        }
    }

    private static void escreverServicos(PdfCanvas pdfCanvas, PdfFont fonte, ReciboConfig config, List<Servicos> servicos, NumberFormat nf) {
        ReciboCampoConfig campo = getCampo(config, "servicos");
        float largura = (float) campo.getLargura();
        float colunaValor = Math.max(60f, Math.min(100f, largura * 0.25f));
        float colunaServico = Math.max(40f, largura - colunaValor);

        Table tabela = new Table(UnitValue.createPointArray(new float[]{colunaServico, colunaValor}));
        tabela.setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);
        tabela.setMargin(0);

        for (Servicos servico : servicos) {
            Double valor = servico.getValor();
            if (valor != null && valor > 0) {
                tabela.addCell(celulaSemBorda(textoSeguro(servico.getNome()), fonte, TextAlignment.LEFT));
                tabela.addCell(celulaSemBorda(nf.format(valor), fonte, TextAlignment.RIGHT));
            }
        }

        try (Canvas canvas = new Canvas(pdfCanvas, toRectangle(campo))) {
            canvas.add(tabela);
        }
    }

    private static void escreverCampo(PdfCanvas pdfCanvas, PdfFont fonte, ReciboConfig config, String campoId, String texto, boolean negrito, TextAlignment alinhamento) {
        Paragraph paragraph = new Paragraph(textoSeguro(texto))
                .setFont(fonte)
                .setFontSize(8)
                .setTextAlignment(alinhamento)
                .setMargin(0)
                .setMultipliedLeading(1.0f);

        if (negrito) {
            paragraph.setBold();
        }

        escrever(pdfCanvas, fonte, paragraph, getCampo(config, campoId), alinhamento);
    }

    private static void escrever(PdfCanvas pdfCanvas, PdfFont fonte, Paragraph texto, ReciboCampoConfig campo, TextAlignment alinhamento) {
        try (Canvas canvas = new Canvas(pdfCanvas, toRectangle(campo))) {
            canvas.add(texto.setFont(fonte).setFontSize(8).setTextAlignment(alinhamento).setMargin(0));
        }
    }

    private static Rectangle toRectangle(ReciboCampoConfig campo) {
        return new Rectangle(
                (float) campo.getX(),
                (float) campo.getY(),
                (float) campo.getLargura(),
                (float) campo.getAltura()
        );
    }

    private static ReciboCampoConfig getCampo(ReciboConfig config, String campoId) {
        ReciboCampoConfig campo = config.getCampo(campoId);
        return campo == null ? JsonReciboConfig.getCampoPadrao(campoId) : campo;
    }

    private static String textoSeguro(String texto) {
        return texto == null ? "" : texto;
    }

    private static InputStream abrirTemplate(ReciboConfig config) throws IOException {
        String templatePath = config.getTemplatePath();

        if (templatePath != null && !templatePath.isBlank()) {
            File templateFile = new File(templatePath);
            if (templateFile.isFile()) {
                return new FileInputStream(templateFile);
            }
        }

        return abrirRecurso(TEMPLATE_RESOURCE);
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
        ReciboCampoConfig campo = new ReciboCampoConfig(x, y, largura, 12);
        escrever(pdfCanvas, fonte, texto, campo, TextAlignment.LEFT);
    }

    static Cell celulaSemBorda(String texto, PdfFont fonte, TextAlignment alinhamento) {
        return new Cell()
                .add(new Paragraph(texto).setFont(fonte).setFontSize(8))
                .setBorder(NO_BORDER)
                .setTextAlignment(alinhamento)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingTop(6f)
                .setPaddingBottom(6f);
    }
}
