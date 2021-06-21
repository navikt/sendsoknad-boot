package no.nav.modig.security.tilgangskontroll.policy.pdp.picketlink;

import no.nav.modig.security.tilgangskontroll.policy.attributes.values.AnyUriValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.Base64BinaryValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.BooleanValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.DateTimeValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.DateValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.DayTimeDuration;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.DnsNameValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.DoubleValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.HexBinaryValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.IntegerValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.IpAddressValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.Rfc822NameValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.StringValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.TimeValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.X500NameValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.YearMonthDuration;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.PolicyAttribute;
import org.apache.commons.lang3.ArrayUtils;
import org.jboss.security.xacml.core.model.context.AttributeType;

import static org.jboss.security.xacml.factories.RequestAttributeFactory.createAnyURIAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createBase64BinaryAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createBooleanAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createDNSNameAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createDateAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createDateTimeAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createDayTimeDurationAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createDoubleAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createHexBinaryAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createIPAddressAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createIntegerAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createStringAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createTimeAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createX509NameAttributeType;
import static org.jboss.security.xacml.factories.RequestAttributeFactory.createYearMonthDurationAttributeType;

/**
 * Internal converter.
 * 
 * Converts {@link PolicyAttribute} to JBoss XACML {@link AttributeType}
 * 
 * Should not be exposed or used outside this package.
 */
final class AttributeConverter {

    @SuppressWarnings("all")
    public static AttributeType convert(PolicyAttribute attribute) {
        Object value = attribute.getAttributeValue();
        String attributeId = attribute.getAttributeId().getURN();

        if (value instanceof StringValue) {
            return createStringAttributeType(attributeId, null, ((StringValue) value).getValue());
        } else if (value instanceof IntegerValue) {
            return createIntegerAttributeType(attributeId, null, ((IntegerValue) value).getValue());
        } else if (value instanceof BooleanValue) {
            return createBooleanAttributeType(attributeId, null, ((BooleanValue) value).getValue());
        } else if (value instanceof DoubleValue) {
            return createDoubleAttributeType(attributeId, null, ((DoubleValue) value).getValue());
        } else if (value instanceof DateTimeValue) {
            return createDateTimeAttributeType(attributeId, null, ((DateTimeValue) value).getValue());
        } else if (value instanceof DateValue) {
            return createDateAttributeType(attributeId, null, ((DateValue) value).getValue());
        } else if (value instanceof TimeValue) {
            return createTimeAttributeType(attributeId, null, ((TimeValue) value).getValue());
        } else if (value instanceof DayTimeDuration) {
            return createDayTimeDurationAttributeType(attributeId, null, ((DayTimeDuration) value).getValue());
        } else if (value instanceof YearMonthDuration) {
            return createYearMonthDurationAttributeType(attributeId, null, ((YearMonthDuration) value).getValue());
        } else if (value instanceof DnsNameValue) {
            return createDNSNameAttributeType(attributeId, null, ((DnsNameValue) value).getValue());
        } else if (value instanceof IpAddressValue) {
            return createIPAddressAttributeType(attributeId, null, ((IpAddressValue) value).getValue());
        } else if (value instanceof AnyUriValue) {
            return createAnyURIAttributeType(attributeId, null, ((AnyUriValue) value).getValue());
        } else if (value instanceof HexBinaryValue) {
            return createHexBinaryAttributeType(attributeId, null, ArrayUtils.toPrimitive(((HexBinaryValue) value).getValue()));
        } else if (value instanceof Base64BinaryValue) {
            return createBase64BinaryAttributeType(attributeId, null, ArrayUtils.toPrimitive(((Base64BinaryValue) value).getValue()));
        } else if (value instanceof X500NameValue) {
            return createX509NameAttributeType(attributeId, null, ((X500NameValue) value).getValue());
        } else if (value instanceof Rfc822NameValue) {
            throw new RuntimeException("Error thrown when converting to JbossXACML request. Rfc822Name not supported");
        } else {
            throw new RuntimeException("Error thrown when converting to JbossXACML request: Unknown datatype for value: " + value);
        }

    }

    private AttributeConverter() {
    }

    static {
        new AttributeConverter();
    }
}
