package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

/**
 * Represents XACML attribute value of type integer
 */
public class IntegerValue extends AttributeValue<Integer> {

    public static final String DATATYPE = "http://www.w3.org/2001/XMLSchema#integer";

    public IntegerValue(Integer value) {
        super(DATATYPE, value);
    }

    @Override
    public IntegerValue getResolvedCopy() {
        return new IntegerValue(getValue());
    }
}
