package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

/**
 * Represents XACML attribute value of type dnsName
 */
public class DnsNameValue extends AttributeValue<String> {

    public static final String DATATYPE = "urn:oasis:names:tc:xacml:2.0:data-type:dnsName";

    public DnsNameValue(String value) {
        super(DATATYPE, value);
    }

    @Override
    public DnsNameValue getResolvedCopy() {
        return new DnsNameValue(getValue());
    }
}
