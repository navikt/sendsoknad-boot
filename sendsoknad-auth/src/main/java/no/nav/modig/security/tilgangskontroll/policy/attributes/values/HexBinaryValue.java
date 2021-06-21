package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

/**
 * Represents XACML attribute value of type hexBinary
 */
public class HexBinaryValue extends AttributeValue<Byte[]> {

    public static final String DATATYPE = "http://www.w3.org/2001/XMLSchema#hexBinary";

    public HexBinaryValue(Byte[] value) {
        super(DATATYPE, value);
    }

    @Override
    public HexBinaryValue getResolvedCopy() {
        return new HexBinaryValue(getValue());
    }
}
