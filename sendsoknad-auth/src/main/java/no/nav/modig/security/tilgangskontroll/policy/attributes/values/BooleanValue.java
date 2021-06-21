package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

/**
 * Represents XACML attribute value of type boolean
 */
public class BooleanValue extends AttributeValue<Boolean> {

    public static final String DATATYPE = "http://www.w3.org/2001/XMLSchema#boolean";

    public BooleanValue(Boolean value) {
        super(DATATYPE, value);
    }

    @Override
    public BooleanValue getResolvedCopy() {
        return new BooleanValue(getValue());
    }
}
