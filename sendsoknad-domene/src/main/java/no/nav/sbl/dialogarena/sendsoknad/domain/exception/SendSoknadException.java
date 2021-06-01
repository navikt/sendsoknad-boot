package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class SendSoknadException extends RuntimeException {
    private String id;

    public SendSoknadException(String melding) {
        super(melding);
    }

    public SendSoknadException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public SendSoknadException(String message, Throwable cause, String id) {
        super(message, cause);
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
