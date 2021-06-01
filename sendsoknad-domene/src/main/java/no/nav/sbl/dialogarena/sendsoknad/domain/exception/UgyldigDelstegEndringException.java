package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class UgyldigDelstegEndringException extends SendSoknadException {
    public UgyldigDelstegEndringException(String message, String id) {
        super(message, null, id);
    }
}
