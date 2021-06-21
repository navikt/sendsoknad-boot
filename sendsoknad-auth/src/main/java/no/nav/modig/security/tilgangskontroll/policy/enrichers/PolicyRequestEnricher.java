package no.nav.modig.security.tilgangskontroll.policy.enrichers;

import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;

/**
 *
 */
public interface PolicyRequestEnricher {

    PolicyRequest enrich(PolicyRequest request);
}
