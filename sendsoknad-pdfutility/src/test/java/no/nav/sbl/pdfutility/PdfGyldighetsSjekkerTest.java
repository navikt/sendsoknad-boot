package no.nav.sbl.pdfutility;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PdfGyldighetsSjekkerTest {

    @Test
    public void testAtFeilKastesDersomPDFErEndringsbeskyttet() throws IOException {
        try {
            byte[] imgData = FilHjelpUtility.getBytesFromFile("/pdfs/endringsbeskyttet.pdf");
            PdfGyldighetsSjekker.erGyldig(imgData);
            fail("Expected exception to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Klarte ikke å sjekke om vedlegget er gyldig", e.getMessage());
        }
    }

    @Test
    public void OpplastingAvNormalPdfSkalGaBra() throws Exception {
        PdfGyldighetsSjekker.erGyldig(FilHjelpUtility.getBytesFromFile("/pdfs/minimal.pdf"));
    }
}
