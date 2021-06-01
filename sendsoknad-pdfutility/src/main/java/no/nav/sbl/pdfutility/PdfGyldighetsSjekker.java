package no.nav.sbl.pdfutility;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;

import static org.slf4j.LoggerFactory.getLogger;

class PdfGyldighetsSjekker {

    private static final Logger logger = getLogger(PdfGyldighetsSjekker.class);

    static void erGyldig(byte[] input) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(input);
            PDDocument document = PDDocument.load(bais)){
            erGyldig(document);
        } catch (Exception e) {
            logger.error("Klarte ikke å sjekke om vedlegget er gyldig {}", e.getMessage());
            throw new RuntimeException("Klarte ikke å sjekke om vedlegget er gyldig");
        }
    }

    private static void erGyldig(PDDocument document) {
        if (document.isEncrypted()) {
            logger.error("Opplasting av vedlegg feilet da PDF er kryptert");
            throw new RuntimeException("opplasting.feilmelding.pdf.kryptert");
        }
    }
}
