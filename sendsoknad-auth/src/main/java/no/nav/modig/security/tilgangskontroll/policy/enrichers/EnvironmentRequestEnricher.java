package no.nav.modig.security.tilgangskontroll.policy.enrichers;

import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;

import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.currentDate;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.currentDateTime;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.currentTime;

public class EnvironmentRequestEnricher implements PolicyRequestEnricher {

    @Override
    public PolicyRequest enrich(PolicyRequest request) {
        return request.copyAndAppend(
                currentTime(),
                currentDate(),
                currentDateTime()
                );
    }

}
