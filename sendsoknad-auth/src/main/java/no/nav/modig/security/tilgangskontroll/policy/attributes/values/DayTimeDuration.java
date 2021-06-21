package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

import javax.xml.datatype.Duration;

/**
 * Represents XACML attribute value of type dayTimeDuration
 */
public class DayTimeDuration extends AttributeValue<Duration> {

    public static final String DATATYPE = "http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration";

    public DayTimeDuration(Duration value) {
        super(DATATYPE, value);
    }

    @Override
    public DayTimeDuration getResolvedCopy() {
        return new DayTimeDuration(getValue());
    }
}
