package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;

public class TjenesteUtilgjengeligException extends SendSoknadException {
    public TjenesteUtilgjengeligException(String message, Exception exception) {
        super(message, exception);
    }
}
