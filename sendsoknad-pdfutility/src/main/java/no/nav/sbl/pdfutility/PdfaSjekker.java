package no.nav.sbl.pdfutility;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.preflight.Format;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.slf4j.Logger;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

import static java.nio.file.Files.*;
import static org.apache.pdfbox.Loader.loadPDF;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TRAILER;
import static org.slf4j.LoggerFactory.getLogger;

class PdfaSjekker {

    private static final Logger logger = getLogger(PdfaSjekker.class);

    public static boolean erPDFA(String behandlingsId, byte[] input) {
        ValidationResult result;
        File file = null;

        try (var document = loadPDF(input)) {
            String filename = String.format("tmp_%s.pdf", UUID.randomUUID());
            file = new File(filename);
            document.save(filename);
            result = PreflightParser.validate(file);
        } catch (Exception e) {
            logger.warn("{}: Problem checking fileFormat", behandlingsId, e);
            return false;
        }  finally {
            try {
                if (file != null) {
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
            // FIXME: Figure out if this is OK
            var errors = result.getErrorsList();
            if (errors.size() == 1 && errors.get(0).getErrorCode().equals(ERROR_SYNTAX_TRAILER)) {
                logger.info("{}: The file is a valid PDF/A-1b file", behandlingsId);
                return true;
            }

            logger.info("{}: The file is not a valid PDF/A-1b file", behandlingsId);
            for (ValidationResult.ValidationError error : errors) {
                logger.info(behandlingsId + ": " + error.getErrorCode() + " : " + error.getDetails());
            }
            return false;
        }

    }
}
