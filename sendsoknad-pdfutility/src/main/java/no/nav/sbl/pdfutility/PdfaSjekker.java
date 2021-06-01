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

    private static final Logger LOGGER = getLogger(PdfaSjekker.class);

    static boolean erPDFA(byte[] input) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
            return erPDFA(new ByteArrayDataSource(bais));
        } catch (IOException e) {
            LOGGER.error("Klarte ikke å sjekke filtype til PDF.", e);
            throw new RuntimeException("Kunne ikke sjekke om PDF oppfyller krav til PDF/A");
        }
    }

    private static boolean erPDFA(DataSource input) {
        ValidationResult result;
        try {
            PreflightParser parser = new PreflightParser(input);
            parser.parse(Format.PDF_A1B);
            PreflightDocument document = parser.getPreflightDocument();
            document.validate();
            result = document.getResult();
            document.close();
        } catch (IOException | NoSuchMethodError e) {
            LOGGER.warn("Problem checking fileFormat",  e);
            return false;
        }
        if (result.isValid()) {
            LOGGER.info("The file "  + " is a valid PDF/A-1b file");
            return true;
        } else {
            LOGGER.info("The file "  + " is not a valid PDF/A-1b file");
            for (ValidationResult.ValidationError error : result.getErrorsList()) {
                LOGGER.debug(error.getErrorCode() + " : " + error.getDetails());
            }
            return false;
        }
    }
}
