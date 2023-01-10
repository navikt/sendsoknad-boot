package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class SoknadCannotBeChangedException extends SendSoknadException {
    public SoknadCannotBeChangedException(String message, Throwable cause, String id) {
        super(message, cause, id);
    }
}
