package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class IkkeFunnetException extends SendSoknadException {
    public IkkeFunnetException(String message, Throwable cause, String id) {
        super(message, cause, id);
    }
}
