package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

/**
 * Represents XACML attribute value of type base64Binary
 */
public class Base64BinaryValue extends AttributeValue<Byte[]> {

    public static final String DATATYPE = "http://www.w3.org/2001/XMLSchema#base64Binary";

    public Base64BinaryValue(Byte[] value) {
        super(DATATYPE, value);
    }

    @Override
    public Base64BinaryValue getResolvedCopy() {
        return new Base64BinaryValue(getValue());
    }
}
