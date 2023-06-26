package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class ClientErrorException extends SendSoknadException {
    public ClientErrorException(String message, String id) {
        super(message, id);
    }
}
