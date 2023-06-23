package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class ClientErrorException extends SendSoknadException {
    public ClientErrorException(String melding) {
        super(melding);
    }

    public ClientErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientErrorException(String message, Throwable cause, String id) {
        super(message, cause, id);
    }

    public ClientErrorException(String message, String id) {
        super(message, id);
    }
}
