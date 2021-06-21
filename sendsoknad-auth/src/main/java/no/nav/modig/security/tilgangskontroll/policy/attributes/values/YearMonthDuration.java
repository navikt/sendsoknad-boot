package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

import javax.xml.datatype.Duration;

/**
 * Represents XACML attribute value of type yearMonthDuration
 */
public class YearMonthDuration extends AttributeValue<Duration> {

    public static final String DATATYPE = "http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration";

    public YearMonthDuration(Duration value) {
        super(DATATYPE, value);
    }

    @Override
    public YearMonthDuration getResolvedCopy() {
        return new YearMonthDuration(getValue());
    }
}
