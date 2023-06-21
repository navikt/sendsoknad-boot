package no.nav.sbl.pdfutility;

import no.nav.sbl.pdfutility.exceptions.PDFException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;

import static org.slf4j.LoggerFactory.getLogger;

class PdfGyldighetsSjekker {

    private static final Logger logger = getLogger(PdfGyldighetsSjekker.class);

    static void erGyldig(String behandlingsId, byte[] input) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(input);
            PDDocument document = PDDocument.load(bais)){
            erGyldig(behandlingsId, document);
            harGyldigAntallSider(behandlingsId, document);
        }
        catch (PDFException e) {
            throw e;
        }
        catch (Exception e) {
            logger.warn("{}: Klarte ikke å sjekke om vedlegget er gyldig - {}", behandlingsId, e.getMessage());
            throw new RuntimeException("Klarte ikke å sjekke om vedlegget er gyldig", e);
        }
    }

    private static void erGyldig(String behandlingsId, PDDocument document) {
        if (document.isEncrypted()) {
            logger.warn("{}: Opplasting av vedlegg feilet da PDF er kryptert", behandlingsId);
            throw new PDFException("PDF er kryptert", null, "opplasting.feilmelding.pdf.kryptert");
        }
    }

    private static void harGyldigAntallSider(String behandlingsId, PDDocument document) {
        if (document.getNumberOfPages() >= 100) {
            logger.warn("{}: Opplasting av vedlegg feilet da PDFen har for mange sider (maks 100 sider)", behandlingsId);
            throw new PDFException("Ugyldig antall sider", null, "opplasting.feilmelding.makssider");
        }
    }
}
