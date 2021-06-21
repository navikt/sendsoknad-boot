package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

import java.net.URI;

/**
 * Represents XACML attribute value of type anyURI
 */
public class AnyUriValue extends AttributeValue<URI> {

    private static final String DATATYPE = "http://www.w3.org/2001/XMLSchema#anyURI";

    public AnyUriValue(URI value) {
        super(DATATYPE, value);
    }

    public AnyUriValue(String value) {
        super(DATATYPE, URI.create(value));
    }

    @Override
    public AnyUriValue getResolvedCopy() {
        return new AnyUriValue(this.getValue());
    }
}
