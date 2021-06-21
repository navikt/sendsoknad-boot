package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

import java.net.InetAddress;

/**
 * Represents XACML attribute value of type ipAddress
 */
public class IpAddressValue extends AttributeValue<InetAddress> {

    public static final String DATATYPE = "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress";

    public IpAddressValue(InetAddress value) {
        super(DATATYPE, value);
    }

    @Override
    public IpAddressValue getResolvedCopy() {
        return new IpAddressValue(getValue());
    }
}
