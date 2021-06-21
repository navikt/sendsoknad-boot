package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Represents XACML attribute value of type time
 */
public class TimeValue extends AttributeValue<XMLGregorianCalendar> {

    public static final String DATATYPE = "http://www.w3.org/2001/XMLSchema#time";

    private TimeValue(XMLGregorianCalendar value) {
        super(DATATYPE, value);
    }

    /**
     * Creates a TimeValue with timezone.
     * <p/>
     * An attribute of type time with timezone cannot be compared with an attribute of type time without timezone.
     * 
     * @param hours
     *            number of hours
     * @param minutes
     *            number of minutes
     * @param seconds
     *            number of seconds
     * @param timezone
     *            offset in minutes
     */
    public TimeValue(int hours, int minutes, int seconds, int timezone) {
        super(DATATYPE, getDatatypeFactory().newXMLGregorianCalendarTime(hours, minutes, seconds, timezone));
    }

    /**
     * Creates a TimeValue without timezone.
     * <p/>
     * An attribute of type time with timezone cannot be compared with an attribute of type time without timezone.
     * 
     * @param hours
     *            number of hours
     * @param minutes
     *            number of minutes
     * @param seconds
     *            number of seconds
     */
    public TimeValue(int hours, int minutes, int seconds) {
        super(DATATYPE, getDatatypeFactory().newXMLGregorianCalendarTime(hours, minutes, seconds, DatatypeConstants.FIELD_UNDEFINED));
    }

    @Override
    public TimeValue getResolvedCopy() {
        return new TimeValue(getValue());
    }

    private static DatatypeFactory getDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
