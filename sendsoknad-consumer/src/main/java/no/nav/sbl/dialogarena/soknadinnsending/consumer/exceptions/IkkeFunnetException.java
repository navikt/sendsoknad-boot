package no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;

public class IkkeFunnetException extends SendSoknadException {
    public IkkeFunnetException(String melding, Exception e) {
        super(melding, e);
    }
}
