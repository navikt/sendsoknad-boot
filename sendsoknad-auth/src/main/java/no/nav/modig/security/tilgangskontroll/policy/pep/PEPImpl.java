package no.nav.modig.security.tilgangskontroll.policy.pep;

import no.nav.modig.core.exception.AuthorizationException;
import no.nav.modig.security.tilgangskontroll.policy.pdp.DecisionPoint;
import no.nav.modig.security.tilgangskontroll.policy.enrichers.PolicyRequestEnricher;
import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;
import no.nav.modig.security.tilgangskontroll.policy.response.Decision;
import no.nav.modig.security.tilgangskontroll.policy.response.PolicyResponse;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class PEPImpl implements EnforcementPoint {



    public enum Bias {
        Permit, Deny
    }

	private static final Logger logger = LoggerFactory.getLogger(PEPImpl.class);
    private final DecisionPoint pdp;
    private Bias bias = Bias.Deny;
    private Collection<PolicyRequestEnricher> requestEnrichers = new ArrayList<PolicyRequestEnricher>();

    public PEPImpl(DecisionPoint pdp) {
        Validate.notNull(pdp, "Pdp can not be null");
        this.pdp = pdp;
    }

    @Override
    public final PolicyResponse evaluate(PolicyRequest request) {
        return pdp.evaluate(prepareRequest(request));
    }

    @Override
    public final void assertAccess(PolicyRequest request) {
        if (!hasAccess(request)) {
			throw new AuthorizationException("Access denied. "  + request.toLogString() + "." );
        }
    }

    @Override
    public final boolean hasAccess(PolicyRequest request) {
        PolicyResponse response = evaluate(request);
		if (bias == Bias.Permit && response.decision() == Decision.Deny) {
			logger.warn("Access denied! " + request.toLogString() + ".");
			return false;
        } else if (bias == Bias.Deny && response.decision() != Decision.Permit) {
			logger.warn("Access denied! " + request.toLogString() + ".");
            return false;
        }
        return true;
    }

    private PolicyRequest prepareRequest(PolicyRequest request) {
        PolicyRequest resolvedRequest = request.copyAndResolveAttributeValues();
        return copyAndEnrich(requestEnrichers.iterator(), resolvedRequest);
    }

    private PolicyRequest copyAndEnrich(Iterator<PolicyRequestEnricher> iterator, PolicyRequest request) {
        if (iterator.hasNext()) {
            return iterator.next().enrich(copyAndEnrich(iterator, request));
        } else {
            return request;
        }
    }

    public void setRequestEnrichers(Collection<? extends PolicyRequestEnricher> requestEnrichers) {
        this.requestEnrichers = new ArrayList<PolicyRequestEnricher>(requestEnrichers);
    }

    public void setBias(Bias bias) {
        Validate.notNull(bias, "Bias can not be null");
        this.bias = bias;
    }

}
