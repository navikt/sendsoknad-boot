package no.nav.sbl.pdfutility;

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
        } catch (Exception e) {
            logger.warn("{}: Klarte ikke å sjekke om vedlegget er gyldig - {}", behandlingsId, e.getMessage());
            throw new RuntimeException("Klarte ikke å sjekke om vedlegget er gyldig");
        }
    }

    private static void erGyldig(String behandlingsId, PDDocument document) {
        if (document.isEncrypted()) {
            logger.warn("{}: Opplasting av vedlegg feilet da PDF er kryptert", behandlingsId);
            throw new RuntimeException("opplasting.feilmelding.pdf.kryptert");
        }
    }
}
