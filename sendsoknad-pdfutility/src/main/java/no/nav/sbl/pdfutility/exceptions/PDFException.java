package no.nav.sbl.pdfutility.exceptions;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;

public class PDFException extends SendSoknadException {
    public PDFException(String message, Throwable cause, String id) {
        super(message, cause, id);
    }
}
