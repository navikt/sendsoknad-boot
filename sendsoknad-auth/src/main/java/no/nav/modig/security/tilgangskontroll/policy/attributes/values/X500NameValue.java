package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

import javax.security.auth.x500.X500Principal;

/**
 * Represents XACML attribute value of type x500Name
 */
public class X500NameValue extends AttributeValue<X500Principal> {

    public static final String DATATYPE = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";

    public X500NameValue(X500Principal value) {
        super(DATATYPE, value);
    }

    @Override
    public X500NameValue getResolvedCopy() {
        return new X500NameValue(getValue());
    }
}
