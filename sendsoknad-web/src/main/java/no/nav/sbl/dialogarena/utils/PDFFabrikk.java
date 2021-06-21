package no.nav.sbl.dialogarena.utils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import org.apache.commons.io.IOUtils;
import org.xhtmlrenderer.pdf.DefaultPDFCreationListener;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PDFFabrikk {

    /**
     * Lag en pdf fra en html-string.
     */
    public static byte[] lagPdfFil(String html, String skjemaPath) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer() {

            };
            renderer.setListener(new DefaultPDFCreationListener() {
                @Override
                public void preOpen(ITextRenderer iTextRenderer) {
                    iTextRenderer.getWriter().setPDFXConformance(PdfWriter.PDFA1A);
                    iTextRenderer.getWriter().createXmpMetadata();

                    super.preOpen(iTextRenderer);
                }
            });

            renderer.setDocumentFromString(html, skjemaPath);

            renderer.getFontResolver().addFont("/fonts/modus/ModusRegular.ttf", "Modus", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/modus/ModusLight.ttf", "Modus", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/modus/ModusBold.ttf", "Modus", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/modus/ModusSemiBold.ttf", "Modus", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/arial/arial.ttf", "ArialSystem", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/arial/arialbd.ttf", "ArialSystem", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);

            renderer.getFontResolver().addFont("/fonts/arial/ArialMT.ttf", "Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/arial/ArialMT.ttf", "Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);

            renderer.getFontResolver().addFont("/fonts/arial/Arial-BoldMT.ttf", "Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/arial/Arial-BoldItalicMT.ttf", "Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);

            renderer.layout();
            renderer.setPDFVersion(PdfWriter.VERSION_1_4);
            renderer.createPDF(os, false, 0);
            renderer.getWriter().setOutputIntents("Custom", "PDF/A", "http://www.color.org", "AdobeRGB1998",
                    IOUtils.toByteArray(PDFFabrikk.class.getClassLoader().getResourceAsStream("AdobeRGB1998.icc")));
            renderer.finishPDF();
            return os.toByteArray();
        } catch (DocumentException|IOException e) {
            throw new SendSoknadException("Kunne ikke lagre oppsummering som PDF", e);
        }
    }
}
