package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

/**
 * Represents XACML attribute value of type string
 */
public class StringValue extends AttributeValue<String> {

    public static final String DATATYPE = "http://www.w3.org/2001/XMLSchema#string";

    public StringValue(String value) {
        super(DATATYPE, value);
    }

    public StringValue(AttributeValueResolver<String> valueResolver) {
        super(DATATYPE, valueResolver);
    }

    @Override
    public StringValue getResolvedCopy() {
        return new StringValue(getValue());
    }
}
