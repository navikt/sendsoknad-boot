package no.nav.sbl.pdfutility;

import org.junit.Test;

import static org.junit.Assert.*;

public class PdfGyldighetsSjekkerTest {

    @Test
    public void testAtFeilKastesDersomPDFErEndringsbeskyttet() {
        try {
            byte[] imgData = FilHjelpUtility.getBytesFromFile("/pdfs/endringsbeskyttet.pdf");
            PdfGyldighetsSjekker.erGyldig(imgData);
            fail("Expected exception to be thrown");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
    }

    @Test
    public void OpplastingAvNormalPdfSkalGaBra() throws Exception {
        PdfGyldighetsSjekker.erGyldig(FilHjelpUtility.getBytesFromFile("/pdfs/minimal.pdf"));
    }
}
