package no.nav.sbl.pdfutility;

import org.apache.pdfbox.preflight.Format;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.preflight.utils.ByteArrayDataSource;
import org.slf4j.Logger;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

class PdfaSjekker {

    private static final Logger logger = getLogger(PdfaSjekker.class);

    static boolean erPDFA(String behandlingsId, byte[] input) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
            return erPDFA(behandlingsId, new ByteArrayDataSource(bais));
        } catch (IOException e) {
            logger.error("{}: Klarte ikke å sjekke filtype til PDF.", behandlingsId, e);
            throw new RuntimeException("Kunne ikke sjekke om PDF oppfyller krav til PDF/A");
        }
    }

    private static boolean erPDFA(String behandlingsId, DataSource input) {
        ValidationResult result;
        try {
            PreflightParser parser = new PreflightParser(input);
            parser.parse(Format.PDF_A1B);
            PreflightDocument document = parser.getPreflightDocument();
            document.validate();
            result = document.getResult();
            document.close();
        } catch (IOException | NoSuchMethodError e) {
            logger.warn("{}: Problem checking fileFormat", behandlingsId, e);
            return false;
        }
        if (result.isValid()) {
            logger.info("{}: The file is a valid PDF/A-1b file", behandlingsId);
            return true;
        } else {
            logger.info("{}: The file is not a valid PDF/A-1b file", behandlingsId);
            for (ValidationResult.ValidationError error : result.getErrorsList()) {
                logger.debug(behandlingsId + ": " + error.getErrorCode() + " : " + error.getDetails());
            }
            return false;
        }
    }
}
