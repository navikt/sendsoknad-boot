package no.nav.modig.security.tilgangskontroll.policy.response;

public class PolicyResponse {

    private final Decision theDecision;

    public PolicyResponse(Decision decision) {
        this.theDecision = decision;
    }

    // private Obligation;
    public Decision decision() {
        return theDecision;
    }

}
