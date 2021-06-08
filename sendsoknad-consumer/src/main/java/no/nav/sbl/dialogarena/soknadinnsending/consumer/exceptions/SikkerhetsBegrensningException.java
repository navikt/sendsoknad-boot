package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;

public class SikkerhetsBegrensningException extends SendSoknadException {
    public SikkerhetsBegrensningException(String message, Exception exception) {
        super(message, exception);
    }
}
