package no.nav.modig.security.tilgangskontroll.policy.pdp;

import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;
import no.nav.modig.security.tilgangskontroll.policy.response.PolicyResponse;

/**
 * Interface for beslutningspunkt som en XACML lik Policy Decision Point (PDP).
 */
public interface DecisionPoint {

    /**
     * Evaluerer en policy forespÃ¸rsel mot et definert set med regler for beslutningspunktet.
     * 
     * @param request
     *            a policyrequest
     * @return en response med resultatet av evalueringen
     */
    PolicyResponse evaluate(PolicyRequest request);
}
