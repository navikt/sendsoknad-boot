package no.nav.modig.security.tilgangskontroll.policy.response;

import org.jboss.security.xacml.interfaces.XACMLConstants;

public enum Decision {

    Deny(XACMLConstants.DECISION_DENY),

    Permit(XACMLConstants.DECISION_PERMIT),

    Indeterminate(XACMLConstants.DECISION_INDETERMINATE),

    NotApplicable(XACMLConstants.DECISION_NOT_APPLICABLE);

    private int decisionCode;

    Decision(int decisionValue) {
        this.decisionCode = decisionValue;
    }

    public static Decision getInstanceFromCodeValue(int codeValue) {
        for (Decision decision : Decision.values()) {
            if (decision.decisionCode == codeValue) {
                return decision;
            }
        }
        throw new RuntimeException("Unknown decision value: " + codeValue);
    }

}
