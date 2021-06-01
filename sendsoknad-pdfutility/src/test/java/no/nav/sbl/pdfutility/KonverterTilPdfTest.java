package no.nav.sbl.pdfutility;

import org.apache.pdfbox.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static no.nav.sbl.pdfutility.FiletypeSjekker.isPdf;
import static no.nav.sbl.pdfutility.PdfaSjekker.erPDFA;
import static org.junit.Assert.*;

public class KonverterTilPdfTest {

    @Test
    public void createPDFFromImage() throws IOException {
        byte[] pdf =  KonverterTilPdf.createPDFFromImage(getBytesFromFile("/images/skog.jpg"));
        assertNotNull(pdf);
        assertTrue(isPdf(pdf));
        assertTrue(erPDFA(pdf));
    }

    // PDFBox støtter konvertering av BMP til PDF, men vi har filtrert BMP fra listen av lovlige bildefiltyper
    @Test
    public void createPDFFrom_bmp() throws IOException {
        byte[] pdf =  KonverterTilPdf.createPDFFromImage(getBytesFromFile("/images/edderkopp.bmp"));
        assertNotNull(pdf);
        assertTrue(isPdf(pdf));
        assertTrue(erPDFA(pdf));
    }

    // PDFBox støtter konvertering av GIF til PDF, men vi har filtrert GIF fra listen av lovlige bildefiltyper
    @Test
    public void createPDFFrom_gif() throws IOException {
        byte[] pdf =  KonverterTilPdf.createPDFFromImage(getBytesFromFile("/images/edderkopp.gif"));
        assertNotNull(pdf);
        assertTrue(isPdf(pdf));
        assertTrue(erPDFA(pdf));
    }

    @Test
    public void createPDFFrom_png() throws IOException {
        byte[] pdf =  KonverterTilPdf.createPDFFromImage(getBytesFromFile("/images/edderkopp.png"));
        assertNotNull(pdf);
        assertTrue(isPdf(pdf));
        assertTrue(erPDFA(pdf));
    }

    @Test
    public void testAtImageKonverteresTilPDFA() throws IOException {
        byte[] imgData = getBytesFromFile("/images/bilde.jpg");
        byte[] pdf = KonverterTilPdf.createPDFFromImage(imgData);
        assertTrue(PdfaSjekker.erPDFA(pdf));
    }

    @Test
    public void testAtPDFKanTestesOmPDFA() throws IOException {
        byte[] pdf = getBytesFromFile("/pdfs/TEST_SKJEMA1.pdf");
        assertFalse(PdfaSjekker.erPDFA(pdf));
    }

    private static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = FilHjelpUtility.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }

}
