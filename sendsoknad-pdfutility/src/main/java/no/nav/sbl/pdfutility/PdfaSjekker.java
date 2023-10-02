package no.nav.sbl.pdfutility;

import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.slf4j.Logger;

import java.io.File;
import java.util.UUID;

import static java.nio.file.Files.*;
import static org.apache.pdfbox.Loader.loadPDF;
import static org.slf4j.LoggerFactory.getLogger;

class PdfaSjekker {

    private static final Logger logger = getLogger(PdfaSjekker.class);

    public static boolean erPDFA(String behandlingsId, byte[] input) {
        ValidationResult result;
        File file = null;

        try (var document = loadPDF(input)) {
            file = File.createTempFile(String.format("tmp_%s", UUID.randomUUID()), ".pdf");
            document.save(file, CompressParameters.NO_COMPRESSION);
            result = PreflightParser.validate(file);
        } catch (Exception e) {
            logger.warn("{}: Problem checking fileFormat", behandlingsId, e);
            return false;
        }  finally {
            try {
                if (file != null) {
                    file.deleteOnExit();
                    deleteIfExists(file.toPath());
                }
            } catch (Exception e) {
                logger.warn("Problem deleting temporary file", e);
            }
        }

        if (result.isValid()) {
            logger.info("{}: The file is a valid PDF/A-1b file", behandlingsId);
            return true;
        } else {
            var errors = result.getErrorsList();
            logger.info("{}: The file is not a valid PDF/A-1b file", behandlingsId);
            for (ValidationResult.ValidationError error : errors) {
                logger.info(behandlingsId + ": " + error.getErrorCode() + " : " + error.getDetails());
            }
            return false;
        }

    }
}
