package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

/**
 * Represents XACML attribute value of type string
 */
public class Rfc822NameValue extends AttributeValue<String> {

    public static final String DATATYPE = "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name";

    public Rfc822NameValue(String value) {
        super(DATATYPE, value);
    }

    @Override
    public Rfc822NameValue getResolvedCopy() {
        return new Rfc822NameValue(getValue());
    }
}
