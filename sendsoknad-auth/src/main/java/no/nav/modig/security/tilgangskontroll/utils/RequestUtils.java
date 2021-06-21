package no.nav.modig.security.tilgangskontroll.utils;

import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.PolicyAttribute;

import java.util.Collection;

/**
 * A collection of utils for creating policy requests
 */
public final class RequestUtils {

    public static PolicyRequest forRequest(PolicyAttribute... attributes) {
        return new PolicyRequest(attributes);
    }

    public static PolicyRequest forRequest(Collection<PolicyAttribute> attributes) {
        return new PolicyRequest(attributes);
    }

    private RequestUtils() {
    }

    static {
        new RequestUtils();
    }
}

