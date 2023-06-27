package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class AuthorizationException extends SendSoknadException {
    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable e) {
        super(message, e);
    }
}
