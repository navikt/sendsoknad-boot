package no.nav.sbl.pdfutility;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.pdfutility.FiletypeSjekker.isPdf;
import static org.junit.Assert.*;

public class PdfMergerTest {

    @Test
    public void mergePdfer() throws IOException {
        List<byte[]> pdfListe = new ArrayList<>();
        pdfListe.add(KonverterTilPdf.createPDFFromImage(FilHjelpUtility.getBytesFromFile("/images/skog.jpg")));
        pdfListe.add(FilHjelpUtility.getBytesFromFile("/pdfs/navskjema.pdf"));

        byte[] merged = PdfMerger.mergePdfer(pdfListe);

        assertNotNull(merged);
        assertTrue(isPdf(merged));
    }

    @Test
    public void finnAntallSider() throws IOException {
        byte[] pdf = FilHjelpUtility.getBytesFromFile("/pdfs/navskjema.pdf");

        int antall = PdfMerger.finnAntallSider(pdf);

        assertEquals(5, antall);
    }

}