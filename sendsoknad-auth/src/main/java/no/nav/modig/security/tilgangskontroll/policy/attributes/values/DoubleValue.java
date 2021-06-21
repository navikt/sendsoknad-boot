package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

/**
 * Represents XACML attribute value of type double
 */
public class DoubleValue extends AttributeValue<Double> {

    public static final String DATATYPE = "http://www.w3.org/2001/XMLSchema#double";

    public DoubleValue(Double value) {
        super(DATATYPE, value);
    }

    @Override
    public DoubleValue getResolvedCopy() {
        return new DoubleValue(getValue());
    }
}
