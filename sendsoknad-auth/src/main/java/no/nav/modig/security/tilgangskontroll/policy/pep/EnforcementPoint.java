package no.nav.modig.security.tilgangskontroll.policy.pep;

import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;
import no.nav.modig.security.tilgangskontroll.policy.response.PolicyResponse;

/**
 * Interface for haandhevingspunkt for tilgangskontroll.
 */
public interface EnforcementPoint {

    /**
     * Validerer tilgang utfra en request. Hvis ikke tilgang kastes en SecurityException.
     * 
     * @param request
     *            PolicyRequest
     */
    void assertAccess(PolicyRequest request);

    /**
     * Boolsk validering av tilgang utfra en request.
     * 
     * @param request
     *            PolicyRequest
     * @return true hvis tilgang
     */
    boolean hasAccess(PolicyRequest request);

    /**
     * Vurdere tilgang utfra en request.
     * 
     * @param request
     *            PolicyRequest
     * @return PolicyResponse med resultatet av yurderingen
     */
    PolicyResponse evaluate(PolicyRequest request);

}
