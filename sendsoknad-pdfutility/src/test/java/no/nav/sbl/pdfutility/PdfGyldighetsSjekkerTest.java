package no.nav.sbl.pdfutility;

import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertThrows;

public class PdfGyldighetsSjekkerTest {

    @Test
    public void testAtFeilKastesDersomPDFErEndringsbeskyttet() throws IOException {
        byte[] imgData = FilHjelpUtility.getBytesFromFile("/pdfs/endringsbeskyttet.pdf");
        assertThrows(RuntimeException.class, () -> PdfGyldighetsSjekker.erGyldig(UUID.randomUUID().toString(), imgData));
    }

    @Test
    public void OpplastingAvNormalPdfSkalGaBra() throws Exception {
        PdfGyldighetsSjekker.erGyldig(UUID.randomUUID().toString(), FilHjelpUtility.getBytesFromFile("/pdfs/minimal.pdf"));
    }
}
